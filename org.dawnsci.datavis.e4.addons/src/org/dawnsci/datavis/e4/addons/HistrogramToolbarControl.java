package org.dawnsci.datavis.e4.addons;

import javax.annotation.PostConstruct;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class HistrogramToolbarControl {

	public static final String CLASS_URI = "bundleclass://org.dawnsci.datavis.e4.addons/" + HistrogramToolbarControl.class.getName();
	public static final String ID = "org.dawnsci.datavis.e4.addons.HistrogramToolbarControl";
	
	private IPlottingSystem<?> system;
	private IPaletteTrace trace;	
	
	@PostConstruct
	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.None);
		c.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		c.setLayout(layout);
		Button b = new Button(c,SWT.TOGGLE);
		b.setText("Lock");
		
		b.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trace != null) {
					trace.setRescaleHistogram(!b.getSelection());
				}
				
			}
			
		});
		
		Text low = new Text(c, SWT.NONE);
		low.setText("0");
		low.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (trace != null) {
					String text = low.getText();
					try {
						double min = Double.parseDouble(text);
						trace.getImageServiceBean().setMin(min);
						trace.setMin(min);
						trace.setPaletteData(trace.getPaletteData());
						system.repaint();
					} catch ( Exception ex) {
						low.setText(trace.getImageServiceBean().getMin().toString());
					}
					
				}
				
			}
		});
		
		Text high = new Text(c, SWT.NONE);
		high.setText("255");
		
	}
	
	public void setSystemTrace(IPlottingSystem<?> system, IPaletteTrace trace) {
		this.system = system;
		this.trace = trace;
	}
	
}
