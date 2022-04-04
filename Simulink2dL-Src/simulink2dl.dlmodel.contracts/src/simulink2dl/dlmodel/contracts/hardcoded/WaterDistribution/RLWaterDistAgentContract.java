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

import simulink2dl.dlmodel.contracts.RLAgentContract;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;


public class RLWaterDistAgentContract extends RLAgentContract {
	/* Simulation Info is transformed to an empty true->true contract */
	public RLWaterDistAgentContract(String serviceName) {
		
		super("TSRL");
		Variable a1 = new Variable("R", "a1");
		Variable a2 = new Variable("R", "a2");
		Variable a3 = new Variable("R", "a3");
		Variable dRL = new Variable("R", "dRL");
		outputs.add(a1);
		outputs.add(a2);
		outputs.add(a3);
		outputs.add(dRL);
		
		setAssumptionGuaranteePair(new BooleanConstant(true),new BooleanConstant(true));
	}
	
}