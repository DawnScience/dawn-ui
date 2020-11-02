package org.dawnsci.plotting.tools.masking;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.plotting.roi.ROIEditTable;
import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.mask.MaskCircularBuffer;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;

public class FastMaskTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(FastMaskTool.class);

	public enum MaskRegionDragMode {
		NO_ACTION("No Drag Action"),PAINT_ON_RELEASE("Paint on Release"),PAINT_ON_DRAG("Paint on Drag");
		
		private String name;
		
		private MaskRegionDragMode(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private Composite control;
	private MaskCircularBuffer buffer;
	private List<NamedRegionType> regionTypes;
	private FastMaskJob job;
	private MaskRegionDragMode maskRegionDragMode = MaskRegionDragMode.NO_ACTION;
	private AtomicReference<Double> lowerValue;
	private AtomicReference<Double> upperValue;
	
	private IDiffractionMetadata metadata;
	private String lastPath = null;
	
	private MaskOnClickListener pixelMaskListener;
	
	private MaskRegionComposite maskRegionComposite;
	
	private IROIListener iroiListener;
	
	
	public FastMaskTool() {
		regionTypes = new ArrayList<>();
		regionTypes.add(new NamedRegionType("Box", RegionType.BOX));
		regionTypes.add(new NamedRegionType("Line", RegionType.LINE));
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
		control.setLayout(GridLayoutFactory.fillDefaults().create());
		
		maskRegionComposite = new MaskRegionComposite(control, SWT.None);
		maskRegionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final TabFolder tabFolder = new TabFolder(control, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		
		ThresholdMaskComposite thresholdMaskComposite = new ThresholdMaskComposite(tabFolder, SWT.None);
		thresholdMaskComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		tabItem.setControl(thresholdMaskComposite);
		tabItem.setText("Threshold");
		
		DiffractionComposite difComposite = new DiffractionComposite(tabFolder, SWT.None);
		difComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setControl(difComposite);
		tabItem.setText("Detector Geometry");
		
		ImageComposite imageProcessingComposite = new ImageComposite(tabFolder, SWT.None);
		imageProcessingComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setControl(imageProcessingComposite);
		tabItem.setText("Mask Processing");
		
		UtilitiesComposite utilitiesComposite = new UtilitiesComposite(control, SWT.NONE);
		utilitiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
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
				if (!maskRegionDragMode.equals(MaskRegionDragMode.NO_ACTION)) evt.getRegion().addROIListener(getROIListener());
				
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
		
		super.createControl(parent);
	}
	
	private IROIListener getROIListener() {
		if (iroiListener != null) {
			return iroiListener;
		}
		
		iroiListener = new IROIListener(){

			@Override
			public void roiDragged(ROIEvent evt) {
				
				if (maskRegionDragMode.equals(MaskRegionDragMode.PAINT_ON_DRAG)) {
					paintRegion(evt);
				}
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				if (maskRegionDragMode.equals(MaskRegionDragMode.PAINT_ON_RELEASE)) {
					paintRegion(evt);
				}
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				//do nothing
			}
			
			private void paintRegion(ROIEvent evt) {

				if (buffer == null && !buildBuffer()) {
					return;
				}

				IROI roi = evt.getROI();

				int thickness = maskRegionComposite.getLineThickness();

				if (roi instanceof LinearROI && thickness != 1) {

					roi = FastMaskTool.this.buildThickLine((LinearROI)roi, thickness);
				}

				final IROI froi = roi;

				Runnable r = () -> buffer.maskROI(froi);

				job.setRunnable(r);
				job.schedule();
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
	
	@Override
	public void deactivate() {
		if (pixelMaskListener != null) {
			IPlottingSystem<Object> plottingSystem = getPlottingSystem();
			if (plottingSystem != null) {
				plottingSystem.removeClickListener(pixelMaskListener);
				pixelMaskListener = null;
			}
		}
		
		if (maskRegionComposite != null) maskRegionComposite.removeListeners();
		
		IROIListener roiListener = getROIListener();
		IPlottingSystem<?> ps = getPlottingSystem();
		if (ps == null) return;
		Collection<IRegion> regions = ps.getRegions();
		if (regions == null) return;
		
		for (IRegion r : regions) {
			r.removeROIListener(roiListener);
		}
		
	}
	
	private String getFileName(){
		FileSelectionDialog dialog = new FileSelectionDialog(getPart().getSite().getShell());
		dialog.setExtensions(new String[]{"nxs"});
		dialog.setFiles(new String[]{"Nexus files"});
		dialog.setNewFile(false);
		dialog.setFolderSelector(false);
		if (lastPath != null) dialog.setPath(lastPath);
		
		dialog.create();
		if (dialog.open() == Dialog.CANCEL) return null;
		return dialog.getPath();
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
			
			if (buffer == null) return Status.OK_STATUS;
			
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
	
	private class UtilitiesComposite extends Composite {

		public UtilitiesComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new FillLayout());
			Group group = new Group(this, SWT.NONE);
			group.setLayout(new GridLayout(4, false));
			Button b2 = new Button(group, SWT.PUSH);
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
			
			
			Button b3a = new Button(group, SWT.PUSH);
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
			
			Button b3 = new Button(group, SWT.PUSH);
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
			
			Button b3l = new Button(group, SWT.PUSH);
			b3l.setText("Load");
			b3l.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String path = getFileName();
					if (path == null) return;
					lastPath = path;
					
					if (buffer == null && !buildBuffer()) {
						return;
					}
					
					final String fileName = path;
					
					Runnable r = () -> {
						
						try {
							IPersistenceService ps = ServiceLoader.getPersistenceService();
							IPersistentFile f = ps.getPersistentFile(fileName);
							Map<String, IDataset> masks = f.getMasks(null);
							for (IDataset m : masks.values()) buffer.merge(DatasetUtils.convertToDataset(m));

						} catch (Exception e1) {
							logger.error("Could not load mask",e1);
						}
					};

					job.setRunnable(r);
					job.schedule();
				}
			});
			
		}
	}
	
	private class DiffractionComposite extends Composite {
		
		public DiffractionComposite(Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new GridLayout(2, false));
			
			Button load = new Button(this, SWT.PUSH);
			load.setText("Load Calibration");
			
			final CLabel currentPath = new CLabel(this, SWT.None);
			
			
			load.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					final String fileName = getFileName();
					if (fileName == null) return;
					
					Runnable r = () ->  {
						try {
							IDiffractionMetadata meta = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(fileName, null);
							if (meta != null) {
								metadata = meta;
								Display.getDefault().asyncExec(() -> {
									currentPath.setText(fileName);
									currentPath.getParent().layout();
								});
							}
						} catch (DatasetException e1) {
							logger.error("Could not read calibration");
						}
					};
					
					job.setRunnable(r);
					job.schedule();
					
				}
				
			});
			
			Button center = new Button(this, SWT.PUSH);
			center.setText("Centre Sectors, Rings and Circles");
			center.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (metadata != null) {
						double[] point = metadata.getDetector2DProperties().getBeamCentreCoords();
						getPlottingSystem().getRegions().stream()
						.map(IRegion::getROI)
						.filter(RingROI.class::isInstance)
						.map(RingROI.class::cast)
						.forEach(r -> r.setPoint(point));
						
						getPlottingSystem().getRegions().stream()
						.map(IRegion::getROI)
						.filter(CircularROI.class::isInstance)
						.map(CircularROI.class::cast)
						.forEach(r -> r.setPoint(point));
						
						getPlottingSystem().repaint();
					}
					
				}
			});
		}
		
	}
	
	private class ImageComposite extends Composite {

		public ImageComposite(Composite parent, int style) {
			super(parent, style);

			this.setLayout(new GridLayout(3, false));
			
			Button b1 = new Button(this, SWT.PUSH);
			b1.setText("Invert");
			b1.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (buffer == null) return;
					
					Runnable r = () -> buffer.invert();
					
					job.setRunnable(r);
					job.schedule();
				}


			});
			
			Button b2 = new Button(this, SWT.PUSH);
			b2.setText("Flip H");
			b2.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (buffer == null) return;
					
					Runnable r = () -> {
						BooleanDataset mask = buffer.getMask();
						SliceND s = new SliceND(mask.getShape());
						s.flip(0);
						buffer.clear();
						buffer.merge(mask.getSlice(s));
					};
					
					job.setRunnable(r);
					job.schedule();
				}


			});
			
			Button b3 = new Button(this, SWT.PUSH);
			b3.setText("Flip V");
			b3.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (buffer == null) return;
					
					Runnable r = () -> {
						BooleanDataset mask = buffer.getMask();
						SliceND s = new SliceND(mask.getShape());
						s.flip(1);
						buffer.clear();
						buffer.merge(mask.getSlice(s));
					};
					
					job.setRunnable(r);
					job.schedule();
				}


			});
		}

	}

	private class ThresholdMaskComposite extends Composite {

		public ThresholdMaskComposite(Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new GridLayout(2, false));
			
			Label ll = new Label(this, SWT.NONE);
			ll.setText("Lower");
			Text lower = new Text(this,SWT.BORDER);
			lower.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			lower.addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e) {
					
					update(lowerValue,e,lower.getText());

				}
			});
			
			Label lu = new Label(this, SWT.NONE);
			lu.setText("Upper");
			Text upper = new Text(this,SWT.BORDER);
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
			
			if (buffer == null && !buildBuffer()) {
				return;
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
	
	private IROI buildThickLine(LinearROI lroi, int thickness) {
		double angle = lroi.getAngle();
		
		RectangularROI rroi = new RectangularROI();
		rroi.setAngle(lroi.getAngle());
		rroi.setLengths(lroi.getLength(), thickness);
		double[] point = lroi.getPoint();
		rroi.setPoint(new double[] {point[0]+Math.sin(angle)*(thickness/2.0), point[1] - Math.cos(angle)*(thickness/2.0)});
		
		return rroi;
	}
	
	private class MaskRegionComposite extends Composite {

		private ROIEditTable roiEditTable;
		private AtomicReference<IRegion> regionRef;
		private IROIListener roiListener;
		private Spinner lineThickness;
		private final List<String> IGNOREIFCONTAINS = new ArrayList<>(Arrays.asList("beam centre", "calibrant")); //sub-strings associated with known DiffractionAugmenter ROIs 
		
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
			group.setLayout(new GridLayout(4, false));
			group.setText("Draw Regions");
			
			Combo combo = new Combo(group, SWT.READ_ONLY);
			combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,2,1));
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
			
			Button b1 = new Button(group, SWT.PUSH);
			b1.setText("Remove All");
			b1.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					IPlottingSystem<?> plottingSystem = getPlottingSystem();
					Collection<IRegion> regions = plottingSystem.getRegions();
					regions.stream().filter(r -> predicateKnownROINames(r)).collect(Collectors.toList())
							.forEach(plottingSystem::removeRegion);
				}

			});
			
			
			Button b = new Button(group, SWT.PUSH);
			b.setText("Apply");
			b.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					if (buffer == null && !buildBuffer()) {
						return;
					}
					
					final Collection<IRegion> regions = getPlottingSystem().getRegions().stream().filter(r -> predicateKnownROINames(r)).collect(Collectors.toList());

					final int thickness = maskRegionComposite.getLineThickness();

					Runnable r = () -> {for (IRegion re: regions) {

						IROI roi = re.getROI();

						if (roi instanceof LinearROI && thickness != 1) {

							roi = buildThickLine((LinearROI)roi, thickness);
						}

						buffer.maskROI(roi);
					}
					};

					job.setRunnable(r);
					job.schedule();
				}
			});
			
			Label tlabel = new Label(group, SWT.None);
			tlabel.setText("Line Thickness");
			tlabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			
			lineThickness = new Spinner(group, SWT.BORDER);
			lineThickness.setMinimum(1);
			lineThickness.setMaximum(10000);
			lineThickness.setSelection(1);
			lineThickness.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			
			ComboViewer comboMode = new ComboViewer(group, SWT.READ_ONLY);
			comboMode.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			comboMode.setContentProvider(new ArrayContentProvider());
			comboMode.setLabelProvider(new ColumnLabelProvider());
			comboMode.setInput(MaskRegionDragMode.values());
			comboMode.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection s = event.getSelection();
					if (s.isEmpty()) return;
					
					if (s instanceof StructuredSelection && ((StructuredSelection)s).getFirstElement() instanceof MaskRegionDragMode) {
						maskRegionDragMode = (MaskRegionDragMode)((StructuredSelection)s).getFirstElement();
						
						Collection<IRegion> regions = getPlottingSystem().getRegions();
						
						if (regions == null) return;
						
						IROIListener roiListener = getROIListener();
						for (IRegion next : regions) {

							if (maskRegionDragMode.equals(MaskRegionDragMode.NO_ACTION)) {
								next.removeROIListener(iroiListener);
							} else {
								next.addROIListener(roiListener);
							}
						}
					}
				}
			});
			
			comboMode.setSelection(new StructuredSelection(MaskRegionDragMode.NO_ACTION));
			combo.redraw();
					
			Composite editComposite = new Composite(group, SWT.None);
			editComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,4,1));
			editComposite.setLayout(new GridLayout(2, false));
			roiEditTable = new ROIEditTable();
			roiEditTable.createPartControl(editComposite);
			
			roiEditTable.addROIListener(roiListener);
			
			final Button bmaskclick = new Button(group, SWT.TOGGLE);
			bmaskclick.setText("Mask Pixel on Click");
			bmaskclick.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					IPlottingSystem<?> ps = getPlottingSystem();
					
					if (ps == null || ps.isDisposed()) return;
					
					if (bmaskclick.getSelection()) {
						
						if (pixelMaskListener == null) {
							pixelMaskListener = new MaskOnClickListener();
						}
						
						ps.addClickListener(pixelMaskListener);
						
					} else {
						if (pixelMaskListener != null) {
							ps.removeClickListener(pixelMaskListener);
							pixelMaskListener = null;
						}
					}
				}
			});
			
		}
		
		private boolean predicateKnownROINames(IRegion region)  {
			String rname = region.getName().toLowerCase();
			for (String scomp : IGNOREIFCONTAINS)
				if (rname.contains(scomp))
					return false;
			return true;
		}

		public void removeListeners() {
			roiEditTable.removeROIListener(roiListener);
		}
		
		public void setROI(IROI roi, final IRegion region){
			regionRef.set(region);
			roiEditTable.setRegion(roi, region.getRegionType(), region.getCoordinateSystem());
			roiEditTable.getTableViewer().refresh();
		}
		
		public int getLineThickness() {
			return lineThickness.getSelection();
		}
	}
	
	private class MaskOnClickListener implements IClickListener {

		
		@Override
		public void clickPerformed(ClickEvent evt) {
			
			if (buffer == null && !buildBuffer()) {
					return;
			}
			
			Runnable r = ()-> {
				double x = evt.getxValue();
				double y = evt.getyValue();
				buffer.maskPixel((int)x, (int)y);
			};
			
			job.setRunnable(r);
			job.schedule();
			
		}

		@Override
		public void doubleClickPerformed(ClickEvent evt) {
			// do nothing
			
		}
		
	}
	
	private boolean buildBuffer() {
		IImageTrace imageTrace = getImageTrace();
		if (imageTrace != null) {
			IDataset data = imageTrace.getData();
			
			buffer = new MaskCircularBuffer(data.getShape());
			return true;
		}
		return false;
	}
}
