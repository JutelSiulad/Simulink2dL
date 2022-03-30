/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of MÃ¼nster
 * SESE Software and Embedded Systems Engineering, TU Berlin
 * 
 * Authors:
 * 	Paula Herber
 * 	Sabine Glesner
 * 	Timm Liebrenz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.TransformedDLModel;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.util.PluginLogger;

public class InportTransformer extends BlockTransformer {

	public InportTransformer(SimulinkModel simulinkModel, TransformedDLModel dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}
	/**
	 * Every Input is treated as potentially continous. Continous Inputs are captured by evaluating them after continuous evolutions.
	 * This is inspired by "Tactical contract composition for hybrid system component verification" Müller et al. 2017
	 * To model discretized inputs, we additionally introduce a "discretized" variable, which stores the input value before every loop iteration.
	 * This results in the following structure of the transformed HP:
	 * {
	 * 	inputDiscrete:=input;
	 * 	ctrl;
	 * 	plant;
	 * 	input:=*;
	 * 	?(test formula on input);
	 * }*
	 */
	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "Inport";
		checkBlock(type, block);

		String blockName =  block.getName().replace(" ", "");
		
		// add discretized input and assign input value to discretized value
		Variable discretizedVariable = new Variable("R", blockName+"Discrete");
		Variable variable = new Variable("R", blockName);
		dlModel.addDiscreteInput(new DiscreteAssignment(discretizedVariable, variable));
		dlModel.addVariable(discretizedVariable);
		dlModel.addVariable(variable);

		// add macro for discretizedVariable
		Term replaceWith = discretizedVariable;
		dlModel.addMacro(new SimpleMacro(environment.getToReplace(block), replaceWith));

		// generate and add limit formula
		Constant upperLimit = new Constant("R", blockName+"MAX");
		Constant lowerLimit = new Constant("R", blockName+"MIN");
		dlModel.addConstant(upperLimit);
		dlModel.addConstant(lowerLimit);

		Conjunction limitFormula = new Conjunction();
		limitFormula.addElement(new Relation(variable, Relation.RelationType.LESS_EQUAL, upperLimit));
		limitFormula.addElement(new Relation(variable, Relation.RelationType.GREATER_EQUAL, lowerLimit));
		dlModel.addInitialCondition(limitFormula);
		
		// add nondeterministic assignment for "continuous" input
		dlModel.addContinousInput(new NondeterministicAssignment(variable));
		dlModel.addContinousInput(new TestFormula(limitFormula));
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

}
