package org.dawnsci.datavis.e4.addons;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.dawnsci.datavis.model.IPlotController;
import org.eclipse.dawnsci.plotting.api.IPlotRegistrationListener;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotRegistrationEvent;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;

public class HistrogramToolbarControl {

	public static final String CLASS_URI = "bundleclass://org.dawnsci.datavis.e4.addons/" + HistrogramToolbarControl.class.getName();
	public static final String ID = "org.dawnsci.datavis.e4.addons.HistrogramToolbarControl";
	
	@Inject
	IPlottingService plottingService;
	
	private IPlottingSystem<?> system;
	private IPaletteTrace trace;	
	
	private Button lock;
	
	private Text low;
	private Text high;
	
	private Image lockImage;
	
	@PostConstruct
	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.None);
		c.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		GridLayout layout = new GridLayout(6, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		c.setLayout(layout);
		
		Label l =new Label(c, SWT.SEPARATOR | SWT.VERTICAL);
		l.setLayoutData(new GridData(2,24));
		
		lock = new Button(c,SWT.TOGGLE);
		
		if (lockImage == null) {
			try {
				ImageDescriptor imd = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.dawnsci.datavis.e4.addons/icons/lock.png"));
				lockImage =  imd.createImage();
			} catch (Exception e) {
				
			}
		}
		
		if (lockImage != null) {
			lock.setImage(lockImage);
		} else {
			lock.setText("Lock");
		}
		
		lock.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trace != null) {
					trace.setRescaleHistogram(!lock.getSelection());
				}
				
			}
			
		});
		
		low = new Text(c, SWT.BORDER);
		low.setText("0");
		low.setLayoutData(new GridData(48, 16));
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
		
		l = new Label(c, SWT.NONE);
		l.setText("-");
		
		high = new Text(c, SWT.BORDER);
		high.setText("255");
		high.setLayoutData(new GridData(48, 16));
		high.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (trace != null) {
					String text = high.getText();
					try {
						double max = Double.parseDouble(text);
						trace.getImageServiceBean().setMax(max);
						trace.setMax(max);
						trace.setPaletteData(trace.getPaletteData());
						system.repaint();
					} catch ( Exception ex) {
						low.setText(trace.getImageServiceBean().getMin().toString());
					}
					
				}
				
			}
		});
		
		l =new Label(c, SWT.SEPARATOR | SWT.VERTICAL);
		l.setLayoutData(new GridData(2,24));
		
		plottingService.addRegistrationListener(new IPlotRegistrationListener.Stub() {
			public void plottingSystemCreated(PlotRegistrationEvent evt) {
				IPlottingSystem<Object> plottingSystem = evt.getPlottingSystem();
				if (plottingSystem.getPlotName().equals("Plot")) {
					HistrogramToolbarControl.this.system = plottingSystem;
					system.addTraceListener(new ITraceListener.Stub() {
						
						@Override
						public void traceAdded(TraceEvent evt) {
							evt.toString();
							
						}

						@Override
						public void traceRemoved(TraceEvent evt) {
							evt.toString();
						}
						
					});
				}
			}
		});
		
	}
	
	
	@Inject
	@Optional
	private void plotControllerUpdate(@UIEventTopic("org/dawnsci/datavis/plot/UPDATE") Event data ) {

		if (system == null) {
			system = plottingService.getPlottingSystem("Plot");
			
			if (system != null) {
				
			}
		}

	} 
	
	
	@PreDestroy
	public void dispose() {
		if (lockImage != null) lockImage.dispose();
	}
	
	public void setSystemTrace(IPlottingSystem<?> system, IPaletteTrace trace) {
		this.system = system;
		this.trace = trace;
		updateUI(trace);
	}
	
	private void updateUI(IPaletteTrace trace) {
		low.setText(Double.toString(trace.getMin().doubleValue()));
		high.setText(Double.toString(trace.getMax().doubleValue()));
		lock.setSelection(!trace.isRescaleHistogram());
	}
	
	
}
