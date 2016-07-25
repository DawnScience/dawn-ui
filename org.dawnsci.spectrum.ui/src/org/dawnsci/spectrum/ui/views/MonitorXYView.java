package org.dawnsci.spectrum.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.dataset.IDataset;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class MonitorXYView extends ViewPart implements IAdaptable {

	private IPlottingSystem<Composite>     system;
	private ITraceListener listener;
	private IPartListener partListener;
	private ComboViewer viewer;
	private ComboViewer colorViewer;
	private IPlottingSystem<Composite> current;
	private LinkedList<ILineTrace> queue;
	private int maxLength = 20;
	private List<Color> colors;

	@Override
	public void createPartControl(Composite parent) {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e1) {
			return;
		}
		partListener = new IPartListener() {
			
			@Override
			public void partOpened(IWorkbenchPart part) {
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
				if (!MonitorXYView.this.getSite().getPage().isPartVisible(MonitorXYView.this)) {
					if (current != null) current.removeTraceListener(listener);
				}
				
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
				if (part.equals(MonitorXYView.this)) if (current != null) current.removeTraceListener(listener);
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part.equals(MonitorXYView.this)) {
					if (current != null) current.addTraceListener(listener);
				}
			}
		};
		
		this.getViewSite().getPage().addPartListener(partListener);
		
		this.addPropertyListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(Object source, int propId) {
				source.toString();
				
			}
		});
		
		queue = new LinkedList<ILineTrace>();
		
		colors = new ArrayList<Color>(maxLength);
		
		//comment
//		Display display = Display.getDefault();
//		colors.add(new Color(display, 255, 0, 0));
//		for (int i = 0 ; i < maxLength-1 ; i++) {
//			colors.add(new Color(display, 255, (int)(200*(i/(double)maxLength))+40, (int)(200*(i/(double)maxLength)+40)));
//		}
		
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
		
		parent.setLayout(new GridLayout(1, true));
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.CENTER, SWT.LEFT, false, false, 1, 1));
		comp.setLayout(new GridLayout(3, false));
		final IPlottingSystem<Composite>[] ps = PlottingFactory.getPlottingSystems();
		
		viewer = new ComboViewer(comp, SWT.READ_ONLY);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				String val = "";
				if (element instanceof IPlottingSystem) val = ((IPlottingSystem<Composite>)element).getPlotName();
				return val;
			}
		});
		viewer.setInput(ps);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (((StructuredSelection)event.getSelection()).getFirstElement() instanceof IPlottingSystem) {
					updateCurrentPlot(event.getSelection());
				}
			}
		});
		

		
		Button refresh = new Button(comp, SWT.NONE);
		refresh.setText("Refresh");
		refresh.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPlottingSystem<Composite>[] ps = PlottingFactory.getPlottingSystems();
				
				boolean contained = false;
				
				for (int i = 0; i < ps.length; i++) {
					if (ps[i].equals(system)) contained = true;
				}
				
				if (contained) {
					List<IPlottingSystem<Composite>> list = Arrays.asList(ps);
					list = new LinkedList<IPlottingSystem<Composite>>(list);
					list.remove(system);
					ps = list.toArray(new IPlottingSystem[list.size()]);
				}
				viewer.setInput(ps);
				viewer.getCombo().select(0);
				updateCurrentPlot(viewer.getSelection());
				viewer.getControl().getParent().layout();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {;
			}
		});
		
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
		Collection<String> colorSchemes = pservice.getColorSchemes();
		colorViewer = new ComboViewer(comp, SWT.READ_ONLY);
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
				if (current != null) current.removeTraceListener(listener);
				
			}
		});
		
		colorViewer.getCombo().select(0);
		viewer.getCombo().select(0);
		updateColor(colorViewer.getSelection());
		updateCurrentPlot(viewer.getSelection());
		
		Composite plotComp = new Composite(parent, SWT.NONE);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		plotComp.setLayout(new FillLayout());
		

		system.createPlotPart(plotComp, getPartName(), getViewSite().getActionBars(), PlotType.IMAGE, this);


		
	}
	
	private void updateCurrentPlot(ISelection selection) {
		if (current != null) current.removeTraceListener(listener);
		current = (IPlottingSystem<Composite>)((StructuredSelection)selection).getFirstElement();
		current.addTraceListener(listener);
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
		if (current != null) current.removeTraceListener(listener);
		List<Color> lc = colors;
		colors = c;
		if (current != null) current.addTraceListener(listener);
		for (Color co : lc) co.dispose();
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
		{
			int i = 0;
			for (ILineTrace t : queue) {
				t.setTraceColor(colors.get(i++));
			}
		}
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (IPlottingSystem.class == adapter) return system;
		if (IToolPageSystem.class == adapter) return system.getAdapter(adapter);
		return super.getAdapter(adapter);
	}

	@Override
	public void setFocus() {
		system.setFocus();
	}

	@Override
	public void dispose() {
		if (colors != null) for (Color c : colors) c.dispose();
		system.dispose();
		super.dispose();
	}

}
