package org.dawnsci.plotting.tools.masking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FastMaskTool extends AbstractToolPage {

	private Composite control;
	private MaskCircularBuffer buffer;
	private List<NamedRegionType> regionTypes;
	private FastMaskJob job;
	
	
	
	public FastMaskTool() {
		regionTypes = new ArrayList<>();
		regionTypes.add(new NamedRegionType("Box", RegionType.BOX));
		regionTypes.add(new NamedRegionType("Sector", RegionType.SECTOR));
		regionTypes.add(new NamedRegionType("Polygon", RegionType.POLYGON));
		regionTypes.add(new NamedRegionType("X-Axis", RegionType.XAXIS));
		regionTypes.add(new NamedRegionType("Y-Axis", RegionType.YAXIS));
		regionTypes.add(new NamedRegionType("Ring", RegionType.RING));
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		this.job = new FastMaskJob();
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		removeMargins(control);
		
		Combo combo = new Combo(control, SWT.READ_ONLY);
		String[] array = regionTypes.stream().map(r -> r.name).toArray(String[]::new);
		combo.setItems(array);
		combo.select(0);
		combo.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = combo.getText();
				Optional<NamedRegionType> findFirst = regionTypes.stream().filter(r -> text.equals(r.name)).findFirst();
				if (findFirst.isPresent()) {
					NamedRegionType nrt = findFirst.get();
					IPlottingSystem system = getPlottingSystem();
					try {
						system.createRegion(RegionUtils.getUniqueName("MaskRegion", system, null), nrt.regionType);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		Button b = new Button(control, SWT.PUSH);
		b.setText("Apply");
		b.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IImageTrace imageTrace = getImageTrace();
				if (imageTrace != null) {
					IDataset data = imageTrace.getData();
					if (buffer == null) buffer = new MaskCircularBuffer(data.getShape());
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					
					Runnable r = new Runnable() {
						
						@Override
						public void run() {
							for (IRegion r : regions) buffer.maskROI(r.getROI());
							
						}
					};
					
					job.setRunnable(r);
					job.schedule();
				}
				
			}
			

		});
		
		Button b2 = new Button(control, SWT.PUSH);
		b2.setText("Undo");
		b2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				buffer.undo();
				IImageTrace imageTrace = getImageTrace();
				imageTrace.setMask(buffer.getMask());
			}


		});
		
		Button b3 = new Button(control, SWT.PUSH);
		b3.setText("Save");
		b3.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}


		});
		
		Button b4 = new Button(control, SWT.CHECK);
		b4.setText("Draw");
		b4.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (b4.getSelection() == true) {
					Collection<IRegion> regions = getPlottingSystem().getRegions();
					IRegion next = regions.iterator().next();
					next.addROIListener(new IROIListener(){

						@Override
						public void roiDragged(ROIEvent evt) {
							IImageTrace imageTrace = getImageTrace();
							if (imageTrace != null) {
								IDataset data = imageTrace.getData();

								if (buffer == null) buffer = new MaskCircularBuffer(data.getShape());
								Runnable r = new Runnable() {
									
									@Override
									public void run() {
										for (IRegion r : regions) buffer.maskROI(r.getROI());
										
									}
								};
								
								job.setRunnable(r);
								job.schedule();
							}

						}

						@Override
						public void roiChanged(ROIEvent evt) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void roiSelected(ROIEvent evt) {
							// TODO Auto-generated method stub
							
						}
						
					});
				}
			}


		});
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	@Override
	public void activate() {
		super.activate();
		// Now add any listeners to the plotting providing getPlottingSystem()!=null
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		// Now remove any listeners to the plotting providing getPlottingSystem()!=null
	}
	@Override
	public void dispose() {
		super.dispose();
        // Anything to kill off? This page is part of a view which is now disposed and will not be used again.
	}
	
	private static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;
	}

	private class NamedRegionType {
		public String name;
		public RegionType regionType;
		
		public NamedRegionType(String name, RegionType regionType) {
			this.name = name;
			this.regionType = regionType;
		}
	}
	
	private class FastMaskJob extends Job {

		private Runnable runnable;
		int count = 0;
		
		public FastMaskJob() {
			super("Apply Mask");
		}
		
		public void setRunnable(Runnable r) {
			this.runnable = r;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable r = runnable;
			r.run();
			count++;
			
			if (count > 10) {
				
			count = 0;
			
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					long t = System.currentTimeMillis();
					getImageTrace().setMask(buffer.getMask());
					System.out.println(System.currentTimeMillis() - t);
				}
			});
			}
			return Status.OK_STATUS;
		}
		
	}
}
