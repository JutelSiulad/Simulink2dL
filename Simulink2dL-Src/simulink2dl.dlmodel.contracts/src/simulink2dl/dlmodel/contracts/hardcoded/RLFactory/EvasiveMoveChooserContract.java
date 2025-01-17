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
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;

public class EvasiveMoveChooserContract  extends HybridContract{
	/* Evasive Move chooser outputs a move with dotproduct of (VXEM,VYEM) and the position vector of the opponent = 0*/
	public EvasiveMoveChooserContract(String serviceName) {
		super();
		Variable DiffXRL = new Variable("R","DiffXRL");
		Variable DiffYRL = new Variable("R","DiffYRL");
		Variable DiffXGoal = new Variable("R","DiffXGoal");
		Variable DiffYGoal = new Variable("R","DiffYGoal");
		inputs.add(DiffXRL);
		inputs.add(DiffYRL);
		inputs.add(DiffXGoal);
		inputs.add(DiffYGoal);
		
		Variable VXEM = new Variable("R","VXEM");
		Variable VYEM = new Variable("R","VYEM");
		outputs.add(VXEM);
		outputs.add(VYEM);
		
		MultiplicationTerm productX = new MultiplicationTerm(DiffXRL,VXEM);
		MultiplicationTerm productY = new MultiplicationTerm(DiffYRL,VYEM);
		AdditionTerm dotProcuct = new AdditionTerm(productX,productY);
		Relation safeMove = new Relation(dotProcuct, LESS_EQUAL, ZERO);
	
		setAssumptionGuaranteePair(TRUE, safeMove);
		
	}
}