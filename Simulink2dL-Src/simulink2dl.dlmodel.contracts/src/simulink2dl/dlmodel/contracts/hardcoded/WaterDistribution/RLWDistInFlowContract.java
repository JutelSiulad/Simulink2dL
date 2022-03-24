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

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.RealTerm;


public class RLWDistInFlowContract extends HybridContract {
	/* Simulation Info is transformed to an empty true->true contract */
	public RLWDistInFlowContract(String serviceName) {
		
		super();
		Variable nPumps = new Variable("R", "NPumps");
		Variable inFlow = new Variable("R", "InFlow");
		Constant flow0 = new Constant("R", "flow0");
		Constant flow1 = new Constant("R", "flow1");
		Constant flow2 = new Constant("R", "flow2");
		Constant flow3 = new Constant("R", "flow3");
		
		inputs.add(nPumps);
		constants.add(flow0);
		constants.add(flow1);
		constants.add(flow2);
		constants.add(flow3);
		outputs.add(inFlow);
		
		setAssumptionGuaranteePair(new BooleanConstant(true), new Conjunction(
				new Implication( new Conjunction(new Relation(nPumps, Relation.RelationType.GREATER_EQUAL, new RealTerm(0)), new Relation(nPumps, Relation.RelationType.LESS_THAN, new RealTerm(1))), new Relation(inFlow, Relation.RelationType.EQUAL, flow0)),
				new Implication( new Conjunction(new Relation(nPumps, Relation.RelationType.GREATER_EQUAL, new RealTerm(1)), new Relation(nPumps, Relation.RelationType.LESS_THAN, new RealTerm(2))), new Relation(inFlow, Relation.RelationType.EQUAL, flow1)),
				new Implication( new Conjunction(new Relation(nPumps, Relation.RelationType.GREATER_EQUAL, new RealTerm(2)), new Relation(nPumps, Relation.RelationType.LESS_THAN, new RealTerm(3))), new Relation(inFlow, Relation.RelationType.EQUAL, flow2)),
				new Implication( new Relation(nPumps, Relation.RelationType.GREATER_EQUAL, new RealTerm(3)), new Relation(inFlow, Relation.RelationType.EQUAL, flow3))));
	}
	
}