package org.dawnsci.plotting.tools.reduction;

import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5FunctionArgumentException;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionVisitor;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionSlice;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * A conversion visitor that delegates to a tool.
 * @author fcp94556
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
		bean.setAxes(nexusAxes);
		bean.setExpandedDatasetNames(getExpandedDatasets());
		DataReductionInfo  info = tool.export(bean);
		if (info.getStatus().isOK()) object = info.getUserData();

		if (context.getMonitor()!=null) context.getMonitor().worked(1);
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
