/*******************************************************************************
 * Copyright (c) 2020
 * AG Embedded Systems, University of Münster
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
package simulink2dl.dlmodel.contracts.hardcoded.WaterDistribution;

import simulink2dl.dlmodel.contracts.DiscreteHybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MinMaxTerm;
import simulink2dl.dlmodel.term.RealTerm;


public class RLFlowCalculationService extends DiscreteHybridContract {
	/* Simulation Info is transformed to an empty true->true contract */
	public RLFlowCalculationService(String serviceName) {
		
		super();
		Variable p1 = new Variable("R", "p1");
		Variable p2 = new Variable("R", "p2");
		Variable p3 = new Variable("R", "p3");
		Variable pb = new Variable("R", "pb");
		Variable a1 = new Variable("R", "a1");
		Variable a2 = new Variable("R", "a2");
		Variable a3 = new Variable("R", "a3");
		Variable dRL = new Variable("R", "dRL");
		Constant FP = new Constant("R", "FP");
		
		Variable i = new Variable("R", "i");
		Variable d = new Variable("R", "d");
		
		inputs.add(p1);
		inputs.add(p2);
		inputs.add(p3);
		inputs.add(pb);
		inputs.add(a1);
		inputs.add(a2);
		inputs.add(a3);
		inputs.add(dRL);
		constants.add(FP);
		
		outputs.add(i);
		outputs.add(d);
		
		Relation iout = new Relation(i, EQUAL, new AdditionTerm(new MinMaxTerm(p1,a1, "min"), new MinMaxTerm(p2,a2, "min"), new MinMaxTerm(p3,a3, "min"), pb));
		Conjunction dout = new Conjunction(new Relation(d, GREATER_EQUAL, dRL),new Relation(d, LESS_EQUAL, dRL));
		
		setAssumptionGuaranteePair(new BooleanConstant(true), new Conjunction(iout, dout));
	}
	
}