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

import java.util.Set;

import simulink2dl.dlmodel.hybridprogram.ContinuousEvolution;
import simulink2dl.dlmodel.hybridprogram.DebugString;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.IfStatement;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.hybridprogram.NondeterministicChoice;
import simulink2dl.dlmodel.hybridprogram.NondeterministicRepetition;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.transform.dlmodel.TransformedSimulinkModel;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;
import simulink2dl.util.PluginLogger;

/**
 * Main Optimizer for Transformed Hybrid Programs
 * @author Timm Liebrenz, Julius Adelt
 *
 */
public class Optimizer {
	
	// available optimization options - in menu creation (TransformPage - putAvailableOptimizersInTable())
	public static final String[] optimizations = {"HybridProgramCollectionOptimizer", "FormulaOptimizer", "ContradictionOptimizer", "EvolutionOptimizer", "ConditionalChoiceOptimizer"};
	private boolean optimizeCollections = false;
	private boolean optimizeFormulas = false;
	private boolean optimizeContradiction = false;
	private boolean optimizeEvolutions = false;
	private boolean optimizeConditionalChoice = false;
	
	public Optimizer(Set<String> optimizations) {
		if(optimizations.contains("HybridProgramCollectionOptimizer")) {
			optimizeCollections=true;
		}
		if(optimizations.contains("FormulaOptimizer")) {
			optimizeFormulas=true;
		}
		if(optimizations.contains("ContradictionOptimizer")) {
			optimizeContradiction=true;
		}
		if(optimizations.contains("EvolutionOptimizer")) {
			optimizeEvolutions=false; // always false, currently not supported!
		}
		if(optimizations.contains("ConditionalChoiceOptimizer")) {
			optimizeConditionalChoice=true;
		}
	}
	
	public void run(TransformedSimulinkModel dLModel) {
		handleInitialConditions(dLModel);
		handleHybridProgramCollection(dLModel.getHybridProgram());
	}

	
	private Formula handleFormula(Formula formula) {
		if(optimizeFormulas) {
			formula = FormulaOptimization.optimize(formula);
		}
		return formula;
	}
	
	private void handleSingleProgram(HybridProgram program) {
		// default hybrid programs
		if (program instanceof ContinuousEvolution) {
			handleContinuousEvolution((ContinuousEvolution) program);
		} else if (program instanceof DebugString) {
			// no handling
		} else if (program instanceof DiscreteAssignment) {
			handleDiscreteAssignment((DiscreteAssignment) program);
		} else if (program instanceof HybridProgramCollection) {
			handleHybridProgramCollection((HybridProgramCollection) program);
		} else if (program instanceof IfStatement) {
			handleIfStatement((IfStatement) program);
		} else if (program instanceof NondeterministicAssignment) {
			handleNondeterministicAssignment((NondeterministicAssignment) program);
		} else if (program instanceof NondeterministicChoice) {
			handleNondeterministicChoice((NondeterministicChoice) program);
		} else if (program instanceof NondeterministicRepetition) {
			handleNondeterministicRepetition((NondeterministicRepetition) program);
		} else if (program instanceof TestFormula) {
			handleTestFormula((TestFormula) program);
		} else
		// special hybrid programs
		if (program instanceof ConditionalChoice) {
			handleConditionalChoice((ConditionalChoice)program);
		} else if (program instanceof ConditionalHybridProgram) {
			handleConditionalHybridProgram((ConditionalHybridProgram) program);
		} else
		// error case, not handled instance of a hybrid program
		{
			PluginLogger
					.error("Hybrid program of type \"" + program.getClass().getName() + "\" not handled in optimizer.");
		}
	}

	private void handleInitialConditions(TransformedSimulinkModel dLModel) {
		Formula initCondition = dLModel.getInitialCondition();
		initCondition = handleFormula(initCondition);
		if(!(initCondition instanceof Conjunction)) {
			initCondition = new Conjunction(initCondition);
		}
		dLModel.setInitialCondition((Conjunction) initCondition);
	}
	
	private void handleIfStatement(IfStatement ifStatement) {
		// handle formula
		ifStatement.setCondition(handleFormula(ifStatement.getCondition()));
		// recursive call for inner programs
		handleSingleProgram(ifStatement.getIfProgram());
		if (ifStatement.hasElse()) {
			handleSingleProgram(ifStatement.getElseProgram());
		}
	}

	private void handleDiscreteAssignment(DiscreteAssignment program) {
		// do nothing
	}

	private void handleNondeterministicAssignment(NondeterministicAssignment nondetAssignment) {
		// do nothing
	}

	private void handleNondeterministicChoice(NondeterministicChoice nondetChoice) {
		// recursive call for inner programs
		for (HybridProgram choice : nondetChoice.getChoices()) {
			handleSingleProgram(choice);
		}
	}
	
	private void handleContinuousEvolution(ContinuousEvolution evolution) {
		Formula newDomain = handleFormula(evolution.getEvolutionDomain());
		evolution.setEvolutionDomain(newDomain);
		if(optimizeEvolutions) {
			EvolutionDomainOptimization.optimize(evolution);
		}
	}

	private void handleNondeterministicRepetition(NondeterministicRepetition nondetRepetition) {
		handleSingleProgram(nondetRepetition.getInnerProgram());
	}

	private void handleTestFormula(TestFormula testFormula) {
		Formula test = handleFormula(testFormula.getFormula());
		testFormula.setFormula(test);
	}

	private void handleConditionalHybridProgram(ConditionalHybridProgram conditionalHybridProgram) {
		Formula newCondition = handleFormula(conditionalHybridProgram.getCondition());
		conditionalHybridProgram.setCondition(newCondition);
		// recursive call
		handleSingleProgram(conditionalHybridProgram.getInnerProgram());
	}

	private void handleHybridProgramCollection(HybridProgramCollection hpCollection) {
		//recursive call for inner programs
		for (HybridProgram program : hpCollection.getSequence()) {
			handleSingleProgram(program);
		}
		if(optimizeCollections) {
			//handleCollection(hpCollection);
			CollectionOptimization.optimize(hpCollection);
		}

	}
	
	private void handleConditionalChoice(ConditionalChoice conditionalChoice) {
		if(optimizeConditionalChoice) {
			ConditionalChoiceOptimization.optimize(conditionalChoice);
		}
		if(optimizeContradiction) {
			ContradictionOptimization.optimize(conditionalChoice);
		}
		
		// recursive call for inner programs
		for (HybridProgram choice : conditionalChoice.getChoices()) {
			handleSingleProgram(choice);
		}
	}

}
