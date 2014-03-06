package org.dawnsci.plotting.tools.powderintegration;

import java.util.List;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.UnitFormat;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.common.ui.menu.MenuAction;
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
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.dawnsci.plotting.tools.powderintegration.PowderIntegrationJob.IntegrationMode;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

public class PowderIntegrationTool extends AbstractToolPage implements IDataReductionToolPage {
	
	IPlottingSystem system;
	private ITraceListener traceListener;
	private IDiffractionMetadata metadata;
	private PowderIntegrationJob fullImageJob;
	private Label statusMessage;
	String[] statusString;
	ILoaderService service;
	Composite baseComposite;
	XAxis xAxis = XAxis.Q;
	IntegrationMode mode = IntegrationMode.NONSPLITTING;
	
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
		statusString = new String[1];
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
		
		baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setLayout(new GridLayout());
		
		createActions();
		
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;
		system.createPlotPart(baseComposite, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());
		
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		system.getSelectedYAxis().setAxisAutoscaleTight(true);
		
		statusMessage = new Label(baseComposite, SWT.WRAP);
		statusMessage.setText("Status...");
		statusMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		statusMessage.setForeground(new Color(statusMessage.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		update(null);
	}
	
	private void createActions() {
		
		final MenuAction modeSelect= new MenuAction("Select Mode");
		
		final Action nonAction = new Action("Non pixel splitting") {
			@Override
			public void run() {
				PowderIntegrationTool.this.fullImageJob.setIntegrationMode(IntegrationMode.NONSPLITTING);
				mode = IntegrationMode.NONSPLITTING;
				modeSelect.setSelectedAction(this);
				update(null);
			}
		};
		
		final Action splitAction = new Action("Pixel splitting") {
			@Override
			public void run() {
				PowderIntegrationTool.this.fullImageJob.setIntegrationMode(IntegrationMode.SPLITTING);
				mode = IntegrationMode.SPLITTING;
				modeSelect.setSelectedAction(this);
				update(null);
			}
		};
		final Action split2DAction = new Action("Pixel splitting 2D") {
			@Override
			public void run() {
				PowderIntegrationTool.this.fullImageJob.setIntegrationMode(IntegrationMode.SPLITTING2D);
				mode = IntegrationMode.SPLITTING2D;
				modeSelect.setSelectedAction(this);
				update(null);
			}
		};
		
		
		final MenuAction axisSelect= new MenuAction("Select Axis");

		final Action qAction = new Action("Q") {
			@Override
			public void run() {
				PowderIntegrationTool.this.fullImageJob.setAxisType(XAxis.Q);
				xAxis = XAxis.Q;
				axisSelect.setSelectedAction(this);
				update(null);
			}
		};

		final Action tthAction = new Action("2\u03b8") {
			@Override
			public void run() {
				PowderIntegrationTool.this.fullImageJob.setAxisType(XAxis.ANGLE);
				xAxis = XAxis.ANGLE;
				axisSelect.setSelectedAction(this);
				update(null);
			}

		};
		
		modeSelect.add(nonAction);
		modeSelect.add(splitAction);
		modeSelect.add(split2DAction);
		modeSelect.setSelectedAction(nonAction);
		
		axisSelect.add(qAction);
		axisSelect.add(tthAction);
		axisSelect.setSelectedAction(qAction);
		
		getSite().getActionBars().getToolBarManager().add(modeSelect);
		getSite().getActionBars().getMenuManager().add(modeSelect);
		
		getSite().getActionBars().getToolBarManager().add(axisSelect);
		getSite().getActionBars().getMenuManager().add(axisSelect);
	}

	@Override
	public Control getControl() {
		//if (system != null) return system.getPlotComposite();
		return baseComposite;
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private void update(IDataset ds) {
		
		if (system == null) return; 
		if (system.getPlotComposite() == null) return;
		
		IImageTrace im = getImageTrace();
		if (ds == null && im == null) return;
		if (ds == null && im != null) ds = im.getData();
		
		logger.debug("Update");
		
		if (metadata != null) {
			DetectorProperties d = metadata.getDetector2DProperties();
			if(d.getPx() != ds.getShape()[0] || d.getPy() != ds.getShape()[1])  {
				metadata = null;
				statusMessage.setText("Data shape not compatible with current metadata");
			} else {
				statusMessage.setText("Metadata OK");
			}
		}
		
		IDiffractionMetadata m = null;
		
		if (ds.getMetadata() != null && ds.getMetadata() instanceof IDiffractionMetadata) {
			m = (IDiffractionMetadata)ds.getMetadata();
			statusMessage.setText("Metadata from data set");
		}
		
		//read from preferences first time
		if (m == null && metadata == null) m = getDiffractionMetaData(ds);
		
		if (m != null) {
			if (statusString[0] != null) {
				statusMessage.setText(statusString[0]);
				statusString[0] = null;
			}
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
				statusMessage.setText("Meta data updated");
			}
		}
		
		if (fullImageJob == null) fullImageJob = new PowderIntegrationJob(metadata, system);
		
		AbstractDataset mask = null;
		
		if (im != null) mask = DatasetUtils.convertToAbstractDataset(im.getMask());
		
		fullImageJob.setData(DatasetUtils.convertToAbstractDataset(ds),
				mask, null);
		
		fullImageJob.setAxisType(xAxis);
		fullImageJob.setIntegrationMode(mode);
		
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
		return DiffractionUtils.getDiffractionMetadata(image, altPath, service, statusString);
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
			UnitFormat unitFormat = UnitFormat.getUCUMInstance();
			
			switch (xAxis) {
			case Q:
				String angstrom = unitFormat.format(NonSI.ANGSTROM.inverse());
				file.setAttribute(s, "units", angstrom);
				break;
			case ANGLE:
				String degrees = unitFormat.format(NonSI.DEGREE_ANGLE);
				file.setAttribute(s, "units", degrees);
				break;

			default:
				break;
			}
			
			file.setNexusAttribute(s, Nexus.SDS);
			
			if (mode == IntegrationMode.SPLITTING2D) {
				file.setIntAttribute(s, NexusUtils.AXIS, 3);
				axis = out.get(2);
				axis = axis.squeeze();
				Dataset s2 = file.createDataset(axis.getName(),  dType,  new long[]{axis.getShape()[0]}, axis.getBuffer(), resultGroup);
				file.setAttribute(s2, "units", unitFormat.format(NonSI.DEGREE_ANGLE));
				file.setIntAttribute(s2, NexusUtils.AXIS, 2);
				
			} else {
				file.setIntAttribute(s, NexusUtils.AXIS, 2);
			}
			
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
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system;
		} else {
			return super.getAdapter(clazz);
		}
	}

}
