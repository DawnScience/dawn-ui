package org.dawb.workbench.plotting.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XYHistoryTool extends AbstractToolPage implements MouseListener {

	private Logger logger = LoggerFactory.getLogger(XYHistoryTool.class);
	
	/**
	 * We simply keep the history in a static map of traces.
	 */
	private static Map<String, HistoryBean> history;
    static {
    	if (history==null) history = new LinkedHashMap<String, HistoryBean>(17);
    }
	
	private Composite      composite;
	private TableViewer    viewer;
	private ITraceListener traceListener;
	
	public XYHistoryTool() {
		this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void tracesPlotted(TraceEvent evt) {
				updatePlots();
			}
			
			@Override
			public void tracesCleared(TraceEvent evet) {
				updatePlots();
			}
		};
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createActions();
				
		getSite().setSelectionProvider(viewer);
		
		viewer.setContentProvider(new IStructuredContentProvider() {			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}		
			@Override
			public Object[] getElements(Object inputElement) {
				return history.values().toArray(new HistoryBean[history.values().size()]);
			}
		});
		viewer.setInput(new Object());

		viewer.getTable().addMouseListener(this);
	}
	
	private void refresh() {
		if (viewer==null || viewer.getControl().isDisposed()) return;
		viewer.refresh();
	}

	private void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.setColumnProperties(new String[] { "Selected", "Name", "Original Plot", "Color" });

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Plot"); // Selected
		var.getColumn().setWidth(50);
		var.setLabelProvider(new HistoryLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new HistoryLabelProvider());
		
		var = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Original Plot");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new HistoryLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Shape");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new HistoryLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("Color");
		var.getColumn().setWidth(60);
		var.setLabelProvider(new HistoryLabelProvider());
		
	}
	

	private void createActions() {
		
		final MenuManager rightClick = new MenuManager();
		
		// Record, plot (tick in hisotry), delete, clear, refresh, rename
		final Action addPlot = new Action("Add plot(s) to history", Activator.getImageDescriptor("icons/add.png")) {
			public void run() {
				final Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
				if (traces==null||traces.isEmpty()) return;
				
				for (ITrace iTrace : traces) {
					final ILineTrace lineTrace = (ILineTrace)iTrace;
					final HistoryBean bean = new HistoryBean();
					bean.setXdata(lineTrace.getXData());
					bean.setYdata(lineTrace.getYData());
					bean.setTraceName(iTrace.getName());
					bean.setPlotColour(lineTrace.getTraceColor().getRGB());
					bean.setPlotName(getPlottingSystem().getPlotName());
					history.put(bean.getTraceKey(), bean);
				}
				refresh();
			}
		};
		getSite().getActionBars().getToolBarManager().add(addPlot);
		rightClick.add(addPlot);
		
		final Action deletePlot = new Action("Delete selected plot", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				final HistoryBean bean = getSelectedPlot();
				if (bean==null) return;
				bean.setSelected(false);
				updatePlot(bean);
				history.remove(bean.getTraceKey());
			    refresh();
			}
		};
		getSite().getActionBars().getToolBarManager().add(deletePlot);
		rightClick.add(deletePlot);
		
		final Menu menu = rightClick.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	@Override
	public Control getControl() {
		return this.composite;
	}

	@Override
	public void setFocus() {
        if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	
	@Override
	public void activate() {
		
        updatePlots();
        refresh();
        
        if (getPlottingSystem()!=null) {
        	getPlottingSystem().addTraceListener(this.traceListener);
        }
        super.activate();
	}
	
	@Override
	public void deactivate() {
        if (getPlottingSystem()!=null) {
        	getPlottingSystem().removeTraceListener(this.traceListener);
        }
		super.deactivate();
	}

	@Override
	public void dispose() {
	    if (viewer!=null&&!viewer.getControl().isDisposed()) {
	    	viewer.getControl().removeMouseListener(this);
	    }
	    deactivate();
		super.dispose();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button==1) toggleSelection();
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void toggleSelection() {
		final HistoryBean bean = getSelectedPlot();
		if (bean!=null) {
 			bean.setSelected(!bean.isSelected());
			viewer.refresh(bean);
			updatePlot(bean);
		}
	}

	private HistoryBean getSelectedPlot() {
		final StructuredSelection sel = (StructuredSelection)viewer.getSelection();
		if (sel.getFirstElement()!=null && sel.getFirstElement() instanceof HistoryBean) {
			return (HistoryBean)sel.getFirstElement();
		}
		return null;
	}

	private boolean updatingAPlotAlready = false;
    private boolean updatingPlotsAlready = false;
    
	private void updatePlots() {
		
		if (updatingPlotsAlready) return;
		try {
			updatingPlotsAlready = true;
			// Loop over history and reprocess it all.
			for (String key : history.keySet()) {
				updatePlot(history.get(key));
			}
		} finally {
			updatingPlotsAlready = false;
		}
	}
	
	/**
	 * Pushes history plot to and from the main plot depending on if it is selected.
	 */
	private void updatePlot(HistoryBean bean) {
		
		if (updatingAPlotAlready) return;
		try {
			updatingAPlotAlready = true;
			
			final boolean isSamePlot = getPlottingSystem().getPlotName()!=null && getPlottingSystem().getPlotName().equals(bean.getPlotName());		
			if (isSamePlot) {
				final String message = "Cannot update "+bean.getTraceName()+" from memory to plot in "+bean.getPlotName()+" as it comes from this plot originally!";
				logger.trace(message);
			    
				// User may be interested in this fact.
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, "org.dawb.workbench.plotting", message));
				final ITrace trace = getPlottingSystem().getTrace(bean.getTraceName());
				if (trace!=null) {
					bean.setPlotColour(((ILineTrace)trace).getTraceColor().getRGB());
					if (viewer!=null) viewer.refresh(bean);
					return;
				}
			}
			
			final String traceName = bean.createTraceName();
				
			if (!bean.isSelected()) {
				final ITrace trace = getPlottingSystem().getTrace(traceName);
				if (trace!=null) getPlottingSystem().removeTrace(trace);
			} else {
				
				if (getPlottingSystem().getTrace(traceName)!=null) {
					logger.warn("Cannot bring "+traceName+" from memory to plot in "+bean.getPlotName()+" as it already exists there!");
					return;
				} else {
					final ILineTrace trace = getPlottingSystem().createLineTrace(traceName);
					trace.setData(bean.getXdata(), bean.getYdata());
					getPlottingSystem().addTrace(trace);
					bean.setPlotColour(trace.getTraceColor().getRGB());
					if (viewer!=null) viewer.refresh(bean);
				}
			}
			getPlottingSystem().repaint();
		} finally {
			updatingAPlotAlready = false;
		}
	}

	private class HistoryLabelProvider extends ColumnLabelProvider {
		
		private Image checkedIcon;
		private Image uncheckedIcon;
		
		public HistoryLabelProvider() {
			
			ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
			checkedIcon   = id.createImage();
			id = Activator.getImageDescriptor("icons/unticked.gif");
			uncheckedIcon =  id.createImage();
		}
		
		private int columnIndex;
		public void update(ViewerCell cell) {
			columnIndex = cell.getColumnIndex();
			super.update(cell);
		}
		
		public Image getImage(Object element) {
			
			if (!(element instanceof HistoryBean)) return null;

			if (columnIndex==0) {
				final HistoryBean bean = (HistoryBean)element;
				return bean.isSelected() ? checkedIcon : uncheckedIcon;
			}
			
			return null;
		}
		
		public String getText(Object element) {
			
			if (element instanceof String) return "";
			
			final HistoryBean bean = (HistoryBean)element;
			if (columnIndex==1) {
			     return bean.getTraceName();
			}
			if (columnIndex==2) {
			     return bean.getPlotName();
			}
			if (columnIndex==3) {
			     return Arrays.toString(bean.getYdata().getShape());
			}
			if (columnIndex==4) {
			     return "-------";
			}
			return "";
		}
		
		public void dispose() {
			super.dispose();
			checkedIcon.dispose();
			uncheckedIcon.dispose();
		}
		
		private Color getColor(Object element) {
			if (!(element instanceof HistoryBean)) return null;
			if (columnIndex==4) {
				final HistoryBean bean = (HistoryBean)element;
				return new Color(null, bean.getPlotColour());
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return getColor(element);
		}

	}

}
