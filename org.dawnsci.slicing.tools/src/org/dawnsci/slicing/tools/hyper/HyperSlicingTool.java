package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.slicing.api.system.SliceSource;
import org.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;

/**
 * This class installs the special Hyper3D slicing tool which
 * replaces the traditional plotting system with an alternative
 * slicer.
 * 
 * @author fcp94556
 *
 */
public class HyperSlicingTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(HyperSlicingTool.class);

	private HyperComponent hyperComponent;
	private Control        originalPlotControl;
	/**
	 * We actually install the HyperComponent instead of the plotting system.
	 */
	@Override
	public void militarize() {
		
		getSlicingSystem().setSliceType(getSliceType());

		final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
        if (hyperComponent==null) {
        	hyperComponent = new HyperComponent(plotSystem.getPart());
        	hyperComponent.createControl(plotSystem.getPlotComposite());
        }
        
        originalPlotControl = plotSystem.setControl(hyperComponent.getControl(), false);
        
        final SliceSource data = getSlicingSystem().getData();
        try {
            hyperComponent.setData(data.getLazySet(), getAbstractNexusAxes(), getSlices(), getOrder());
        } catch (Exception ne) {
        	logger.error("Cannot set data to HyperComponent!", ne);
        }
        
		getSlicingSystem().refresh();
	}
	
	private int[] getOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	private Slice[] getSlices() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<AbstractDataset> getAbstractNexusAxes() throws Exception {
		
		final List<IDataset>        ia = getNexusAxes();
		if (ia==null || ia.isEmpty()) return null;
		final List<AbstractDataset> ret= new ArrayList<AbstractDataset>(ia.size());
		for (IDataset i : ia) ret.add((AbstractDataset)i);
		return ret;
	}

	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {
		if (originalPlotControl==null) return;
        final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
		plotSystem.setControl(originalPlotControl, true);
		originalPlotControl = null;
	}
	
	@Override
	public void dispose() {
		demilitarize();
	}


	@Override
	public Enum getSliceType() {
		return HyperType.Line_Axis;
	}

	
	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == HyperComponent.class) {
			return hyperComponent;
		}
		return super.getAdapter(clazz);
	}
}
