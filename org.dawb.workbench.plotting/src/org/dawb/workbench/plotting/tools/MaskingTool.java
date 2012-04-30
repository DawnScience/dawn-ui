package org.dawb.workbench.plotting.tools;


import java.util.Collection;

import org.dawb.common.services.ImageServiceBean.HistogramBound;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
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
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
	
	private Composite       composite;
	private Spinner         minimum, maximum;
	private Button          autoApply;
	private MaskCreator     maskCreator;
	private ITraceListener  traceListener;
	private IRegionListener regionListener;
	private IROIListener    regionBoundsListener;
	private MaskJob         maskJob;
	
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
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		final IImageTrace image = getImageTrace();

		final Group masking = new Group(composite, SWT.NONE);
		masking.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		masking.setLayout(new GridLayout(2, false));
		if (image!=null) {
			masking.setText("Masking '"+image.getName()+"'");
		} else {
			masking.setText("Masking ");
		}
		
		Label label = new Label(masking, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,1));
		label.setText("Apply a mask to the original data. "+
		              "The mask changes the data of the image permanently (the mask may be reset).");	
		
		final Button lowerEnabled =  new Button(masking, SWT.CHECK);
		lowerEnabled.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		lowerEnabled.setText("Enable lower mask    ");
		lowerEnabled.setToolTipText("Enable the lower bound mask, removing pixels with lower intensity.");
		lowerEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				minimum.setEnabled(lowerEnabled.getSelection());
				processMask(true);
			}
		});
		
		this.minimum = new Spinner(masking, SWT.NONE);
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
	
		
		final Button upperEnabled =  new Button(masking, SWT.CHECK);
		upperEnabled.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		upperEnabled.setText("Enable upper mask    ");
		upperEnabled.setToolTipText("Enable the upper bound mask, removing pixels with higher intensity.");
		upperEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				maximum.setEnabled(upperEnabled.getSelection());
				processMask(true);
			}
		});
		
		this.maximum = new Spinner(masking, SWT.NONE);
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
		
		
		label = new Label(masking, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,1));
		label.setText("Mask Color");
		
		final ColorSelector selector = new ColorSelector(masking);
		selector.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		if (image!=null) selector.setColorValue(image.getNanBound().getColor());
		selector.addListener(new IPropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				getImageTrace().setNanBound(new HistogramBound(Double.NaN, selector.getColorValue()));
				processMask(true);
			}
		});
		
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
