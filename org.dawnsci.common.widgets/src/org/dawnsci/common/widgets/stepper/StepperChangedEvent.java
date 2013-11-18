/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.stepper;

public class StepperChangedEvent {
	private String label;

	private int position;

	private Object source;

	public StepperChangedEvent(Stepper source, String label, int position) {
		this.source = source;
		this.label = label;
		this.position = position;
	}

	public StepperChangedEvent(Stepper stepper, int selection) {
		this(stepper, null, selection);
	}

	public String getLabel() {
		return label;
	}

	public int getPosition() {
		return position;
	}

	public Object getSource() {
		return source;
	}
}