package org.dawnsci.datavis.e4.addons;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.dawnsci.plotting.api.IPlotRegistrationListener;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotRegistrationEvent;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HistogramToolbarControl {

	public static final String CLASS_URI = "bundleclass://org.dawnsci.datavis.e4.addons/" + HistogramToolbarControl.class.getName();
	public static final String ID = "org.dawnsci.datavis.e4.addons.histogramtoolbar";
	
	@Inject
	private IPlottingService plottingService;
	
	private IPlottingSystem<?> system;
	private IPaletteTrace trace;
	private IPaletteListener listener;
	private ITraceListener traceListener;
	private IPlotRegistrationListener regListener;
	
	private Button lock;
	
	private Text low;
	private Text high;
	
	private Image lockImage;
	private Image unlockImage;
	private Image nolockImage;
	
	private Composite control;

	@PostConstruct
	public void createControl(Composite parent) {
		control = new Composite(parent, SWT.None);
		control.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		control.setLayout(layout);
		
		lock = new Button(control,SWT.TOGGLE);
		
		if (lockImage == null) {
			try {
				ImageDescriptor imd = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.dawnsci.datavis.e4.addons/icons/lock.png"));
				lockImage =  imd.createImage();
				unlockImage = new Image(Display.getCurrent(), lockImage, SWT.IMAGE_DISABLE);
				nolockImage = new Image(Display.getCurrent(), lockImage, SWT.IMAGE_GRAY);
			} catch (Exception e) {
				
			}
		}
		
		if (lockImage == null) {
			lock.setText("Lock");
		}

		lock.setToolTipText("Lock min and max colourmap values");
		
		lock.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trace != null) {
					boolean rescale = !lock.getSelection();
					trace.setRescaleHistogram(rescale);
					lock.setImage(rescale ? unlockImage : lockImage);
				}
				
			}
			
		});
		
		VerifyListener v = e -> {

			//Validation for keys like Backspace, left arrow key, right arrow key and del keys
			if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT
					|| e.keyCode == SWT.ARROW_RIGHT
					|| e.keyCode == SWT.DEL || e.character == '.') {
				e.doit = true;
				return;
			}

			if (e.character == '\0') {
				e.doit = true;
				return;
			}

			if (e.character == '-') {
				e.doit = true;
				return;
			}

			if (!('0' <= e.character && e.character <= '9')){
				e.doit = false;
				return;
			}
		};
		
		low = new Text(control, SWT.BORDER | SWT.RIGHT);
		low.setText("0");
		low.setLayoutData(new GridData(48, 16));
		low.setToolTipText("Set lower value");
		low.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				updateMin();
				
			}
		});
		
		low.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e) {
				updateMin();
			}
		});
		
		low.addVerifyListener(v);
		
		Label l = new Label(control, SWT.NONE);
		l.setText("-");
		
		high = new Text(control, SWT.BORDER | SWT.RIGHT);
		high.setText("255");
		high.setLayoutData(new GridData(48, 16));
		high.setToolTipText("Set upper value");
		high.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				updateMax();
			}
		});
		
		high.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e) {
				updateMax();
				
			}
		});
		
		high.addVerifyListener(v);
		
		traceListener = new ITraceListener.Stub() {
			
			@Override
			public void traceAdded(TraceEvent evt) {
				Object source = evt.getSource();
				if (hasPalette(source)) {
					IPaletteTrace pt = (IPaletteTrace) source;
					HistogramToolbarControl.this.trace = pt;
					updateUI(pt);
					HistogramToolbarControl.this.trace.addPaletteListener(listener);
				} else {
					enable(null);
					HistogramToolbarControl.this.trace = null;
				}
				
			}
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				Object source = evt.getSource();
				if (hasPalette(source)) {
					IPaletteTrace pt = (IPaletteTrace) source;
					HistogramToolbarControl.this.trace = pt;
					updateUI(pt);
				} else {
					enable(null);
					HistogramToolbarControl.this.trace = null;
				}
			}

			@Override
			public void traceRemoved(TraceEvent evt) {
				Object source = evt.getSource();
				if (source == HistogramToolbarControl.this.trace) {
					HistogramToolbarControl.this.trace.removePaletteListener(listener);
					HistogramToolbarControl.this.trace = null;
				}
				enable(null);
			}
			
		};

		regListener = new IPlotRegistrationListener.Stub() {
			public void plottingSystemCreated(PlotRegistrationEvent evt) {
				IPlottingSystem<Object> plottingSystem = evt.getPlottingSystem();
				if ("Plot".equals(plottingSystem.getPlotName())) {
					HistogramToolbarControl.this.system = plottingSystem;
					system.addTraceListener(traceListener);
				}
			}
		};

		plottingService.addRegistrationListener(regListener);
		
		enable(null);
		
		listener = new IPaletteListener.Stub() {
			@Override 
			protected void updateEvent(PaletteEvent evt) {
				IPaletteTrace t = evt.getTrace();
				updateUI(t);
			}
		};
	}

	private static boolean hasPalette(Object source) {
		if (source instanceof IPaletteTrace) {
			IPaletteTrace pt = (IPaletteTrace) source;
			return pt.getMin() != null && pt.getMax() != null;
		}
		return false;
	}

	private void updateMin() {
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
	
	private void updateMax() {
		if (trace != null) {
			String text = high.getText();
			try {
				double max = Double.parseDouble(text);
				trace.getImageServiceBean().setMax(max);
				trace.setMax(max);
				trace.setPaletteData(trace.getPaletteData());
				system.repaint();
			} catch ( Exception ex) {
				high.setText(trace.getImageServiceBean().getMax().toString());
			}
			
		}
	}
	
	@PreDestroy
	public void dispose() {
		if (lockImage != null) lockImage.dispose();
		if (unlockImage != null) unlockImage.dispose();
		if (nolockImage != null) nolockImage.dispose();
		if (system != null) system.removeTraceListener(traceListener);
		plottingService.removeRegistrationListener(regListener);
	}

	public void setSystemTrace(IPlottingSystem<?> system, IPaletteTrace trace) {
		this.system = system;
		this.trace = trace;
		updateUI(trace);
	}

	private void updateUI(IPaletteTrace trace) {
		low.setText(trace.getMin().toString());
		high.setText(trace.getMax().toString());
		enable(trace);
	}

	private void enable(IPaletteTrace pt) {
		boolean enable = pt != null;
		Control[] children = control.getChildren();
		
		for (Control c : children) {
			c.setEnabled(enable);
		}

		boolean rescale = enable ? pt.isRescaleHistogram() : true;
		lock.setSelection(!rescale);
		if (lockImage != null) {
			lock.setImage(enable ? (rescale ? unlockImage : lockImage) : nolockImage);
		} else {
			lock.setGrayed(!enable);
		}
	}
}
