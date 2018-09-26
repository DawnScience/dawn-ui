package org.dawnsci.datavis.e4.addons;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IPlotDataModifier;
import org.dawnsci.datavis.model.PlotDataModifierStack;
import org.dawnsci.datavis.model.PlotEventObject;
import org.dawnsci.datavis.model.PlotModeChangeEventListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;

public class PlotModifierOffsetControl {
	
	public static final String CLASS_URI = "bundleclass://org.dawnsci.datavis.e4.addons/" + PlotModifierOffsetControl.class.getName();
	public static final String ID = "org.dawnsci.datavis.e4.addons.plotmodifieroffsetslider";
	
	private Button normButton;
	private Image normImage;
	private Button stackButton;
	private Image stackImage;
	private Composite control;
	private PlotModeChangeEventListener listener;
	
	@Inject
	private IPlotController controller;
	
	@PostConstruct
	public void createControl(Composite parent) {
		control = new Composite(parent, SWT.None);
		control.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		control.setLayout(layout);
		
		stackButton = new Button(control, SWT.TOGGLE);
		
		if (stackImage == null) {
			try {
				ImageDescriptor imd = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.dawnsci.datavis.e4.addons/icons/offset.png"));
				stackImage =  imd.createImage();
			} catch (Exception e) {
				
			}
		}
		
		if (stackImage != null) {
			stackButton.setImage(stackImage);
		} else {
			stackButton.setText("Stack");
		}
		
		stackButton.setToolTipText("Stack XY data (using current x axis range)");
		
		normButton = new Button(control, SWT.TOGGLE);
		normButton.setToolTipText("Normalise data (between 0-1)");
		normButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPlotDataModifier pm = controller.getEnabledPlotModifier();
				if (pm instanceof PlotDataModifierStack) {
					((PlotDataModifierStack)pm).setNormalise(normButton.getSelection());
					controller.forceReplot();
				}
			}
			
		});
		
		if (normImage == null) {
			try {
				ImageDescriptor imd = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.dawnsci.datavis.e4.addons/icons/norm.png"));
				normImage =  imd.createImage();
			} catch (Exception e) {
				
			}
		}
		
		if (normImage != null) {
			normButton.setImage(normImage);
		} else {
			normButton.setText("Norm");
		}
		
		
		
		Scale s = new Scale(control,SWT.NONE);
		s.setLayoutData(new GridData(48, 16));
		s.setMaximum(100);
		s.setMinimum(0);
		
		s.setToolTipText("Set magnitude of offset");
		
		s.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPlotDataModifier pm = controller.getEnabledPlotModifier();
				if (pm instanceof PlotDataModifierStack) {
					((PlotDataModifierStack)pm).setProportion(s.getSelection()/100.0);
					controller.forceReplot();
				}
			}
			
		});
		
		stackButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (stackButton.getSelection()) {
					IPlotDataModifier[] currentPlotModifiers = controller.getCurrentPlotModifiers();
					
					for (IPlotDataModifier m : currentPlotModifiers) {
						if (m instanceof PlotDataModifierStack) {
							((PlotDataModifierStack)m).setProportion(s.getSelection()/100.0);
							controller.enablePlotModifier(m);
							s.setEnabled(true);
							normButton.setEnabled(true);
						}
					}
					
				} else {
					s.setEnabled(false);
					normButton.setEnabled(false);
					controller.enablePlotModifier(null);
				}
			}
			
		});
		
		listener = new PlotModeChangeEventListener() {
			
			@Override
			public void plotStateEvent(PlotEventObject event) {
				//do nothing
				
			}
			
			@Override
			public void plotModeChanged() {
				plotControllerUpdate();
			}
		};
		
		
		controller.addPlotModeListener(listener);
		
	}
	
	private void plotControllerUpdate() {

		boolean selected = false;
		
		IPlotDataModifier pm = controller.getEnabledPlotModifier();
		if (pm instanceof PlotDataModifierStack) {
			selected = true;
				
		}
		
		IPlotDataModifier[] currentPlotModifiers = controller.getCurrentPlotModifiers();
		
		boolean enable = false;
		
		for (IPlotDataModifier m : currentPlotModifiers) {
			if (m instanceof PlotDataModifierStack) {
				enable = true;
			}
		}
		
		enable(enable);
		
		stackButton.setSelection(selected);	

	}
	
	@PreDestroy
	public void dispose() {
		if (stackImage != null) stackImage.dispose();
		if (normImage != null) normImage.dispose();
		controller.removePlotModeListener(listener);
	}

	private void enable(boolean enable) {
		
		if (control.isDisposed()) return;
		
		if (!(controller.getEnabledPlotModifier() instanceof PlotDataModifierStack) && enable) {
			stackButton.setEnabled(true);
		} else {
			Control[] children = control.getChildren();
			
			for (Control c : children) {
				c.setEnabled(enable);
			}
		}
		
		
	}
}
