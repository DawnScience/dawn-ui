package org.dawnsci.plotting.tools.powderintegration;

import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;


import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

public class PowderIntegrationTool extends AbstractToolPage implements IDataReductionToolPage {
	
	IPlottingSystem system;
	private ITraceListener traceListener;
	private IDiffractionMetadata metadata;
	private PowderIntegrationJob fullImageJob;
	ILoaderService service;
	
	public PowderIntegrationTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		
		this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				PowderIntegrationTool.this.update(evt.getImage());
			}
////			
//			@Override
//			public void traceAdded(TraceEvent evt) {
//				PowderIntegrationTool.this.update();
//			}
//			
//			@Override
//			public void traceUpdated(TraceEvent evt) {
//				PowderIntegrationTool.this.update();
//			}
//			
//			@Override
//			public void traceRemoved(TraceEvent evt) {
//				PowderIntegrationTool.this.update();
//			}
			
		};
		
		this.service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		getPlottingSystem().addTraceListener(traceListener);

		super.activate();
	}
	
	@Override
	public void deactivate() {
		getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
	}

	@Override
	public void createControl(Composite parent) {
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;

		system.createPlotPart(parent, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		system.getSelectedYAxis().setAxisAutoscaleTight(true);

	}

	@Override
	public Control getControl() {
		if (system != null) return system.getPlotComposite();
		return null;
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private void update(IDataset ds) {
		IImageTrace im = getImageTrace();
		logger.debug("Update");
		
		if (im == null) {
			//cleanPlottingSystem();
			return;
		}
		
//		final AbstractDataset ds = (AbstractDataset)im.getData();
		if (ds==null) return;
			
		IDiffractionMetadata m = getDiffractionMetaData();
		
		if (metadata == null) {
			metadata = m;
			fullImageJob = new PowderIntegrationJob(metadata, system);
		}
		else {
			if (metadata != m) {
				fullImageJob = new PowderIntegrationJob(metadata, system);
			}
		}
		
		fullImageJob.setData(DatasetUtils.convertToAbstractDataset(ds),
				DatasetUtils.convertToAbstractDataset(im.getMask()), null);
		
		fullImageJob.schedule();
	}
	
	private IDiffractionMetadata getDiffractionMetaData() {
		IDataset image = getImageTrace() == null ? null : getImageTrace().getData();
		IWorkbenchPart part = getPart();
		String altPath = null;
		if(part instanceof IEditorPart){
			altPath = EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput());
		} else if (part instanceof IViewPart){
			try {
				if (image == null) return null;
				IMetaData md = image.getMetadata();
				if(md != null)
					altPath = md.getFilePath();
			} catch (Exception e) {
				logger.debug("Exception getting the image metadata", e);
			}
		}
		return DiffractionUtils.getDiffractionMetadata(image, altPath, service, null);
	}
	
	/**
	 * Same tool called recursively from the DataReductionWizard
	 */
	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
		return null;
	}

}
