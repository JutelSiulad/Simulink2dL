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
package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.DLModelFromSimulink;
import simulink2dl.transform.macro.ConditionalMacro;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.MacroContainer;
import simulink2dl.transform.macro.SimpleMacro;

public class MultiPortSwitchTransformer extends BlockTransformer {

	public MultiPortSwitchTransformer(SimulinkModel simulinkModel, DLModelFromSimulink dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		List<Macro> macros = createMacro(block);
		// add alternative to model
		for (Macro macro : macros) {
			dlModel.addMacro(macro);
		}
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		List<Macro> macros = new ArrayList<>();
		String type = "Switch";
		checkBlock(type, block);

		int numPorts = block.getInPorts().size();
		
		SimulinkOutPort controlPort = environment.getConnectedOuputPort(block, 1);//get control signal
		String controlPortId = environment.getPortID(controlPort);
		Term controlPortReplaceable = new PortIdentifier(controlPortId);
		LinkedList<MacroContainer> cases = new LinkedList<MacroContainer>();
		
		for(int i = 1; i<numPorts; i++) {
			//get switch condition for port i
			Conjunction testPorti;
			if(i<numPorts-1) {
				testPorti = new Conjunction(
								new Relation(new RealTerm(i), Relation.RelationType.GREATER_THAN, controlPortReplaceable),
								new Relation(controlPortReplaceable, Relation.RelationType.GREATER_EQUAL, new RealTerm(i-1))); 
			}
			else {
				testPorti = new Conjunction(
								new Relation(controlPortReplaceable, Relation.RelationType.GREATER_EQUAL, new RealTerm(i-1)));
			}
			
			
			//handle data port i
			SimulinkOutPort dataPorti = environment.getConnectedOuputPort(block, i+1);//get data port
			String dataPortiId = environment.getPortID(dataPorti);
			
			SimpleMacro macroPorti = new SimpleMacro(environment.getToReplace(block), new PortIdentifier(dataPortiId));
			MacroContainer macroContaineri = new MacroContainer(macroPorti, testPorti, null);
			cases.add(macroContaineri);
		}

		macros.add(new ConditionalMacro(environment.getToReplace(block), cases));
		return macros;
	}
}