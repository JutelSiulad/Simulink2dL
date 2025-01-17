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
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.MultiplicationTerm;

public class VelocityAdjustorContract3  extends HybridContract{
	
	public VelocityAdjustorContract3(String serviceName) {
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
		
		//SAME SIGNS OF MOVES AND ADJUSTED MOVES for Velocity >0, i.e. sign(VX)=sign(VXPM)
		Relation Assumption3 = new Relation(Velocity, GREATER_THAN, ZERO);
		Implication XLZERO = new Implication(new Relation(DirX, LESS_THAN, ZERO), new Relation(VXPM, LESS_THAN, ZERO));
		Implication YLZERO = new Implication(new Relation(DirY, LESS_THAN, ZERO), new Relation(VYPM, LESS_THAN, ZERO));
		Implication XEZERO = new Implication(new Relation(DirX, EQUAL, ZERO), new Relation(VXPM, EQUAL, ZERO));
		Implication YEZERO = new Implication(new Relation(DirY, EQUAL, ZERO), new Relation(VYPM, EQUAL, ZERO));
		Implication XGZERO = new Implication(new Relation(DirX, GREATER_THAN, ZERO), new Relation(VXPM, GREATER_THAN, ZERO));
		Implication YGZERO = new Implication(new Relation(DirY, GREATER_THAN, ZERO), new Relation(VYPM, GREATER_THAN, ZERO));
		Conjunction SAMESIGN = new Conjunction(XLZERO, XEZERO, XGZERO,YLZERO, YEZERO, YGZERO);
		
		//X VALUES GREATER ZERO IMPLY SAME Y/X RATIO OF ADJUSTED MOVES
		Conjunction XNOTZERO = new Conjunction(new Relation(DirX, NOT_EQUAL, ZERO), new Relation(VXPM, NOT_EQUAL, ZERO));
		MultiplicationTerm RATBEF= new MultiplicationTerm(DirY);
		RATBEF.dividedBy(DirX);
		MultiplicationTerm RATAFT= new MultiplicationTerm(VYPM);
		RATAFT.dividedBy(VXPM);
		Relation SAMERATIO = new Relation(RATBEF,EQUAL, RATAFT);
		Implication XNOTZEROSAMERATIO = new Implication(XNOTZERO,SAMERATIO);

		Conjunction sameSignAndSameRatio = new Conjunction(SAMESIGN, XNOTZEROSAMERATIO);

		setAssumptionGuaranteePair(Assumption3, sameSignAndSameRatio);
		
	}
}
