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
package simulink2dl.transform.dlmodel.hybridprogram;

import java.util.List;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * A conditional hybrid program is a tuple of two hybrid programs. First, a test
 * formula contains a condition under which the program should be executed.
 * Second, a hybrid program contains the functionality that is executed. Note
 * that often the hybrid program is of the type HybridProgramCollection. This
 * class is mostly used in conditional choices.
 * 
 * @author Timm Liebrenz
 *
 */
public class ConditionalHybridProgram implements HybridProgram {

	private Formula condition;
	private HybridProgram choice;

	public ConditionalHybridProgram(Formula condition, HybridProgram choice) {
		this.condition = condition;
		this.choice = choice;
	}

	public Formula getCondition() {
		return condition;
	}

	public HybridProgram getInnerProgram() {
		return choice;
	}
	
	public void setInnerProgram(HybridProgram newHP) {
		choice = newHP;
	}

	@Override
	public String toString() {
		return new HybridProgramCollection(new TestFormula(condition), choice).toString();
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		return new HybridProgramCollection(new TestFormula(condition), choice).toStringFormatted(indent,
				multiLineTestFormulas, multiLineEvolutionDomains);
	}

	@Override
	public boolean containsTerm(Term term) {
		if (condition.equals(term)) {
			return true;
		}
		if (choice.equals(term)) {
			return true;
		}
		if (condition.containsTerm(term)) {
			return true;
		}
		if (choice.containsTerm(term)) {
			return true;
		}
		return false;
	}

	@Override
	public ConditionalHybridProgram createDeepCopy() {
		return new ConditionalHybridProgram(condition.createDeepCopy(), choice.createDeepCopy());
	}

	@Override
	public HybridProgram expand() {
		/*condition.expand();*/
		choice = choice.expand();
		return this;
	}

	@Override
	public List<Variable> getBoundVariables(List<Variable> vars) {
		return choice.getBoundVariables(vars);
	}

	@Override
	public List<Variable> getVariables(List<Variable> vars) {
		vars.addAll(choice.getVariables(vars));
		vars.addAll(condition.getVariables(vars));
		return vars;
	}

	@Override
	public List<HybridProgram> getInnerPrograms(List<HybridProgram> hps) {
		hps.add(choice);
		return hps;
	}

	@Override
	public List<Term> getInnerTerms(List<Term> terms) {
		terms.add(condition);
		return terms;
	}

	public void setCondition(Formula newCondition) {
		this.condition = newCondition;
		
	}

}
