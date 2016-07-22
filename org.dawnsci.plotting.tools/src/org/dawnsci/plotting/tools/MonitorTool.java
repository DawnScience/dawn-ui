package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.PlatformUI;

public class MonitorTool extends AbstractToolPage {

	private static final int MAXIMUM_NUMBER = 255;
	
	private IPlottingSystem<Composite> system;
	private ITraceListener listener;
	private ComboViewer colorViewer;
	private LinkedList<ILineTrace> queue;
	private int maxLength = 100;
	private List<Color> colors;
	private Composite control;

	
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
		queue = new LinkedList<ILineTrace>();
		
		colors = new ArrayList<Color>(maxLength);
		
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
		comp.setLayout(new GridLayout(5, false));
		
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
				queue.clear();
				system.clear();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		new Label(comp, SWT.NONE).setText("Max No.:");
		
		final Spinner spinner = new Spinner(comp, SWT.NONE);
		spinner.setMaximum(MAXIMUM_NUMBER);
		spinner.setSelection(maxLength);
		spinner.setMinimum(1);
		spinner.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getPlottingSystem() == null) return;
				getPlottingSystem().removeTraceListener(listener);
				maxLength = spinner.getSelection();
				queue = new LinkedList<ILineTrace>();
				system.clear();
				updateColor(colorViewer.getSelection());
				getPlottingSystem().addTraceListener(listener);
				
			}
		});
		
		
		colorViewer = new ComboViewer(comp, SWT.READ_ONLY);
		colorViewer.getCombo().setLayoutData(new GridData());
		colorViewer.setContentProvider(ArrayContentProvider.getInstance());
		colorViewer.setLabelProvider(new LabelProvider());
		colorViewer.setInput(colorSchemes.toArray());
		
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
		updateColor(colorViewer.getSelection());
		
		
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
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
		PaletteData paletteData = pservice.getDirectPaletteData(name);
		if (paletteData == null) return;
		RGB[] rgbs = paletteData.getRGBs();
		Display display = Display.getDefault();
		List<Color> c = new ArrayList<Color>();
		for (int i = 0 ; i < maxLength ; i++) {
			c.add(new Color(display, rgbs[(255/maxLength)*i]));
		}
		if (getPlottingSystem() != null) getPlottingSystem().removeTraceListener(listener);
		List<Color> lc = colors;
		colors = c;
		if (getPlottingSystem() != null) getPlottingSystem().addTraceListener(listener);
		for (Color co : lc) co.dispose();
		updatePlot();
	}
	
	private void processLineTrace(ILineTrace trace){
		IDataset xData = trace.getXData();
		IDataset yData = trace.getYData();
		List<ITrace> traces = system.createPlot1D(xData, Arrays.asList(new IDataset[]{yData}), null);
		
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
		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(listener);
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		if (colors != null) for (Color c : colors) c.dispose();
		system.dispose();
		super.dispose();
	}

}
