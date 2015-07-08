/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.transferable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.editors.preference.EditorConstants;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.hdf5.editor.H5Path;
import org.eclipse.dawnsci.plotting.api.expressions.IExpressionObject;
import org.eclipse.dawnsci.slicing.api.data.AbstractTransferableDataObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class TransferableDataObject extends AbstractTransferableDataObject implements H5Path {
	
	private static final Logger logger = LoggerFactory.getLogger(TransferableDataObject.class);

	private IDataHolder       holder;
	private IMetadata         metaData;

	private static int expressionCount=0;
	
	/**
	 * Clones the object and sets the transientData flag to true.
	 */
	public ITransferableDataObject clone() {
		TransferableDataObject ret = new TransferableDataObject();
		ret.holder   = holder.clone();
		ret.metaData = metaData.clone();
		ret.setChecked(isChecked());
		ret.name = name;
		ret.setVariable(getVariable());
		ret.setExpression(getExpression());
		ret.setMementoKey(getMementoKey());
		ret.service   = service;
		ret.transientData=true;
		return ret;
	}

	private TransferableDataObject() {
		
	}
	
	protected TransferableDataObject(IDataHolder holder, IMetadata meta) {
		this.holder   = holder;
		this.metaData = meta;
		expressionCount++;
		setVariable("expr"+expressionCount);
	}

	protected TransferableDataObject(IDataHolder holder, IMetadata meta, final String name) {
		super(name);
		this.holder   = holder;
		this.metaData = meta;
		
		setVariable(service.getSafeName(name));
	}

	protected TransferableDataObject(IDataHolder holder, IMetadata meta, IExpressionObject expression2) {
		this.holder     = holder;
		this.metaData   = meta;
		setExpression(expression2);
		expressionCount++;
		setVariable("expr"+expressionCount);
		expression2.setExpressionName(getVariable());
	}
	

	@Override
	public IDataset getData(IMonitor monitor) {
		
		IDataset set = null;
		if (!isExpression()) {
			try {
				if (holder==null) return null;
				set = holder.getDataset(getName());
			} catch(IllegalArgumentException ie) {
				try {
					ILazyDataset lz = holder.getLazyDataset(name);
					IDataset all = lz.getSlice();
					if (all.getSize()<2) throw new Exception();
					set =  all;
				} catch (Exception e) {
					try {
						set =  LoaderFactory.getDataSet(holder.getFilePath(), getName(), monitor);
					} catch (Exception e1) {
						return null;
					}
				}
			}
		} else {
			try {
				set =  getExpression().getDataSet(name, monitor);
			} catch (Exception e) {
				return null;
			}
		}	
		
		if (set!=null) set.setName(getName());
		return set;
	}

	@Override
	public ILazyDataset getLazyData(IMonitor monitor) {
		ILazyDataset set = null;
		if (!isExpression()) {
			if (holder==null) return null;
			set = holder.getLazyDataset(getName());
		} else {
			try {
				set = getExpression().getLazyDataSet(name, monitor);
			} catch (Exception e) {
				return null;
			}
		}		
		if (set!=null) set.setName(getName());
		return set;
	}

	@Override
	public int[] getShape(boolean force) {
		
		if (isExpression()) {
		    try {
		    	final IExpressionObject expr = getExpression();
		    	if (force) {
					final ILazyDataset lz = expr.getLazyDataSet(getVariable(), new IMonitor.Stub());
				    return lz.getShape();
		    	} else {
		    		final ILazyDataset lz = expr.getCachedLazyDataSet();
		    		return lz!=null ? lz.getShape() : null;
		    	}
			} catch (Exception e) {
				logger.error("Could not get shape of "+getVariable());
				return force ? new int[]{1} : null;
			}
		}
		
		final String name = getName();
		if (metaData==null || metaData.getDataShapes()==null || metaData.getDataShapes().get(name)==null) {
			final ILazyDataset set = getLazyData(null);
			// Assuming it has been squeezed already
			if (set!=null) return set.getShape();
			return force ? new int[]{1} : null;

		} else if (metaData.getDataShapes().containsKey(name)) {
			final int[] shape = metaData.getDataShapes().get(name);
			if (force) {
				final List<Integer> ret = new ArrayList<Integer>(shape.length);
				for (int i : shape) if (i>1) ret.add(i);
				Integer[] ia = ret.toArray(new Integer[ret.size()]);
				int[]     pa = new int[ia.length];
				for (int i = 0; i < ia.length; i++) pa[i] = ia[i];
				return pa;
 			} else {
				return shape;
			}
		}
		return new int[]{1};
	}



	public static boolean isMementoKey(final String key) {
		if (key==null)      return false;
		if ("".equals(key)) return false;
		return key.matches("CheckableObject\\$(\\d)+\\$(.+)");
	}

	/**
	 * Data *must* have been cloned before doing this.
	 * @param name
	 */
	@Override
	public void setName(String name) {
		if (isExpression()) throw new IllegalArgumentException("Cannot set the name of an expression object!");
		ILazyDataset old = holder.getLazyDataset(getName());
		super.setName(name);
		holder.addDataset(name, old);
	}

	@Override
	public String getPath() {
		return getName();
	}
	

	@Override
	public String getDisplayName(String rootName) {
		String setName = toString();
		
		boolean localNameAllowed = Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.SHOW_LOCALNAME);
		if (localNameAllowed && !isExpression() && metaData!=null) {
			String attr = setName.concat("@local_name");
			try {
				Object localattr = metaData.getMetaValue(attr);
				if (localattr != null && !"".equals(localattr)) {
					return localattr.toString();
				}
			} catch (Exception e) {
				logger.error("Cannot get meta value for "+attr, e);
			}
		}

		if (!isExpression() && rootName!=null && setName.startsWith(rootName)) {
			setName = setName.substring(rootName.length());
		}
		return setName;
	}

	@Override
	public String getFileName() {
		if (holder==null) return null;
		if (holder.getFilePath()==null) return null;
		return (new File(holder.getFilePath())).getName();
	}

	@Override
	public String getFilePath() {
		if (holder==null) return null;
		if (holder.getFilePath()==null) return null;
		return holder.getFilePath();
	}
	
	/**
	 * Nullifies a few things to help the garbage collector
	 * be more efficient.
	 */
	public void dispose() {
		if (getExpression()!=null) getExpression().clear();
		setExpression(null);
		holder     = null;
		metaData   = null;
	}
 }
