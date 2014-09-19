/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

/**
 * This interface must be used to render a StyledTreeBreadcrumbViewer.
 * 
 * @author Matthew Gerring
 *
 */
public interface IStyledTreeLabelProvider extends ILabelProvider, IStyledLabelProvider{

}
