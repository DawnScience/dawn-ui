package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.processing.ui.slice.IOperationInputData;
import org.dawnsci.processing.ui.slice.SlicedDataUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelUtils;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.RangeType;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.ILockableRegion;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureOperationModelDialog extends Dialog implements PropertyChangeListener{
	
	private IPlottingSystem input;
	private IPlottingSystem output;
	private OperationModelViewer modelViewer;
	IOperationInputData data;
	private Job update;
	private IOperationModel omodel;
	private IOperationModel model;
	private Label errorLabel;
	
	private final static Logger logger = LoggerFactory.getLogger(ConfigureOperationModelDialog.class);

	protected ConfigureOperationModelDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.DIALOG_TRIM);
	}
	
	public Control createDialogArea(Composite parent) {
		
		SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		final SashForm left = new SashForm(sashForm, SWT.VERTICAL);
		left.setLayout(new GridLayout(2, false));
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new GridLayout());
		
		sashForm.setWeights(new int[]{40,60});
		
		
		input = createPlottingSystem(left,"DialogInput");
		modelViewer = new OperationModelViewer();
		modelViewer.createPartControl(left);
		
		output = createPlottingSystem(right,"DialogOutput");
		left.setWeights(new int[]{70,30});
		errorLabel = new Label(parent, SWT.WRAP);
		return parent;
	}
	
	private IPlottingSystem createPlottingSystem(Composite right, String name){
		Composite plotComp = new Composite(right, SWT.NONE);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		plotComp.setLayout(new GridLayout());
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
		Composite displayPlotComp  = new Composite(plotComp, SWT.BORDER);
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlotComp.setLayout(new FillLayout());
		
		IPlottingSystem system = null;
		
		try {
			system = PlottingFactory.createPlottingSystem();
			system.createPlotPart(displayPlotComp, name, actionBarWrapper, PlotType.IMAGE, null);
			
		} catch (Exception e) {
			return null;
		}
		
		return system;
	}
	
	public void setOperationInputData(final IOperationInputData data) {
		
		this.data = data;
		
		
		modelViewer.setOperation(data.getCurrentOperation());
		
		model = data.getCurrentOperation().getModel();
		try {
			omodel = (IOperationModel)BeanUtils.cloneBean(model);
		} catch (Exception e) {
			logger.warn("Could not clone model: " + e.getMessage());
		} 
		
		if (model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).addPropertyChangeListener(this);
		}
		
		try {
			SlicedDataUtils.plotDataWithMetadata(data.getInputData(),input, null);
		} catch (Exception e) {
			logger.warn("Could not plot data: " + e.getMessage());
		}
		
		update();
		
		Map<String,ROIStruct> rois = getROIs(data.getCurrentOperation().getModel(),data.getInputData());
		boolean sector = false;
		
		IDiffractionMetadata d = AbstractOperation.getFirstDiffractionMetadata(data.getInputData());
		
		for (ROIStruct r :rois.values()) if (r.roi instanceof RingROI) {
			sector = true;
			break;
		}
		
		if (d != null && sector) {
			sector = false;
			sector = MessageDialog.openConfirm(this.getShell(),"Diffraction Regions", "Lock sectors to beam centre?");
		}
		
		final IDataset[] axes = SlicedDataUtils.getAxesFromMetadata(data.getInputData());
		for (final Entry<String,ROIStruct> entry : rois.entrySet()) {
			try {
				final IRegionService rservice = (IRegionService)ServiceManager.getService(IRegionService.class);
				
				if (entry.getValue().roi instanceof RingROI && d != null && sector) {
					entry.getValue().roi.setPoint(d.getDetector2DProperties().getBeamCentreCoords());
				}

				IRegion reg = rservice.createRegion(input,entry.getValue().roi,entry.getKey());
				
				if (reg instanceof ILockableRegion && sector) ((ILockableRegion)reg).setCentreMovable(false);
				
				final String name = entry.getKey();
				reg.addROIListener(new IROIListener.Stub() {
					
					@Override
					public void roiChanged(ROIEvent evt) {
						IROI roi = evt.getROI();
						double[] range = null;
						if (roi instanceof RectangularROI) range = getStartAndEndXYFromRectangularROI((RectangularROI)roi,axes);
						IOperationModel model= data.getCurrentOperation().getModel();
						try {
							switch (entry.getValue().type) {
							case NONE:
									model.set(name, roi);
									break;
							case XRANGE:
									model.set(name, new double[]{range[0],range[1]});
									break;
							case YRANGE:
									model.set(name, new double[]{range[2],range[3]});
									break;
							case XSINGLE:
								model.set(name, range[0]);
								break;
							case YSINGLE:
								model.set(name, range[2]);
								break;
							case XYRANGE:
								model.set(name, range);
								break;
							default:
								break;
							}
						} catch (Exception e) {
							logger.warn("Could set values in model: " + e.getMessage());
						}
						
						modelViewer.setModel(model);

						
					}
				});
			} catch (Exception e) {
				logger.warn("Could not create region: " + e.getMessage());
			}
		}
		
	}
	
	private Map<String, ROIStruct> getROIs(IOperationModel model, IDataset data) {
		final List<Field> allFields = new ArrayList<Field>(31);
		allFields.addAll(Arrays.asList(model.getClass().getDeclaredFields()));
		allFields.addAll(Arrays.asList(model.getClass().getSuperclass().getDeclaredFields()));

		Map<String,ROIStruct> rois = new HashMap<String,ROIStruct>();
		IDataset[] axes = SlicedDataUtils.getAxesFromMetadata(data);
		
		for (Field field : allFields) {
			Class<?> class1 = field.getType();
			OperationModelField an = ModelUtils.getAnnotation(model, field.getName());
			try { 
				if (IROI.class.isAssignableFrom(class1)) {

					rois.put(field.getName(),new ROIStruct((IROI)model.get(field.getName()),RangeType.NONE));

				} else if (an != null && an.rangevalue() != RangeType.NONE) {

					double[] range = new double[1];

					if (an.rangevalue() == RangeType.XSINGLE || an.rangevalue() == RangeType.YSINGLE) range[0] = (double)model.get(field.getName());
					else range = (double[])model.get(field.getName());

					IROI roi = getROIFromRange(range, an.rangevalue(), axes);
					if (roi != null) rois.put(field.getName(),new ROIStruct(roi,an.rangevalue()));
				}
			} catch (Exception e) {
				logger.warn("Could create roi " + e.getMessage());
			}
		}

		return rois;
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure processing parameters");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1200, 600);
	}

	private double[] getStartAndEndXYFromRectangularROI(RectangularROI roi, IDataset[] axes) {
		
		double[] out = new double[]{roi.getPointX(), roi.getLength(0)+ roi.getPointX(),
				roi.getPointY(), roi.getPointY()+ roi.getLength(1)};

		if (input.is2D()) {
			if (axes != null) {
				if (axes[1] != null) {
					out[0] = axes[1].getDouble((int)out[0] > 0 ? (int)out[0] : 0);
					out[1] = axes[1].getDouble((int)out[1] < axes[1].getSize() ? (int)out[1] : axes[1].getSize()-1);
				}
				if (axes[0] != null) {
					out[2] = axes[0].getDouble((int)out[2] > 0 ? (int)out[2] : 0);
					out[3] = axes[0].getDouble((int)out[3] < axes[0].getSize() ? (int)out[3] : axes[0].getSize()-1);
				}
			}
		} 

		return out;
	}
	
	private IROI getROIFromRange(double[] range, RangeType type, IDataset[] axes) {
		
		if (range == null) range = new double[4];
		
		switch (type) {
		case XRANGE:
			if (input.is2D() && axes != null && axes[1] != null) {
				range[0] = Maths.abs(Maths.subtract(axes[1], range[0])).argMin();
				range[1] = Maths.abs(Maths.subtract(axes[1], range[1])).argMin();
			}
				return new XAxisBoxROI(range[0],0,range[1]-range[0],0,0);
		case YRANGE:
			if (input.is2D() && axes != null && axes[0] != null) {
				range[0] = Maths.abs(Maths.subtract(axes[0], range[0])).argMin();
				range[1] = Maths.abs(Maths.subtract(axes[0], range[1])).argMin();
			}
				return new YAxisBoxROI(0, range[0], 0, range[1] - range[0], 0);
		case XSINGLE:
			if (input.is2D() && axes != null && axes[1] != null) {
				range[0] = Maths.abs(Maths.subtract(axes[1], range[0])).argMin();
			}
			return new XAxisBoxROI(range[0],0,range[0],0,0);
		case YSINGLE:
			if (input.is2D() && axes != null && axes[0] != null) {
				range[0] = Maths.abs(Maths.subtract(axes[0], range[0])).argMin();
			}
				return new YAxisBoxROI(0, range[0], 0, range[0], 0);
		case XYRANGE:
			if (input.is2D() && axes != null) {
				if (axes[1] != null) {
					range[0] = Maths.abs(Maths.subtract(axes[1], range[0])).argMin();
					range[1] = Maths.abs(Maths.subtract(axes[1], range[1])).argMin();
				}
				if (axes[0] != null) {
					range[2] = Maths.abs(Maths.subtract(axes[0], range[2])).argMin();
					range[3] = Maths.abs(Maths.subtract(axes[0], range[3])).argMin();
				}
				
				return new RectangularROI(new double[]{range[0],range[1]}, new double[]{range[2],range[3]});
			}
		default:
			break;
		}
		
		return null;
	}
	

	
	private void update() {

		if (update == null) {
			update = new Job("calculate...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						final IDataset out = data.getCurrentOperation().execute(data.getInputData(),new ProgressMonitorWrapper(monitor)).getData();

						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								try {
									SlicedDataUtils.plotDataWithMetadata(out,output, null);
									errorLabel.setText("");
								} catch (Exception e) {
									logger.warn("Could not plot data: " + e.getMessage());
								}

							}
						});
						
					} catch (final Exception e) {
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								if (!errorLabel.isDisposed()) {
									errorLabel.setText(e.getMessage());
									errorLabel.pack();
								}
								
							}
						});
						
					}
					return Status.OK_STATUS;
				}
			};
		}

		update.cancel();
		update.schedule();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).removePropertyChangeListener(this);
		}
		
		if (buttonId == Dialog.CANCEL) {
			try {
				BeanUtils.copyProperties(model, omodel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		update();
	}
	
	private class ROIStruct {
		
		public IROI roi;
		public RangeType type;
		
		public ROIStruct(IROI roi, RangeType type) {
			this.roi = roi;
			this.type = type;
		}
	}

}
