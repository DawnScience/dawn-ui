package org.dawb.workbench.plotting.tools.profile;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.views.ImageItem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class ImageTableTool extends AbstractToolPage  implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
	
	private   Table                  table;
	private   Composite              main;
	private   ITraceListener         traceListener;
	private   IRegionListener        regionListener;
	private   IPaletteListener       paletteListener;
	private   ProfileTableJob             updateProfiles;

	public ImageTableTool() {
		
		try {
			updateProfiles = new ProfileTableJob();
			
			this.paletteListener = new IPaletteListener.Stub() {
				@Override
				public void maskChanged(PaletteEvent evt) {
					update(null, null, false);
				}
			};
			
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
					ImageTableTool.this.update(null, null, false);
				}
				@Override
				protected void update(TraceEvent evt) {
					ImageTableTool.this.update(null, null, false);
				}

			};
			
			this.regionListener = new IRegionListener.Stub() {			
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(ImageTableTool.this);
					}
				}
				@Override
				public void regionAdded(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						ImageTableTool.this.update(null, null, false);
					}
				}
				
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().addROIListener(ImageTableTool.this);
					}
				}
				
				protected void update(RegionEvent evt) {
					ImageTableTool.this.update(null, null, false);
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	
	@Override
	public void createControl(Composite parent) {

        
		final IPageSite site = getSite();
		
		final Action reselect = new Action("Create new profile.", getImageDescriptor()) {
			public void run() {
				createNewRegion();
			}
		};
		site.getActionBars().getToolBarManager().add(reselect);
		site.getActionBars().getToolBarManager().add(new Separator());

		this.main = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		main.setLayout(gridLayout);

	}

	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		if (table!=null && !table.isDisposed()) table.setFocus();
	}
	
	public void activate() {
		super.activate();
		update(null, null, false);
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(regionListener);
		}	
		
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) iRegion.addROIListener(this);
		}
		
		// We try to listen to the image mask changing and reprofile if it does.
		if (getPlottingSystem()!=null) {
			if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
		}
		
		createNewRegion();
	}
	
	private void createNewRegion() {
		// Start with a selection of the right type
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Profile", getPlottingSystem()), getCreateRegionType());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
	}

	/**
	 * 
	 * @return
	 */
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.BOX;
	}
	
	/**
	 * 
	 */
    protected RegionType getCreateRegionType() {
    	return RegionType.BOX;
    }
    
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(regionListener);
		}
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) iRegion.removeROIListener(this);
		}
		if (getPlottingSystem()!=null) {
			if (getImageTrace()!=null) getImageTrace().removePaletteListener(paletteListener);
		}

	}
	
	@Override
	public Control getControl() {
		if (main==null || main.isDisposed()) return null;
		return main;
	}
	
	public void dispose() {
		deactivate();
		
		if (table!=null) table.dispose();
		super.dispose();
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected void createProfile(IImageTrace image, 
								IRegion region, 
								ROIBase rbs, 
								boolean tryUpdate, 
								boolean isDrag,
								IProgressMonitor monitor) {
		
		if (isDrag) return; // Table tool slow to be that live.
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (region.getRegionType()!=RegionType.BOX) return;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;

		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		
		try {
			final AbstractDataset slice = image.getData().getSlice(new int[] { (int) bounds.getPoint()[1],    (int) bounds.getPoint()[0] },
					                                               new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
					                                               new int[] {yInc, xInc});
		
			removeTable();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					updateTable(slice);
				}
			});

		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}

	}

	protected void updateTable(final AbstractDataset slice) {
		

		this.table = new Table(main, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER| SWT.VIRTUAL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		final int xSize = slice.getShape()[1];
		final int ySize = slice.getShape()[0];
		for (int i = 0; i < xSize; i++) {
			TableColumn col = new TableColumn(table, SWT.NONE, i);
			col.setWidth(80);
			col.setText(String.valueOf(i));
		}
		
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				
				TableItem line = (TableItem) event.item;
				int yIndex = table.indexOf(line);
				
				for (int x = 0; x < xSize; x++) {
				    final double    val  = slice.getDouble(yIndex, x);
				    line.setText(x, formatValue(val));
				}
				
			}
		});

		table.setItemCount(ySize);
		
		main.layout();
	}
	
	/**
	 * Thread safe
	 */
	private void removeTable() {
		if (table!=null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (!table.isDisposed()) {
						GridUtils.setVisible(table, false);
						table.dispose();
						main.layout();
					}
				}
			});
		}
	}

	private DecimalFormat format;
	
	private String formatValue(final double val) {
	    try {
	    	if (format==null) {
				final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
				String formatString = store.getString("data.format.editor.view");
				if (formatString==null) formatString = "#0.00";
		    	format = new DecimalFormat(formatString);
	    	}
			return format.format(val);
	    } catch (Exception ne) {
	    	logger.debug("Format does not work!", ne);
	    	return String.valueOf(val);
	    }
	}


	@Override
	public void roiDragged(ROIEvent evt) {
		update((IRegion)evt.getSource(), evt.getROI(), true);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		final IRegion region = (IRegion)evt.getSource();
		update(region, region.getROI(), false);
	}
	
	protected synchronized void update(IRegion r, ROIBase rb, boolean isDrag) {
	
		if (r!=null && !isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
         
		updateProfiles.profile(r, rb, isDrag);
	}
	
	private final class ProfileTableJob extends Job {
		
		private   IRegion                currentRegion;
		private   ROIBase                currentROI;
		private   boolean                isDrag;

		ProfileTableJob() {
			super("Profile update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, ROIBase rb, boolean isDrag) {

	        // This in principle is not needed and appears to make no difference wether in or out.
		    // However Irakli has advised that it is needed in some circumstances.
			// This causes the defect reported here however: http://jira.diamond.ac.uk/browse/DAWNSCI-214
			// therefore we are currently not using the extra cancelling.
	        //for (Job job : Job.getJobManager().find(null))
	        //    if (job.getClass()==getClass() && job.getState() != Job.RUNNING)
	        //	    job.cancel();

			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
	        
          	schedule();		
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			if (!isActive()) return Status.CANCEL_STATUS;

			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;

			if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
			if (image==null) {
				removeTable();
				return Status.OK_STATUS;
			}

			// Get the profiles from the line and box regions.
			if (currentRegion==null) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions!=null) {
					for (IRegion iRegion : regions) {
						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
						createProfile(image, iRegion, null, false, isDrag, monitor);
					}
				}
			} else {

				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
				createProfile(image, 
						currentRegion, 
						currentROI!=null?currentROI:currentRegion.getROI(), 
								true, 
								isDrag,
								monitor);

			}

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;

			return Status.OK_STATUS;

		}	
		
		
	}
	
	public class NoContentProvider implements IContentProvider {
		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	/**
	 * Tries to get the meta from the editor part or uses the one in AbtractDataset of the image
	 * @return IMetaData, may be null
	 */
	protected IMetaData getMetaData() {
		
		if (getPart() instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart)getPart();
	    	try {
				return LoaderFactory.getMetaData(EclipseUtils.getFilePath(editor.getEditorInput()), null);
			} catch (Exception e) {
				logger.error("Cannot get meta data for "+EclipseUtils.getFilePath(editor.getEditorInput()), e);
			}
		}
		
		return getImageTrace().getData().getMetadata();
	}
}
