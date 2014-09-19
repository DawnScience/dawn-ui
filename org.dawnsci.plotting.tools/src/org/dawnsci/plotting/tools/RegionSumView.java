/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * View to used to open the RegionSumTool programmatically
 * @author wqk87977
 *
 */
public class RegionSumView extends ViewPart {

	private Composite parent;
	public RegionSumView() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		this.parent.setLayout(new GridLayout(1, false));
		this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public Composite getComposite(){
		return parent;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose(){
		
	}
}
