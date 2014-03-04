package org.dawnsci.plotting.tools.powderintegration;

import java.util.List;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawb.hdf5.nexus.NexusUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.eclipse.core.runtime.Status;
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
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
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
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (system!= null) system.clear();
			}
			
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
		super.activate();
		getPlottingSystem().addTraceListener(traceListener);
		
		IImageTrace im = getImageTrace();
		
		if (im != null && im.getData() != null) update(im.getData()); 
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
		
		if (ds == null) return;
		
		IImageTrace im = getImageTrace();
		logger.debug("Update");
		
//		final AbstractDataset ds = (AbstractDataset)im.getData();
		
		if (metadata != null) {
			DetectorProperties d = metadata.getDetector2DProperties();
			if(d.getPx() != ds.getShape()[0] || d.getPy() != ds.getShape()[1]) metadata = null;
		}
		
		IDiffractionMetadata m = null;
		
		if (ds.getMetadata() != null && ds.getMetadata() instanceof IDiffractionMetadata) {
			m = (IDiffractionMetadata)ds.getMetadata();
		}
		
		//read from preferences first time
		if (m == null && metadata == null) m = getDiffractionMetaData(ds);
		
		if (m != null) {
			DetectorProperties d = m.getDetector2DProperties();
			if(d.getPx() != ds.getShape()[0] || d.getPy() != ds.getShape()[1]) m = null;
		}
		
		if (m == null && metadata == null) return;
		
		if (metadata == null) {
			metadata = m;
			fullImageJob = new PowderIntegrationJob(metadata, system);
		}
		else {
			if (m != null && (!metadata.getDetector2DProperties().equals(m.getDetector2DProperties()) ||
					!metadata.getDiffractionCrystalEnvironment().equals(m.getDiffractionCrystalEnvironment()))) {
				metadata = m;
				fullImageJob = new PowderIntegrationJob(metadata, system);
			}
		}
		
		if (fullImageJob == null) fullImageJob = new PowderIntegrationJob(metadata, system);
		
		AbstractDataset mask = null;
		
		if (im != null) mask = DatasetUtils.convertToAbstractDataset(im.getMask());
		
		fullImageJob.setData(DatasetUtils.convertToAbstractDataset(ds),
				mask, null);
		
		fullImageJob.schedule();
	}
	
	private IDiffractionMetadata getDiffractionMetaData(IDataset image) {
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
		
		IHierarchicalDataFile file = slice.getFile();
		Group dataGroup = slice.getParent();
		
		Group resultGroup = file.group("integration_result", dataGroup);
		file.setNexusAttribute(resultGroup, Nexus.DATA);
		slice.setParent(resultGroup);
		
		List<AbstractDataset> out = fullImageJob.process(DatasetUtils.convertToAbstractDataset(slice.getData()));
		
		AbstractDataset axis = out.get(0);
		
		//Test if axis is made
		boolean axisMade = false;
		
		for (HObject ob : resultGroup.getMemberList()) {
			if (ob.getName().equals(axis.getName())) { 
				axisMade = true;
				break;
			}
		}
		
		if (!axisMade) {
			H5Datatype dType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
			axis = axis.squeeze();
			Dataset s = file.createDataset(axis.getName(),  dType,  new long[]{axis.getShape()[0]}, axis.getBuffer(), resultGroup);
			file.setNexusAttribute(s, Nexus.SDS);
			file.setIntAttribute(s, NexusUtils.AXIS, 2);
			
		}
		
		AbstractDataset signal = out.get(1);
		signal.setName("data");
		slice.appendData(signal);
		
		if (!axisMade) {
			for (HObject ob :resultGroup.getMemberList()) {
				if (ob instanceof Dataset && ob.getName().equals(signal.getName())) {
					Dataset ds = (Dataset)ob;
					file.setIntAttribute(ds, NexusUtils.SIGNAL, 1);
				}
			}
		}
		
		return new DataReductionInfo(Status.OK_STATUS);
	}

}
