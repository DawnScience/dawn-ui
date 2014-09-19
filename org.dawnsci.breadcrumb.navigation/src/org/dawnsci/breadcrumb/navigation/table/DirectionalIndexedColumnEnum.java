/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.table;

import java.util.Collection;


public interface DirectionalIndexedColumnEnum {

	/**
	 * Name (label) of column
	 * @return
	 */
	public String getName() ;
	
	/**
	 * Column width
	 * @return
	 */
	public int getWidth() ;
	
	/**
	 * Expandable or no
	 * @return
	 */
	public boolean isExpandable();
	
	/**
	 * Direction of sort
	 * @return
	 */
	public int getDirection() ;
	
	/**
	 *  Direction of sort
	 * @param direction
	 */
	public void setDirection(int direction);
	
	/**
	 * Direction of sort
	 * @return
	 */
    public int toggleDirection() ;
    
    /**
     * Var name used for search
     * @return
     */
	public String getVarName();
	
	/**
	 * Label for given direction.
	 * @return
	 */
	public String getDirectionLabel() ;
	
	/**
	 * 0-based index of column.
	 * @return
	 */
	public int getIndex() ;

	/**
	 * Names of variables available for search
	 * @return
	 */
	public Collection<String> getVariableNames();

	/**
	 * Just returns the value of the values() call
	 * @return
	 */
	public DirectionalIndexedColumnEnum[] allValues();

	/**
	 * 
	 * @return the date column
	 */
	public DirectionalIndexedColumnEnum getDateColumn();
}
