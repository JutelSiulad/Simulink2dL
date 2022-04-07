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
package simulink2dl.transform.blocktransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.DLModel;
import simulink2dl.dlmodel.contracts.ContinuousGhostVariable;
import simulink2dl.dlmodel.contracts.ContinuousHybridContract;
import simulink2dl.dlmodel.contracts.GhostVariable;
import simulink2dl.dlmodel.contracts.HybridContract;
import simulink2dl.dlmodel.contracts.RLAgentContract;
import simulink2dl.dlmodel.contracts.TimedContract;
import simulink2dl.dlmodel.contracts.hardcoded.TemperatureControlContract;
import simulink2dl.dlmodel.elements.Constant;
import simulink2dl.dlmodel.elements.Variable;
import simulink2dl.dlmodel.hybridprogram.DiscreteAssignment;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.NondeterministicAssignment;
import simulink2dl.dlmodel.operator.formula.Conjunction;
import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.dlmodel.term.Term;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.TransformedSimulinkModel;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.SimpleMacro;
import simulink2dl.transform.model.TimedContractBehavior;
import simulink2dl.util.PluginLogger;

/** Service transformer for the Simulink2dL project.
 * TODO: Rework contract implementation.
 * - Use parser instead of hard-coded contracts.
 * - Delete Contract interface, which only exists to support deprecated SimpleContract.
*/
public class ServiceTransformer extends BlockTransformer {
	protected List<Variable> inputVariables;
	protected List<Variable> outputVariables;
	
	public ServiceTransformer(SimulinkModel simulinkModel, TransformedSimulinkModel dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
		
		this.inputVariables = new LinkedList<Variable>();
		this.outputVariables = new LinkedList<Variable>();
	}
	
	public void transformBlock(SimulinkBlock block) {
		List<HybridContract> contracts = createContracts(dlModel, block.getName());
		transformContracts(contracts, block);
	}
	
	/**
	 * Transform contract to HP
	 * @param contracts
	 * @param block
	 */
	private void transformContracts(List<HybridContract> contracts, SimulinkBlock block) {

		List<Macro> inputMacros = new LinkedList<Macro>();
		List<Macro> outputMacros = new LinkedList<Macro>();
		
		Conjunction initConditions = new Conjunction();
		HybridProgramCollection ghostAssignments = new HybridProgramCollection();
		HybridProgramCollection continuousGhostAssignments = new HybridProgramCollection();
		HybridProgramCollection contractAssignments = new HybridProgramCollection();
		HybridProgramCollection contractFormulas = new HybridProgramCollection();
		
		LinkedList<Constant> constants = new LinkedList<Constant>();
		LinkedList<Variable> contractVariables = new LinkedList<Variable>();
		LinkedList<Variable> ghostVariables = new LinkedList<Variable>();
		
		// preparations
		for(HybridContract contract : contracts) {
			addInputMacros(contract, block, inputMacros);
			addGhostAssignments(contract, ghostAssignments, continuousGhostAssignments, ghostVariables);
			addContractAssignments(contract, block, contractAssignments, contractVariables, outputMacros);
			addConstants(contract, constants);
			initConditions.addElement(contract.asFormula());
			contractFormulas.addElement(contract.asTestFormula());
		}
		
		// replace internal contract variables with inputs from surrounding model
		applyInputMacros(inputMacros, initConditions, ghostAssignments, continuousGhostAssignments, contractAssignments, contractFormulas);
//		for(Macro inputMacro : inputMacros) {
//			dlModel.addMacro(inputMacro);
//		}
		//add Variables to model
		for (Constant constant : constants) {
			dlModel.addConstant(constant);
		}
		for (Variable variable : contractVariables) {
			dlModel.addVariable(variable);
		}
		for (Variable variable : ghostVariables) {
			dlModel.addVariable(variable);
		}
		
		//add contracts to model
		HybridContract firstContract = contracts.get(0); //assumption: all contracts in the list are from the same class, TODO add method to check this 
		if(firstContract instanceof TimedContract || firstContract instanceof RLAgentContract) {
			String sampleTime = ((RLAgentContract)firstContract).getSampleTime();
			addTimedContracts(dlModel, initConditions, ghostAssignments, contractAssignments, contractFormulas, constants, contractVariables, sampleTime);
			if(firstContract instanceof RLAgentContract) {
				dlModel.addRLContractsToModel(contracts);
			}
		} else if (firstContract instanceof ContinuousHybridContract) {
			addContinuousContracts(dlModel, initConditions, ghostAssignments, continuousGhostAssignments, contractAssignments, contractFormulas, constants, contractVariables);
		} else {
			addDiscreteContracts(dlModel, initConditions, ghostAssignments, contractAssignments, contractFormulas, constants, contractVariables);
		}
		
		for(Macro outputMacro : outputMacros) {
			dlModel.addMacro(outputMacro);
		}
		

	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

	// TODO temporary solution, fixed contracts
	// TODO later on: provide contracts in file
	protected List<HybridContract> createContracts(DLModel model, String serviceName) {
		List<HybridContract> contracts = new LinkedList<HybridContract>();
		//contract for non RL overriden in RL ServiceTransformer
		if (serviceName.endsWith("TemperatureControl")) {
			Constant smallstep = dlModel.getConstantByName("SMALLSTEPSIZE");
			contracts.add(new TemperatureControlContract(serviceName, smallstep));
		} else {
			PluginLogger.error("No contract creation given for service type: " + serviceName);
			return null;
		}
		return contracts;
	}
	
	
	
	/**
	 * add macros for replacing contract inputs with surrounding inputs to list
	 * @param contract
	 * @param block
	 * @param inputMacros
	 */
	private void addInputMacros(HybridContract contract, SimulinkBlock block, List<Macro> inputMacros) {
		LinkedList<Variable> inputs = contract.getInputs();
		Iterator<Variable> inputIterator = inputs.iterator();
		List<SimulinkInPort> inPorts= sortWithIndexIn(block.getInPorts());
		Iterator<SimulinkInPort> inPortIterator = inPorts.iterator();
		
		while(inputIterator.hasNext() && inPortIterator.hasNext()) {
			SimulinkOutPort connectedPort = environment.getConnectedOuputPort(inPortIterator.next());
			String connectedPortID = environment.getPortID(connectedPort);
			ReplaceableTerm inputTerm = new PortIdentifier(connectedPortID);
			ReplaceableTerm contractTermToReplace = inputIterator.next();
			inputMacros.add(new SimpleMacro(contractTermToReplace,inputTerm));
		}
	}
	
	/**
	 * The port collections returned by the block.getInPorts() and block.getOutPort() methods are not sorted.
	 * TODO: Revisit
	 * @param ports
	 * @return sorted List of inPorts
	 */
	private List<SimulinkInPort> sortWithIndexIn(Collection<SimulinkInPort> ports) {
		SimulinkInPort[] sortedCollection = new SimulinkInPort[ports.size()];
		Iterator<SimulinkInPort> portIterator = ports.iterator();
		while(portIterator.hasNext()) {
			SimulinkInPort curPort = portIterator.next();
			int index = Integer.parseInt(curPort.getIndex());
			sortedCollection[index-1]=curPort;
		}
		return Arrays.asList(sortedCollection);
	}
	
	/**
	 * The port collections returned by the block.getInPorts() and block.getOutPort() methods are not sorted.
	 * TODO: Revisit
	 * @param ports
	 * @return sorted List of outPorts
	 */
	private List<SimulinkOutPort> sortWithIndexOut(Collection<SimulinkOutPort> ports) {
		SimulinkOutPort[] sortedCollection = new SimulinkOutPort[ports.size()];
		Iterator<SimulinkOutPort> portIterator = ports.iterator();
		while(portIterator.hasNext()) {
			SimulinkOutPort curPort = portIterator.next();
			int index = Integer.parseInt(curPort.getIndex());
			sortedCollection[index-1]=curPort;
		}
		return Arrays.asList(sortedCollection);
	}
	
	/**
	 * add ghost variables and assignments to lists
	 * @param contract
	 * @param ghostAssignments
	 * @param ghostVariables
	 */
	private void addGhostAssignments(HybridContract contract, HybridProgramCollection ghostAssignments, HybridProgramCollection continuousGhostAssignments, LinkedList<Variable> ghostVariables) {
		for (GhostVariable ghost : contract.getGhostVariables()) {
			// quick and dirty way of enabling shared ghosts between contracts - TODO rework
			if(!(ghost instanceof ContinuousGhostVariable)) {
				if(!ghostVariables.contains(ghost) && dlModel.getVariableByName(ghost.getName())==null) {
					ghostAssignments.addElement(new DiscreteAssignment(ghost, ghost.getAssignedTerm()));
					ghostVariables.add(ghost);
				}
			}
			if(ghost instanceof ContinuousGhostVariable) {
				if(!ghostVariables.contains(ghost) && dlModel.getVariableByName(ghost.getName())==null) {
					continuousGhostAssignments.addElement(new DiscreteAssignment(ghost, ghost.getAssignedTerm()));
					ghostVariables.add(ghost);
				}
			}
		}
	}

	
	/**
	 * add output assignments to list
	 * @param contract
	 * @param block
	 * @param outputAssignments
	 * @param outputVariables
	 * @param outputMacros
	 */
	private void addContractAssignments(HybridContract contract, SimulinkBlock block, HybridProgramCollection outputAssignments, LinkedList<Variable> outputVariables, List<Macro> outputMacros) {
		LinkedList<Variable> outputs = contract.getOutputVariables();
		Iterator<Variable> outputIterator = outputs.iterator();
		List<SimulinkOutPort> outPorts = sortWithIndexOut(block.getOutPorts());
		Iterator<SimulinkOutPort> outPortIterator = outPorts.iterator();
		
		while(outputIterator.hasNext() && outPortIterator.hasNext()) {
			Variable variable = outputIterator.next();
			SimulinkOutPort outPort = outPortIterator.next();
			if(!outputVariables.contains(variable)) {
				outputVariables.add(variable);
				outputAssignments.addElement(new NondeterministicAssignment(variable));
			}
			Term replaceWith = variable;
			outputMacros.add(new SimpleMacro(environment.getToReplace(outPort), replaceWith));
		}
	}
	
	/**
	 * add contract constants to list
	 * @param contract
	 * @param constants
	 */
	private void addConstants(HybridContract contract, List<Constant> constants) {
		for (Constant constant : contract.getConstants()) {
			if(!constants.contains(constant)) {
				constants.add(constant);
			}
		}
	}

	private void applyInputMacros(List<Macro> inputMacros, Conjunction initialCondition, HybridProgramCollection ghostAssignments, HybridProgramCollection continuousGhostAssignments, HybridProgramCollection contractAssignments, HybridProgramCollection contractFormulas) {
		for(Macro inputMacro : inputMacros) {
			inputMacro.applyToHybridProgramCollection(ghostAssignments);
			inputMacro.applyToHybridProgramCollection(continuousGhostAssignments);
			inputMacro.applyToHybridProgramCollection(contractAssignments);
			inputMacro.applyToHybridProgramCollection(contractFormulas);
			inputMacro.applyToInitialConditions(initialCondition);
		}
	}
	
	private void addContinuousContracts(TransformedSimulinkModel dlModel, Conjunction initialCondition, HybridProgramCollection ghostAssignments, HybridProgramCollection continuousGhostAssignments, HybridProgramCollection contractAssignments, HybridProgramCollection contractFormulas, LinkedList<Constant> constants, LinkedList<Variable> outputVariables) {
		/* create contract capturing behavior of services that show continuous/concurrent behavior */
		dlModel.addInitialCondition(initialCondition);
		dlModel.addGhostAssignment(ghostAssignments);
		dlModel.addContinuousGhostAssignment(continuousGhostAssignments);
		dlModel.addContinuousContractAssignment(contractAssignments);
		dlModel.addContinuousContractTest(contractFormulas);
	}
	
	private void addDiscreteContracts(TransformedSimulinkModel dlModel, Conjunction initialCondition, HybridProgramCollection ghostAssignments, HybridProgramCollection contractAssignments, HybridProgramCollection contractFormulas, LinkedList<Constant> constants, LinkedList<Variable> outputVariables) {
		dlModel.addInitialCondition(initialCondition);
		dlModel.addGhostAssignment(ghostAssignments);
		dlModel.addDiscreteBehavior(contractAssignments);
		dlModel.addDiscreteBehavior(contractFormulas);
	}
	
	private void addTimedContracts(TransformedSimulinkModel dlModel, Conjunction initialCondition, HybridProgramCollection ghostAssignments, HybridProgramCollection contractAssignments, HybridProgramCollection contractFormulas, LinkedList<Constant> constants, LinkedList<Variable> outputVariables, String sampleTime) {
		dlModel.addInitialCondition(initialCondition);
		TimedContractBehavior discreteBehavior = new TimedContractBehavior(sampleTime);
		discreteBehavior.addBehavior(ghostAssignments);
		discreteBehavior.addBehavior(contractAssignments);
		discreteBehavior.addBehavior(contractFormulas);
		discreteBehavior.addDiscreteContract(dlModel);
	}

}
