package org.dawb.workbench.plotting.tools;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.services.HistogramBound;
import org.dawb.common.ui.image.IconUtils;
import org.dawb.common.ui.image.ShapeType;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IPlottingSystem;
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
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.gda.common.rcp.util.GridUtils;

public class MaskingTool extends AbstractToolPage implements MouseListener{


	private static final Logger logger = LoggerFactory.getLogger(MaskingTool.class);
	
	private Group           composite;
	private Spinner         minimum, maximum;
	private Button          autoApply;
	private MaskObject      maskObject;
	private MaskJob         maskJob;
	private TableViewer     regionTable;
	private ToolBarManager  directToolbar;
	
	private IPaletteListener    paletteListener;
	private ITraceListener      traceListener;
	private IRegionListener     regionListener;
	private IROIListener        regionBoundsListener;
	private MaskMouseListener   clickListener;
	
	
	public MaskingTool() {
		
		
		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void traceAdded(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) {

					((IImageTrace)evt.getSource()).setMask(maskObject.getMaskDataset());
					((IImageTrace)evt.getSource()).addPaletteListener(paletteListener);
				}
			}
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) {
					((IImageTrace)evt.getSource()).removePaletteListener(paletteListener);
				}
			}
		};
				
		this.paletteListener = new IPaletteListener.Stub() {
			
			@Override
			public void nanBoundsChanged(PaletteEvent evt) {
				updateIcons(evt.getTrace().getNanBound().getColor());
			}
		};
		
		this.clickListener = new MaskMouseListener();
		
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionCreated(RegionEvent evt) {
				// Those created while the tool is active are mask regions			
                evt.getRegion().setMaskRegion(true);
                int wid = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.FREE_DRAW_WIDTH);
                evt.getRegion().setLineWidth(wid);
			}
			@Override
			public void regionAdded(final RegionEvent evt) {
				evt.getRegion().addROIListener(regionBoundsListener);
				processMask(evt.getRegion());
				regionTable.refresh();

				if (Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.MASK_DRAW_MULTIPLE)) {
					Display.getDefault().asyncExec(new Runnable(){
						public void run() {
							try {
								getPlottingSystem().createRegion(RegionUtils.getUniqueName(evt.getRegion().getRegionType().getName(), getPlottingSystem()),
										evt.getRegion().getRegionType());
							} catch (Exception e) {
								logger.error("Cannot add multple regions.", e);
							}
						}
					});
				}
			}			
			@Override
			public void regionRemoved(RegionEvent evt) {
				evt.getRegion().removeROIListener(regionBoundsListener);
				processMask(true, false, null);
				regionTable.refresh();
			}			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				processMask(true, false, null);
				regionTable.refresh();
			}
		};
		
		this.regionBoundsListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				processMask((IRegion)evt.getSource());
			}
		};
		this.maskJob = new MaskJob();
		maskJob.setPriority(Job.INTERACTIVE);
		maskJob.setSystem(true);
		maskJob.setUser(false);
	}

	protected final class MaskMouseListener extends MouseMotionListener.Stub implements org.eclipse.draw2d.MouseListener		 {

		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {			
			if (((AbstractPlottingSystem)getPlottingSystem()).getSelectedCursor()==null) {
				ActionContributionItem item = (ActionContributionItem)directToolbar.find(ShapeType.NONE.getId());
				if (item!=null) item.getAction().setChecked(true);
				return;
			}
			maskJob.schedule(false, null, me.getLocation());
		}
		@Override
		public void mouseDragged(org.eclipse.draw2d.MouseEvent me) {
			if (((AbstractPlottingSystem)getPlottingSystem()).getSelectedCursor()==null) {
				ActionContributionItem item = (ActionContributionItem)directToolbar.find(ShapeType.NONE.getId());
				if (item!=null) item.getAction().setChecked(true);
				return;
			}
			maskJob.schedule(false, null, me.getLocation());
		}
		@Override
		public void mouseReleased(org.eclipse.draw2d.MouseEvent me) { }

		@Override
		public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) { }
		
	}

	public void setPlottingSystem(IPlottingSystem system) {
		super.setPlottingSystem(system);
		this.maskObject   = new MaskObject(); //TODO maybe make maskCreator by only processing visible regions.
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
		label.setText("Create a mask, the mask is saved and available in other tools.");	
		
		// Max and min
		
		final Button minEnabled =  new Button(minMaxComp, SWT.CHECK);
		minEnabled.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,1));
		minEnabled.setText("Enable lower mask    ");
		minEnabled.setToolTipText("Enable the lower bound mask, removing pixels with lower intensity.");
		minEnabled.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				minimum.setEnabled(minEnabled.getSelection());
				if (!minEnabled.getSelection()) {
					processMask(true, true, null);
				} else {
				    processMask(false, true, null);
				}
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
				processMask(true, false, null);
			}
		});
		minimum.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character=='\n' || e.character=='\r') {
					processMask(true, true, null);
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
				if (!maxEnabled.getSelection()) {
					processMask(true, true, null);
				} else {
				    processMask(false, true, null);
				}
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
				processMask(true, false, null);
			}
		});
		maximum.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character=='\n' || e.character=='\r') {
					processMask(true, true, null);
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
				getImageTrace().rehistogram();
			}
		});
		

		final Group drawGroup = new Group(composite, SWT.SHADOW_NONE);
		drawGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		drawGroup.setLayout(new GridLayout(3, false));
		
		label = new Label(drawGroup, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.FILL, false, false);
		label.setLayoutData(data);
		label.setText("Mask using:  ");
	
		final Button directDraw = new Button(drawGroup, SWT.RADIO);
		directDraw.setText("Direct draw");
		directDraw.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		
		final Button regionDraw = new Button(drawGroup, SWT.RADIO);
		regionDraw.setText("Regions");
		regionDraw.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		
		label = new Label(drawGroup, SWT.HORIZONTAL|SWT.SEPARATOR);
		GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gdata.verticalIndent=5;
		label.setLayoutData(gdata);

		final Composite drawContent = new Composite(drawGroup, SWT.NONE);
		drawContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		drawContent.setLayout(new StackLayout());

		final Composite directComp = new Composite(drawContent, SWT.NONE);
		directComp.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(directComp);
		
		this.directToolbar = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		createDirectToolbarActions(directToolbar);
		Control          tb         = directToolbar.createControl(directComp);
		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
     
		// Regions
		final Composite        regionComp = new Composite(drawContent, SWT.NONE);
		regionComp.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(regionComp);

		final ToolBarManager   regionToolbar = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		
		createMaskingRegionActions(regionToolbar);
		tb         = regionToolbar.createControl(regionComp);
		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		this.regionTable = new TableViewer(regionComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		regionTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(regionTable);
		regionTable.getTable().setLinesVisible(true);
		regionTable.getTable().setHeaderVisible(true);
		regionTable.getTable().addMouseListener(this);
		
		getSite().setSelectionProvider(regionTable);
		regionTable.setContentProvider(new IStructuredContentProvider() {			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {				
			}			
			@Override
			public void dispose() {
			}		
			@Override
			public Object[] getElements(Object inputElement) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
				final List<IRegion> supported = new ArrayList<IRegion>(regions.size());
				for (IRegion iRegion : regions) if (maskObject.isSupportedRegion(iRegion) &&
						                            iRegion.isUserRegion()) {
					supported.add(iRegion);
				}
				return supported.toArray(new IRegion[supported.size()]);
			}
		});
		regionTable.setInput(new Object());
		
		final Composite buttons = new Composite(regionComp, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		this.autoApply     = new Button(buttons, SWT.CHECK);
		autoApply.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		autoApply.setText("Automatically apply mask when something changes.");
		autoApply.setSelection(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.MASK_AUTO_APPLY));
		
		final Button apply = new Button(buttons, SWT.PUSH);
		apply.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		apply.setImage(Activator.getImage("icons/apply.gif"));
		apply.setText("Apply");
		apply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				processMask(true, true, null);
			}
		});
		apply.setEnabled(true);
		
		autoApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_AUTO_APPLY, autoApply.getSelection());
				apply.setEnabled(!autoApply.getSelection()); 
				processMask();
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
		
		createActions(getSite().getActionBars());
		
		
		final StackLayout layout = (StackLayout)drawContent.getLayout();
		layout.topControl = directComp;
		directDraw.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_DRAW_TYPE, "direct");
				layout.topControl = directComp;
				drawContent.layout();
				ShapeType penShape = ShapeType.valueOf(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE));
				ActionContributionItem item= ((ActionContributionItem)directToolbar.find(penShape.getId()));
				if (item!=null) item.getAction().run();
			}
		});
		
		regionDraw.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_DRAW_TYPE, "region");
				layout.topControl = regionComp;
				drawContent.layout();
				((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(null);
			}
		});
		
		String drawType = Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_DRAW_TYPE);
        if (drawType!=null && !"".equals(drawType)) {
        	if ("direct".equals(drawType)) {
        		directDraw.setSelection(true);
				layout.topControl = directComp;
				ShapeType penShape = ShapeType.NONE;
				if (Activator.getDefault().getPreferenceStore().contains(PlottingConstants.MASK_PEN_SHAPE)) {
					penShape = ShapeType.valueOf(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE));
				}
				ActionContributionItem item= ((ActionContributionItem)directToolbar.find(penShape.getId()));
				if (item!=null) item.getAction().run();
     		
        	} else if ("region".equals(drawType)) {
        		regionDraw.setSelection(true);
				layout.topControl = regionComp;
        	}
        }
	}

	/**
	 * Actions for 
	 * @param freeToolbar2
	 */
	private void createDirectToolbarActions(ToolBarManager man) {
		
		final MenuAction penSize = new MenuAction("Pen Size");
		penSize.setToolTipText("Pen size");
		man.add(penSize);

		CheckableActionGroup group = new CheckableActionGroup();		
		final int[] pensizes = new int[]{1,2,3,4,5,6,7,8,9,10,12,14,16,18,20,24,32,64};
		Action currentSize=null;
		for (final int pensize : pensizes) {
			
			final Action action = new Action("Pen size of "+String.valueOf(pensize), IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_SIZE, pensize);
					penSize.setSelectedAction(this);
					penSize.setToolTipText(getToolTipText());
					
					ShapeType penShape = ShapeType.valueOf(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE));
					if (penShape!=null) {
					    ((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(IconUtils.getPenCursor(pensize, penShape));
					}
				}

			};
			
			action.setImageDescriptor(IconUtils.createPenDescriptor(pensize));
			penSize.add(action);
			group.add(action);
			if (Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE)==pensize) {
				currentSize = action;
			}
			action.setToolTipText("Set pen size to "+pensize);
			
		}
		
		if (currentSize!=null) {
			currentSize.setChecked(true);
			penSize.setSelectedAction(currentSize);
			penSize.setToolTipText(currentSize.getToolTipText());
		}
		
		man.add(new Separator());

		group = new CheckableActionGroup();
		Action action = new Action("Square brush", IAction.AS_CHECK_BOX) {
			public void run() {
				int pensize = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE);
				ShapeType penShape = ShapeType.SQUARE;
				((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(IconUtils.getPenCursor(pensize, penShape));
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_SHAPE, penShape.name());
			}
		};
		action.setId(ShapeType.SQUARE.getId());
		group.add(action);
		man.add(action);
		
		action = new Action("Triangle brush", IAction.AS_CHECK_BOX) {
			public void run() {
				int pensize = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE);
				ShapeType penShape = ShapeType.TRIANGLE;
				((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(IconUtils.getPenCursor(pensize, penShape));
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_SHAPE, penShape.name());
			}
		};
		action.setId(ShapeType.TRIANGLE.getId());
		group.add(action);
		man.add(action);
		
		action = new Action("Circular brush", IAction.AS_CHECK_BOX) {
			public void run() {
				int pensize = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE);
				ShapeType penShape = ShapeType.CIRCLE;
				((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(IconUtils.getPenCursor(pensize, penShape));
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_SHAPE, penShape.name());
			}
		};
		action.setId(ShapeType.CIRCLE.getId());
		group.add(action);
		man.add(action);
		
		action = new Action("None", IAction.AS_CHECK_BOX) {
			public void run() {
				ShapeType penShape = ShapeType.NONE;
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_SHAPE, penShape.name());
				((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(null);
			}
		};
		action.setId(ShapeType.NONE.getId());
		action.setImageDescriptor(Activator.getImageDescriptor("icons/MouseArrow.png"));
		group.add(action);
		man.add(action);

				
		man.add(new Separator());
		
		group = new CheckableActionGroup();
		final Action mask = new Action("Mask", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_MASKOUT, true);
				updateIcons(getImageTrace().getNanBound().getColor());
			}
		};
		mask.setImageDescriptor(Activator.getImageDescriptor("icons/mask-add.png"));
		group.add(mask);
		man.add(mask);
		
		final Action unmask = new Action("Unmask", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_PEN_MASKOUT, false);
				updateIcons(null);
			}
		};
		unmask.setImageDescriptor(Activator.getImageDescriptor("icons/mask-remove.png"));
		group.add(unmask);
		man.add(unmask);
		
		man.add(new Separator());

		boolean maskout = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.MASK_PEN_MASKOUT);
		if (maskout) {
			updateIcons(getImageTrace().getNanBound().getColor());
			mask.setChecked(true);
		} else {
			updateIcons(null);
			unmask.setChecked(true);
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				String savedShape = Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE);
				if (savedShape!=null && !"".equals(savedShape)) {
					ShapeType type = ShapeType.valueOf(savedShape);
					((ActionContributionItem)directToolbar.find(type.getId())).getAction().setChecked(true);
				}
			}
		});
	}
	
	private void updateIcons(RGB maskColor) {
		
		((ActionContributionItem)directToolbar.find(ShapeType.SQUARE.getId())).getAction().setImageDescriptor(IconUtils.getBrushIcon(  12, ShapeType.SQUARE,   maskColor));
		((ActionContributionItem)directToolbar.find(ShapeType.TRIANGLE.getId())).getAction().setImageDescriptor(IconUtils.getBrushIcon(12, ShapeType.TRIANGLE, maskColor));
		((ActionContributionItem)directToolbar.find(ShapeType.CIRCLE.getId())).getAction().setImageDescriptor(IconUtils.getBrushIcon(  12, ShapeType.CIRCLE,   maskColor));
	}


	private static BooleanDataset savedMask=null;
	private static boolean        autoApplySavedMask=false;
	private static final String   AUTO_SAVE_PROP="org.dawb.workbench.plotting.tools.auto.save.mask";
	private Action loadMask;
	
	/**
	 * You can programmatically set the last saved mask by calling this method.
	 * 
	 * NOTE: In future a mask saving and retrieving methodology will exist.
	 *       This method is used for an NCD workaround and is not intended
	 *       as a long term solution for mask saving/loading
	 * 
	 * @param sm          - static dataset to use as the mask
	 * @param autoApply   - if true the mask will be applied when the tool is activated
	 */
	public static void setSavedMask(final BooleanDataset sm, final boolean autoApply) {
		savedMask          = sm;
		autoApplySavedMask = autoApply;
	}
	
	public static BooleanDataset getSavedMask() {
		return savedMask;
	}
	
	private void createActions(IActionBars actionBars) {
		
		final Action reset = new Action("Reset Mask", Activator.getImageDescriptor("icons/reset.gif")) {
			public void run() {
				resetMask();
			}
		};
		actionBars.getToolBarManager().add(reset);
		actionBars.getToolBarManager().add(new Separator());
		
		final Action saveMask  = new Action("Save the mask into a buffer", Activator.getImageDescriptor("icons/import_wiz.gif")) {
			public void run() {
				saveMaskBuffer();
			}
		};
		actionBars.getToolBarManager().add(saveMask);
		
	    loadMask  = new Action("Merge the previously saved mask from buffer with this mask (if any)", Activator.getImageDescriptor("icons/export_wiz.gif")) {
			public void run() {
				mergeSavedMask();
			}
		};
		loadMask.setEnabled(savedMask!=null);
		actionBars.getToolBarManager().add(loadMask);
		
		actionBars.getToolBarManager().add(new Separator());
		Action alwaysSave  = new Action("Auto-save the mask to the buffer when it changes", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(AUTO_SAVE_PROP, isChecked());
			}
		};
		alwaysSave.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-masking-autosave.png"));
		if (Activator.getDefault().getPreferenceStore().contains(AUTO_SAVE_PROP)) {
			alwaysSave.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(AUTO_SAVE_PROP));
		} else {
			Activator.getDefault().getPreferenceStore().setValue(AUTO_SAVE_PROP, true);
			alwaysSave.setChecked(true);
		}
		actionBars.getToolBarManager().add(alwaysSave);

        final IPlottingSystem system = getPlottingSystem();
        system.getPlotActionSystem().fillTraceActions(actionBars.getToolBarManager(), getImageTrace(), system);	
	}

	protected void saveMaskBuffer() {
		savedMask = maskObject.getMaskDataset();
		loadMask.setEnabled(savedMask!=null);
	}

	protected void mergeSavedMask() {
		
		if (savedMask==null) return;
		if (maskObject.getImageDataset()==null){
			maskObject.setImageDataset(getImageTrace().getData());
		}
		maskObject.process(savedMask);
		getImageTrace().setMask(maskObject.getMaskDataset());
	}

	private void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.setColumnProperties(new String[] { "Mask", "Name", "Type" });

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Mask");
		var.getColumn().setWidth(50);
		var.setLabelProvider(new MaskingLabelProvider());

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new MaskingLabelProvider());
		
		var = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Type");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MaskingLabelProvider());
	}
		
	private class MaskingLabelProvider extends ColumnLabelProvider {
	
		private Image checkedIcon;
		private Image uncheckedIcon;
		
		public MaskingLabelProvider() {
			
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
			
			if (columnIndex!=0) return null;
			if (!(element instanceof IRegion)) return null;
			final IRegion region = (IRegion)element;
			return region.isMaskRegion() && regionTable.getTable().isEnabled() ? checkedIcon : uncheckedIcon;
		}
		
		public String getText(Object element) {
			
			if (element instanceof String) return "";
			
			final IRegion region = (IRegion)element;
			switch(columnIndex) {
			case 1:
			return region.getName();
			case 2:
			return region.getRegionType().getName();
			}
			return "";
		}
		
		public void dispose() {
			super.dispose();
			checkedIcon.dispose();
			uncheckedIcon.dispose();
		}

	}

	private static final Collection<RegionType> maskingTypes;
	static {
		maskingTypes = new ArrayList<RegionType>(6);
		maskingTypes.add(RegionType.FREE_DRAW);
		maskingTypes.add(RegionType.RING);
		maskingTypes.add(RegionType.BOX);
		maskingTypes.add(RegionType.LINE);
		maskingTypes.add(RegionType.POLYLINE);
		maskingTypes.add(RegionType.POLYGON);
		maskingTypes.add(RegionType.SECTOR);
	}
	private void createMaskingRegionActions(IToolBarManager man) {		
		
		final Action multipleRegion  = new Action("Continuously add the same region", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.MASK_DRAW_MULTIPLE, isChecked());
			}
		};
		multipleRegion.setImageDescriptor(Activator.getImageDescriptor("icons/RegionMultiple.png"));
		multipleRegion.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.MASK_DRAW_MULTIPLE));
		man.add(multipleRegion);
		man.add(new Separator());
		
		final MenuAction widthChoice = new MenuAction("Line With");
		widthChoice.setToolTipText("Line width for free draw and line regions");
		man.add(widthChoice);
		
		// Region actions supported
		ActionContributionItem menu  = (ActionContributionItem)getPlottingSystem().getActionBars().getMenuManager().find("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
		MenuAction        menuAction = (MenuAction)menu.getAction();	
		IAction fd = null;
		for (RegionType type : maskingTypes) {
			final IAction action = menuAction.findAction(type.getId());
			if (action==null) continue;
			man.add(action);
			
			if (type==RegionType.FREE_DRAW) {
				fd = action;
				man.add(new Separator());
			}
		}
		
		CheckableActionGroup group = new CheckableActionGroup();
		final IAction freeDraw = fd;
		
		final int maxWidth = 10;
		for (int iwidth = 1; iwidth <= maxWidth; iwidth++) {
			
			final int width = iwidth;
			final Action action = new Action("Draw width of "+String.valueOf(width), IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.FREE_DRAW_WIDTH, width);
					widthChoice.setSelectedAction(this);
					freeDraw.run();
				}
			};
			
			action.setImageDescriptor(IconUtils.createIconDescriptor(String.valueOf(iwidth)));
			widthChoice.add(action);
			group.add(action);
			action.setChecked(false);
			action.setToolTipText("Set line width to "+iwidth);
			
		}
       	int wid = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.FREE_DRAW_WIDTH);
        widthChoice.setSelectedAction(wid-1);
		widthChoice.setCheckedAction(wid-1, true);

		man.add(new Separator());
		menu  = (ActionContributionItem)getPlottingSystem().getActionBars().getToolBarManager().find("org.dawb.workbench.ui.editors.plotting.swtxy.removeRegions");
		if (menu!=null) {
			menuAction = (MenuAction)menu.getAction();	
			man.add(menuAction);
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
	
	
	private void processMask() {
		processMask(false, false, null);
	}
	private void processMask(final IRegion region) {
		processMask(false, false, region);
	}
	/**
	 * Either adds a new region directly or 
	 * @param forceProcess
	 * @param roi
	 * @return true if did some masking
	 */
	private void processMask(final boolean resetMask, boolean ignoreAuto, final IRegion region) {
		
		if (!ignoreAuto && !autoApply.getSelection()) return;
		
		final IImageTrace image = getImageTrace();
		if (image == null) return;
		
		maskJob.schedule(resetMask, region, null);
	}
	
	protected void resetMask() { // Reread the file from disk or cached one if this is a view
		
		final IImageTrace image = getImageTrace();
		if (image==null) return;
		maskObject.reset();
	    image.setMask(null);
	    
	    if (autoApplySavedMask) { // If it is auto-apply then it needs to be reset when you reset.
	    	final boolean ok = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Confirm Removal of Saved Mask", 
	    			                  "You have a saved mask which can be cleared as well.\n\nWould you like to remove the saved mask?");
	    	if (ok) {
	    		savedMask = null;
	    		autoApplySavedMask = false;
	    	}
	    	loadMask.setEnabled(savedMask!=null);
	    }
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
				if (!maskObject.isSupportedRegion(region)) continue;
				region.addROIListener(this.regionBoundsListener);
			}
			
			if (getImageTrace()!=null) {
				getImageTrace().addPaletteListener(paletteListener);
			}

			((AbstractPlottingSystem)getPlottingSystem()).addMouseClickListener(clickListener);
			((AbstractPlottingSystem)getPlottingSystem()).addMouseMotionListener(clickListener);
		}
		if (this.regionTable!=null && !regionTable.getControl().isDisposed()) {
			regionTable.refresh();
		}

		if (loadMask!=null) loadMask.setEnabled(savedMask!=null);
		
		if (autoApplySavedMask && savedMask!=null) {
			try {
			    mergeSavedMask();
			} catch (Throwable ne) {
				logger.error("Problem loading saved mask!", ne);
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
			
			if (getImageTrace()!=null) {
				getImageTrace().removePaletteListener(paletteListener);
			}

			((AbstractPlottingSystem)getPlottingSystem()).removeMouseClickListener(clickListener);
			((AbstractPlottingSystem)getPlottingSystem()).removeMouseMotionListener(clickListener);
			((AbstractPlottingSystem)getPlottingSystem()).setSelectedCursor(null);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (composite!=null) composite.dispose();
		composite      = null;
		traceListener  = null;
		regionListener = null;
		regionBoundsListener = null;
		if (this.regionTable!=null && !regionTable.getControl().isDisposed()) {
			regionTable.getTable().removeMouseListener(this);
		}
	}

	
	public class MaskJob extends Job {

		public MaskJob() {
			super("Masking image");
		}

		private boolean resetMask         = false;
		private boolean isRegionsEnabled  = false;
		private IRegion region            = null;
		private Integer min=null, max=null;
		private org.eclipse.draw2d.geometry.Point location;
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
						
			final IImageTrace image = getImageTrace();
			if (image == null) return Status.CANCEL_STATUS;
			
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			
			if (region!=null && !isRegionsEnabled) return Status.CANCEL_STATUS;
			
			if (resetMask)  maskObject.setMaskDataset(null, false);
			
			if (maskObject.getMaskDataset()==null) {
				// The mask must be maintained as a BooleanDataset so that there is the option
				// of applying the same mask to many images.
				final AbstractDataset unmasked = image.getData();
				maskObject.setMaskDataset(new BooleanDataset(unmasked.getShape()), true);
				maskObject.setImageDataset(unmasked);
			}
			
			if (maskObject.getImageDataset()==null) {
				final AbstractDataset unmasked = image.getData();
				maskObject.setImageDataset(unmasked);
			}
			
			// Keep the saved mask
			if (autoApplySavedMask && savedMask!=null) maskObject.process(savedMask);
			
			// Just process a changing region
			if (location!=null) {
				maskObject.process(location, getPlottingSystem());
				
			} else if (region!=null) {
				if (!maskObject.isSupportedRegion(region)) return Status.CANCEL_STATUS;
				maskObject.process(region);
				
				
			} else { // process everything
				
				maskObject.process(min, max, isRegionsEnabled?getPlottingSystem().getRegions():null);
				
			}
			
			if (Activator.getDefault().getPreferenceStore().getBoolean(AUTO_SAVE_PROP)) {
				saveMaskBuffer();
			}
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					// NOTE the mask will have a reference kept and
					// will downsample with the data.
					image.setMask(maskObject.getMaskDataset()); 
				}
			});
			
			return Status.OK_STATUS;
		}

		/**
		 * Uses job thread
		 * @param resetMask
		 * @param region
		 * @param loc
		 */
		public void schedule(boolean resetMask, IRegion region, org.eclipse.draw2d.geometry.Point loc) {
			assign(resetMask, region, loc);
			schedule();
		}
		
		private void assign(boolean resetMask, IRegion region, org.eclipse.draw2d.geometry.Point loc) {
			this.isRegionsEnabled = regionTable.getTable().isEnabled();
			this.resetMask    = resetMask;
			this.region       = region;
			this.location     = loc;
			min = (minimum.isEnabled()) ? minimum.getSelection() : null;
		    max = (maximum.isEnabled()) ? maximum.getSelection() : null;			
		}

		/**
		 * Done in calling thread.
		 * @param resetMask
		 * @param region
		 * @param loc
		 */
		public void run(boolean resetMask, IRegion region, org.eclipse.draw2d.geometry.Point loc) {
			assign(resetMask, region, loc);
			try {
			    run(new NullProgressMonitor());
			} catch (Throwable ne) {
				logger.error("Cannot run masking job!", ne);
			}
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		final TableItem item = this.regionTable.getTable().getItem(new Point(e.x, e.y));
		if (item!=null) {
			IRegion region = (IRegion)item.getData();
			region.setMaskRegion(!region.isMaskRegion());
			regionTable.refresh(region);
			processMask(true, false, null);
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		
	}

}
