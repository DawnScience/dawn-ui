package org.dawb.workbench.plotting.tools.history;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * Tool whereby images may be added to a static list which has various
 * operations. TODO Create a static method for adding images, for instance
 * from the Project Explorer.
 * 
 * @author fcp94556
 *
 */
public class ImageHistoryTool extends AbstractHistoryTool implements MouseListener {

	/**
	 * We simply keep the history in a static map of traces.
	 */
	private static Map<String, HistoryBean> imageHistory;
    static {
    	if (imageHistory==null) {
    		imageHistory = new LinkedHashMap<String, HistoryBean>(17);   		
     	}
    }
    
    protected Map<String, HistoryBean> getHistoryCache() {
    	return imageHistory;
    }
    
    /**
     * Assigned on the first activate and then kept to avoid
     * the wrong data being used as the base because the plot
     * will be changing.
     */
    private AbstractDataset originalData;
	private MathsJob        updateJob;
    
    public ImageHistoryTool() {
    	super();
    	this.updateJob = new MathsJob();
    }
	
	@Override
	public void activate() {
		
        if (getPlottingSystem()!=null && originalData==null) {        	
        	final IImageTrace imageTrace = getImageTrace();
        	this.originalData = imageTrace!=null ? imageTrace.getData() : null;
		}
		super.activate();
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	protected IAction createAddAction() {
		return new Action("Add image to compare table", Activator.getImageDescriptor("icons/add.png")) {
			public void run() {
				final Collection<ITrace> traces = getPlottingSystem().getTraces(IImageTrace.class);
				if (traces==null||traces.isEmpty()) return;
				
				// TODO Check if one of our history traces.
				for (ITrace iTrace : traces) {
					
					if (iTrace.getUserObject()==HistoryType.HISTORY_PLOT) continue;
					final IImageTrace imageTrace = (IImageTrace)iTrace;
					
					final HistoryBean bean = new HistoryBean();
					bean.setData(imageTrace.getData());
					bean.setAxes(imageTrace.getAxes());
					bean.setTraceName(iTrace.getName());
					bean.setPlotName(getPlottingSystem().getPlotName());
					bean.setOperator(ImageOperator.MULTIPLY);
					imageHistory.put(bean.getTraceKey(), bean);
				}
				refresh();
			}
		};	
	}
	
	// TODO Move up/down in list
	protected MenuManager createActions() {
        return super.createActions();
	}

	@Override
	protected void updatePlots() {
		updateJob.cancel();
		updateJob.schedule();
	}
	
	private class MathsJob extends Job {
		
		public MathsJob() {
			super("Process images");
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}
		
		public IStatus run(IProgressMonitor monitor) {

			// Loop over history and reprocess maths.
			AbstractDataset a = originalData!=null?createCopy(originalData):null;
			for (String key : imageHistory.keySet()) {
				
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				
				HistoryBean bean = imageHistory.get(key);
				if (bean==null) continue;
				if (a==null) { 
					if (bean.getData()==null) continue;
					a = createCopy(bean.getData());
					continue;
				}
				if (bean.isSelected()) {
					if (!a.isCompatibleWith(bean.getData())) {
						bean.setSelected(false);
						viewer.refresh(bean);
						continue;
					}
					bean.getOperator().process(a, bean.getData());
				}
			}

			if (a!=null) { // We plot it.
				final AbstractDataset plot = a;
				Display.getDefault().syncExec(new Runnable() {
					public void run () {
						getPlottingSystem().removeTraceListener(traceListener);
						try {
							final IImageTrace imageTrace = getImageTrace();
							getPlottingSystem().clear();
							final IImageTrace image = getPlottingSystem().createImageTrace(imageTrace!=null?imageTrace.getName():"Image");
							image.setData(plot, imageTrace!=null?imageTrace.getAxes():null, false);
							getPlottingSystem().addTrace(image);
						} finally {
							getPlottingSystem().addTraceListener(traceListener);
						}
					}
				});
			}

			return Status.OK_STATUS;
		}
		
	}
	
	private static AbstractDataset createCopy(final AbstractDataset a) {
		final int ia = a.getElementsPerItem();
		return a.clone().cast(false, a.getDtype(), ia);
	}

	
	@Override
	protected void updatePlot(HistoryBean bean) {
		updatePlots(); // We update everything when one changes.
	}

	@Override
	protected void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.setColumnProperties(new String[] { "Selected", "Name", "Original Plot", "Operator" });
		
		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Plot"); // Selected
		var.getColumn().setWidth(50);
		var.setLabelProvider(new ImageCompareLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ImageCompareLabelProvider());
		var.setEditingSupport(new ImageNameEditingSupport(viewer));
		
		var = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Original File");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ImageCompareLabelProvider());
		
		var = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Operator");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new ImageCompareLabelProvider());
		var.setEditingSupport(new ImageOperatorEditingSupport(viewer));
		
		var = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("Shape");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new ImageCompareLabelProvider());

	}
	
	private class ImageCompareLabelProvider extends ColumnLabelProvider {
		
		private Image checkedIcon;
		private Image uncheckedIcon;
		
		public ImageCompareLabelProvider() {
			
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
			     return bean.getOperator().getName();
			}
			if (columnIndex==4) {
				try {
			        return Arrays.toString(bean.getData().getShape());
				} catch (Throwable ne) {
					return "";
				}
			}
			return "";
		}
		
		public Color getForeground(Object element) {
			if (!(element instanceof HistoryBean)) return null;
			HistoryBean bean = (HistoryBean)element;
			
			if (columnIndex==4&&!isShapeCompatible(element)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			}
			
			return bean.isSelected() 
				   ? Display.getDefault().getSystemColor(SWT.COLOR_BLACK)
				   : Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		
		public String getToolTipText(Object element) {
			if (!isShapeCompatible(element)) return "Shape of compare image is not the same as the plot.";
			return super.getToolTipText(element);
		}

		private boolean isShapeCompatible(Object element) {
			if (originalData==null) return true;
			if (!(element instanceof HistoryBean)) return true;
			HistoryBean bean = (HistoryBean)element;
			return originalData.isCompatibleWith(bean.getData());
		}

		public void dispose() {
			super.dispose();
			checkedIcon.dispose();
			uncheckedIcon.dispose();
		}
	}
	
	private class ImageNameEditingSupport extends EditingSupport {

		public ImageNameEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Composite)getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((HistoryBean)element).getTraceName();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((HistoryBean)element).setTraceName((String)value);
			viewer.refresh(element);
		}

	}

	private class ImageOperatorEditingSupport extends EditingSupport {

		public ImageOperatorEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			ComboBoxCellEditor ed = new ComboBoxCellEditor((Composite)getViewer().getControl(), ImageOperator.getOperators(), SWT.READ_ONLY);
		
			((CCombo)ed.getControl()).addSelectionListener(new SelectionListener() {			
				@Override
				public void widgetSelected(SelectionEvent e) {
					ImageOperatorEditingSupport.this.setValue(element, ((CCombo)e.getSource()).getSelectionIndex());
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
			return ed;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((HistoryBean)element).getOperator().getIndex();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((HistoryBean)element).setOperator(ImageOperator.getOperator((Integer)value));
			((HistoryBean)element).setSelected(true);
			viewer.refresh(element);
			updatePlots();
		}

	}


}
