/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

import javax.measure.Quantity;

public class AmountEvent<Q extends Quantity<Q>> extends EventObject {
	private static final long serialVersionUID = -7433184477752660193L;

	private Quantity<Q> amount;

	public AmountEvent(Object source, Quantity<Q> amount) {
		super(source);
		this.amount = amount;
	}

	public Quantity<Q> getAmount() {
		return amount;
	}

	public void setAmount(Quantity<Q> amount) {
		this.amount = amount;
	}
}
