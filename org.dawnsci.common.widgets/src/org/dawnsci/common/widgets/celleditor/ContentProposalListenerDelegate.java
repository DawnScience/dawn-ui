/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener;

public abstract class ContentProposalListenerDelegate implements IContentProposalListener {

    protected ContentProposalAdapter adapter;

	public void setAdapter(ContentProposalAdapter adapter) {
    	this.adapter = adapter;
    }
}
