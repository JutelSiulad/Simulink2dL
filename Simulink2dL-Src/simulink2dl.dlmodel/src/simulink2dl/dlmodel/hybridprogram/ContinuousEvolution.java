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
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.term.Term;

/**
 * This class represents the continuous evolution along a differential equation
 * system.
 * 
 * @author Timm Liebrenz
 *
 */
public class ContinuousEvolution implements HybridProgram {

	private List<DifferentialEquation> differentialEquations;

	private Formula evolutionDomain;

	/**
	 * Constructor without given evolutions.
	 */
	public ContinuousEvolution() {
		this.differentialEquations = new LinkedList<DifferentialEquation>();
		this.evolutionDomain = new BooleanConstant(true);
	}

	/**
	 * Constructor with given evolution formulas and no evolution domain.
	 * 
	 * @param newEvolutions
	 */
	public ContinuousEvolution(DifferentialEquation... newEvolutions) {
		this.differentialEquations = new LinkedList<DifferentialEquation>();
		for (DifferentialEquation newEvolution : newEvolutions) {
			this.differentialEquations.add(newEvolution);
		}
		this.evolutionDomain = null;
	}

	/**
	 * Constructor with given evolution formulas and evolution domain.
	 * 
	 * @param evolutionDomain
	 * @param newEvolutions
	 */
	public ContinuousEvolution(Formula evolutionDomain, DifferentialEquation... newEvolutions) {
		this.differentialEquations = new LinkedList<DifferentialEquation>();
		for (DifferentialEquation newEvolution : newEvolutions) {
			this.differentialEquations.add(newEvolution);
		}
		this.evolutionDomain = evolutionDomain;
	}

	/**
	 * Adds a single evolution to this continuous evolution.
	 * 
	 * @param variable
	 * @param term
	 */
	public void addSingleEvolution(DifferentialEquation evolutionTerm) {
		differentialEquations.add(evolutionTerm);
	}

	/**
	 * Adds a continuous evolution for the given variable in the form "variable' =
	 * term".
	 * 
	 * @param variable
	 * @param term
	 */
	public void addSingleEvolution(Variable variable, Term term) {
		differentialEquations.add(new DifferentialEquation(variable, term));
	}

	/**
	 * Returns the evolution domain of this continuous evolution.
	 * 
	 * @return
	 */
	public Formula getEvolutionDomain() {
		return evolutionDomain;
	}

	/**
	 * Sets the given formula as new evolution domain.s
	 * 
	 * @param newEvolutionDomain
	 */
	public void setEvolutionDomain(Formula newEvolutionDomain) {
		this.evolutionDomain = newEvolutionDomain;
	}

	public List<DifferentialEquation> getEvolutionFormulas() {
		return differentialEquations;
	}

	@Override
	public String toString() {
		String evolutionString = "";
		boolean isFirst = true;
		for (DifferentialEquation formula : differentialEquations) {
			if (!isFirst) {
				evolutionString += ",";
			} else {
				isFirst = false;
			}
			evolutionString += formula.toString();
		}
		if (evolutionDomain != null) {
			return "(" + evolutionString + "& " + evolutionDomain.toString() + ")";
		} else {
			return "(" + evolutionString + " & true)";
		}
	}

	@Override
	public String toStringFormatted(String indent, boolean multiLineTestFormulas, boolean multiLineEvolutionDomains) {
		String evolutionString = "";
		boolean isFirst = true;
		for (DifferentialEquation formula : differentialEquations) {
			if (!isFirst) {
				evolutionString += ",\n" + indent + "  ";
			} else {
				isFirst = false;
			}
			evolutionString += formula.toString();
		}
		String evolutionDomainString;
		if (evolutionDomain == null) {
			evolutionDomainString = "true";
		} else if (multiLineEvolutionDomains) {
			evolutionDomainString = evolutionDomain.toStringFormatted(indent + "  ", multiLineTestFormulas,
					multiLineEvolutionDomains);
		} else {
			evolutionDomainString = evolutionDomain.toString();
		}
		return "{\n" + indent + "  " + evolutionString + "\n" + indent + "& " + evolutionDomainString + "\n" + indent
				+ "}";
	}
	
	@Override
	public ContinuousEvolution createDeepCopy() {
		ContinuousEvolution result = new ContinuousEvolution(evolutionDomain.createDeepCopy());

		for (DifferentialEquation singleEvolution : differentialEquations) {
			result.addSingleEvolution(singleEvolution.createDeepCopy());
		}

		return result;
	}

	@Override
	public HybridProgram expand() {
		List<DifferentialEquation> expandedEvolutionFormulas = new LinkedList<DifferentialEquation>();
		for (DifferentialEquation evolution : differentialEquations) {
			LinkedList<DifferentialEquation> expandedEvolutions = evolution.expand();
			for(DifferentialEquation expandedEvolution : expandedEvolutions) {
				expandedEvolutionFormulas.add(expandedEvolution);
			}
		}
		differentialEquations = expandedEvolutionFormulas;
		return this;
	}

	@Override
	public List<Variable> getBoundVariables(List<Variable> vars) {
		for (DifferentialEquation evolution : differentialEquations) {
			Variable cvar = evolution.getVariable();
			if(!vars.contains(evolution.getVariable())) {
				cvar.getVariables(vars);
			}
		}
		return vars;
	}

	@Override
	public List<HybridProgram> getInnerPrograms(List<HybridProgram> hps) {
		return new LinkedList<HybridProgram>();
	}

	@Override
	public List<Term> getInnerTerms(List<Term> terms) {
		terms.add(evolutionDomain);
		terms.addAll(differentialEquations);
		return terms;
	}
	

}
