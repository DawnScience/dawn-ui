package org.dawnsci.plotting.tools.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.dawnsci.plotting.api.filter.FilterConfiguration;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A tool which reads the plot filter extension point 
 * and allows the user to experiment with different filters.
 * 
 * For instance this tool can be used to apply the PHA algorithm 
 * and the fano factor.
 * 
 * @author Matthew Gerring
 *
 */
public class FilterTool extends AbstractToolPage {

	/* Map id to filter */
	private Map<String, IPlottingFilter> filters;
	
	/* Map id to ui */
	private Map<String, Composite>       components;
	
	/* Map label to id */
	private Map<String, String>          labels;
	
	/* Current active filter */
	private IPlottingFilter  currentFilter;
	/* original data */
	private IDataset originalData;
	private List<IDataset> originalAxes;	
	/* UI */
	private Combo            filterChoice;
	private Composite        control;
	private StackLayout      slayout;
	private boolean isResetting = false;

	private ITraceListener traceListener;

	public FilterTool() {
		super();
		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void traceUpdated(TraceEvent evt) {
				boolean isOn = ((AbstractDelayedFilter)currentFilter).isFilterOn();
				if (isOn && !isResetting)
					applyFilter();
			}
		};
 	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	@Override
	public void createControl(Composite parent) {
		// Create the UI for the filters
		this.control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, false));	
		
		final Label label = new Label(control, SWT.WRAP);
		label.setText("Please choose a filter then press apply");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final Label filterName = new Label(control, SWT.NONE);
		filterName.setText("Filter");
		
		this.filterChoice = new Combo(control, SWT.READ_ONLY);
		filterChoice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Composite   configuration = new Composite(control, SWT.BORDER);
		this.slayout       = new StackLayout();
		configuration.setLayout(slayout);
		configuration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		filters    = new HashMap<String, IPlottingFilter>(7);
		components = new HashMap<String, Composite>(7);
		labels     = new TreeMap<String, String>();
		
		String noneId = getClass().getName()+".none";
		filters.put(noneId,    null);
		components.put(noneId, new Composite(configuration, SWT.NONE));
		
		try {
			IConfigurationElement[] ele = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.plotting.api.plottingFilter");
			for (IConfigurationElement e : ele) {
				final String        id     = e.getAttribute("id");
				final String        slabel = e.getAttribute("label");
				
				IPlottingFilter     filter = (IPlottingFilter)e.createExecutableExtension("filter");
				if (filter instanceof AbstractDelayedFilter) ((AbstractDelayedFilter)filter).setFilterName(slabel);
				
				FilterConfiguration config = (FilterConfiguration)e.createExecutableExtension("ui");
				labels.put(slabel, id);
				filters.put(id, filter);
				
				config.init(getPlottingSystem(), filter);
				Composite conf = config.createControl(configuration);
				components.put(id, conf);
			}
		} catch (Exception ne) {
			logger.error("Cannot read extension points for filters!", ne);
		}

		filterChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String label = filterChoice.getItem(filterChoice.getSelectionIndex());
				setUISelected(label);
			}
		});
		
		final List<String> ls = new ArrayList<String>(labels.size()+1);
		ls.addAll(labels.keySet());
		
		labels.put("<None>", noneId);
		ls.add(0, "<None>");
		filterChoice.setItems(ls.toArray(new String[ls.size()]));
		filterChoice.select(0);
		
		final Composite buttons = new Composite(control, SWT.BORDER);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		
		final Button apply = new Button(buttons, SWT.PUSH);
		apply.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		apply.setImage(Activator.getImage("icons/apply.gif"));
		apply.setText("Apply");
		apply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyFilter();
			}
		});
		
		final Button reset = new Button(buttons, SWT.PUSH);
		reset.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		reset.setImage(Activator.getImage("icons/reset.gif"));
		reset.setText("Reset");
		reset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				reset();
			}
		});
		createActions();
		getPlottingSystem().addTraceListener(traceListener);
		super.createControl(parent);
	}

	private void reset() {
		try {
			isResetting = true;
			filterChoice.select(0);
			if (originalData != null)
				getPlottingSystem().updatePlot2D(originalData, originalAxes,
						null);
			setUISelected("<None>");
			getPlottingSystem().repaint();
		} finally {
			isResetting = false;
		}
	}

	private void createActions() {
		final IToolBarManager man = getSite().getActionBars().getToolBarManager();
		final IAction reset = new Action("Reset filter", Activator.getImageDescriptor("icons/reset.gif")) {
			public void run() {
				reset();
			}
		};
		man.add(reset);
	}

	private void setUISelected(String label) {
		if (labels == null)
			return;
		final String id = labels.get(label);
		slayout.topControl = components.get(id);
		for (Control c : control.getChildren())
			if (c instanceof Composite)
				((Composite) c).layout();

		currentFilter = filters.get(id);
	}

	private void applyFilter() {
		// Save original data & axes
		if (originalData == null)
			originalData = getImageTrace().getData().clone();
		if (originalAxes == null) {
			List<IDataset> axes = getImageTrace().getAxes();
			if (axes != null) {
				IDataset xAxis = axes.get(0);
				IDataset yAxis = axes.get(1);
				originalAxes = new ArrayList<IDataset>();
				originalAxes.add(xAxis.clone());
				originalAxes.add(yAxis.clone());
			}
		}
		// Apply the filter to the current image trace
		IImageTrace image = getImageTrace();
		if (image != null) {
			try {
				if (currentFilter == null)
					return;
				currentFilter.filter(getPlottingSystem(),
						new TraceWillPlotEvent(image, image.getData(), image.getAxes()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			image.setData(image.getData(), image.getAxes(), false);
		}
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null)
			getPlottingSystem().addTraceListener(traceListener);
		if(currentFilter != null)
			applyFilter();
	}

	@Override
	public void dispose() {
		if (getPlottingSystem() != null)
			getPlottingSystem().removeTraceListener(traceListener);
	}

 	public void deactivate() {
		super.deactivate();
		if (originalData != null)
			getPlottingSystem().updatePlot2D(originalData, originalAxes, null);
		setUISelected("<None>");
		if (getPlottingSystem()!=null)
			getPlottingSystem().removeTraceListener(traceListener);
	}
}
