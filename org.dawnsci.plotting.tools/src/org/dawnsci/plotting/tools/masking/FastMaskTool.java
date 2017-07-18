package org.dawnsci.plotting.tools.masking;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.plotting.roi.ROIEditTable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.mask.MaskCircularBuffer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FastMaskTool extends AbstractToolPage {

	private Composite control;
	private MaskCircularBuffer buffer;
	private List<NamedRegionType> regionTypes;
	private FastMaskJob job;
	private boolean paintMode = false;
	private AtomicReference<Double> lowerValue;
	private AtomicReference<Double> upperValue;
	
	private MaskRegionComposite maskRegionComposite;
	
	private IROIListener iroiListener;
	
	
	public FastMaskTool() {
		regionTypes = new ArrayList<>();
		regionTypes.add(new NamedRegionType("Box", RegionType.BOX));
		regionTypes.add(new NamedRegionType("Sector", RegionType.SECTOR));
		regionTypes.add(new NamedRegionType("Polygon", RegionType.POLYGON));
		regionTypes.add(new NamedRegionType("X-Axis", RegionType.XAXIS));
		regionTypes.add(new NamedRegionType("Y-Axis", RegionType.YAXIS));
		regionTypes.add(new NamedRegionType("Ring", RegionType.RING));
		regionTypes.add(new NamedRegionType("Circle", RegionType.CIRCLE));
		lowerValue = new AtomicReference<Double>(null);
		upperValue = new AtomicReference<Double>(null);
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
		
		maskRegionComposite = new MaskRegionComposite(control, SWT.None);
		maskRegionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		ThresholdMaskComposite thresholdMaskComposite = new ThresholdMaskComposite(control, SWT.None);
		thresholdMaskComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button b3b = new Button(control, SWT.PUSH);
		b3b.setText("Invert");
		b3b.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buffer == null) return;
				
				Runnable r = () -> buffer.invert();
				
				job.setRunnable(r);
				job.schedule();
			}


		});
		
		Button b2 = new Button(control, SWT.PUSH);
		b2.setText("Undo");
		b2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buffer == null) return;
				Runnable r = () -> buffer.undo();
				
				job.setRunnable(r);
				job.schedule();
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
					logger.error("Could not open wizard",e1);
				}
				
			}


		});
		
		
		Button b3a = new Button(control, SWT.PUSH);
		b3a.setText("Clear");
		b3a.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (buffer == null) return;
				
				Runnable r = () -> buffer.clear();
				
				job.setRunnable(r);
				job.schedule();
			}


		});
		
		getPlottingSystem().addRegionListener(new IRegionListener() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				Collection<IRegion> regions = evt.getRegions();
				for (IRegion r : regions) r.removeROIListener(getROIListener());
				
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				evt.getRegion().removeROIListener(getROIListener());
				
			}
			
			@Override
			public void regionNameChanged(RegionEvent evt, String oldName) {
				
			}
			
			@Override
			public void regionCreated(RegionEvent evt) {
				
			}
			
			@Override
			public void regionCancelled(RegionEvent evt) {
				
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (paintMode) evt.getRegion().addROIListener(getROIListener());
				
				IRegion region = (IRegion)evt.getSource();
				maskRegionComposite.setROI(evt.getRegion().getROI(), region);
				
				evt.getRegion().addROIListener(new IROIListener.Stub() {
					@Override
					public void roiSelected(ROIEvent evt) {
						IRegion region = (IRegion)evt.getSource();
						maskRegionComposite.setROI(evt.getROI(), region);
					}
					
					@Override
					public void roiChanged(ROIEvent evt) {
						IRegion region = (IRegion)evt.getSource();
						maskRegionComposite.setROI(evt.getROI(),region);

					}
					
				});
				
			}
		});
	}
	
	private IROIListener getROIListener() {
		if (iroiListener != null) {
			return iroiListener;
		}
		
		iroiListener = new IROIListener(){

			@Override
			public void roiDragged(ROIEvent evt) {
				
				IImageTrace imageTrace = getImageTrace();
				if (imageTrace != null) {
					IDataset data = imageTrace.getData();

					final IROI roi = evt.getROI();

					if (buffer == null) buffer = new MaskCircularBuffer(data.getShape());
					Runnable r = () -> buffer.maskROI(roi);

					job.setRunnable(r);
					job.schedule();
				}

			}

			@Override
			public void roiChanged(ROIEvent evt) {
				//Do nothing
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				//do nothing
			}

		};
		
		return iroiListener;
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
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
			
			BooleanDataset mask = buffer.getMask();
			
			Double lower = lowerValue.get();
			Double upper = upperValue.get();
			
			if (lower != null || upper != null) {
				includeThreshold(mask, lower, upper, DatasetUtils.convertToDataset(getImageTrace().getData()));
			}
			
			Runnable rDisplay = () -> getImageTrace().setMask(mask);
			
			Display.getDefault().syncExec(rDisplay);

			return Status.OK_STATUS;
		}
		
		private void includeThreshold(BooleanDataset mask, Double lower, Double upper, Dataset data) {
			if (!Arrays.equals(mask.getShape(), data.getShape())) throw new IllegalArgumentException("must have same shape");
			
			
			int count = 0;
			
			IndexIterator iterator = data.getIterator();
			
			while (iterator.hasNext()) {
				double element = data.getElementDoubleAbs(iterator.index);
				
				if (lower != null && element < lower) {
					mask.setAbs(count, false);
				}
				
				if (upper != null && element > upper) {
					mask.setAbs(count, false);
				}
				
				count++;
			}
		}
		
	}
	
	private class ThresholdMaskComposite extends Composite {

		public ThresholdMaskComposite(Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new FillLayout());
			Group group = new Group(this, SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			group.setText("Threshold");
			
			
			Label ll = new Label(group, SWT.NONE);
			ll.setText("Lower");
			Text lower = new Text(group,SWT.BORDER);
			lower.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			lower.addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e) {
					
					update(lowerValue,e,lower.getText());

				}
			});
			
			Label lu = new Label(group, SWT.NONE);
			lu.setText("Upper");
			Text upper = new Text(group,SWT.BORDER);
			upper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			upper.addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e) {
					
					update(upperValue,e,upper.getText());

				}
			});
		}
		
		private void update(AtomicReference<Double> value, TraverseEvent e, String text) {
			if (e.detail != SWT.TRAVERSE_RETURN && e.detail != SWT.TRAVERSE_TAB_NEXT) return;
			
			if (buffer == null) {
				IImageTrace imageTrace = getImageTrace();
				if (imageTrace == null) return;
				
				IDataset data = imageTrace.getData();
				buffer = new MaskCircularBuffer(data.getShape());
				
			}
			
			Double val = null;
			
			try{
				val = Double.parseDouble(text);
			} catch (Exception ex) {
				//ignore
			}
			
			value.set(val);
			//Does nothing
			Runnable r = () -> {};

			job.setRunnable(r);
			job.schedule();
		}
		
	}
	
	private class MaskRegionComposite extends Composite {

		private ROIEditTable roiEditTable;
		private AtomicReference<IRegion> regionRef;
		private IROIListener roiListener;
		
		public MaskRegionComposite(Composite parent, int style) {
			super(parent, style);
			regionRef = new AtomicReference<IRegion>(null);
			roiListener = new IROIListener.Stub() {			
				@Override
				public void roiChanged(ROIEvent evt) {
					IRegion iRegion = regionRef.get();
					IROI roi = evt.getROI();
					if (iRegion != null) {
						try {
							iRegion.setROI(roi);
						} catch (Exception e) {
							logger.debug("error setting roi",e);
						}
					}
					
				}
			};
			this.setLayout(new FillLayout());
			Group group = new Group(this, SWT.NONE);
			group.setLayout(new GridLayout(2, false));
			group.setText("Draw Regions");
			
			Combo combo = new Combo(group, SWT.READ_ONLY);
			combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			String[] array = regionTypes.stream().map(r -> r.name).toArray(String[]::new);
			combo.setItems(array);
			combo.select(0);
			
			Button draw = new Button(group, SWT.PUSH);
			draw.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			draw.setText("Draw");
			draw.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					String text = combo.getText();
					Optional<NamedRegionType> findFirst = regionTypes.stream().filter(r -> text.equals(r.name)).findFirst();
					if (findFirst.isPresent()) {
						NamedRegionType nrt = findFirst.get();
						IPlottingSystem<?> system = getPlottingSystem();
						try {
							system.createRegion(RegionUtils.getUniqueName("MaskRegion", system, (String)null), nrt.regionType);
						} catch (Exception e1) {
							logger.error("Could not create region!",e1);
						}
					}
					
				}
				

			});
			
			
			
			Button b = new Button(group, SWT.PUSH);
			b.setText("Apply");
			b.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					IImageTrace imageTrace = getImageTrace();
					if (imageTrace != null) {
						IDataset data = imageTrace.getData();
						if (buffer == null) buffer = new MaskCircularBuffer(data.getShape());
						final Collection<IRegion> regions = getPlottingSystem().getRegions();
						
						Runnable r = () -> {for (IRegion re: regions) {
							buffer.maskROI(re.getROI());
							};
						};
							
						job.setRunnable(r);
						job.schedule();
					}
					
				}
				

			});
			
			Button b4 = new Button(group, SWT.CHECK);
			b4.setText("Paint on region drag");
			b4.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					paintMode = b4.getSelection();

						Collection<IRegion> regions = getPlottingSystem().getRegions();
						IROIListener roiListener = getROIListener();
						for (IRegion next : regions) {

							if (paintMode) {
								next.addROIListener(roiListener);
							} else {
								next.removeROIListener(iroiListener);
							}
						}
					
				}


			});
			
			roiEditTable = new ROIEditTable();
			roiEditTable.createPartControl(group);
			roiEditTable.addROIListener(roiListener);
			
		}
		
		public void setROI(IROI roi, final IRegion region){
			regionRef.set(region);
			roiEditTable.setRegion(roi, region.getRegionType(), region.getCoordinateSystem());
			roiEditTable.getTableViewer().refresh();
//			this.roiViewer.setRegion(region.getROI(), region.getRegionType(), region.getCoordinateSystem());

		}
		
	}
}
