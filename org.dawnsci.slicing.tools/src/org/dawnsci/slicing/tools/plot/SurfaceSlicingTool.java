package org.dawnsci.slicing.tools.plot;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple type of slice tool based on the available plot
 * options of the plotting system. However custom slice tools may
 * exist which have more complex UI.
 * 
 * @author fcp94556
 *
 */
public class SurfaceSlicingTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(SurfaceSlicingTool.class);
	
	private Link openWindowing;

	@Override
	public void createToolComponent(Composite area) {
		this.openWindowing = new Link(area, SWT.WRAP);
		openWindowing.setText("Data is being viewed using a <a>window</a>");
		openWindowing.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridUtils.setVisible(openWindowing,         false);
		openWindowing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (getSlicingSystem().getPlottingSystem()!=null) {
					showWindowTool();
				}
			}
		});
		
	}

	@Override
	public void militarize() {
		
		GridUtils.setVisible(openWindowing, getSlicingSystem().getPlottingSystem()!=null);
		openWindowing.getParent().layout(new Control[]{openWindowing});

		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) dimsDataList.setTwoAxesOnly(AxisType.X, AxisType.Y);   
		getSlicingSystem().refresh();
		getSlicingSystem().update(true);
		
		showWindowTool();
		
	}
	
	@Override
	public void demilitarize() {
		GridUtils.setVisible(openWindowing, false);
		openWindowing.getParent().layout(new Control[]{openWindowing});
	}

	protected void showWindowTool() {
		try {
			final IToolPageSystem system = (IToolPageSystem)getSlicingSystem().getPlottingSystem().getAdapter(IToolPageSystem.class);
			system.setToolVisible("org.dawb.workbench.plotting.tools.windowTool", ToolPageRole.ROLE_3D, 
					                      "org.dawb.workbench.plotting.views.toolPageView.3D");
		} catch (Exception e1) {
			logger.error("Cannot open window tool!", e1);
		}
	}

	@Override
	public Enum getSliceType() {
		return PlotType.SURFACE;
	}
}
