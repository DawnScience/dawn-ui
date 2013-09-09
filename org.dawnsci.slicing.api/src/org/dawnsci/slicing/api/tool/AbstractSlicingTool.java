package org.dawnsci.slicing.api.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawnsci.slicing.api.system.DimsData;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.dawnsci.slicing.api.util.SliceUtils;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Convenience class for extending to provide a tool.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractSlicingTool implements ISlicingTool {

	protected ISliceSystem slicingSystem;
	protected String       toolId;

	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void dispose() {
		
	}

	@Override
	public String getToolId() {
		return toolId;
	}

	@Override
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	@Override
	public ISliceSystem getSlicingSystem() {
		return slicingSystem;
	}

	@Override
	public void setSlicingSystem(ISliceSystem slicingSystem) {
		this.slicingSystem = slicingSystem;
	}
	
	
	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {
		
	}

	@Override
	public Object getAdapter(Class clazz) {
        return null;
	}
	
	
	/**
	 * May be null
	 * @return
	 */
	protected List<IDataset> getNexusAxes() throws Exception {
		
		final Map<Integer, String> names = getSlicingSystem().getAxesNames();
		final DimsDataList         ddl   = getSlicingSystem().getDimsDataList();
		
		IDataset x=null; IDataset y=null;
		for (DimsData dd : ddl.getDimsData()) {
			
			if (dd.getPlotAxis()==0) {
				final String name = names.get(dd.getDimension()+1);
				try {
					x = SliceUtils.getNexusAxis(getSlicingSystem().getCurrentSlice(), name, false, null);
				} catch (Throwable e) {
					return null;
				}
			}
			if (dd.getPlotAxis()==1) {
				final String name = names.get(dd.getDimension()+1);
				try {
					y = SliceUtils.getNexusAxis(getSlicingSystem().getCurrentSlice(), name, false, null);
				} catch (Throwable e) {
					return null;
				}
			}

		}
		if (x==null && y==null) return null;
		if (x!=null && y==null) return Arrays.asList(x);
		if (x!=null && y!=null) return Arrays.asList(x,y);
		return null;
	}

}
