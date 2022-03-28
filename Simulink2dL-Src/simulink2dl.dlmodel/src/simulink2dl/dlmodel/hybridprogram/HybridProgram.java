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
package simulink2dl.dlmodel.hybridprogram;

import java.util.LinkedList;
import java.util.List;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;

public interface HybridProgram {

	public String toString();

	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains);

	public default void replaceTermRecursive(Term toReplace, Term replaceWith) {
		List<HybridProgram> innerHPs = this.getInnerPrograms(new LinkedList<HybridProgram>());
		for(HybridProgram innerHP : innerHPs) {
			innerHP.replaceTermRecursive(toReplace, replaceWith); //only overridden in DiscreteAssignment and ContinuousEvolution
		}
		List<Term> innerTerms = this.getInnerTerms(new LinkedList<Term>());
		for(Term innerTerm : innerTerms) {
			innerTerm.replaceTermRecursive(toReplace, replaceWith);
		}
	}

	public default boolean containsTerm(Term term) {
		List<HybridProgram> innerHPs = this.getInnerPrograms(new LinkedList<HybridProgram>());
		for(HybridProgram innerHP : innerHPs) {
			if(innerHP.containsTerm(term)) {
				return true;
			}
		}
		List<Term> innerTerms = this.getInnerTerms(new LinkedList<Term>());
		for(Term innerTerm : innerTerms) {
			if(innerTerm.containsTerm(term)) {
				return true;
			}
		}
		return false;
	}

	public default List<Variable> getBoundVariables(List<Variable> vars) {
		List<HybridProgram> innerHPs = this.getInnerPrograms(new LinkedList<HybridProgram>());
		for(HybridProgram innerHP : innerHPs) {
			innerHP.getBoundVariables(vars);
		}
		return vars;
	}
	
	public default List<Variable> getVariables(List<Variable> vars) {
		List<HybridProgram> innerHPs = this.getInnerPrograms(new LinkedList<HybridProgram>());
		for(HybridProgram innerHP : innerHPs) {
			innerHP.getVariables(vars);
		}
		List<Term> innerTerms = this.getInnerTerms(new LinkedList<Term>());
		for(Term innerTerm : innerTerms) {
			innerTerm.getVariables(vars);
		}
		return vars;
	}
	
	/**
	 * returns any hybrid program, which is an internal part of this hybrid program
	 * empty list for programs that do not have internal HPs
	 * @param hps TODO
	 * @return
	 */
	public List<HybridProgram> getInnerPrograms(List<HybridProgram> hps);
	
	/**
	 * returns any term, which is an internal part of this hybrid program
	 * empty list for programs that do not have internal terms
	 * @param terms TODO
	 * @return
	 */
	public List<Term> getInnerTerms(List<Term> terms);

	public HybridProgram createDeepCopy();
	
	/**
	 * Handles Simulink Vectors in hybrid programs. 
	 * Hybrid programs that contain VectorTerms or resized Variables are replaced by expanded HPs
	 * @return expanded hybrid program
	 */
	public HybridProgram expand();
}
