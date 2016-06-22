/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.history.operation;

/**
 * Factory for making operations. May extend if more
 * values of Operator become possible.
 * 
 * @author Matthew Gerring
 * @author Baha El Kassaby - duplicated code from org.dawb.common.gpu (this code
 *         has been moved here as it is only used for HistoryTool)
 *
 */
public class OperationFactory {

	/**
	 * @return
	 */
	public static IOperation getBasicCpuOperation() {
		return new BasicCPUOperation();
	}

}
