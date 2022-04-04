/**
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
import simulink2dl.dlmodel.term.RealTerm;


public class RLBackuppumpContract extends DiscreteHybridContract {
	/* Simulation Info is transformed to an empty true->true contract */
	public RLBackuppumpContract(String serviceName) {
		
		super();
		Variable h = new Variable("R", "h");
		inputs.add(h);
		
		Constant hBackup = new Constant("R", "hBackup");
		constants.add(hBackup);
		
		serviceName=serviceName.replace("RLService", "");
		serviceName=serviceName.replace("Pump", "");
		Variable p = new Variable("R", "p"+serviceName);
		outputs.add(p);
		
		Implication aboveBackup = new Implication(new Relation(h, GREATER_THAN, hBackup), new Relation(p, EQUAL, new RealTerm(0)));
		Implication belowEqualBackup = new Implication(new Relation(h, LESS_EQUAL, hBackup), new Relation(p, EQUAL, new RealTerm(1)));
		
		setAssumptionGuaranteePair(new BooleanConstant(true), new Conjunction(aboveBackup, belowEqualBackup));
	}
	
}