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
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.term.AdditionTerm;
import simulink2dl.dlmodel.term.ExponentTerm;
import simulink2dl.dlmodel.term.MultiplicationTerm;
import simulink2dl.dlmodel.term.RealTerm;

public class InvalidMoveCheckerContract extends HybridContract{
	
	public InvalidMoveCheckerContract(String serviceName) {
		
		super();
		
		Variable PropVX = new Variable("R","PropVX");
		Variable PropVY = new Variable("R","PropVY");
		
		Variable DiffXRL = new Variable("R","DiffXRL");
		Variable DiffYRL = new Variable("R","DiffYRL");
		
		Variable THRESHOLD = new Variable("R","THRESHOLD");
		
		inputs.add(PropVX);
		inputs.add(PropVY);
		inputs.add(DiffXRL);
		inputs.add(DiffYRL);
		inputs.add(THRESHOLD);
		
		Variable MoveInvalid = new Variable("R","MoveInvalid");
		outputs.add(MoveInvalid);

		ExponentTerm EuclDist = new ExponentTerm(new AdditionTerm(new ExponentTerm(DiffXRL, new RealTerm(2)), new ExponentTerm(DiffYRL, new RealTerm(2)) ), new RealTerm(0.5));
		
		Relation ThresholdPos = new Relation(THRESHOLD, GREATER_EQUAL, ZERO);
		Relation belowDistThreshold = new Relation(EuclDist, LESS_EQUAL, THRESHOLD);
		AdditionTerm dotProduct = new AdditionTerm(new MultiplicationTerm(DiffXRL,PropVX),new MultiplicationTerm(DiffYRL,PropVY));
		Relation dotProdPos = new Relation(dotProduct, GREATER_THAN, ZERO);
		
		/* move is considered unsafe, if the distance to the RL robot 
		 * is less than the threshold and the dot product of the move and the RL robots position is greater than 0 */
		Conjunction moveIsUnsafe = new Conjunction(ThresholdPos,belowDistThreshold,dotProdPos);
		

		Relation invalidMoveSignaled = new Relation(MoveInvalid, GREATER_THAN, ZERO);
		
		setAssumptionGuaranteePair(moveIsUnsafe,invalidMoveSignaled);
	}
}
