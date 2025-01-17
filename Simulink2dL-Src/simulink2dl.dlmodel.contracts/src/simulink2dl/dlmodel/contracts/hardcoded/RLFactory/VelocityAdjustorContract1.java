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

import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.RealTerm;

public class VelocityAdjustorContract1  extends HybridContract{
	
	public VelocityAdjustorContract1(String serviceName) {
		super();
		
		Variable DirX = new Variable("R","DirX");
		Variable DirY = new Variable("R","DirY");
		Variable Velocity = new Variable("R","Velocity");
		inputs.add(DirX);
		inputs.add(DirY);
		inputs.add(Velocity);
		
		Variable VXPM = new Variable("R","VXPM");
		Variable VYPM = new Variable("R","VYPM");
		outputs.add(VXPM);
		outputs.add(VYPM);
		
		//Movement Speed is equal to input Velocity
		Relation VelocityGEZERO = new Relation(Velocity, GREATER_EQUAL, ZERO);
		Disjunction DistGreaterZero = new Disjunction(new Relation(DirX,NOT_EQUAL,ZERO), new Relation(DirY,NOT_EQUAL,ZERO));
		Conjunction Assumption = new Conjunction(VelocityGEZERO,DistGreaterZero);

		ExponentTerm PosChange = new ExponentTerm(new AdditionTerm(new ExponentTerm(VXPM,new RealTerm(2)),new ExponentTerm(VYPM,new RealTerm(2))),new RealTerm(0.5));
		Relation VeloEqual = new Relation(PosChange, LESS_EQUAL, Velocity);
		
		setAssumptionGuaranteePair(Assumption, VeloEqual);
		
	}
}
