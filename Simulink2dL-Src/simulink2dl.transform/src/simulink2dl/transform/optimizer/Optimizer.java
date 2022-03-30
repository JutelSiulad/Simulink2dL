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
import simulink2dl.dlmodel.operator.Operator;
import simulink2dl.dlmodel.operator.formula.BooleanConstant;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Disjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Implication;
import simulink2dl.dlmodel.operator.formula.Negation;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.StringFormula;
import simulink2dl.transform.dlmodel.TransformedDLModel;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;
import simulink2dl.util.PluginLogger;

public abstract class Optimizer {

	public void run(TransformedDLModel dLModel) {
		handleSingleProgram(dLModel.getHybridProgram());
	}

	private void handleSingleProgram(HybridProgram program) {
		// default hybrid programs
		if (program instanceof ContinuousEvolution) {
			ContinuousEvolution continuousEvolution = (ContinuousEvolution) program;
			// handle formula
			handleFormula(continuousEvolution.getEvolutionDomain());
			// handle program
			handleContinuousEvolution(continuousEvolution);
			// no recursive call
		} else if (program instanceof DebugString) {
			// no handling
			// no recursive call
		} else if (program instanceof DiscreteAssignment) {
			// handle program
			handleDiscreteAssignment((DiscreteAssignment) program);
			// no recursive call
		} else if (program instanceof HybridProgramCollection) {
			// handle program
			handleCollection((HybridProgramCollection) program);
		} else if (program instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) program;
			// handle formula
			handleFormula(ifStatement.getCondition());
			// handle program
			handleIfStatement((IfStatement) program);
			// recursive call for inner programs
			handleSingleProgram(ifStatement.getIfProgram());
			if (ifStatement.hasElse()) {
				handleSingleProgram(ifStatement.getElseProgram());
			}
		} else if (program instanceof NondeterministicAssignment) {
			// handle program
			handleNondeterministicAssignment((NondeterministicAssignment) program);
			// no recursive call
		} else if (program instanceof NondeterministicChoice) {
			NondeterministicChoice nondetChoice = (NondeterministicChoice) program;
			// handle program
			handleNondeterministicChoice(nondetChoice);
			// recursive call for inner programs
			for (HybridProgram choice : nondetChoice.getChoices()) {
				handleSingleProgram(choice);
			}
		} else if (program instanceof NondeterministicRepetition) {
			NondeterministicRepetition nondetRepetition = (NondeterministicRepetition) program;
			// handle program
			handleNondeterministicRepetition(nondetRepetition);
			// recursive call for inner program
			handleSingleProgram(((NondeterministicRepetition) program).getInnerProgram());
		} else if (program instanceof TestFormula) {
			TestFormula testFormula = (TestFormula) program;
			// handle formula
			handleFormula(testFormula.getFormula());
			// handle program
			handleTestFormula(testFormula);
			// no recursive call
		} else

		// special hybrid programs
		if (program instanceof ConditionalChoice) {
			ConditionalChoice conditionalChoice = (ConditionalChoice) program;
			// handle program
			handleConditionalChoice(conditionalChoice);
			// recursive call for inner programs
			for (HybridProgram choice : conditionalChoice.getChoices()) {
				handleSingleProgram(choice);
			}
		} else if (program instanceof ConditionalHybridProgram) {
			ConditionalHybridProgram conditionalHybridProgram = (ConditionalHybridProgram) program;
			// handle formula
			handleFormula(conditionalHybridProgram.getCondition());
			// handle program
			handleConditionalHybridProgram(conditionalHybridProgram);
			// recursive call for inner program
			handleSingleProgram(conditionalHybridProgram.getInnerProgram());
		} else

		// error case, not handled instance of a hybrid program
		{
			PluginLogger
					.error("Hybrid program of type \"" + program.getClass().getName() + "\" not handled in optimizer.");
		}
	}

	private void handleFormula(Formula formula) {
		// default formulas
		if (formula instanceof Conjunction) {
			Conjunction conjunction = (Conjunction) formula;
			
			
			// recursive call
			for (Operator elementOperator : conjunction.getElements()) {
				Formula element = (Formula) elementOperator;
				handleFormula(element);
			}
			// handle formula
			handleConjunctionFormula(conjunction);
		} else if (formula instanceof Disjunction) {
			Disjunction disjunction = (Disjunction) formula;
			// recursive call
			for (Operator elementOperator : disjunction.getElements()) {
				Formula element = (Formula) elementOperator;
				handleFormula(element);
			}
			// handle formula
			handleDisjunctionFormula(disjunction);
		} else if (formula instanceof Negation) {
			Negation negation = (Negation) formula;
			// recursive call
			handleFormula((Formula) negation.getInnerFormula());
			// handle formula
			handleNegationFormula(negation);
		} else if (formula instanceof Relation) {
			// no recursive call
			// handle formula
			handleRelationFormula((Relation) formula);
		} else if (formula instanceof BooleanConstant) {
			// no recursive call
			// no handling
		} else if (formula instanceof StringFormula) {
			// no recursive call
			// no handling
		} else

		// error case, not handled instance of a formula
		{
			PluginLogger.error("Formula of type \"" + formula.getClass().getName() + "\" not handled in optimizer.");
		}
	}
	
	protected void handleContinuousEvolution(ContinuousEvolution conEvolution) {
		// do nothing
	}

	protected void handleDiscreteAssignment(DiscreteAssignment disAssignment) {
		// do nothing
	}

	protected void handleCollection(HybridProgramCollection hpCollection) {
		
		simplifyTriviallyTrueConditionalProgram(hpCollection);
		
		simplifyTriviallyTrueConditionalChoice(hpCollection);
		
		removeTrivialTests(hpCollection);
		
		trimCollection(hpCollection);
		
		mergeConditionalHybridPrograms(hpCollection);
		//recursive call for inner programs
		for (HybridProgram program : hpCollection.getInnerPrograms(new LinkedList<HybridProgram>())) {
			handleSingleProgram(program);
		}
		
		
	}
	
	protected void simplifyTriviallyTrueConditionalProgram(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for (int i = 0; i<hps.size(); i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof ConditionalHybridProgram) {
				if(FormulaOptimizer.isTriviallyTrue(((ConditionalHybridProgram)hp).getCondition())) {
					hps.set(i, ((ConditionalHybridProgram)hp).getInnerProgram());
				}
			}
		}
	}
	
	protected void simplifyTriviallyTrueConditionalChoice(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for (int i = 0; i<hps.size(); i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof ConditionalChoice) {
				ConditionalChoice condChoice = (ConditionalChoice)hp;
				if(condChoice.getChoices().size()==1) {
					ConditionalHybridProgram onlyChoice = condChoice.getChoices().get(0);
					if(FormulaOptimizer.isTriviallyTrue(onlyChoice.getCondition())) {
						hps.set(i, onlyChoice.getInnerProgram());
					}
				}
			}
		}
	}
	
	protected void trimCollection(HybridProgramCollection hpCollection) {
		List<HybridProgram> collectionSequence = hpCollection.getSequence();
		
		for(int i = 0; i<collectionSequence.size(); i++) {
			if(collectionSequence.get(i) instanceof HybridProgramCollection) {
				List<HybridProgram> nestedSequence = ((HybridProgramCollection)collectionSequence.get(i)).getSequence();
				if(nestedSequence.size()==1) {
					collectionSequence.set(i, nestedSequence.get(0));
				}
			}
		}
		
		for(int i = 0; i<collectionSequence.size(); i++) {
			HybridProgram hp = collectionSequence.get(i);
			if(hp instanceof HybridProgramCollection) {
				HybridProgramCollection innerProgram = (HybridProgramCollection) hp;
				if(innerProgram.isEmpty()) {
					collectionSequence.remove(i);
					i--;
				}
			}
		}
	}

	protected void removeTrivialTests(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for(int i = 0; i<hps.size();i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof TestFormula) {
				Formula test = ((TestFormula)hp).getFormula();
				if(FormulaOptimizer.isTriviallyTrue(test)) {
					hps.remove(i);
					i--;
				}
			}
		}
	}
	
	// merges successive conditional choice programs if tests are equal TODO: check if bound variables are influenced
	protected void mergeConditionalHybridPrograms(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for(int i = 0; i<hps.size()-1;i++) {
			HybridProgram currentHP = hps.get(i);
			HybridProgram nextHP = hps.get(i+1);
			if(currentHP instanceof ConditionalChoice && nextHP instanceof ConditionalChoice) {
				ConditionalChoice currentConditionalChoice = (ConditionalChoice) currentHP;
				ConditionalChoice nextConditionalChoice = (ConditionalChoice) nextHP;
				List<ConditionalHybridProgram> currentChoices = currentConditionalChoice.getChoices();
				List<ConditionalHybridProgram> nextChoices = nextConditionalChoice.getChoices();
				boolean equal = true;
				if(!(currentChoices.size()==nextChoices.size())) {
					equal = false;
				} else {
					for(int j = 0; j<currentChoices.size(); j++) {
						ConditionalHybridProgram currentChoice = currentChoices.get(j);
						ConditionalHybridProgram nextChoice = nextChoices.get(j);
						if(!currentChoice.getCondition().equals(nextChoice.getCondition())) {
							equal = false;
						}
					}
				}
				if(equal) {
					for(int j = 0; j<currentChoices.size(); j++) {
						ConditionalHybridProgram currentChoice = currentChoices.get(j);
						ConditionalHybridProgram nextChoice = nextChoices.get(j);
						HybridProgramCollection newHP = new HybridProgramCollection();
						newHP.addElement(currentChoice.getInnerProgram());
						newHP.addElement(nextChoice.getInnerProgram());
						currentChoice.setInnerProgram(newHP);
					}
					hps.remove(i+1);
					i--;
				}
			}
		}
	}

	protected void handleIfStatement(IfStatement ifStatement) {
		// do nothing
	}

	protected void handleNondeterministicAssignment(NondeterministicAssignment nondetAssignment) {
		// do nothing
	}

	protected void handleNondeterministicChoice(NondeterministicChoice nondetChoice) {
		// do nothing
	}

	protected void handleNondeterministicRepetition(NondeterministicRepetition nondetRepetition) {
		// do nothing
	}

	protected void handleTestFormula(TestFormula testFormula) {
		// do nothing
	}

	protected void handleConditionalChoice(ConditionalChoice condChoice) {
		// do nothing
	}

	protected void handleConditionalHybridProgram(ConditionalHybridProgram condHybridProgram) {
		// do nothing
	}

	protected void handleConjunctionFormula(Conjunction conjunction) {
		// do nothing
	}

	protected void handleDisjunctionFormula(Disjunction disjunction) {
		// do nothing
	}

	protected void handleNegationFormula(Negation negation) {
		// do nothing
	}

	protected void handleRelationFormula(Relation relation) {
		// do nothing
	}

}
