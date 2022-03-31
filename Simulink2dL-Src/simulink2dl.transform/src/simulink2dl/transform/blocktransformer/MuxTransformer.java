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
import java.util.List;

import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;

import simulink2dl.dlmodel.term.PortIdentifier;
import simulink2dl.dlmodel.term.ReplaceableTerm;
import simulink2dl.transform.Environment;
import simulink2dl.transform.dlmodel.TransformedSimulinkModel;
import simulink2dl.transform.macro.Macro;
import simulink2dl.transform.macro.VectorMacro;
import simulink2dl.util.PluginLogger;

public class MuxTransformer extends BlockTransformer {

	public MuxTransformer(SimulinkModel simulinkModel, TransformedSimulinkModel dlModel, Environment environment) {
		super(simulinkModel, dlModel, environment);
	}

	@Override
	public void transformBlock(SimulinkBlock block) {
		String type = "Mux";
		checkBlock(type, block);

		// get number of input ports
		String numberOfInputsString = block.getParameter("Inputs");

		// see https://www.mathworks.com/help/simulink/slref/demux.html under
		// parameter to see how input value is evaluated
		int numberOfInputs;
		try {
			numberOfInputs = Integer.parseInt(numberOfInputsString);
		} catch (NumberFormatException e) {
			PluginLogger.error("Could not parse number of inputs for mux block \"" + block.getName()
					+ "\". Number of inputs String is : " + numberOfInputsString);
			return;
		}

		// add vector macro
		VectorMacro vectorMacro = new VectorMacro(environment.getToReplace(block));

		for (int i = 1; i <= numberOfInputs; i++) {
			// get connected port
			SimulinkOutPort connectedPort = environment.getConnectedOuputPort(block, i);
			String connectedPortID = environment.getPortID(connectedPort);

			vectorMacro.addElement(new PortIdentifier(connectedPortID));
		}

		dlModel.addMacro(vectorMacro);
	}

	@Override
	public List<Macro> createMacro(SimulinkBlock block) {
		// TODO Auto-generated method stub
		List<Macro> macros = new ArrayList<>();
		PluginLogger.error("createMacro() is not yet implemented for " + this.getClass().getSimpleName());
		return macros;
	}

}
