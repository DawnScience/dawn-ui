/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventListener;

import javax.measure.quantity.Quantity;

public interface AmountListener<E extends Quantity> extends EventListener {

	void amountChanged(AmountEvent<E> evt);
}
