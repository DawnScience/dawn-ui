package org.dawb.workbench.plotting.tools;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ImageServiceBean.HistogramBound;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;

public class MaskingTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(MaskingTool.class);
	
	private Group           composite;
	private Spinner         minimum, maximum;
	private Button          autoApply;
	private MaskCreator     maskCreator;
	private ITraceListener  traceListener;
	private IRegionListener regionListener;
	private IROIListener    regionBoundsListener;
	private MaskJob         maskJob;

	private TableViewer regionTable;
	
	public MaskingTool() {
		
		this.maskCreator   = new MaskCreator();
		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void traceAdded(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) {
					processMask(true); // New image new process.
				}
			}
		};
		
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				evt.getRegion().addROIListener(regionBoundsListener);
				processMask(false, evt.getRegion());
				regionTable.refresh();
			}			
			@Override
			public void regionRemoved(RegionEvent evt) {
				evt.getRegion().removeROIListener(regionBoundsListener);
				processMask(false);
				regionTable.refresh();
			}			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				processMask(false);
				regionTable.refresh();
			}
		};
		
		this.regionBoundsListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				processMask(false, (IRegion)evt.getSource());
			}
		};
		
		this.maskJob = new MaskJob();
		maskJob.setSystem(true);
		maskJob.setUser(false);
		maskJob.setPriority(Job.INTERACTIVE);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Group(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		final IImageTrace image = getImageTrace();
		if (image!=null) {
			composite.setText("Masking '"+image.getName()+"'");
		} else {
			composite.setText("Masking ");
		}
		

		final Composite minMaxComp = new Composite(composite, SWT.NONE);
		minMaxComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		minMaxComp.setLayout(new GridLayout(2, false));
		
		Label label = new Label(minMaxComp, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,1));
		label.setText("Apply a mask to the original data. "+
		              "The mask is saved and available in other tools.");	
		
		// Max and min
		
		final Button minEnabled =  new Button(minMaxComp, SWT.CHECK);
		minEnabled.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		minEnabled.setText("Enable lower mask    ");
		minEnabled.setToolTipText("Enable the lower bound mask, removing pixels with lower intensity.");
		minEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				minimum.setEnabled(minEnabled.getSelection());
				processMask(true);
			}
		});
		
		this.minimum = new Spinner(minMaxComp, SWT.NONE);
		minimum.setEnabled(false);
		minimum.setMinimum(Integer.MIN_VALUE);
		minimum.setMaximum(Integer.MAX_VALUE);
		minimum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (image!=null) minimum.setSelection(getValue(image.getMin(), image.getMinCut(), 0));
		minimum.setToolTipText("Press enter to apply a full update of the mask.");
		minimum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				processMask(false);
			}
		});
		minimum.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character=='\n' || e.character=='\r') {
					processMask(true);
				}
			}
		});
	
		
		final Button maxEnabled =  new Button(minMaxComp, SWT.CHECK);
		maxEnabled.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		maxEnabled.setText("Enable upper mask    ");
		maxEnabled.setToolTipText("Enable the upper bound mask, removing pixels with higher intensity.");
		maxEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				maximum.setEnabled(maxEnabled.getSelection());
				processMask(true);
			}
		});
		
		this.maximum = new Spinner(minMaxComp, SWT.NONE);
		maximum.setEnabled(false);
		maximum.setMinimum(Integer.MIN_VALUE);
		maximum.setMaximum(Integer.MAX_VALUE);
		maximum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (image!=null) maximum.setSelection(getValue(image.getMax(), image.getMaxCut(), Integer.MAX_VALUE));
		maximum.setToolTipText("Press enter to apply a full update of the mask.");
		maximum.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				processMask(false);
			}
		});
		maximum.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character=='\n' || e.character=='\r') {
					processMask(true);
				}
			}
		});
		
		
		label = new Label(minMaxComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,1));
		label.setText("Mask Color");
		
		final ColorSelector selector = new ColorSelector(minMaxComp);
		selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		if (image!=null) selector.setColorValue(image.getNanBound().getColor());
		selector.addListener(new IPropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				getImageTrace().setNanBound(new HistogramBound(Double.NaN, selector.getColorValue()));
				processMask(true);
			}
		});
		
		
		// Regions
		final Composite        regionComp = new Composite(composite, SWT.NONE);
		regionComp.setLayout(new GridLayout(1, false));
		regionComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		label = new Label(regionComp, SWT.HORIZONTAL|SWT.SEPARATOR);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		final ToolBarManager   man        = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		fillMaskingRegionActions(man);
		final Control          tb         = man.createControl(regionComp);
		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		this.regionTable = new TableViewer(regionComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		regionTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(regionTable);
		regionTable.getTable().setLinesVisible(true);
		regionTable.getTable().setHeaderVisible(true);
		
		getSite().setSelectionProvider(regionTable);
		regionTable.setContentProvider(new IStructuredContentProvider() {			
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
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
				final List<IRegion> supported = new ArrayList<IRegion>(regions.size());
				for (IRegion iRegion : regions) if (maskCreator.isSupportedRegion(iRegion)) {
					supported.add(iRegion);
				}
				return supported.toArray(new IRegion[supported.size()]);
			}
		});
		regionTable.setInput(new Object());
		
		final Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		this.autoApply     = new Button(buttons, SWT.CHECK);
		autoApply.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		autoApply.setText("Automatically apply mask when something changes.");
		autoApply.setSelection(false);
		
		final Button apply = new Button(buttons, SWT.PUSH);
		apply.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		apply.setImage(Activator.getImage("icons/apply.gif"));
		apply.setText("Apply");
		apply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				processMask(true);
			}
		});
		apply.setEnabled(true);
		
		autoApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				apply.setEnabled(!autoApply.getSelection()); 
				processMask(false);
			}
		});
		
		final Button reset = new Button(buttons, SWT.PUSH);
		reset.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		reset.setImage(Activator.getImage("icons/reset.gif"));
		reset.setText("Reset");
		reset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetMask();
			}
		});
		
	}

	private void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Region Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MaskingLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Include in mask");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new MaskingLabelProvider());
		var.setEditingSupport(new MaskingEditingSupport(viewer));
		
	}
	
	private Map<String,Boolean> maskedRegions = new HashMap<String,Boolean>(7);
	
	private class MaskingEditingSupport extends EditingSupport {

		public MaskingEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(composite);
		}

		@Override
		protected boolean canEdit(Object element) {
			return element instanceof IRegion;
		}

		@Override
		protected Object getValue(Object element) {
			if (!(element instanceof IRegion)) return null;
			final IRegion region = (IRegion)element;
			if (maskedRegions.containsKey(region.getName())) {
				return maskedRegions.get(region.getName());
			} else {
			    return Boolean.TRUE;
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (!(element instanceof IRegion)) return;
			final IRegion region = (IRegion)element;
			maskedRegions.put(region.getName(), (Boolean)value);
		}

	}
	
	private class MaskingLabelProvider extends ColumnLabelProvider {
	
		private int col;
		public void update(ViewerCell cell) {
			col = cell.getColumnIndex();
			super.update(cell);
		}
		
		public String getText(Object element) {
			
			if (element instanceof String) return "";
			
			final IRegion region = (IRegion)element;
			switch(col) {
			case 0:
			return region.getName();
			case 1:
			return "true";
			}
			return "";
		}

	}

	private void fillMaskingRegionActions(IToolBarManager man) {
		
		final ActionContributionItem menu  = (ActionContributionItem)getPlottingSystem().getActionBars().getToolBarManager().find("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
		final MenuAction        menuAction = (MenuAction)menu.getAction();
		
		final RegionType[] regions = new RegionType[]{RegionType.LINE, RegionType.BOX, RegionType.POLYLINE, RegionType.FREE_DRAW, RegionType.XAXIS, RegionType.YAXIS};
		for (RegionType type : regions) {
			final IAction action = menuAction.findAction(type.getId());
			man.add(action);
		}
	}

	private int getValue(Number bound, HistogramBound hb, int defaultInt) {
        if (bound!=null) return bound.intValue();
        if (hb!=null && hb.getBound()!=null) {
        	if (!Double.isInfinite(hb.getBound().doubleValue())) {
        		return hb.getBound().intValue();
        	}
        }
        return defaultInt;
	}

	private void processMask(boolean forceProcess) {
        processMask(forceProcess, null);
	}
	
	/**
	 * Either adds a new region directly or 
	 * @param forceProcess
	 * @param roi
	 * @return true if did some masking
	 */
	private void processMask(final boolean forceProcess, final IRegion region) {
		
		if (!forceProcess && !autoApply.getSelection()) return;
		
		final IImageTrace image = getImageTrace();
		if (image == null) return;
		
		maskJob.schedule(forceProcess, region);
	}
	
	protected void resetMask() { // Reread the file from disk or cached one if this is a view
		
		final IImageTrace image = getImageTrace();
		if (image==null) return;
		
	    image.setMask(null);
	}
		
	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		if (composite!=null) composite.setFocus();
	}
	
	@Override
	public void activate() {
		super.activate();
		
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener); // If it changes get reference to image.
			
			getPlottingSystem().addRegionListener(regionListener); // Listen to new regions
			
			// For all supported regions, add listener for rois
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion region : regions) {
				if (!maskCreator.isSupportedRegion(region)) continue;
				region.addROIListener(this.regionBoundsListener);
			}
		}
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
			
			getPlottingSystem().removeRegionListener(regionListener);// Stop listen to new regions
			
			// For all supported regions, add listener
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion region : regions)  region.removeROIListener(this.regionBoundsListener);
		}
	}
	
	@Override
	public void dispose() {
		deactivate();
		super.dispose();
		if (composite!=null) composite.dispose();
		composite      = null;
		traceListener  = null;
		regionListener = null;
		regionBoundsListener = null;
	}

	
	public class MaskJob extends Job {

		public MaskJob() {
			super("Masking image");
		}

		private boolean forceProcess = false;
		private IRegion region       = null;
		private Integer min=null,max=null;
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
						
			final IImageTrace image = getImageTrace();
			if (image == null) return Status.CANCEL_STATUS;
			
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			
			if (maskCreator.getMaskDataset()==null || region==null || forceProcess) {
				// The mask must be maintained as a BooleanDataset so that there is the option
				// of applying the same mask to many images.
				final AbstractDataset unmasked = image.getData();
				maskCreator.setMaskDataset(new BooleanDataset(unmasked.getShape()));
				maskCreator.setImageDataset(unmasked);
			}
			
			if (maskCreator.getImageDataset()==null) {
				final AbstractDataset unmasked = image.getData();
				maskCreator.setImageDataset(unmasked);
			}
			
			// Just process a changing region
			if (region!=null && !forceProcess) {
				if (!maskCreator.isSupportedRegion(region)) return Status.CANCEL_STATUS;
				maskCreator.processRegion(region);
				
				
			} else { // process everything
				
				maskCreator.processBounds(min, max);
				
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion r : regions) {
					if (!maskCreator.isSupportedRegion(r)) continue;
					maskCreator.processRegion(r);
				}
			}
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					// NOTE the mask will have a reference kept and
					// will downsample with the data.
					image.setMask(maskCreator.getMaskDataset()); 
				}
			});
			
			return Status.OK_STATUS;
		}

		public void schedule(boolean force, IRegion region) {
			cancel(); // should stop the queue getting too large.
			this.forceProcess = force;
			this.region       = region;
			min = (minimum.isEnabled()) ? minimum.getSelection() : null;
		    max = (maximum.isEnabled()) ? maximum.getSelection() : null;
			super.schedule(5);
		}
	}

}
