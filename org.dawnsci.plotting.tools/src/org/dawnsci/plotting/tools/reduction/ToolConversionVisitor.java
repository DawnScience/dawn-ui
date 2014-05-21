package org.dawnsci.plotting.tools.reduction;

import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5FunctionArgumentException;
import ncsa.hdf.object.Group;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionVisitor;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionSlice;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawnsci.plotting.api.tool.IToolPage;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * A conversion visitor that delegates to a tool.
 * @author fcp94556
 *
 */
class ToolConversionVisitor implements IConversionVisitor {
	
	private List<IDataset>         nexusAxes;
	private IDataReductionToolPage tool;

	public ToolConversionVisitor(IDataReductionToolPage tool) {
		this.tool = tool;
	}

	@Override
	public String getConversionSchemeName() {
		return tool.getTitle();
	}

	private IHierarchicalDataFile output;
	private Group                 group;
	
	@Override
	public void init(IConversionContext context) throws Exception {
		output = HierarchicalDataFactory.getWriter(context.getOutputPath());
	}

	private Object  object;

	@Override
	public void visit(IConversionContext context, IDataset slice) throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) return;
		
		Group grp = createGroupIfRequired(context);
		DataReductionSlice bean = new DataReductionSlice(output, grp, slice, object, context.getSelectedSlice(), context.getSelectedShape(), context.getMonitor());
		bean.setAxes(nexusAxes);
		DataReductionInfo  info = tool.export(bean);
		if (info.getStatus().isOK()) object = info.getUserData();

		if (context.getMonitor()!=null) context.getMonitor().worked(1);
	}
	
	private int     groupId;
	private String  currentH5Path;     

	private Group createGroupIfRequired(IConversionContext context) throws Exception {
		
		if (currentH5Path!=null && currentH5Path.equals(context.getSelectedH5Path())) {
			return group;
		}
		
		try {
		    if (group!=null) group.close(groupId);
		} catch (Exception ne) {
			if (ne instanceof HDF5FunctionArgumentException) {
				// We carry on
			} else {
				throw ne;
			}
		}
		
		final String h5Path = context.getSelectedH5Path();
		if (h5Path != null) {
			final String[] rps  = 	h5Path.split("/");
			group=(Group)output.getData(h5Path);

			if (group==null) for (String stub : rps) {
				if (stub==null || "".equals(stub)) continue;
				if (group==null) {
					group = output.group(stub);
				} else {
					group = output.group(stub, group);
				}
				output.setNexusAttribute(group, Nexus.DATA);
			}

			currentH5Path = context.getSelectedH5Path();
			groupId       = group.open();
			return group;
		} else { // if not path, we create a default entry
			Group entryGroup = output.group("entry");
			output.setNexusAttribute(entryGroup, Nexus.ENTRY);
			group = output.group("data", entryGroup);
			output.setNexusAttribute(group, Nexus.DATA);
			return group;
		}
	}

	@Override
	public void close(IConversionContext context) throws Exception {
		if (group!=null) try {
			group.close(groupId);
		} catch (Exception ne) {
			throw ne;
		} finally {
		    if (output!=null) output.close();
		}
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

}
