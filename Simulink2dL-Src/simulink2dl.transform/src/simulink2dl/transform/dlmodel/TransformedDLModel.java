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
package simulink2dl.transform.dlmodel;

import java.util.LinkedList;
import java.util.List;
import simulink2dl.dlmodel.DLModelDefaultStructure;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.DifferentialEquation;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.dlmodel.operator.formula.Relation;
import simulink2dl.dlmodel.operator.formula.Relation.RelationType;
import simulink2dl.dlmodel.term.RealTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.dlmodel.term.VectorTerm;
import simulink2dl.transform.Environment;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SizePropagationMacro;
import simulink2dl.transform.model.ConcurrentContractBehavior;
import simulink2dl.transform.model.ContinuousEvolutionHandler;
import simulink2dl.transform.model.TimedBehaviorHandler;
import simulink2dl.transform.model.TimedContractBehavior;
import simulink2dl.transform.model.container.ContinuousEvolutionContainer;
import simulink2dl.util.PluginLogger;

public class TransformedDLModel extends DLModelDefaultStructure {

	private List<Macro> macros;
	private List<HybridContract> rLContracts;
	private List<TimedBehaviorHandler> timedBehaviorHandlers;
	private ConcurrentContractBehavior concurrentContracts;
	private HybridProgramCollection unsortedOutpus;
	private ContinuousEvolutionHandler continuousEvolutionHandler;
	
	private HybridProgramCollection smallStepAndEpsilonAssignment;
	private HybridProgramCollection discreteInputBehavior;
	private HybridProgramCollection contractGhostAssignments;
	private HybridProgramCollection timedOutputBehavior;
	private HybridProgramCollection discreteBehavior;
	private HybridProgramCollection discreteOutputBehavior;
	private HybridProgramCollection continousBehavior;
	private HybridProgramCollection continousOutputBehavior;
	private HybridProgramCollection continuousContractAssignments;
	private HybridProgramCollection continuousContractTests;
	private HybridProgramCollection continuousInputBehavior;
	private HybridProgramCollection continuousInputTests;
	
	@Deprecated
	protected HybridProgramCollection contractBehavior;

	protected List<HybridContract> contracts;

	private Variable simClock;

	public TransformedDLModel() {
		rLContracts = new LinkedList<HybridContract>();
		macros = new LinkedList<Macro>();
		contracts = new LinkedList<HybridContract>();
		timedBehaviorHandlers = new LinkedList<TimedBehaviorHandler>();
		continuousEvolutionHandler = new ContinuousEvolutionHandler();
		unsortedOutpus = new HybridProgramCollection();
		
		// transformed model structure to ease accessing individual parts of the hybrid program
		smallStepAndEpsilonAssignment = new HybridProgramCollection();
		discreteInputBehavior = new HybridProgramCollection();
		contractGhostAssignments = new HybridProgramCollection();
		timedOutputBehavior = new HybridProgramCollection();
		discreteBehavior = new HybridProgramCollection();
		discreteOutputBehavior = new HybridProgramCollection();
		continousBehavior = new HybridProgramCollection();
		continousOutputBehavior = new HybridProgramCollection();
		continuousContractAssignments  = new HybridProgramCollection();
		continuousContractTests = new HybridProgramCollection();
		continuousInputBehavior = new HybridProgramCollection();
		continuousInputTests = new HybridProgramCollection();
		// add everything to the model
		this.addToHybridProgram(smallStepAndEpsilonAssignment);
		this.addToHybridProgram(contractGhostAssignments);
		this.addToHybridProgram(discreteInputBehavior);
		this.addToHybridProgram(timedOutputBehavior);
		this.addToHybridProgram(discreteBehavior);
		this.addToHybridProgram(discreteOutputBehavior);
		this.addToHybridProgram(continousBehavior); //this will remain empty until the model is finalized and the evolutions from continuousEvolutionHandler are added
		this.addToHybridProgram(continousOutputBehavior);
		this.addToHybridProgram(continuousContractAssignments);
		this.addToHybridProgram(continuousContractTests);
		this.addToHybridProgram(continuousInputBehavior);
		this.addToHybridProgram(continuousInputTests);
	}

	public void addInitialCondition(Relation initCondition) {
		initialConditions.addElement(initCondition);
		
	}
	
	public void addToSmallstepReset(HybridProgram hp) {
		smallStepAndEpsilonAssignment.addElement(hp);
	}

	public void addSimClock(DLModelDefaultStructure model) {
		simClock = new Variable("R", "simTime");
		addInitialCondition(new Relation(simClock, RelationType.EQUAL, new RealTerm(0.0)));
		addVariable(simClock);
		continuousEvolutionHandler.addNewSingleEvolution(new DifferentialEquation(simClock, new RealTerm(1.0)));
	}
	
	public void addDiscreteBehavior(HybridProgram hp) {
		discreteBehavior.addElement(hp);
	}
	
	
	public void addOutput(HybridProgram output) {
		unsortedOutpus.addElement(output);
	}
	
	public void addContinousOutput(HybridProgram output) {
		continousOutputBehavior.addElement(output);
	}
	
	public void addContinousBehavior(HybridProgram hp) {
		continousBehavior.addElement(hp);
	}
	
	
	public void addDiscreteOutput(HybridProgram output) {
		discreteOutputBehavior.addElement(output);
	}
	
	public void addMacro(Macro newMacro) {
		macros.add(newMacro);
	}
	
	public void addTimedOutputBehavior(HybridProgram stepOutputChoice) {
		timedOutputBehavior.addElement(stepOutputChoice);
	}
	/**
	 * Replaces vector-variables with one variable for each entry
	 */
	public void expandVariables() {
		for(int i = 0; i<variables.size(); i++) {
			Variable curVar = variables.get(i);
			if(curVar.getSize()>0) { //Variable has been resized
				VectorTerm newVariables = curVar.getVector();
				for(int j=0; j<newVariables.size();j++) {
					variables.add(i+j, (Variable)newVariables.get(j)); 
				}
				variables.remove(curVar);
			}
			
		}
	}
	
	public void finalizeModel(Environment environment) {
		finalizeMacros();
		
		for (TimedBehaviorHandler timedBehavior : timedBehaviorHandlers) {
			timedBehavior.addToModel(this);
		}
		// apply macros to all parts of the model
		applyMacros();
		handleUnsortedOutputs();
		expandVectors();
		environment.finalizeEnvironment();
		
		if(concurrentContracts!=null) {
			//concurrentContracts.addToModelHandler(this);
		}
		
		continousBehavior.addElement(continuousEvolutionHandler.asHybridProgram());

		// add discrete steps to continuous evolution domains
		for (TimedBehaviorHandler timedBehavior : timedBehaviorHandlers) {
			Variable clock = timedBehavior.getClockVariable();
			Constant stepsize = timedBehavior.getStepSizeConstant();

			Formula stepCondition = new Relation(clock, Relation.RelationType.LESS_EQUAL, stepsize);

			addConjunctionToAllEvolutionDomains(stepCondition);
		}
		
	}
	
	private void handleUnsortedOutputs() {
		List<HybridProgram> outputAssignments = unsortedOutpus.getInnerPrograms(new LinkedList<HybridProgram>());
		
		LinkedList<Variable> discBoundVars = new LinkedList<Variable>();
		discreteBehavior.getBoundVariables(discBoundVars);
		LinkedList<Variable> contBoundVars = new LinkedList<Variable>();
		continuousEvolutionHandler.getBoundVariables(contBoundVars);
		
		for(HybridProgram e : outputAssignments) {
			DiscreteAssignment outputAssignment = (DiscreteAssignment)e;
			LinkedList<Variable> freeVariables = new LinkedList<Variable>();
			outputAssignment.getAssignmentTerm().getVariables(freeVariables);
			
			//check whether output is dependent on continuous variable
			boolean isContinuous = false;
			boolean isDiscrete = false;
			for(Variable fvar : freeVariables) {
				if(contBoundVars.contains(fvar)) {
					isContinuous=true;
					break; //break on first variable changed by continuous evolution
				} else if (discBoundVars.contains(fvar)) {
					isDiscrete=true; 
					// keep searching for continuous variable
				}
			}
			
			if(isContinuous|!(isDiscrete)) {
				continousOutputBehavior.addElement(e);
			} else {
				discreteOutputBehavior.addElement(e);
			}
		}
	}

	/**
	 * Apply Macros to the dL model
	 */
	private void applyMacros() {
		for (Macro macro : macros) {
			
			// handle initial conditions
			macro.applyToInitialConditions(initialConditions);
			// apply to unsorted outputs
			macro.applyToHybridProgramCollection(unsortedOutpus);
			// apply to all parts of the program
			macro.applyToHybridProgramCollection(hybridProgram);
			macro.applyToContinuousBehavior(continuousEvolutionHandler);
		}
	}
	
	/**
	 * Expand HPs with VectorTerms
	 */
	private void expandVectors() {
		unsortedOutpus = (HybridProgramCollection) unsortedOutpus.expand();
		hybridProgram = (HybridProgramCollection) hybridProgram.expand();
		continuousEvolutionHandler = continuousEvolutionHandler.expand();
		initialConditions = (Conjunction) initialConditions.expand();
		expandVariables();
	}

	/**
	 * Applies all macros to each other until no new Macros are created.
	 */
	private void finalizeMacros() {
		long finalStart = System.currentTimeMillis();
		List<Macro> newMacros;
		PluginLogger.info("[EVALUATION] A total of " + macros.size() + " macros where handled.");
		do {
			newMacros = applyMacrosToEachOther();
			macros.addAll(newMacros);
		}while(newMacros.size()>0);

		long finalEnd = System.currentTimeMillis();
		PluginLogger.info("[EVALUATION] " + (finalEnd - finalStart) + " ms for macro finalizing.");
	}
	
	/**
	 * Applies all macros to each other. A macro that replaces the term "#x" updates
	 * all macros, which have the term "#x" in their replaceWith term.
	 * 
	 * Conditional macros applied to normal macros create new conditional macros and
	 * remove the original normal macro. Whenever a conditional macro is applied to
	 * another conditional macro, the original macro is extended by new cases and
	 * its condition formulas are updated.
	 */
	public List<Macro> applyMacrosToEachOther() {
		// apply each macro to each other macro
		LinkedList<Macro> newMacros = new LinkedList<Macro>();
		
		PluginLogger.debug("\n applyMacrosToEachOther called");
		for (int outerIndex = 0; outerIndex < macros.size(); outerIndex++) {
			Macro outerMacro = macros.get(outerIndex);
			if(outerMacro instanceof SizePropagationMacro){
				continue;
			}
			
			PluginLogger.debug("\n Outer macro: "+outerMacro.getClass().toString().replace("class simulink2dl.transform.macro.","")+": "+"<"+outerMacro.toString()+">");
			
			for (int innerIndex = 0; innerIndex < macros.size(); innerIndex++) {
				Macro innerMacro = macros.get(innerIndex);
				
				// check whether the outer macro influences the inner macro
				if (!outerMacro.equals(innerMacro) && innerMacro.containsTerm(outerMacro.getToReplace())) {
					
					PluginLogger.debug("\tApplying outer macro to " + 
							innerMacro.getClass().toString().replace("class simulink2dl.transform.macro.",""));
					PluginLogger.debug("\t<"+innerMacro.toString()+">" + " to " +"<"+outerMacro.toString()+">");
					
					// create and add new macros
					Macro originalMacro = innerMacro.createDeepCopy();
					List<Macro> newMacrosThisIteration = innerMacro.applyOtherMacro(outerMacro);
					
					// avoid infinite loops in case applyOtherMacro returns unchanged innerMacro
					for (Macro macro : newMacrosThisIteration) {
						if(macro.equals(originalMacro)) {
							newMacrosThisIteration.remove(macro);
						}
					}
					
					if(newMacrosThisIteration.size()>0) {
						// remove old macro
						macros.remove(innerIndex);
						innerIndex += - 1;
						
						newMacros.addAll(newMacrosThisIteration);
						
						for(Macro mac : newMacrosThisIteration) {
							System.out.println("\tresulting macro: "+mac.getClass().toString().replace("class simulink2dl.transform.macro.","")+": " + mac.toString());
						}	
					}
				}
			}
		}
		return newMacros;
	}

	
	/**
	 * Applies a macro to all other macros.
	 */
	public void finalizeSingleMacro(Macro macro) {
		for (int i = 0; i < macros.size(); i++) {
			Macro apply = macros.get(i);
			if (macro.equals(apply))
				continue;

			if (macro.containsTerm(apply.getToReplace())) {
				macro.applyOtherMacro(apply);
			}
		}
	}

	public void addContractBehavior(HybridProgram newContractBehavior) {
		contractBehavior.addElement(newContractBehavior);
	}

	public HybridProgramCollection getContractBehavior() {
		return contractBehavior;
	}

	/**
	 * Returns a behavior block for discrete behavior with the given step size. If
	 * such a block already exists, this block is returned otherwise a new one is
	 * created.
	 * 
	 * @param stepSize
	 * @return
	 */
	public TimedBehaviorHandler getDiscreteBehavior(String stepSize) {
		// search existing behavior blocks for given stepSize
		for (TimedBehaviorHandler existingBehavior : timedBehaviorHandlers) {
			if (!(existingBehavior instanceof TimedContractBehavior) & existingBehavior.isStepTime(stepSize)) {
				return existingBehavior;
			}
		}

		// create new block
		TimedBehaviorHandler newBehavior = new TimedBehaviorHandler(stepSize);
		timedBehaviorHandlers.add(newBehavior);
		return newBehavior;
	}
	
	public ConcurrentContractBehavior getConcurrentContractBehavior() {
		if(concurrentContracts == null) {
			concurrentContracts = new ConcurrentContractBehavior();
		}
		return concurrentContracts;
	}

	/**
	 * Adds a continuous evolution to the model. The evolution has the form
	 * "variable' = evolutionTerm"
	 * 
	 * @param variable
	 * @param evolutionTerm
	 */
	public void addContinuousEvolution(Variable variable, Term evolutionTerm) {
		continuousEvolutionHandler.addNewSingleEvolution(new DifferentialEquation(variable, evolutionTerm));
	}
	
	/**
	 * Adds a continuous evolution to the model. The evolution has the form
	 * "variable' = evolutionTerm"
	 * 
	 * @param variable
	 * @param evolutionTerm
	 */
	public void addTimedBehavior(TimedBehaviorHandler behavior) {
		timedBehaviorHandlers.add(behavior);
	}

	/**
	 * Adds new continuous evolution to the continuous behavior. Each given element
	 * can contain a number of evolution domains and each element can have an
	 * individual condition. The new evolution alternatives are added
	 * 
	 * @param evolution
	 */
	public void addNewEvolutionAlternatives(List<ContinuousEvolutionContainer> toAddEvolutionsAlternatives) {
		continuousEvolutionHandler.addNewEvolutionAlternatives(toAddEvolutionsAlternatives);
	}

	public Variable getSimulationClockVariable() {
		return simClock;
	}

	public void addAlternativeToAllEvolutionDomains(Formula evolutionDomain) {
		continuousEvolutionHandler.addAlternativeToAllEvolutionDomains(evolutionDomain);
	}
	public void addConjunctionToAllEvolutionDomains(Formula evolutionDomain) {
		continuousEvolutionHandler.addConjunctionToAllEvolutionDomains(evolutionDomain);
	}
	
	public void addContracts(List<HybridContract> contracts2) {
		contracts.addAll(contracts2);
	}

	public void addRLContractsToModel(List<HybridContract> contracts){
		rLContracts.addAll(contracts);
	}
	
	public void addRLContractToModel(HybridContract contract){
		rLContracts.add(contract);
	}

	public List<HybridContract> getRLContracts() {
		return rLContracts;
	}

	public void addDiscreteInput(HybridProgram inputProgram) {
		discreteInputBehavior.addElement(inputProgram);
	}

	public void addContinousInput(HybridProgram inputProgram) {
		continuousInputBehavior.addElement(inputProgram);
	}
	
	public void addGhostAssignment(HybridProgram inputProgram) {
		continuousInputBehavior.addElement(inputProgram);
	}
	
	public void addContinuousContractAssignment(HybridProgram inputProgram) {
		continuousContractAssignments.addElement(inputProgram);
	}
	
	public void addContinuousContractTest(HybridProgram inputProgram) {
		continuousContractTests.addElement(inputProgram);
	}
	

}
