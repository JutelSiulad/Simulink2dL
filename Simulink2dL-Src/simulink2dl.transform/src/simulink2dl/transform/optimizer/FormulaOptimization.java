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
package simulink2dl.transform.optimizer;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.StringFormula;
import simulink2dl.util.PluginLogger;

/**
 * This optimizer simplifies formulas
 * @author Timm Liebrenz, Julius Adelt
 *
 */
public abstract class FormulaOptimization {
	
	private static Formula trueElement = new BooleanConstant(true);
	private static Formula falseElement = new BooleanConstant(false);
	
	public static Formula optimize(Formula formula) {
		Formula newFormula;
		if (formula instanceof Conjunction) {
			newFormula = handleConjunctionFormula((Conjunction)formula);
		} else if (formula instanceof Disjunction) {
			newFormula = handleDisjunctionFormula((Disjunction)formula);
		} else if (formula instanceof Negation) {
			newFormula = handleNegationFormula((Negation) formula);
		} else if (formula instanceof Relation) {
			newFormula = handleRelationFormula((Relation) formula);
		}  else if (formula instanceof Implication) {
			newFormula = handleImplicationFormula((Implication) formula);
		} else if (formula instanceof BooleanConstant || formula instanceof StringFormula) {
			newFormula = formula;
		} else	{
		// error case, not handled instance of a formula
			PluginLogger.error("Formula of type \"" + formula.getClass().getName() + "\" not handled in optimizer.");
			newFormula = formula;
		}
		return newFormula;
	}
	
	protected static Formula handleConjunctionFormula(Conjunction conjunction) {
		List<Operator> newElements = new LinkedList<Operator>();



		for (Operator toCheckOperator : conjunction.getElements()) {
			Formula toCheck = optimize((Formula) toCheckOperator);
			if (toCheck.equals(falseElement)) {
				// if one element of the conjunction is false, the whole
				// conjunction is false
				return falseElement;
			}
			if (isTrue(toCheck)) {
				// remove true elements, i.e. do not add it to new element list
				continue;
			}
			if (newElements.contains(toCheck.createNegation())) {
				// the conjunction contains a contradiction, therefore the whole
				// conjunction is false
				return falseElement;
			}
			if (!newElements.contains(toCheck)) {
				// element is not yet in list, so add it
				newElements.add(toCheck);
			}
		}

		if (newElements.isEmpty()) {
			// if no elements are remaining, set the conjunction to true
			return trueElement;
		} else if (newElements.size()==1 && newElements.get(0) instanceof Formula) {
			// if only one element is remaining, return it
			return (Formula)newElements.get(0);
		}
		Conjunction newConjunction = new Conjunction();
		newConjunction.setElements(newElements);
		return newConjunction;
	}

	protected static Formula handleDisjunctionFormula(Disjunction disjunction) {
		List<Operator> newElements = new LinkedList<Operator>();

		Formula trueElement = new BooleanConstant(true);
		Formula falseElement = new BooleanConstant(false);

		for (Operator toCheckOperator : disjunction.getElements()) {
			Formula toCheck = optimize((Formula) toCheckOperator);
			if (isTrue(toCheck)) {
				// if one element of the disjunction is true, the whole
				// conjunction is true
				return trueElement;
			}
			if (toCheck.equals(falseElement)) {
				// remove false elements, i.e. do not add it to new element list
				continue;
			}
			if (newElements.contains(toCheck.createNegation())) {
				// the conjunction contains a tautology, therefore the whole
				// conjunction is true
				return trueElement;
			}
			if (!newElements.contains(toCheck)) {
				// element is not yet in list, so add it
				newElements.add(toCheck);
			}
		}
		if (newElements.isEmpty()) {
			// if no elements are remaining, set the disjunction to false
			return falseElement;
		} else if (newElements.size()==1 && newElements.get(0) instanceof Formula) {
			// if only one element is remaining, return it
			return (Formula)newElements.get(0);
		}
		Disjunction newDisjunction = new Disjunction();
		newDisjunction.setElements(newElements);
		return newDisjunction;
	}
	
	protected static Formula handleImplicationFormula(Implication implication) {
		Operator antecedent = implication.getAntecedent();
		Operator consequent = implication.getConsequent();
		//optimize antecedent and consequent separately
		if(antecedent instanceof Formula) {
			antecedent=optimize((Formula)antecedent);
		}
		if(consequent instanceof Formula) {
			consequent=optimize((Formula)consequent);
		}
		//optimize antecedent is true, return only consequent
		if(antecedent instanceof Formula && consequent instanceof Formula) {
			if(isTrue((Formula) antecedent)) {
				return (Formula) consequent;
			}
		}
		return new Implication(antecedent, consequent);
	}
	
	static boolean isTrue(Formula formula) {		
		if (formula.equals(trueElement)) {
				return true;
		} 
		return false;
	}
	
	protected static Formula handleNegationFormula(Negation negation) {
		return new Negation(optimize((Formula) negation.getInnerFormula()));
	}

	protected static Formula handleRelationFormula(Relation relation) {
		return relation;
		// do nothing
	}
	
	

}
