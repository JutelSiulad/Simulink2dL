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

import java.util.List;

import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.term.Term;

public interface HybridProgram {

	public String toString();

	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains);

	public void replaceTermRecursive(Term toReplace, Term replaceWith);

	public boolean containsTerm(Term term);

	public HybridProgram createDeepCopy();
	
	/**
	 * Handles Simulink Vectors in hybrid programs. 
	 * Hybrid programs that contain VectorTerms or resized Variables are replaced by expanded HPs
	 * @return expanded hybrid program
	 */
	public HybridProgram expand();
	
	public void getBoundVariables(List<Variable> vars);
	
	public void getVariables(List<Variable> vars);

}
