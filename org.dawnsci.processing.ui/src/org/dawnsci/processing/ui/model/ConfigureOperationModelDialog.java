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
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
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
	private IDataset[] axes;
	private double[] minMax = new double[4];
	
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
		
		axes = SlicedDataUtils.getAxesFromMetadata(data.getInputData());
		
		if (input.is2D()) {
			minMax[0] = 0;
			minMax[1] = data.getInputData().getShape()[1];
			minMax[2] = 0;
			minMax[3] = data.getInputData().getShape()[0];
		} else {
			minMax[0] = 0;
			minMax[1] = data.getInputData().getShape()[0];
			minMax[2] = data.getInputData().min().doubleValue();
			minMax[3] = data.getInputData().max().doubleValue();
		}
		
		if (axes != null) {
			if (input.is2D()) { 
				if (axes[1] != null) {
					minMax[0] = axes[1].getDouble(0);
					minMax[1] = axes[1].getDouble(axes[1].getSize()-1);
				}
				if (axes[0] != null) {
					minMax[2] = axes[0].getDouble(0);
					minMax[3] = axes[0].getDouble(axes[0].getSize()-1);
				}
			} else {
				if (axes[0] != null) {
					minMax[0] = axes[0].getDouble(0);
					minMax[1] = axes[0].getDouble(axes[0].getSize()-1);
				}
			}
		}
		
		Map<String,ROIStruct> rois = getROIs(data.getCurrentOperation().getModel(),data.getInputData());
		boolean sector = false;
		
		IDiffractionMetadata d = AbstractOperation.getFirstDiffractionMetadata(data.getInputData());
		
		if (d != null) for (ROIStruct r :rois.values()) if (r.roi instanceof RingROI) {
			sector = true;
			break;
		}
		
		if (d != null && sector) {
			sector = false;
			sector = MessageDialog.openConfirm(this.getShell(),"Diffraction Regions", "Lock sectors to beam centre?");
		}
		
		
		for (final Entry<String,ROIStruct> entry : rois.entrySet()) {
			try {
				final IRegionService rservice = (IRegionService)ServiceManager.getService(IRegionService.class);
				
				if (entry.getValue().roi instanceof RingROI && d != null && sector) {
					entry.getValue().roi.setPoint(d.getDetector2DProperties().getBeamCentreCoords());
				}

				IRegion reg = null;
				if (entry.getValue().type != RangeType.NONE) {
					reg = input.createRegion(entry.getKey(), getRegionType(entry.getValue().type));
					reg.setROI(entry.getValue().roi);
					input.addRegion(reg);
				} else {
					reg = rservice.createRegion(input,entry.getValue().roi,entry.getKey());
				}
				
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
							logger.warn("Couldnt set values in model: " + e.getMessage());
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
					
					IROI roi = getRoiFromAnnotation(an, field.getName());
					if (roi != null) rois.put(field.getName(),new ROIStruct(roi,an.rangevalue()));
				}
			} catch (Exception e) {
				logger.warn("Couldnt create roi " + e.getMessage());
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
	
	private RectangularROI getROIFromRange(double[] range, RangeType type, IDataset[] axes) {
		
		if (range == null) range = minMax.clone();
		
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
	
	private RegionType getRegionType(RangeType type) {
		switch (type) {
		case NONE:
			return null;
		case XRANGE:
			return RegionType.XAXIS;
		case XSINGLE:
			return RegionType.XAXIS_LINE;
		case XYRANGE:
			return RegionType.BOX;
		case YRANGE:
			return RegionType.YAXIS;
		case YSINGLE:
			return RegionType.YAXIS_LINE;
		default:
			return null;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			IRegion region = input.getRegion(evt.getPropertyName());

			Object object = data.getCurrentOperation().getModel().get(evt.getPropertyName());

			if (object instanceof IROI) region.setROI((IROI)object);
			else {
				OperationModelField an = ModelUtils.getAnnotation(model, evt.getPropertyName());
				
				if (evt.getNewValue() != null && evt.getNewValue() instanceof Double) {
					RectangularROI roi = getRoiFromAnnotation(an, evt.getPropertyName());
					region.setROI(roi);
				} else if (evt.getNewValue() != null && evt.getNewValue().getClass().isArray()){

						if (!Arrays.equals((double[])evt.getNewValue(), (double[])evt.getOldValue())) {
							RectangularROI roi = getRoiFromAnnotation(an, evt.getPropertyName());
							region.setROI(roi);
							return;
						}
					
				} else if (evt.getNewValue() == null) {
					RectangularROI roi = getRoiFromAnnotation(an, evt.getPropertyName());
					region.setROI(roi);
					return;
				}
				
			}
		} catch (Exception e) {
			logger.warn("Couldnt update region");
		}

		
		update();
	}
	
	private RectangularROI getRoiFromAnnotation(OperationModelField an, String name) {
		
		if (an != null && an.rangevalue() != RangeType.NONE) {
			try {
				return getROIFromRange(getRangeFromAnnotation(an, name), an.rangevalue(), axes);
			} catch (Exception e) {
				logger.warn("Could not build roi");
				return null;
			}

			
		}
		
		return null;
	}
	
	private double[] getRangeFromAnnotation(OperationModelField an, String name){
		double[] range = minMax.clone();
		
		try {
			Object object = model.get(name);
			
			if (an.rangevalue() == RangeType.XSINGLE || an.rangevalue() == RangeType.YSINGLE) {
				if (object != null) range[0] = (double)object;
				else if (an.rangevalue() == RangeType.YSINGLE) {
					range[0] = range[2];
				}

			}
			else if (object != null)  {
				range = (double[])object;
			}
		} catch (Exception e) {
			logger.warn("Could not build roi");
			return null;
		}
		
		return range;
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
