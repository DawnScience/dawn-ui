package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.RunningAverage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.PlatformUI;

public class MonitorTool extends AbstractToolPage {

	private static final int MAXIMUM_NUMBER = 255;
	
	private IPlottingSystem<Composite> system;
	private ITraceListener listener;
	private ComboViewer modeViewer;
	private ComboViewer colorViewer;
	
	private Composite control;
	
	private OscilloscopeMonitor scopeMonitor;
	private AverageMonitor averageMonitor;

	private MonitorMode currentMode = MonitorMode.SCOPE_MODE;
	
	private StackLayout stackLayout;
	private OscilloscopeComposite scopeComp;
	private AverageComposite averageComp;
	
	private Composite stack;
	
	public MonitorTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		
		scopeMonitor = new OscilloscopeMonitor();
		averageMonitor = new AverageMonitor();
		
		listener = new ITraceListener.Stub() {
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				if (evt.getSource() instanceof ILineTrace) {
					processLineTrace((ILineTrace)evt.getSource());
				}
			}
			
			@Override
			public void traceAdded(TraceEvent evt) {
				evt.toString();
			}
			
			@Override
			public void tracesUpdated(TraceEvent evt) {
				evt.toString();
			}
			
			@Override
			public void tracesAdded(TraceEvent evt) {
				if (evt.getSource() instanceof ILineTrace) {
					processLineTrace((ILineTrace)evt.getSource());
				}
				
				if (evt.getSource() instanceof List<?>){
					List<?> l = (List<?>)evt.getSource();
					for (Object ob : l) {
						if (ob instanceof ILineTrace) {
							processLineTrace((ILineTrace)ob);
						}
					}
				}
			}
			
			@Override
			protected void update(TraceEvent evt) {
				evt.toString();
			}
		};
		
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		
		Composite comp = new Composite(control, SWT.NONE);
		Composite plotComp = new Composite(control, SWT.NONE);
		comp.setLayoutData(new GridData());
		comp.setLayout(new GridLayout(6, false));
		
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
		Collection<String> colorSchemes = pservice.getColorSchemes();
		
		final Button play = new Button(comp, SWT.TOGGLE);
		play.setText("Store");
		play.setSelection(true);
		play.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlottingSystem() == null) return;
				if (play.getSelection()) {
					getPlottingSystem().addTraceListener(listener);
				} else {
					getPlottingSystem().removeTraceListener(listener);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		final Button clear = new Button(comp, SWT.PUSH);
		clear.setText("Clear");
		clear.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				scopeMonitor.clear();
				averageMonitor.clear();
				averageComp.updateCount(0);
				system.clear();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		modeViewer = new ComboViewer(comp, SWT.READ_ONLY);
		modeViewer.getCombo().setLayoutData(new GridData());
		modeViewer.setContentProvider(ArrayContentProvider.getInstance());
		modeViewer.setLabelProvider(new LabelProvider());
		modeViewer.setInput(new Object[] {MonitorMode.SCOPE_MODE,MonitorMode.AVERAGE_MODE});
		modeViewer.getCombo().select(0);
		
		modeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection)event.getSelection()).getFirstElement() instanceof MonitorMode) {
					MonitorMode mode = (MonitorMode) ((StructuredSelection)event.getSelection()).getFirstElement();
					if (!mode.equals(currentMode)) {
						currentMode = mode;
						
						switch (currentMode) {
						case AVERAGE_MODE:
							stackLayout.topControl = averageComp;
							break;
						case SCOPE_MODE:
							stackLayout.topControl = scopeComp;
							break;
						}
						stack.layout();
						
						scopeMonitor.clear();
						averageMonitor.clear();
						averageComp.updateCount(0);
						system.clear();
					}
				}
			}
		});
		
		stack = new Composite(comp, SWT.NONE);
		stack.setLayoutData(new GridData());
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		
		scopeComp = new OscilloscopeComposite(stack, SWT.NONE);
		scopeComp.setLayoutData(new GridData());
		
		scopeComp.setInput(colorSchemes.toArray());
		
		averageComp = new AverageComposite(stack, SWT.NONE);
		
		stackLayout.topControl = scopeComp;
		
		stack.layout();
		
//		new Label(comp, SWT.NONE).setText("Max No.:");
//		
//		final Spinner spinner = new Spinner(comp, SWT.NONE);
//		spinner.setMaximum(MAXIMUM_NUMBER);
//		spinner.setSelection(scopeMonitor.getMaxLength());
//		spinner.setMinimum(1);
//		spinner.addSelectionListener(new SelectionAdapter() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if (getPlottingSystem() == null) return;
//				getPlottingSystem().removeTraceListener(listener);
//				scopeMonitor.setMaxLength(spinner.getSelection());
//				system.clear();
//				updateColor(colorViewer.getSelection());
//				getPlottingSystem().addTraceListener(listener);
//				
//			}
//		});
//		
//		
//		colorViewer = new ComboViewer(comp, SWT.READ_ONLY);
//		colorViewer.getCombo().setLayoutData(new GridData());
//		colorViewer.setContentProvider(ArrayContentProvider.getInstance());
//		colorViewer.setLabelProvider(new LabelProvider());
//		colorViewer.setInput(colorSchemes.toArray());
//		
//		colorViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				if (((StructuredSelection)event.getSelection()).getFirstElement() instanceof String) {
//					updateColor(event.getSelection());
//				}
//			}
//		});
//		
//		colorViewer.addOpenListener(new IOpenListener() {
//			
//			@Override
//			public void open(OpenEvent event) {
//				if (getPlottingSystem() != null) getPlottingSystem().removeTraceListener(listener);
//				
//			}
//		});
//		
//		colorViewer.getCombo().select(0);
//		updateColor(colorViewer.getSelection());
		
		
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		plotComp.setLayout(new FillLayout());
		

		system.createPlotPart(plotComp, getViewPart().getTitle(), getSite().getActionBars(), PlotType.XY, getPart());

		super.createControl(parent);
	}
	
	
	@Override
	public Control getControl() {
		return control;
//		return system == null ? null : system.getPlotComposite();
	}
	
	private void updateColor(ISelection selection) {
		String name = (String)((StructuredSelection)selection).getFirstElement();
		
		if (getPlottingSystem() != null) getPlottingSystem().removeTraceListener(listener);
		scopeMonitor.updateColours(name);

		if (getPlottingSystem() != null) getPlottingSystem().addTraceListener(listener);

	}
	
	private void processLineTrace(ILineTrace trace){
		IDataset xData = trace.getXData();
		IDataset yData = trace.getYData();
		
		if (currentMode.equals(MonitorMode.AVERAGE_MODE)) {
			
			if (!averageMonitor.isCompatible(yData)) {
				averageMonitor.clear();
			}
			
			IDataset update = averageMonitor.update(yData);
			update.setName("Average");
			averageComp.updateCount(averageMonitor.getCount());
			system.updatePlot1D(xData, Arrays.asList(new IDataset[]{update}), null);
			system.repaint();
			
		} else {
			List<ITrace> traces = system.createPlot1D(xData, Arrays.asList(new IDataset[]{yData}), null);
			scopeMonitor.addTrace(traces);
		}
		
		
	}
	


	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	@Override
	public void activate() {
		if (isActive()) return;
		if (getPlottingSystem() ==  null) return;
		getPlottingSystem().addTraceListener(listener);
		super.activate();
	}
	
	@Override
	public void deactivate() {
		if (getPlottingSystem()!=null && listener != null) getPlottingSystem().removeTraceListener(listener);
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		system.dispose();
		scopeMonitor.dispose();
		super.dispose();
	}
	
	private class OscilloscopeMonitor {
		private LinkedList<ILineTrace> queue;
		private int maxLength = 100;
		public int getMaxLength() {
			return maxLength;
		}

		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			queue = new LinkedList<ILineTrace>();
		}

		private List<Color> colors;
		
		public OscilloscopeMonitor() {
			queue = new LinkedList<ILineTrace>();
			colors = new ArrayList<Color>(maxLength);
		}
		
		public void updateColours(String name) {
			final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
			PaletteData paletteData = pservice.getDirectPaletteData(name);
			if (paletteData == null) return;
			RGB[] rgbs = paletteData.getRGBs();
			Display display = Display.getDefault();
			List<Color> c = new ArrayList<Color>();
			for (int i = 0 ; i < maxLength ; i++) {
				c.add(new Color(display, rgbs[(255/maxLength)*i]));
			}
//			if (getPlottingSystem() != null) getPlottingSystem().removeTraceListener(listener);
			List<Color> lc = colors;
			colors = c;
//			if (getPlottingSystem() != null) getPlottingSystem().addTraceListener(listener);
			for (Color co : lc) co.dispose();
			
			updatePlot();
		}
		
		public void addTrace(List<ITrace> traces) {
			for (ITrace t : traces) {
				queue.addFirst((ILineTrace)t);
				if (queue.size() > maxLength) {
					ILineTrace c = queue.removeLast();
					system.removeTrace(c);
				}
			}
			
			updatePlot();
		}
		
		private void updatePlot(){
			int i = 0;
			int delta = (int)Math.floor((maxLength-1)/(queue.size() == 1 ? queue.size() : queue.size()-1));
			for (ILineTrace t : queue) {
				t.setTraceColor(colors.get(i*delta));
				i++;
			}
		}
		
		
		public void dispose() {
			if (colors != null) for (Color c : colors) c.dispose();
		}
		
		public void clear() {
			queue.clear();
		}
	}
	
	private class AverageMonitor {
		RunningAverage runningAverage;
		
		public int getCount() {
			return runningAverage.getCount();
		}
		
		public IDataset update(IDataset y) {
			if (runningAverage == null) {
				runningAverage = new RunningAverage(y);
			}
			runningAverage.update(y);
			return runningAverage.getCurrentAverage();
		}
		
		
		public void clear() {
			runningAverage = null;
		}
		
		public boolean isCompatible(IDataset y) {
			if (runningAverage == null) return true;
			
			return Arrays.equals(y.getShape(), runningAverage.getCurrentAverage().getShape());
		}
		
	}
	
	private class AverageComposite extends Composite {

		private Label count;
		
		public AverageComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new GridLayout(1, false));
			
			count = new Label(this, SWT.NONE);
			updateCount(0);
		}

		private void updateCount(int i) {
			count.setText("Average of " + Integer.toString(i));
			this.layout();
		}
		
	}
	
	private class OscilloscopeComposite extends Composite {

		public OscilloscopeComposite(Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new GridLayout(3, false));
			
			new Label(this, SWT.NONE).setText("Max No.:");
			
			final Spinner spinner = new Spinner(this, SWT.NONE);
			spinner.setMaximum(MAXIMUM_NUMBER);
			spinner.setSelection(scopeMonitor.getMaxLength());
			spinner.setMinimum(1);
			spinner.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (getPlottingSystem() == null) return;
					getPlottingSystem().removeTraceListener(listener);
					scopeMonitor.setMaxLength(spinner.getSelection());
					system.clear();
					updateColor(colorViewer.getSelection());
					getPlottingSystem().addTraceListener(listener);
					
				}
			});
			
			
			colorViewer = new ComboViewer(this, SWT.READ_ONLY);
			colorViewer.getCombo().setLayoutData(new GridData());
			colorViewer.setContentProvider(ArrayContentProvider.getInstance());
			colorViewer.setLabelProvider(new LabelProvider());
			
			colorViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if (((StructuredSelection)event.getSelection()).getFirstElement() instanceof String) {
						updateColor(event.getSelection());
					}
				}
			});
			
			colorViewer.addOpenListener(new IOpenListener() {
				
				@Override
				public void open(OpenEvent event) {
					if (getPlottingSystem() != null) getPlottingSystem().removeTraceListener(listener);
					
				}
			});
			
			colorViewer.getCombo().select(0);
//			updateColor(colorViewer.getSelection());
		}
		
		public void setInput(Object[] input) {
			colorViewer.setInput(input);
			colorViewer.getCombo().select(0);
			updateColor(colorViewer.getSelection());
		}
		
	}
	
	private enum MonitorMode {
		SCOPE_MODE {
			@Override
			public String toString() {
				return "Oscilloscope";
			}
		}, 
		AVERAGE_MODE {
			@Override
			public String toString() {
				return "Running Average";
			}
		},
	}
}
