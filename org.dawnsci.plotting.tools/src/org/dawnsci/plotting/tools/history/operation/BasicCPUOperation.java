/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.history.operation;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

/**
 * Operation which supports basic operators.
 * 
 * @author Matthew Gerring
 * @author Baha El Kassaby - duplicated code from org.dawb.common.gpu (this code
 *         has been moved here as it is only used for HistoryTool)
 *
 */
class BasicCPUOperation implements IOperation {


	@Override
	public Dataset process(Dataset a, double b, Operator operation) {

		switch (operation) {
		case ADD:
			return Maths.add(a,b);
		case SUBTRACT:
			return Maths.subtract(a,b);
		case MULTIPLY:
			return Maths.multiply(a,b);
		case DIVIDE:
			return Maths.divide(a,b);
		}
		return null;
	}

	@Override
	public Dataset process(Dataset a, Dataset b, Operator operation) {

		switch (operation) {
		case ADD:
			return Maths.add(a,b);
		case SUBTRACT:
			return Maths.subtract(a,b);
		case MULTIPLY:
			return Maths.multiply(a,b);
		case DIVIDE:
			return Maths.divide(a,b);
		}
		return null;
	}


	/**
	 * Dispose is not a final state. You can still reuse the IOperation after this.
	 */
	@Override
	public void deactivate() {
		// Nothing to do
	}

}
