/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.reduction;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5FunctionArgumentException;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionVisitor;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionSlice;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;

/**
 * A conversion visitor that delegates to a tool.
 * @author Matthew Gerring
 *
 */
class ToolConversionVisitor implements IConversionVisitor {
	
	private List<IDataset>         nexusAxes;
	private IDataReductionToolPage tool;
	private List<String>           expandedDatasets;

	public ToolConversionVisitor(IDataReductionToolPage tool) {
		this.tool = tool;
	}

	@Override
	public String getConversionSchemeName() {
		return tool.getTitle();
	}

	private IHierarchicalDataFile output;
	private String                group;
	private String                initName;
	
	@Override
	public void init(IConversionContext context) throws Exception {
		output = HierarchicalDataFactory.getWriter(context.getOutputPath());
		initName = tool.exportInit();
	}

	private Object  object;

	@Override
	public void visit(IConversionContext context, IDataset slice) throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) throw new Exception("Execution cancelled!");
		
		String grp = createGroupIfRequired(context);
		DataReductionSlice bean = new DataReductionSlice(output, grp, slice, object, context.getSelectedSlice(), context.getSelectedShape(), context.getMonitor());
		bean.setAxes(getAxes(context, slice));
		bean.setExpandedDatasetNames(getExpandedDatasets());
		DataReductionInfo  info = tool.export(bean);
		if (info.getStatus().isOK()) object = info.getUserData();

		if (context.getMonitor()!=null) context.getMonitor().worked(1);
	}
	
	/**
	 * Deals with getting correct axes for slice
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	private List<IDataset> getAxes(IConversionContext context, IDataset slice) throws Exception {
		
		if (nexusAxes==null) return null;

		// If all the nexusAxes are 1D then we do what we used to
		// NOTE Might be able to ignore this step one day
		boolean all1D = true;
		for (IDataset i : nexusAxes) {
			if (i.getRank()!=1) all1D = false;
		}
		if (all1D) return nexusAxes; // No need to slice them.
		
		// TODO Might be able to use these meta-data things but not sure how to relate these to the slice
		// We know the axis name but this information cannot be looked up in AxesMetadata
		// therefore AxesMetadata is a bit useless in thin context? Might need to change it
		final List<AxesMetadata> adata = slice.getMetadata(AxesMetadata.class);
		
		final List<IDataset> ret = new ArrayList<IDataset>(nexusAxes.size());
		
		// This sucks, sorry.
		for (IDataset i : nexusAxes) {
			if (i.getRank()>1) {
				
			    // FIXME Yuckiness warning: we have the full path to the 
				// axis but the AxesMetadata has sets named badly (arguable...)
				try {
					// We need to slice em 
					String name = slice.getName();
					name = name.substring(name.lastIndexOf("/")+1);
					
					// Search for axes by name.
                    AXIS_LOOP: for (AxesMetadata amd : adata) {
						for (ILazyDataset axis : amd.getAxes()) {
							if (axis.getName().contains("'"+name+"'")) {
								i = axis.getSlice();
								break AXIS_LOOP;
							}
						}
					}
					
				} catch (Exception ne) {
					i = ((Dataset)i).mean(0);
				}
			}
			ret.add(i);
		}
		return ret;
	}

	private String  currentH5Path;     

	private String createGroupIfRequired(IConversionContext context) throws Exception {
		
		//Group made and not h5, return group
		if (group != null && context.getSelectedH5Path() == null) {
			
			return output.group(group);
		}
		
		String path = initName == null ?"data" : initName;
		
		if (context.getSelectedH5Path() != null) {
			
			String flatPath = context.getSelectedH5Path();
			if (flatPath.startsWith("/entry/"))  flatPath = flatPath.substring("/entry/".length());
			if (flatPath.startsWith("/entry1/")) flatPath = flatPath.substring("/entry1/".length());
			flatPath = flatPath.replace("/", "_");
			
			//group made, h5 and same
			if (currentH5Path!=null && currentH5Path.equals(flatPath) && group != null) return group;
			
			currentH5Path = flatPath;
			path = flatPath;
		}
		
		//else build new group using either h5 [/ replaced with _] or default data path
		
		try {
//		    if (group!=null) {
//		    	
//		    	group.close(groupId);
//		    }
		} catch (Exception ne) {
			if (ne instanceof HDF5FunctionArgumentException) {
				// We carry on
			} else {
				throw ne;
			}
		}
		
		String entryGroup = output.group("entry");
		output.setNexusAttribute(entryGroup, Nexus.ENTRY);
		group = output.group(path, entryGroup);
		// Fix to http://jira.diamond.ac.uk/browse/SCI-1898
		// We switch this to NXsubentry later and must have enough chars to change attribute
		output.setNexusAttribute(group, Nexus.DATA+"     ");
		return group;
		
	}

	@Override
	public void close(IConversionContext context) throws Exception {
		
		// Notify tool of closure
		Exception onFinish = null;
		try {
		    tool.exportFinished();
		} catch (Exception ne) {
			onFinish = ne;
		}
		
		// Close actual file.
		if (output!=null) output.close();
		
		if (onFinish!=null) throw onFinish;
	}


	@Override
	public boolean isRankSupported(int length) {
		return true;
	}


	public void setNexusAxes(List<IDataset> nexusAxes) {
		this.nexusAxes = nexusAxes;
	}

	public IToolPage getTool() {
		return tool;
	}

	public List<String> getExpandedDatasets() {
		return expandedDatasets;
	}

	public void setExpandedDatasets(List<String> expandedDatasets) {
		this.expandedDatasets = expandedDatasets;
	}

}
