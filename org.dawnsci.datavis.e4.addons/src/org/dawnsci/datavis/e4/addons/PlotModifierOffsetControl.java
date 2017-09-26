package org.dawnsci.datavis.e4.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IPlotDataModifier;
import org.dawnsci.datavis.model.PlotController;
import org.dawnsci.datavis.model.PlotDataModifierStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class PlotModifierOffsetControl {
	
	public static final String CLASS_URI = "bundleclass://org.dawnsci.datavis.e4.addons/" + PlotModifierOffsetControl.class.getName();
	public static final String ID = "org.dawnsci.datavis.e4.addons.PlotModifierOffsetControl";
	
	@Inject
	private IPlotController controller;
	
	@PostConstruct
	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.None);
		c.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		c.setLayout(layout);
	
		Label l =new Label(c, SWT.SEPARATOR | SWT.VERTICAL);
		l.setLayoutData(new GridData(2,24));
		
		Button b = new Button(c, SWT.TOGGLE);
		
		b.setText("On");
		
		
		Scale s = new Scale(c,SWT.NONE);
		s.setLayoutData(new GridData(48, 16));
		s.setMaximum(100);
		s.setMinimum(0);
		
		s.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPlotDataModifier pm = controller.getEnabledPlotModifier();
				if (pm instanceof PlotDataModifierStack) {
					((PlotDataModifierStack)pm).setProportion(s.getSelection()/100.0);
					controller.enablePlotModifier(null);
					controller.enablePlotModifier(pm);
				}
			}
			
		});
		
		l = new Label(c, SWT.SEPARATOR | SWT.VERTICAL);
		l.setLayoutData(new GridData(2,24));
		
		
	}

}
