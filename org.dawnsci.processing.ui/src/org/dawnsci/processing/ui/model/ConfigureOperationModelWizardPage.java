package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationDataForDisplay;
import org.eclipse.dawnsci.analysis.api.processing.PlotAdditionalData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelUtils;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.RangeType;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
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
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureOperationModelWizardPage extends AbstractOperationModelWizardPage {
	
	protected IPlottingSystem<Composite> input;
	protected IPlottingSystem<Composite> output;
	private OperationModelViewer modelViewer;
	protected Job updateJob;
	protected Label errorLabel;
	private IDataset[] axes;
	private double[] minMax = new double[4];
	
	private final static Logger logger = LoggerFactory.getLogger(ConfigureOperationModelWizardPage.class);

	public ConfigureOperationModelWizardPage() {
		super();
	}
	
	public ConfigureOperationModelWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		super(operation);
	}

	public void createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, true));
		SashForm sashForm= new SashForm(container, SWT.HORIZONTAL);
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
		errorLabel = new Label(container, SWT.WRAP);
		
		modelViewer.setOperation(operation);
		
		setControl(container);
	}
	
	private IPlottingSystem<Composite> createPlottingSystem(Composite right, String name){
		Composite plotComp = new Composite(right, SWT.NONE);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		plotComp.setLayout(new GridLayout());
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
		Composite displayPlotComp  = new Composite(plotComp, SWT.BORDER);
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlotComp.setLayout(new FillLayout());
		
		IPlottingSystem<Composite> system = null;
		
		try {
			system = PlottingFactory.createPlottingSystem();
			system.createPlotPart(displayPlotComp, name, actionBarWrapper, PlotType.IMAGE, null);
			
		} catch (Exception e) {
			return null;
		}
		
		return system;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (!visible)
			return;
			
		try {
			MetadataPlotUtils.plotDataWithMetadata(id.getData() ,input);
		} catch (Exception e) {
			logger.warn("Could not plot data: " + e.getMessage());
			return;
		}
		
		update();
		
		axes = MetadataPlotUtils.getAxesFromMetadata(id.getData());
		
		if (input.is2D()) {
			minMax[0] = 0;
			minMax[1] = id.getData().getShape()[1];
			minMax[2] = 0;
			minMax[3] = id.getData().getShape()[0];
		} else {
			minMax[0] = 0;
			minMax[1] = id.getData().getShape()[0];
			minMax[2] = id.getData().min().doubleValue();
			minMax[3] = id.getData().max().doubleValue();
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
		
		Map<String,ROIStruct> rois = getROIs(model, id.getData());
		boolean sector = false;
		
		IDiffractionMetadata d = AbstractOperation.getFirstDiffractionMetadata(id.getData());
		
		if (d != null) for (ROIStruct r :rois.values()) if (r.roi instanceof RingROI) {
			sector = true;
			break;
		}
		
		if (d != null && sector) {
			sector = false;
			sector = MessageDialog.openConfirm(this.getShell(),"Diffraction Regions", "Lock sectors to beam centre?");
		}
		
		
		for (final Entry<String,ROIStruct> entry : rois.entrySet()) {
			if (entry.getValue().roi == null) {
				continue;
			}
			try {
				final IRegionService rservice = (IRegionService) ServiceManager.getService(IRegionService.class);
				
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
		IDataset[] axes = MetadataPlotUtils.getAxesFromMetadata(data);
		
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
		range = range.clone();
		
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
	

	
	@Override
	protected void update() {
		if (updateJob == null) {
			updateJob = new Job("calculate...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						od = operation.execute(id.getData(),new ProgressMonitorWrapper(monitor));
						if (od == null) {
							return Status.OK_STATUS;
						}
						final IDataset out = od.getData();
						PlotAdditionalData an = operation.getClass().getAnnotation(PlotAdditionalData.class);
						IDataset aux = null;
						boolean onIn = false;
						if (an != null) {
							String name = an.dataName();
							onIn = an.onInput();
							for (Serializable s : od.getAuxData()) {
								if (s instanceof IDataset) {
									IDataset d = (IDataset)s;
									if (name.equals(d.getName())) {
										aux = d;
										break;
									}
								}
							}
						}
						
						final IDataset additional = aux;
						final boolean onInput =onIn;

						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								try {
									output.clear();
									output.clearTraces();
									MetadataPlotUtils.plotDataWithMetadata(out,output);
									if (additional!=null) {
										if (onInput) {
											input.clear();
											input.clearTraces();
											MetadataPlotUtils.plotDataWithMetadata(id.getData(),input);
											MetadataPlotUtils.plotDataWithMetadata(additional, input, false);
										} else {
											MetadataPlotUtils.plotDataWithMetadata(additional, output, false);
										}
									}
									if (od instanceof OperationDataForDisplay) {
										OperationDataForDisplay odd = (OperationDataForDisplay) od;
										IDataset[] dd = odd.getDisplayData();
										if (dd != null) {
											if (input != null && odd.isShowSeparately()) {
												for (IDataset d : dd) {
													IDataset view = d.getSliceView().squeeze();
													MetadataPlotUtils.plotDataWithMetadata(view, input, false);
												}
												input.repaint();
											} else {
												for (IDataset d : dd) {
													IDataset view = d.getSliceView().squeeze();
													if (view.getRank() == 1) {
														MetadataPlotUtils.plotDataWithMetadata(view, output, false);
													}
												}
											}
										}
									}
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
									errorLabel.setText(e.getMessage() == null ? "Unknown error" : e.getMessage());
									errorLabel.pack();
								}
								
							}
						});
						
					}
					return Status.OK_STATUS;
				}
			};
		}

		updateJob.cancel();
		updateJob.schedule();
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

			Object object = model.get(evt.getPropertyName());
			
			
			
			if (object instanceof IROI) region.setROI((IROI)object);
			else {
				OperationModelField an = ModelUtils.getAnnotation(model, evt.getPropertyName());
				
				if (evt.getNewValue() != null && evt.getNewValue() instanceof Double) {
					RectangularROI roi = getRoiFromAnnotation(an, evt.getPropertyName());
					region.setROI(roi);
				} else if (evt.getNewValue() != null && evt.getNewValue().getClass().isArray()){

						if (!Arrays.equals((double[])evt.getNewValue(), (double[])evt.getOldValue())) {
							RectangularROI roi = getRoiFromAnnotation(an, evt.getPropertyName());
							
							
							
							if (valuesChanged(region,roi)) region.setROI(roi);
							update();
							return;
						} else {
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
	
	private boolean valuesChanged(IRegion region, RectangularROI roi) {
		
		boolean changed = true;
		
		if (region.getRegionType() ==  RegionType.XAXIS) {
			double roiX = roi.getPointX();
			double roiW = roi.getLength(0);
			RectangularROI r = (RectangularROI)region.getROI();
			double regionX = r.getPointX();
			double regionW = r.getLength(0);
			
			if (roiX == regionX && roiW == regionW) return false;
			else return true;
			
		}
		
		if (region.getRegionType() ==  RegionType.YAXIS) {
			double roiY = roi.getPointY();
			double roiH = roi.getLength(1);
			RectangularROI r = (RectangularROI)region.getROI();
			double regionY = r.getPointY();
			double regionH = r.getLength(1);
			
			if (roiY == regionY && roiH == regionH) return false;
			else return true;
			
		}
		
		return changed;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		if (input != null && !input.isDisposed()) input.dispose();
		if (output != null && !output.isDisposed()) output.dispose();
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

	@Override
	public void createControl(Composite parent) {
		createDialogArea(parent);
		//setVisible(true);
	}


}
