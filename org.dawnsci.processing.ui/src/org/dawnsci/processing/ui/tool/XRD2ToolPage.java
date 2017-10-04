package org.dawnsci.processing.ui.tool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.model.OperationModelViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.mask.MaskCircularBuffer;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.MaskMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;

import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.ImportMaskModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.ImportMaskOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.MaskOutsideRegionOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.powder.AzimuthalPixelIntegrationModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.powder.AzimuthalPixelIntegrationOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.twod.DiffractionMetadataImportOperation;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class XRD2ToolPage extends AbstractToolPage {

	private SashForm control;
	private IPlottingSystem<Composite> plottingSystem;
	private IDiffractionMetadata metadata;
	private String lastPath = null;
	private Label statusLabel;
	
	private XRD2Job job;
	private XRD2Model model;
	
	private IRegion integrationBox;
	private final static String BOXNAME = "BoxIntegrationRegion";
	
	private IRegion lineMarker;
	private final static String LINENAME = "LineMarkerRegion";
	
	private IRegion ringMarker;
	private final static String RINGNAME = "RingMarkerRegion";
	
	private XAxis currentAxis;
	
	private ITraceListener traceListener;
	
	@Override
	public void createControl(Composite parent) {
		this.control = new SashForm(parent, SWT.VERTICAL);
		Composite upper = new Composite(control, SWT.NONE);
		upper.setLayout(new GridLayout(1, false));
		
		SetupComposite dc = new SetupComposite(upper, SWT.None);
		dc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		job = new XRD2Job();
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			
			return;
		}
		
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;
		
		plottingSystem.createPlotPart(upper, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());
		
		plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		statusLabel = new Label(upper, SWT.None);
		statusLabel.setText("");
		statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		
		Composite lower = new Composite(control, SWT.NONE);
		lower.setLayout(new GridLayout());
		
		OperationModelViewer modelEditor = new OperationModelViewer(true);
		modelEditor.createPartControl(lower);
		
		model = new XRD2Model();
		currentAxis = model.getAxisType();
		
		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				IDataset d = getImageTrace().getData();
				job.setModel(new XRD2Model(model),d, null);
				job.schedule();
				
				XAxis x = model.getAxisType();
				if (!currentAxis.equals(x)) {
					XAxis old = currentAxis;
					currentAxis = x;
					updateMarkers(old);
				}
			}
		});
		
		modelEditor.setModel(model);
		
		update();
		
		traceListener = new ITraceListener.Stub() {
			protected void update(TraceEvent evt) {
				XRD2ToolPage.this.update();
			}
		};
		
		
	}
	
	private void update() {
		
		IDataset d = null;
		IROI r = null;
		
		IImageTrace image = getImageTrace();
		
		if (image != null) {
			d = image.getData();
		}
		
		if (integrationBox != null) {
			r = integrationBox.getROI();
		}
			

		job.setModel(new XRD2Model(model),d, r);
		job.schedule();

	}
	
	@Override
	public void activate() {
		super.activate();
		if (getPlottingSystem() != null && traceListener != null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		
		if (getPlottingSystem() == null) return;
		if (traceListener != null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		
		if (integrationBox != null) {
			getPlottingSystem().removeRegion(integrationBox);
			integrationBox = null;
		}
		
		if (ringMarker != null) {
			getPlottingSystem().removeRegion(ringMarker);
			ringMarker = null;
		}
	}
	
	private void updateMarkers(XAxis previous) {
		
	}
	
	private void setUpBoxRegion(){
		try {

			integrationBox = getPlottingSystem().createRegion(RegionUtils.getUniqueName(BOXNAME, getPlottingSystem(), (String[])null), RegionType.BOX);
			RectangularROI roi = new RectangularROI(100, 100, 100, 100, 0);
			integrationBox.setROI(roi);
			getPlottingSystem().addRegion(integrationBox);
			integrationBox.addROIListener(new IROIListener() {

				@Override
				public void roiSelected(ROIEvent evt) {
					// TODO Auto-generated method stub

				}

				@Override
				public void roiDragged(ROIEvent evt) {
					IROI roi2 = evt.getROI();
					IDataset d = getImageTrace().getData();

					job.setModel(new XRD2Model(model),d, roi2);
					job.schedule();

				}

				@Override
				public void roiChanged(ROIEvent evt) {
					// TODO Auto-generated method stub

				}
			});




		} catch (Exception e) {
			logger.error("Error making box region",e);
		}
	}
	
	private void setUpLineRegion() {
		double res = 10;
		
		try {
			XAxis axisType = model.getAxisType();
			res = axisType.convertToRESOLUTION(1.72, metadata.getDiffractionCrystalEnvironment().getWavelength());
		
		
		IROI r = DSpacing.conicFromDSpacing(metadata.getDetector2DProperties(), metadata.getDiffractionCrystalEnvironment(), res);
		
		RegionType reg = getConicRegionType(r);
		
		lineMarker = plottingSystem.createRegion(RegionUtils.getUniqueName(LINENAME, plottingSystem, (String[])null), RegionType.XAXIS_LINE);
		XAxisBoxROI xroi = new XAxisBoxROI();
		xroi.setPoint(1.72, 1);
		lineMarker.setROI(xroi);
		plottingSystem.addRegion(lineMarker);
		
		ringMarker = getPlottingSystem().createRegion(RegionUtils.getUniqueName(RINGNAME, plottingSystem, (String[])null), reg);
		ringMarker.setROI(r);
		ringMarker.setUserRegion(false);
		ringMarker.setMobile(false);
		ringMarker.setFill(false);
		getPlottingSystem().addRegion(ringMarker);
		
		lineMarker.addROIListener(new IROIListener.Stub() {
			
			
			@Override
			public void roiDragged(ROIEvent evt) {
				double pointX = evt.getROI().getPointX();
				try {
					double resx = currentAxis.convertToRESOLUTION(pointX, metadata.getDiffractionCrystalEnvironment().getWavelength());
					IROI r = DSpacing.conicFromDSpacing(metadata.getDetector2DProperties(), metadata.getDiffractionCrystalEnvironment(), resx);
					
					if (!r.getClass().equals(ringMarker.getROI().getClass())) {
						getPlottingSystem().removeRegion(ringMarker);
						RegionType reg = getConicRegionType(r);
						ringMarker = getPlottingSystem().createRegion(RegionUtils.getUniqueName(RINGNAME, plottingSystem, (String[])null), reg);
						ringMarker.setROI(r);
						ringMarker.setUserRegion(false);
						ringMarker.setMobile(false);
						ringMarker.setFill(false);
						getPlottingSystem().addRegion(ringMarker);
						
					} else {
						ringMarker.setROI(r);
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static RegionType getConicRegionType(IROI roi) {
		RegionType type = null;
		if (roi instanceof EllipticalROI) {
			type = RegionType.ELLIPSE;
		} else if (roi instanceof ParabolicROI) {
			type = RegionType.PARABOLA;
		} else if (roi instanceof HyperbolicROI) {
			type = RegionType.HYPERBOLA;
		}
		return type;
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}
	
	private String getFileName(){
		FileSelectionDialog dialog = new FileSelectionDialog(getPart().getSite().getShell());
		dialog.setExtensions(new String[]{"nxs"});
		dialog.setFiles(new String[]{"Nexus files"});
		dialog.setNewFile(false);
		dialog.setFolderSelector(false);
		if (lastPath != null) dialog.setPath(lastPath);
		
		dialog.create();
		if (dialog.open() == Dialog.CANCEL) return null;
		return dialog.getPath();
	}
	
	private class XRD2Job extends Job {
		
		private AtomicReference<List<IOperation>> operationsReference;
		private AtomicReference<DataROIHolder> datasetReference;
		private AtomicReference<XRD2Model> modelReference;
		private MaskOutsideRegionOperation maskOutside;

		public XRD2Job() {
			super("XRD2 processing");
			datasetReference = new AtomicReference<>();
			modelReference = new AtomicReference<>();
			operationsReference = new AtomicReference<>();
		}
		
		public void setModel(XRD2Model model, IDataset dataset, IROI roi) {
			modelReference.set(model);
			setData(dataset, roi);
			operationsReference.set(null);
		}
		
		public void setData(IDataset data, IROI roi) {
			DataROIHolder r = new DataROIHolder();
			r.data = data;
			r.roi = roi;
			datasetReference.set(r);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			DataROIHolder drh = datasetReference.getAndSet(null);
			List<IOperation> op = operationsReference.get();
			
			if (drh == null|| drh.data == null) {
				plottingSystem.clear();
				return Status.OK_STATUS;
			}
			
			if (op == null){
				op = buildOperations();
			}
			
			IDataset d = drh.data.getSliceView();
			IROI roi = drh.roi;
			
			if (op == null) return Status.OK_STATUS;
			

			SliceND slice = new SliceND(d.getShape());
			int[] dataDims = new int[]{0, 1};
			SliceInformation si = new SliceInformation(slice, slice, slice, dataDims, 1, 1);
			SourceInformation so = new SourceInformation("", "", d);
			SliceFromSeriesMetadata sliceMeta = new SliceFromSeriesMetadata(so, si);
			
			d.setMetadata(sliceMeta);
			
			OperationData opdata = null;
			
			IDataset d2 = null;
			
			for (IOperation o : op) {
				
				if (o instanceof AzimuthalPixelIntegrationOperation) {
					DiffractionMetadata md = d.getFirstMetadata(DiffractionMetadata.class);
					if (md == null) {
						//do something;
					} else {
						metadata = md;
					}
				}
				
				try {
					opdata = o.execute(d, null);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					plottingSystem.clear();
					Display.getDefault().syncExec(() -> {
						statusLabel.setText("Error: " + e.getMessage());
						statusLabel.getParent().layout();
					});
					return Status.OK_STATUS;
				}
				 
				 
				 
				 if (o instanceof AzimuthalPixelIntegrationOperation && roi != null) {
					if (maskOutside == null) {
						IOperationService opServer = ServiceHolder.getOperationService();
						try {
							maskOutside = (MaskOutsideRegionOperation) opServer.create("uk.ac.diamond.scisoft.analysis.processing.operations.mask.MaskOutsideRegionOperation");
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					maskOutside.getModel().setRegion(roi);
					IDataset masked = maskOutside.execute(d, null).getData();
					d2 = o.execute(masked, null).getData();
				 }
				 
				 d = opdata.getData();
			}
			
//			OperationData execute = azimuthalIntegration.execute(d, null);
			
			IDataset integrated = opdata.getData();
			integrated.setName("Integrated");
			
			List<ILineTrace> traces = new ArrayList<>();
			

			MetadataPlotUtils.plotDataWithMetadata(integrated, plottingSystem,false);
			if (d2 != null) {
				d2.setName("Region");
				MetadataPlotUtils.plotDataWithMetadata(d2, plottingSystem,false);
			} else {
				ITrace trace = plottingSystem.getTrace("Region");
				
				if (trace != null) {
					Display.getDefault().syncExec(() -> plottingSystem.removeTrace(trace));
				}
				
				
			}
			
			plottingSystem.repaint();
			Display.getDefault().syncExec(() -> statusLabel.setText(""));
			return Status.OK_STATUS;
		}
		
		private List<IOperation> buildOperations(){
			
			XRD2Model model = modelReference.get();
			List<IOperation> ops = new ArrayList<IOperation>();
			operationsReference.set(ops);
			
			OperationHolder h = new OperationHolder();
			
			try {
				IOperationService opServer = ServiceHolder.getOperationService();
				h.azimuthalIntegration = (AzimuthalPixelIntegrationOperation<AzimuthalPixelIntegrationModel>) opServer.create("uk.ac.diamond.scisoft.analysis.processing.operations.powder.AzimuthalPixelIntegrationOperation");
				h.maskImport = (ImportMaskOperation<ImportMaskModel>)opServer.create("uk.ac.diamond.scisoft.analysis.processing.operations.ImportMaskOperation");
				h.calibrationImport = (DiffractionMetadataImportOperation)opServer.create("uk.ac.diamond.scisoft.analysis.processing.operations.DiffractionMetadataImportOperation");
				
				
				
				if (model.getCalibrationFilePath() != null && !model.getCalibrationFilePath().isEmpty()) {
					h.calibrationImport.getModel().setFilePath(model.getCalibrationFilePath());
					ops.add(h.calibrationImport);
				}
				
				if (model.getMaskFilePath() != null && !model.getMaskFilePath().isEmpty()) {
					h.maskImport.getModel().setFilePath(model.getMaskFilePath());
					ops.add(h.maskImport);
				}
				
				h.azimuthalIntegration.getModel().setAxisType(model.getAxisType());
				
				ops.add(h.azimuthalIntegration);
				
				//has been nulled by another call during creation
				if (operationsReference.get() == null) {
					return null;
				}
				
				return ops;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		private class OperationHolder {
			public AzimuthalPixelIntegrationOperation<AzimuthalPixelIntegrationModel> azimuthalIntegration;
			public DiffractionMetadataImportOperation calibrationImport;
			public ImportMaskOperation<ImportMaskModel> maskImport;
		}
		
		private class DataROIHolder {
			public IDataset data;
			public IROI roi;
		}
		
	}
	
	private class XRD2Model extends AbstractOperationModel {
		
		@OperationModelField(hint="Type for radial range", label = "X axis")
		private XAxis axisType = XAxis.Q;
		@OperationModelField(hint="Path to the calibration NeXus file", file = FileType.EXISTING_FILE, label = "Calibration File")
		private String calibrationFilePath;
		@OperationModelField(hint="Path to the mask file", file = FileType.EXISTING_FILE, label = "Mask File")
		private String maskFilePath;

		public XRD2Model(){};
		
		public XRD2Model(XRD2Model model) {
			this.axisType = model.axisType;
			this.calibrationFilePath = model.calibrationFilePath;
			this.maskFilePath = model.maskFilePath;
		}
		
		public XAxis getAxisType() {
			return axisType;
		}

		public void setAxisType(XAxis axisType) {
			firePropertyChange("axisType", this.axisType, this.axisType = axisType);
		}

		public String getCalibrationFilePath() {
			return calibrationFilePath;
		}

		public void setCalibrationFilePath(String calibrationFilePath) {
			firePropertyChange("calibrationFilePath", this.calibrationFilePath, this.calibrationFilePath = calibrationFilePath);
		}

		public String getMaskFilePath() {
			return maskFilePath;
		}

		public void setMaskFilePath(String maskFilePath) {
			firePropertyChange("maskFilePath", this.maskFilePath, this.maskFilePath = maskFilePath);
		}
		
		
		
	}
	
	private class SetupComposite extends Composite {
		
		public SetupComposite(Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new FillLayout());
			Group group = new Group(this, SWT.NONE);
			group.setLayout(new GridLayout(3, false));
			group.setText("Options");
			
			Button showPanel = new Button(group, SWT.TOGGLE);
			showPanel.setText("Show Panel");
			showPanel.setSelection(true);
			showPanel.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (showPanel.getSelection()) {
						control.setWeights(new int[]{70,30});
					} else {
						control.setWeights(new int[]{100,0});
					}
					
				} 
				
			});
			
			Button showLine = new Button(group, SWT.TOGGLE);
			showLine.setText("Show Line");
			showLine.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (showLine.getSelection()) {
						setUpLineRegion();
					} else {
						plottingSystem.removeRegion(lineMarker);
						getPlottingSystem().removeRegion(ringMarker);
						lineMarker = null;
						ringMarker = null;
					}
					
					
				}
			});
			
			Button showRegion = new Button(group, SWT.TOGGLE);
			showRegion.setText("Show Region");
			showRegion.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (showRegion.getSelection()) {
						setUpBoxRegion();
					} else {
						getPlottingSystem().removeRegion(integrationBox);
						integrationBox = null;
						job.setData(getImageTrace().getData(), null);
						job.schedule();
					}
					
					
				}
			});

		}
	}
	

}
