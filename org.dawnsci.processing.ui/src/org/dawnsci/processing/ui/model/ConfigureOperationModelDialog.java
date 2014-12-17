package org.dawnsci.processing.ui.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.jface.dialogs.Dialog;
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

public class ConfigureOperationModelDialog extends Dialog implements PropertyChangeListener{
	
	private IPlottingSystem input;
	private IPlottingSystem output;
	private OperationModelViewer modelViewer;
	IOperationInputData data;
	private Job update;
	private IOperationModel omodel;
	private IOperationModel model;

	protected ConfigureOperationModelDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE|SWT.DIALOG_TRIM);
	}
	
	public Control createDialogArea(Composite parent) {
		
		
		SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		final SashForm left = new SashForm(sashForm, SWT.VERTICAL);
		left.setLayout(new GridLayout(2, false));
//		final Composite mid = new Composite(sashForm, SWT.NONE);
//		mid.setLayout(new GridLayout(2, false));
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new GridLayout());
		
		sashForm.setWeights(new int[]{40,60});
		
		
		input = createPlottingSystem(left);
		modelViewer = new OperationModelViewer();
		modelViewer.createPartControl(left);
		
		output = createPlottingSystem(right);
		left.setWeights(new int[]{70,30});
		return parent;
	}
	
	private IPlottingSystem createPlottingSystem(Composite right){
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
			system.createPlotPart(displayPlotComp, "Slice", actionBarWrapper, PlotType.IMAGE, null);
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (model instanceof AbstractOperationModel) {
			((AbstractOperationModel)model).addPropertyChangeListener(this);
		}
		
		try {
			SlicedDataUtils.plotDataWithMetadata(data.getInputData(),input, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		update();
		
		Map<String,IROI> rois = getROIs(data.getCurrentOperation().getModel());
		
		for ( Entry<String,IROI> entry : rois.entrySet()) {
			try {
				final IRegionService rservice = (IRegionService)ServiceManager.getService(IRegionService.class);
				IRegion reg = rservice.createRegion(input,entry.getValue(),entry.getKey());
				input.addRegion(reg);
				final String name = entry.getKey();
				reg.addROIListener(new IROIListener.Stub() {
					
					@Override
					public void roiChanged(ROIEvent evt) {
						IOperationModel model= data.getCurrentOperation().getModel();
							try {
								model.set(name, evt.getROI());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							modelViewer.setModel(model);
							ConfigureOperationModelDialog.this.update();
						
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private Map<String, IROI> getROIs(IOperationModel model) {
		final List<Field> allFields = new ArrayList<Field>(31);
		allFields.addAll(Arrays.asList(model.getClass().getDeclaredFields()));
		allFields.addAll(Arrays.asList(model.getClass().getSuperclass().getDeclaredFields()));

		Map<String,IROI> rois = new HashMap<String,IROI>();

		for (Field field : allFields) {
			Class<?> class1 = field.getType();
			if (IROI.class.isAssignableFrom(class1)) {
				try {
					rois.put(field.getName(),(IROI)model.get(field.getName()));
				} catch (Exception e) {
					//Do nothing
				}
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
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

}
