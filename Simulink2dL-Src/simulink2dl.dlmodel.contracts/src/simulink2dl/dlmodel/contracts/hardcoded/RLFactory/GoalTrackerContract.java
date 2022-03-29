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
package simulink2dl.dlmodel.contracts.hardcoded.RLFactory;

import simulink2dl.dlmodel.contracts.DiscreteHybridContract;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;


public class GoalTrackerContract extends DiscreteHybridContract {
	/* Goal Tracker is transformed to an empty true->true contract */
	public GoalTrackerContract(String serviceName) {
		
		super();
		
		Variable X = new Variable("R","X");
		Variable Y = new Variable("R","Y");
		Variable XGoal = new Variable("R","XGoal");
		Variable YGoal = new Variable("R","YGoal");
		inputs.add(X);
		inputs.add(Y);
		inputs.add(XGoal);
		inputs.add(YGoal);

		Variable Dist = new Variable("R","DistToGoal");
		Variable Arrived = new Variable("R","ArrivedAtGoal");

		outputs.add(Dist);
		outputs.add(Arrived);
		
		setAssumptionGuaranteePair(new BooleanConstant(true),new BooleanConstant(true));
	}
	
}