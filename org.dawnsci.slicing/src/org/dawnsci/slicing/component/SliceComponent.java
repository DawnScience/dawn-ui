/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.slicing.component;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.ui.Activator;
import org.dawb.common.ui.DawbUtils;
import org.dawb.common.ui.components.cell.ScaleCellEditor;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.preferences.ViewConstants;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.nexus.NexusUtils;
import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.SpinnerCellEditorWithPlayButton;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.IPaletteListener;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.PaletteEvent;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.slicing.api.system.DimensionalEvent;
import org.dawnsci.slicing.api.system.DimensionalListener;
import org.dawnsci.slicing.api.system.DimsData;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.system.ISliceGallery;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.dawnsci.slicing.api.system.SliceSource;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;


/**
 * Dialog to slice multi-dimensional data to images and 1D plots.
 * 
 * Copied from nexus tree viewer but in a simpler to use UI.
 *  
 *
 */
public class SliceComponent implements ISliceSystem {
	

	private static final Logger logger = LoggerFactory.getLogger(SliceComponent.class);

	private static final List<String> COLUMN_PROPERTIES = Arrays.asList(new String[]{"Dimension","Axis","Slice","Axis Data"});
	
	private ILazyDataset    lazySet; // The dataset that we are slicing.
	private SliceObject     sliceObject;
	private int[]           dataShape;
	private IPlottingSystem plottingSystem;

	private TableViewer     viewer;
	private DimsDataList    dimsDataList;

	private CLabel          errorLabel, explain;
	private Link            openWindowing;
	private Composite       area;
	private boolean         isErrorCondition=false;
    private SliceJob        sliceJob;
    private String          sliceReceiverId;
     
    private PlotType        plotType=PlotType.IMAGE;
    private Action          updateAutomatically;

	private ITraceListener.Stub traceListener;
	private List<IAction> customActions;
	
	/**
	 * 1 is first dimension, map of names available for axis, including indices.
	 */
	private Map<Integer, List<String>> dimensionNames;

	/**
	 * Format used to show value in nexus axes
	 */
	private NumberFormat format;

	private ToolBarManager sliceToolbar;

	public SliceComponent() {
		this.sliceJob        = new SliceJob();
		this.dimensionNames  = new HashMap<Integer,List<String>>(5);
		this.format          = DecimalFormat.getNumberInstance();
	}
	
	@Override
	public void setSliceGalleryId(String id) {
		this.sliceReceiverId = id;
	}
	
	@Override
	public String getSliceName() {
		return getCurrentSlice().getName();
	}
	
	/**
	 * Please call setPlottingSystem(...) before createPartControl(...) if
	 * you would like the part to show controls for images.
	 * 
	 * @param parent
	 * @return
	 */
	public Control createPartControl(Composite parent) {
		
		this.area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(1, false));
		
		this.explain = new CLabel(area, SWT.WRAP);
		final GridData eData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		eData.heightHint=44;
		explain.setLayoutData(eData);
	
		this.sliceToolbar = createSliceActions();
		final ToolBar        tool    = sliceToolbar.createControl(area);
		tool.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		final Composite tableComp = new Composite(area, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComp.setLayout(tableColumnLayout);

		this.viewer = new TableViewer(tableComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				event.doit=false;
				// Do nothing disabled
			}
		});		

		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = 45;
			}
		});

		createColumns(viewer, tableColumnLayout);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(COLUMN_PROPERTIES.toArray(new String[COLUMN_PROPERTIES.size()]));			
		
		this.errorLabel = new CLabel(area, SWT.WRAP);
		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		errorLabel.setImage(Activator.getImageDescriptor("icons/error.png").createImage());
		GridUtils.setVisible(errorLabel,         false);
		
		this.openWindowing = new Link(area, SWT.WRAP);
		openWindowing.setText("Data is being viewed using a <a>window</a>");
		openWindowing.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridUtils.setVisible(openWindowing,         false);
		openWindowing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (plottingSystem!=null) {
					try {
						final IToolPageSystem system = (IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class);
						system.setToolVisible("org.dawb.workbench.plotting.tools.windowTool", ToolPageRole.ROLE_3D, 
								                      "org.dawb.workbench.plotting.views.toolPageView.3D");
					} catch (Exception e1) {
						logger.error("Cannot open window tool!", e1);
					}
				}
			}
		});
		
		final Composite bottom = new Composite(area, SWT.NONE);
		bottom.setLayout(new GridLayout(4, false));
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));					

		// Something to tell them their image orientation (X and Y may be mixed up!)
		if (plottingSystem!=null) {
			final StyledText imageOrientation = new StyledText(bottom, SWT.NONE);
			imageOrientation.setEditable(false);
			imageOrientation.setBackground(bottom.getBackground());
			imageOrientation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
			GridUtils.setVisible(imageOrientation, plottingSystem.is2D());
			
			addImageOrientationListener(imageOrientation);
			
			this.traceListener = new ITraceListener.Stub() {
				protected void update(TraceEvent evt) {
					GridUtils.setVisible(imageOrientation, plottingSystem.is2D());
					setImageOrientationText(imageOrientation);
					area.layout();
					addImageOrientationListener(imageOrientation);
				}
			};
			imageOrientation.setToolTipText("The image orientation currently set by the plotting.");
			plottingSystem.addTraceListener(traceListener);
		}

		// Same action on slice table
		final MenuManager man = new MenuManager();
		final Action openGal  = new Action("Open data in gallery", Activator.getImageDescriptor("icons/imageStack.png")) {
			public void run() {openGallery();}
		};
		man.add(openGal);
		man.add(reverse);
		final Menu menu = man.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
				sliceJob.cancel();
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				if (dimsDataList==null) return DimsDataList.getDefault();
				return dimsDataList.getElements();
			}
		});
		viewer.setInput(new Object());
		
		sliceToolbar.update(true);
    	
		return area;
	}

	private boolean axesVisible = true;
	public void setAxesVisible(boolean isVis) {
		axesVisible = isVis;
		if (viewer==null || viewer.getTable()==null || viewer.getTable().getColumnCount()<4) return;
		if (!isVis) {
		    viewer.getTable().getColumn(3).setWidth(0);
		    viewer.getTable().getColumn(3).setMoveable(false);
		} else {
		    viewer.getTable().getColumn(3).setWidth(200);
		    viewer.getTable().getColumn(3).setMoveable(true);
		}
	}
	private boolean rangesAllowed = false;
	public void setRangesAllowed(boolean isVis) {
		rangesAllowed = isVis;
	}

	public void setSliceActionsEnabled(boolean enabled) {
		sliceToolbar.getControl().setEnabled(enabled);
	}
	/**
	 * 
	 * @param actionId
	 * @throws NPE if action not found.
	 */
	public void setActionActive(String actionId) {
		IContributionItem item = sliceToolbar.find(actionId);
    	ActionContributionItem iaction = (ActionContributionItem)item;
    	iaction.getAction().setChecked(!iaction.getAction().isChecked());
    	iaction.getAction().run();
	}
	
	private Map<PlotType, Action> plotTypeActions;
	private Action                reverse;
	/**
	 * Creates the actions for 
	 * @return
	 */
	private ToolBarManager createSliceActions() {

		plotTypeActions= new HashMap<PlotType, Action>();
		
		final ToolBarManager man = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		man.add(new Separator("group1"));
		
        final CheckableActionGroup grp = new CheckableActionGroup();
        final Action xyPlot = new Action("Slice as line plots", IAction.AS_CHECK_BOX) {
        	public void run() {
        		saveSliceSettings();
        		boolean wasImage = plotType==PlotType.IMAGE || plotType==PlotType.SURFACE;
        		plotType = PlotType.XY;
        		// Loop over DimsData to ensure 1X only.
        		if (dimsDataList!=null) {
        			if (wasImage&&dimsDataList.isXFirst()) {
        				dimsDataList.setSingleAxisOnly(1, 0);   		
        			} else {
        				dimsDataList.setSingleAxisOnly(0, 0);
        			}
        		}
        		updatePlottingType();
        	}
		};
		xyPlot.setId(xyPlot.getText());
		man.add(xyPlot);
		xyPlot.setImageDescriptor(Activator.getImageDescriptor("icons/TraceLine.png"));
		grp.add(xyPlot);
		plotTypeActions.put(PlotType.XY, xyPlot);
		
		
        final Action stackPlot = new Action("Slice as a stack of line plots", IAction.AS_CHECK_BOX) {
        	public void run() {
        		saveSliceSettings();
        		setChecked(true);
        		plotType = PlotType.XY_STACKED;
         		// Loop over DimsData to ensure 1X only.
        		if (dimsDataList!=null) dimsDataList.setTwoAxisOnly(0, 1);   		
        		updatePlottingType();
         	}
		};
		stackPlot.setId(stackPlot.getText());
		stackPlot.setImageDescriptor(Activator.getImageDescriptor("icons/TraceLines.png"));
		grp.add(stackPlot);
		plotTypeActions.put(PlotType.XY_STACKED, stackPlot);
		man.add(stackPlot);

        final Action stackPlot3D = new Action("Slice as a stack of line plots in 3D", IAction.AS_CHECK_BOX) {
        	public void run() {
        		saveSliceSettings();
        		setChecked(true);
        		plotType = PlotType.XY_STACKED_3D;
        		// Loop over DimsData to ensure 1X only.
        		if (dimsDataList!=null) dimsDataList.setTwoAxisOnly(0, 1);   		
        		updatePlottingType();
        	}
		};
		stackPlot3D.setId(stackPlot3D.getText());
		man.add(stackPlot3D);
		stackPlot3D.setImageDescriptor(Activator.getImageDescriptor("icons/TraceLines3D.png"));
		grp.add(stackPlot3D);
		plotTypeActions.put(PlotType.XY_STACKED_3D, stackPlot3D);
			
        final Action imagePlot = new Action("Slice as image", IAction.AS_CHECK_BOX) {
        	public void run() {
        		saveSliceSettings();
       		    plotType = PlotType.IMAGE;
        		if (dimsDataList!=null) dimsDataList.setTwoAxisOnly(0, 1);   		
        		viewer.refresh();
        		updatePlottingType();
        	}
		};
		imagePlot.setId(imagePlot.getText());
		man.add(imagePlot);
		imagePlot.setImageDescriptor(Activator.getImageDescriptor("icons/TraceImage.png"));
		grp.add(imagePlot);
		plotTypeActions.put(PlotType.IMAGE, imagePlot);
		
        final Action surfacePlot = new Action("Slice as surface", IAction.AS_CHECK_BOX) {
        	public void run() {
        		saveSliceSettings();
        		plotType = PlotType.SURFACE;
        		if (dimsDataList!=null) dimsDataList.setTwoAxisOnly(0, 1);   		
        		viewer.refresh();
        		updatePlottingType();
        	}
		};
		surfacePlot.setId(surfacePlot.getText());
		man.add(surfacePlot);
		surfacePlot.setImageDescriptor(Activator.getImageDescriptor("icons/TraceSurface.png"));
		grp.add(surfacePlot);
		plotTypeActions.put(PlotType.SURFACE, surfacePlot);
		//surfacePlot.setEnabled(false);
		
		man.add(new Separator("group2"));
		
		this.updateAutomatically = new Action("Update plot when slice changes", IAction.AS_CHECK_BOX) {
			public void run() {
				slice(false);
			}
		};
		updateAutomatically.setToolTipText("Update plot when slice changes");
		updateAutomatically.setChecked(true);
		updateAutomatically.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.png"));
		man.add(updateAutomatically);
		
		man.add(new Separator("group3"));
		Action openGallery = new Action("Open data set in a gallery.\nFor instance a gallery of images.", Activator.getImageDescriptor("icons/imageStack.png")) {
			public void run() {
				openGallery();
			}
		};
		openGallery.setId(openGallery.getText());
		man.add(openGallery);
		man.add(new Separator("group4"));

		final CheckableActionGroup grp2 = new CheckableActionGroup();
		final MenuAction editorMenu = new MenuAction("Edit the slice with different editors.");
		man.add(editorMenu);
		editorMenu.setImageDescriptor(Activator.getImageDescriptor("icons/spinner_buttons.png"));
		
		final Action asScale = new Action("Sliding scale", IAction.AS_CHECK_BOX) {
			public void run () {
				
				viewer.cancelEditing();
				Activator.getDefault().getPreferenceStore().setValue(ViewConstants.SLICE_EDITOR, 0);
			}
		};
		grp2.add(asScale);
		asScale.setChecked(Activator.getDefault().getPreferenceStore().getInt(ViewConstants.SLICE_EDITOR)==0);
		editorMenu.add(asScale);
		
		final Action asSpinner = new Action("Slice index (only)", IAction.AS_CHECK_BOX) {
			public void run () {
				viewer.cancelEditing();
				Activator.getDefault().getPreferenceStore().setValue(ViewConstants.SLICE_EDITOR, 1);
			}
		};
		grp2.add(asSpinner);
		asSpinner.setChecked(Activator.getDefault().getPreferenceStore().getInt(ViewConstants.SLICE_EDITOR)==1);
		editorMenu.add(asSpinner);
		
		if (customActions!=null) {
			man.add(new Separator("group5"));
			for (IAction action : customActions) man.add(action);
		}

		man.add(new Separator("group6"));
		this.reverse = new Action("Reverse image axes", Activator.getImageDescriptor("icons/reverse_axes.png")) {
			public void run () {
				dimsDataList.reverseImage();
				viewer.refresh();
				slice(false);
			}
		};
		man.add(reverse);
		return man;
	}
	
	private Map<PlotType, DimsDataList> sliceSettings;
	private void saveSliceSettings() {
		if (dimsDataList==null || dimsDataList.isEmpty()) return;
		if (sliceSettings == null) sliceSettings = new HashMap<PlotType, DimsDataList>(3);
		final DimsDataList ddl = dimsDataList.clone();
		sliceSettings.put(plotType, ddl);
	}

	
	private void updatePlottingType() {
		
		viewer.cancelEditing();
		if (sliceSettings!=null && sliceSettings.containsKey(plotType) && !dimsDataList.isEmpty()) {
			this.dimsDataList = sliceSettings.get(plotType);
		}
		
		final String[] items = getAxisItems();
		typeEditor.setItems(items);
		
		viewer.refresh();
		reverse.setEnabled(plotType==PlotType.IMAGE||plotType==PlotType.SURFACE);
		GridUtils.setVisible(openWindowing, plotType.is3D() && plottingSystem!=null);
		openWindowing.getParent().layout(new Control[]{openWindowing});

		// Save preference
		Activator.getDefault().getPreferenceStore().setValue(SliceConstants.PLOT_CHOICE, plotType.toString());
   		boolean isOk = updateErrorLabel();
   		if (isOk) slice(true);
   	}

	private void setImageOrientationText(final StyledText text) {
		text.setText("");
		text.append("Image Orientation: ");
		Iterator<ITrace> it = plottingSystem.getTraces(IImageTrace.class).iterator();
		if (it.hasNext()) {
			final IImageTrace trace  = (IImageTrace) it.next();
            final ImageOrigin io     = trace.getImageOrigin();
            text.append(io.getLabel());
            /*  Might be need if users get confused.
            if (io==ImageOrigin.TOP_LEFT || io==ImageOrigin.BOTTOM_RIGHT) {
            	String reverseLabel = "    (X and Y are reversed)";
            	int len = text.getText().length();
            	text.append(reverseLabel);
                text.setStyleRange(new StyleRange(len, reverseLabel.length(), null, null, SWT.BOLD));
            }
            */
		} else {
			text.setStyleRange(null);
			text.setText("");
		}
	}
	
	private IPaletteListener orientationListener;
	private void addImageOrientationListener(final StyledText text) {
		Iterator<ITrace> it = plottingSystem.getTraces(IImageTrace.class).iterator();
		if (it.hasNext()) {
			final IImageTrace trace  = (IImageTrace) it.next();
            if (orientationListener == null) {
            	orientationListener = new IPaletteListener.Stub() {
            		@Override
            		public void imageOriginChanged(PaletteEvent evt) {
    					setImageOrientationText(text);
    					slice(true);
           		    }      
            	};
            }
            // PaletteListeners are cleared when traces are removed.
            trace.addPaletteListener(orientationListener);
		}
	}

	private void updateAxesChoices() {
		dimensionNames.clear();
		for (int idim =1; idim<=dimsDataList.size(); ++idim) {
			updateAxis(idim);
		}
	}
	
	/**
	 * 
	 * @param idim 1 based index of axis.
	 */
	private void updateAxis(int idim) {
		
		try {    	
			if (!dimsDataList.isExpression() && !HierarchicalDataFactory.isHDF5(sliceObject.getPath())) {
				sliceObject.setNexusAxis(idim, "indices");
				dimensionNames.put(idim, Arrays.asList("indices"));
				return;
			}

			List<String> names = null;
			// Nexus axes
			try {
				if (sliceObject.getPath()!=null && sliceObject.getName()!=null) {
				    names = NexusUtils.getAxisNames(sliceObject.getPath(), sliceObject.getName(), idim);
				}
			} catch (Throwable ne) {
				if (!dimsDataList.isExpression()) throw ne; // Expressions, we don't care that
				                                            // cannot read nexus
			}
			names = names!=null ? names : new ArrayList<String>(1);
			
			// Add any expressions 
	    	final IExpressionObjectService service = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
	        final List<IExpressionObject>  exprs   = service.getActiveExpressions(sliceObject.getPath());

	        if (exprs!=null) {
				final int size = this.lazySet.getShape()[idim-1];
				
				for (IExpressionObject iExpressionObject : exprs) {
					final ILazyDataset set = iExpressionObject.getLazyDataSet(iExpressionObject.getExpressionName(), new IMonitor.Stub());
					if (set.getRank()==1 && set.getSize()==size){
						final String name = iExpressionObject.getExpressionName()+" [Expression]";
						names.add(name);
						final IDataset axisData = iExpressionObject.getDataSet(iExpressionObject.getExpressionName(), new IMonitor.Stub());
						sliceObject.putExpressionAxis(name, axisData);
					}
				}				
	        }

	        // indices, last but not least.
			names.add("indices");
			dimensionNames.put(idim, names);
			
			final String dimensionName = sliceObject.getNexusAxis(idim);
			if (!names.contains(dimensionName)) {
				// We get an axis not used elsewhere for the default
				final Map<Integer,String> others = new HashMap<Integer,String>(sliceObject.getNexusAxes());
				others.keySet().removeAll(Arrays.asList(idim));
				boolean found = false;
				Collection<String> values = others.values();
				for (String n : names) {
					if (!values.contains(n)) {
						sliceObject.setNexusAxis(idim, n);
						found = true;
						break;
					}
				}
				if (!found) {
					sliceObject.setNexusAxis(idim, "indices");
					//dimensionNames.put(idim, Arrays.asList("indices"));
				}
			}
			

			
		} catch (Throwable e) {
			logger.info("Cannot assign axes!", e);
			sliceObject.setNexusAxis(idim, "indices");
			dimensionNames.put(idim, Arrays.asList("indices"));
			
		}
	}
	
	protected void openGallery() {
		
		if (sliceReceiverId==null) return;
		SliceObject cs;
		try {
			cs = SliceUtils.createSliceObject(dimsDataList, dataShape, sliceObject);
		} catch (Exception e1) {
			logger.error("Cannot create a slice!");
			return;
		}
		
		IViewPart view;
		try {
			view = EclipseUtils.getActivePage().showView(sliceReceiverId);
		} catch (PartInitException e) {
			logger.error("Cannot find view "+sliceReceiverId);
			return;
		}
		if (view instanceof ISliceGallery) {
			((ISliceGallery)view).updateSlice(lazySet, cs);
		}
		
	}

	private void createDimsData(boolean isExpression) {
		
		final int dims = dataShape.length;
		
		if (plottingSystem!=null) {
			final File dataFile     = new File(sliceObject.getPath());
			final File lastSettings = new File(DawbUtils.getDawnHome()+dataFile.getName()+getSafeFileName(sliceObject.getName())+".xml");
			if (lastSettings.exists()) {
				XMLDecoder decoder = null;
				try {
					this.dimsDataList = new DimsDataList();
					decoder = new XMLDecoder(new FileInputStream(lastSettings));
					
					int from = 0;
					Object firstObject = decoder.readObject();
					try {
						this.plotType = (PlotType)firstObject;
					} catch (Throwable ne) {
						dimsDataList.add((DimsData)firstObject);
						from = 1;
					}

					for (int i = from; i < dims; i++) {
						dimsDataList.add((DimsData)decoder.readObject());
					}
									
					
				} catch (Exception ne) {
					// This might not always be an error.
					logger.debug("Cannot load slice data from last settings!");
				} finally {
					if (decoder!=null) decoder.close();
				}
			}
		}
		
		if (dimsDataList==null || dimsDataList.size()!=dataShape.length) {
			try {
				this.dimsDataList = new DimsDataList(dataShape);
			} catch (Exception e) {
				logger.error("Cannot make new dims data list!", e);
			}
			
		}
		
		if (dimsDataList!=null) {
			if (plotType==null) {
				try {
				    plotType = PlotType.valueOf(Activator.getDefault().getPreferenceStore().getString(SliceConstants.PLOT_CHOICE));
				    if (dimsDataList.getAxisCount()<2) plotType = PlotType.XY;
				} catch (Throwable ignored) {
					// Ok then
				}
			}

			if (plotType==null) plotType = dimsDataList.getAxisCount()>1 ? PlotType.IMAGE : PlotType.XY;
			final Action action = plotTypeActions.get(plotType);
			action.setChecked(true);
			
			// We make sure that the size is not outside
			for (int i = 0; i < dims; i++) {
				DimsData dd = dimsDataList.getDimsData(i);
				if (dd!=null) {
					if (dd.getSlice()>=dataShape[i]) {
						dd.setSlice(0);
					}
				}
			}

		}

		if (plotType==null) plotType = PlotType.XY;
		reverse.setEnabled(plotType==PlotType.IMAGE||plotType==PlotType.SURFACE);
		
		// Parse if ranges allowed to try to assign at least one dims data to a range
		if (rangesAllowed) {
			final int[] shape = this.lazySet.getShape();
			for (int dim = 0; dim < shape.length; dim++) {
				DimsData dd = dimsDataList.getDimsData(dim);
			    if (dd.isSlice() && shape[dim]>1) { // Slice found
			    	dd.setPlotAxis(DimsData.RANGE);
			    	break;
			    }
			}
		}
		
		dimsDataList.setExpression(isExpression);
	}

	/**
	 * Method ensures that one x and on y are defined.
	 * @param data
	 * @return true if no error
	 */
	protected boolean synchronizeSliceData(final DimsData data) {
				
		final int usedAxis = data!=null ? data.getPlotAxis() : -2;
		
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).equals(data)) continue;
			if (dimsDataList.getDimsData(i).getPlotAxis()==usedAxis) dimsDataList.getDimsData(i).setPlotAxis(-1);
		}
		
		Display.getCurrent().syncExec(new Runnable() {
			public void run() {
		        updateErrorLabel();
			}
		});
		return !errorLabel.isVisible();
	}
	
	/**
	 * returns true if there is no error
	 * @return
	 */
	private boolean updateErrorLabel() {
				
		boolean isX = false;
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).getPlotAxis()==0) isX = true;
		}
		boolean isY = false;
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).getPlotAxis()==1) isY = true;
		}

		String errorMessage = "";
		boolean ok = false;
		if (plotType==PlotType.XY) {
			ok = isX;
			errorMessage = "Please set an X axis.";
		} else {
			ok = isX&&isY;
			errorMessage = "Please set an X and Y axis or switch to 'Slice as line plot'.";
		}
		
		
		if (!ok) {
			errorLabel.setText(errorMessage);
		}
		GridUtils.setVisible(errorLabel,         !(ok||rangesAllowed));
		isErrorCondition = errorLabel.isVisible();
		updateAutomatically.setEnabled(ok&&plottingSystem!=null);
		errorLabel.getParent().layout(new Control[]{errorLabel});

		return ok;
	}



	protected void updateSlice(boolean doSlice) {
		final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
		final Scale scale = (Scale)scaleEditor.getControl();
		final int value = scale.getSelection();
		data.setSlice(value);
		data.setSliceRange(null);
		scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));		
		if (doSlice&&synchronizeSliceData(data)) slice(false);
	}

	@SuppressWarnings("unused")
	protected String[] getAxisItems() {
		String[] items = null;
		if (plotType==PlotType.XY) {
			items = new String[]{"X"};
		} else if (plotType==PlotType.XY_STACKED) {
			items = new String[]{"X","Y (Many)"};
		} else {
			if (isReversedImage()) {
				items = new String[]{"Y","X"};
			} else {
				items = new String[]{"X","Y"};
			}
		}
		if (items!=null) {
			if (rangesAllowed) {
				String[] ret = new String[items.length+2];
				for (int i = 0; i < items.length; i++) ret[i] = items[i];
				ret[ret.length-2] = "(Slice)";
				ret[ret.length-1] = "(Range)";
				return ret;
			} else {
				String[] ret = new String[items.length+1];
				for (int i = 0; i < items.length; i++) ret[i] = items[i];
				ret[ret.length-1] = "(Slice)";
				return ret;
			}
		}
		return null;
	}

	protected String getScaleTooltip(DimsData data, int minimum, int maximum) {
		
		int value = data.getSlice();
        final StringBuffer buf = new StringBuffer();
        
        IDataset axis = null;
        if (axesVisible) try {
			final String axisName = SliceUtils.getNexusAxisName(sliceObject, data);
            axis = SliceUtils.getNexusAxis(this.sliceObject, axisName, false, null);
        } catch (Throwable ne) {
        	axis = null;
        }
        
        String min = String.valueOf(minimum);
        String max = String.valueOf(maximum);
        String val = String.valueOf(value);
        try {
	        if (axis!=null) {
				min = format.format(axis.getDouble(minimum));
				max = format.format(axis.getDouble(maximum));
				val = format.format(axis.getDouble(value));
	        } 
        } catch (Throwable ignored) {
        	// Use indices
        }
    
        buf.append(min);
        buf.append(" <= ");
        buf.append(val);
        buf.append(" <= ");
        buf.append(max);
        return buf.toString();
	}

	private void createColumns(final TableViewer viewer, TableColumnLayout layout) {
		
		final TableViewerColumn dim   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		dim.getColumn().setText("Dim");
		layout.setColumnData(dim.getColumn(), new ColumnWeightData(42));
		dim.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(0)));
		
		final TableViewerColumn type   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		type.getColumn().setText("Type");
		layout.setColumnData(type.getColumn(), new ColumnWeightData(65));
		type.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(1)));
		type.setEditingSupport(new TypeEditingSupport(viewer));

		final TableViewerColumn slice   = new TableViewerColumn(viewer, SWT.LEFT, 2);
		slice.getColumn().setText("Slice Value");
		layout.setColumnData(slice.getColumn(), new ColumnWeightData(140));
		slice.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(2)));
		slice.setEditingSupport(new SliceEditingSupport(viewer));
		
		if (axesVisible) {
			final TableViewerColumn axis   = new TableViewerColumn(viewer, SWT.LEFT, 3);
			axis.getColumn().setText("Axis Data");
			layout.setColumnData(axis.getColumn(), new ColumnWeightData(140));
			axis.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(3)));
			axis.setEditingSupport(new AxisEditingSupport(viewer));
		}
	}	

	private class AxisEditingSupport extends EditingSupport {
	
		private CComboCellEditor axisDataEditor;

		public AxisEditingSupport(ColumnViewer viewer) {
			super(viewer);
			this.axisDataEditor = new CComboCellEditor(((TableViewer)viewer).getTable(), new String[]{"indices"}, SWT.READ_ONLY) {
				protected int getDoubleClickTimeout() {
					return 0;
				}		
				
				public void activate() {
					final DimsData     data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
					final int idim  = data.getDimension()+1;
					final List<String> names = dimensionNames.get(idim);
					final String[] items = names.toArray(new String[names.size()]);
					
					if (!Arrays.equals(this.getCombo().getItems(), items)) {
						this.getCombo().setItems(items);
					}
					
					final int isel = names.indexOf(sliceObject.getNexusAxis(idim));
					if (isel>-1 && getCombo().getSelectionIndex()!=isel) {
						this.getCombo().select(isel);
					}
					super.activate();
				}
			};
			
		}
		@Override
		protected CellEditor getCellEditor(Object element) {
			return axisDataEditor;
		}
	
		@Override
		protected boolean canEdit(Object element) {
			final DimsData data = (DimsData)element;
		    boolean isSliceIndex = Activator.getDefault().getPreferenceStore().getInt(ViewConstants.SLICE_EDITOR)==1;
			return isSliceIndex ? data.getPlotAxis()>-1 : true;
		}
	
		@Override
		protected Object getValue(Object element) {
			final DimsData data = (DimsData)element;
			final int idim  = data.getDimension()+1;
			final String dimensionDataName = sliceObject.getNexusAxis(idim);
			final List<String> names = dimensionNames.get(idim);
			int selection = names.indexOf(dimensionDataName);
			return selection>-1 ? selection : 0;
		}
	
		@Override
		protected void setValue(Object element, Object value) {
			final DimsData data = (DimsData)element;
			final int idim  = data.getDimension()+1;
			if (value instanceof Integer) {
				final List<String> names = dimensionNames.get(idim);
				sliceObject.setNexusAxis(idim, names.get(((Integer)value).intValue()));
			} else {
				sliceObject.setNexusAxis(idim, (String)value);
		    }
			update(data);
		}
	
	}
	private ScaleCellEditor                 scaleEditor;
	private SpinnerCellEditorWithPlayButton spinnerEditor;
	private TextCellEditor                  rangeEditor;
	private CComboCellEditor                typeEditor;

	private class SliceEditingSupport extends EditingSupport {

		public SliceEditingSupport(ColumnViewer viewer) {
			super(viewer);
			
			scaleEditor = new ScaleCellEditor((Composite)viewer.getControl(), SWT.NO_FOCUS);
			final Scale scale = (Scale)scaleEditor.getControl();
			scale.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			scaleEditor.setMinimum(0);
			scale.setIncrement(1);
			scale.addMouseListener(new MouseAdapter() {			
				@Override
				public void mouseUp(MouseEvent e) {
					if (!plotType.is3D()) return;
					updateSlice(true);
				}
			});
			scaleEditor.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateSlice(!plotType.is3D());
				}
			});
			
			final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
			spinnerEditor = new SpinnerCellEditorWithPlayButton((TableViewer)viewer, "Play through slices", store.getInt("data.format.slice.play.speed"));
			spinnerEditor.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			spinnerEditor.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
	                final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
	                final int value = ((Spinner)e.getSource()).getSelection();
	                data.setSlice(value);
	                data.setSliceRange(null);
	         		if (synchronizeSliceData(data)) slice(false);
				}
				
			});

			rangeEditor = new TextCellEditor((Composite)viewer.getControl(), SWT.NONE);
			((Text)rangeEditor.getControl()).addModifyListener(new ModifyListener() {			
				@Override
				public void modifyText(ModifyEvent e) {
	                final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
					final Text text = (Text)e.getSource();
					final String range = text.getText();
					
					final Matcher matcher = Pattern.compile("(\\d+)\\:(\\d+)").matcher(range);
					if ("all".equals(range)) {
						text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					} else if (matcher.matches()) {
						final int[] shape = lazySet.getShape();
						int start = Integer.parseInt(matcher.group(1));
						int end   = Integer.parseInt(matcher.group(2));
						if (start>-1&&end>-1&&start<shape[data.getDimension()]&&end<shape[data.getDimension()]) {
						    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
						} else {
							text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						}
					} else {
						text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}			
				}
			});
			((Text)rangeEditor.getControl()).setToolTipText("Please enter \"all\" or a range of the form <int>:<int>.");

		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			
			final DimsData data = (DimsData)element;
			if (data.isRange()) return rangeEditor;
			if (Activator.getDefault().getPreferenceStore().getInt(ViewConstants.SLICE_EDITOR)==1) {
	            spinnerEditor.setMaximum(dataShape[data.getDimension()]-1);
			    return spinnerEditor;
			} else {
				final Scale scale = (Scale)scaleEditor.getControl();
				scale.setMaximum(dataShape[data.getDimension()]-1);
				scale.setPageIncrement(scale.getMaximum()/10);

				scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));
				return scaleEditor;
			}
		}

		@Override
		protected boolean canEdit(Object element) {
			final DimsData data = (DimsData)element;
			if (dataShape[data.getDimension()]<2) return false;
			if (data.isRange()) return true;
			return data.getPlotAxis()<0;
		}

		@Override
		protected Object getValue(Object element) {
			final DimsData data = (DimsData)element;
			if (data.isRange()) return data.getSliceRange() != null ? data.getSliceRange() : "all";
			return data.getSlice();
		}

		@Override
		protected void setValue(Object element, Object value) {
			final DimsData data = (DimsData)element;
			if (value instanceof Integer) {
				data.setSlice((Integer)value);
			} else {
				data.setSliceRange((String)value);
			}
			update(data);
		}

	}

	private String[] getAxisChoices() {
		return rangesAllowed ? new String[]{"X","Y","(Slice)","(Range)"} : new String[]{"X","Y","(Slice)"};
	}

	private class TypeEditingSupport extends EditingSupport {

		public TypeEditingSupport(ColumnViewer viewer) {
			super(viewer);
			typeEditor = new CComboCellEditor(((TableViewer)viewer).getTable(), getAxisChoices(), SWT.READ_ONLY) {
				protected int getDoubleClickTimeout() {
					return 0;
				}	

				public void activate() {
					String[] items = getAxisItems();
					if (!Arrays.equals(this.getCombo().getItems(), items)) {
						this.getCombo().setItems(items);
					}
					super.activate();
				}

			};
			final CCombo combo = typeEditor.getCombo();
			combo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					
					if (!typeEditor.isActivated()) return;
					final String   value = combo.getText();
					if ("".equals(value) || "(Slice)".equals(value)) {
						typeEditor.applyEditorValueAndDeactivate(-1);
						return; // Bit of a bodge
					}
					if ("(Range)".equals(value)) {
						typeEditor.applyEditorValueAndDeactivate(DimsData.RANGE);
						return; // Bit of a bodge
					}
					final String[] items = typeEditor.getItems();
					if (items!=null) for (int i = 0; i < items.length; i++) {
						if (items[i].equalsIgnoreCase(value)) {
							typeEditor.applyEditorValueAndDeactivate(i);
							return;
						}
					}
				}
			});
		}

		@Override
		protected CellEditor getCellEditor(Object element) {			
			return typeEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			final DimsData data = (DimsData)element;
			int axis = data.getPlotAxis();
			if (axis==-1) {
				final String[] items = typeEditor.getCombo().getItems();
				axis = items.length-1; // (Slice)
			}
			return axis;
		}

		@Override
		protected void setValue(Object element, Object value) {
			final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
			if (data==null) return;
			int axis = (Integer)value;
			if (plotType==PlotType.XY) axis = axis>-1 ? 0 : -1;
			data.setPlotAxis(axis);
			updateAxesChoices();
			update(data);
			fireDimensionalListeners();
		}

	}
	
	private Collection<DimensionalListener> dimensionalListeners;
	public void addDimensionalListener(DimensionalListener l) {
		if (dimensionalListeners==null) dimensionalListeners= new HashSet<DimensionalListener>(7);
		dimensionalListeners.add(l);
	}
	
	public void removeDimensionalListener(DimensionalListener l) {
		if (dimensionalListeners==null) return;
		dimensionalListeners.remove(l);
	}
	
	protected void fireDimensionalListeners() {
		if (dimensionalListeners==null) return;
		final DimensionalEvent evt = new DimensionalEvent(this, dimsDataList);
		for (DimensionalListener l : dimensionalListeners) {
			l.dimensionsChanged(evt);
		}
	}

	private void update(DimsData data) {
		final boolean isValidData = synchronizeSliceData(data);
		viewer.cancelEditing();
		viewer.refresh();
		
		if (isValidData) slice(false);
	}

	private class SliceColumnLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

		private int col;
		public SliceColumnLabelProvider(int i) {
			this.col = i;
		}
		@Override
		public StyledString getStyledText(Object element) {
			
			if (viewer.getTable().getColumn(col).getWidth()<1) return new StyledString();
					
			final DimsData data = (DimsData)element;
			final StyledString ret = new StyledString();
			switch (col) {
			case 0:
				ret.append((data.getDimension()+1)+"");
				break;
			case 1:
				ret.append( getAxisLabel(data) );
				break;
			case 2:
				if (data.isRange()) {
					ret.append(data.getSliceRange()!=null? new StyledString(data.getSliceRange()) : new StyledString("all"));
				} else {
					final int slice = data.getSlice();
					String formatValue = String.valueOf(slice);
					try {
						if (axesVisible) {
							Number value = SliceUtils.getNexusAxisValue(sliceObject, data, slice, null);
							formatValue = format.format(value);
						} else {
							formatValue = String.valueOf(slice);
						}
					} catch (Throwable ne) {
						formatValue = String.valueOf(slice);
					}
					ret.append( slice>-1 ? formatValue : "" );
				}
				
				try {
					if ((data.isSlice() || data.isRange()) && !errorLabel.isVisible() && lazySet.getShape()[data.getDimension()]>1) {
						ret.append(new StyledString(" (click to change)", StyledString.QUALIFIER_STYLER));
					}
				} catch (Throwable largelyIgnored) {
					logger.error("Unable to determine if editable.");
				}
				break;
			case 3:
				if (sliceObject!=null) {
					final String axisName = SliceUtils.getNexusAxisName(sliceObject, data);
					if (axisName!=null) ret.append(axisName);
				}
			default:
				ret.append( "" );
				break;
			}
			
			return ret;
		}
				
	}
	
	private SliceSource sliceSource;
	public SliceSource getData() {
		return sliceSource;
	}
	
	/**
	 * Call this method to show the slice dialog.
	 * 
	 * This non-modal dialog allows the user to slice
	 * data out of n-D data sets into a 2D plot.
	 * 
	 * This method is not thread safe, please call in the UI thread.
	 */
	public void setData(SliceSource source) {
		
		if (Display.getDefault().getThread()!=Thread.currentThread()) {
			throw new SWTError("Please call setData(...) in slice component from the UI thread only!");
		}
		sliceJob.cancel();
		saveSettings();
		if (sliceSettings!=null) sliceSettings.clear();

		this.sliceSource = source;
		this.lazySet     = source.getLazySet();
		final SliceObject object = new SliceObject();
		object.setPath(source.getFilePath());
		object.setName(source.getDataName());
		setSliceObject(object);
		setDataShape(lazySet.getShape());
		
		explain.setText("Create a slice of "+sliceObject.getName()+".\nIt has the shape "+Arrays.toString(dataShape));
		spinnerEditor.setPlayButtonVisible(false);
		
		createDimsData(source.isExpression());
		updateAxesChoices();
		viewer.refresh();
    	
		synchronizeSliceData(null);
		slice(true);
		
		if (plottingSystem==null) {
			updateAutomatically.setEnabled(false);
			viewer.getTable().getColumns()[2].setText("Start Index or Slice Range");
		}
		
		final String[] items = getAxisItems();
		typeEditor.setItems(items);

	}
	
	public void setLabel(final String text) {
		explain.setText(text);
	}

	public String getAxisLabel(DimsData data) {

		final int axis = data.getPlotAxis();
		if (data.isRange()) return "(Range)";
		if (plotType==PlotType.XY) {
			return axis>-1 ? "X" : "(Slice)";
		}
		if (plotType==PlotType.XY_STACKED) {
			return axis==0 ? "X" : axis==1 ? "Y (Many)" : "(Slice)";
		}
		if (plottingSystem!=null) {
			if (isReversedImage()) {
				return axis==0 ? "Y" : axis==1 ? "X" : "(Slice)";				
			}
		}
		return axis==0 ? "X" : axis==1 ? "Y" : "(Slice)";
	}


	protected boolean isReversedImage() {
		if (plottingSystem==null) return false;
		Iterator<ITrace> it = plottingSystem.getTraces(IImageTrace.class).iterator();
		if (it.hasNext()) {
			final IImageTrace trace = (IImageTrace) it.next();
			return trace.getImageOrigin()==ImageOrigin.TOP_LEFT || trace.getImageOrigin()==ImageOrigin.BOTTOM_RIGHT;
		} else {
			try {
				final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
				ImageOrigin origin = ImageOrigin.forLabel(store.getString("org.dawb.plotting.system.originChoice"));
				return origin==ImageOrigin.TOP_LEFT || origin==ImageOrigin.BOTTOM_RIGHT;
			} catch (Throwable e) {
				return true;
			}
		}
	}
	
	/**
	 * Does slice in monitored job
	 */
	public void slice(final boolean force) {
		if (plottingSystem==null) return;
		if (!force) {
		    if (updateAutomatically!=null && !updateAutomatically.isChecked()) return;
		}

		try {
			SliceObject cs = SliceUtils.createSliceObject(dimsDataList, dataShape, sliceObject);
			sliceJob.schedule(cs);
		} catch (Exception e) {
			logger.error("Cannot create a slice object!");
		}
	}
	
	public void dispose() {
		if (plottingSystem!=null && traceListener!=null) {
			plottingSystem.removeTraceListener(traceListener);	
		}
		if (dimensionalListeners!=null) dimensionalListeners.clear();
		dimensionalListeners = null;
		sliceJob.cancel();
		saveSettings();
	}
	
	private void saveSettings() {
		
		if (sliceObject == null || isErrorCondition) return;
		
		final File dataFile     = new File(sliceObject.getPath());
		final File lastSettings = new File(DawbUtils.getDawnHome()+dataFile.getName()+"."+getSafeFileName(sliceObject.getName())+".xml");
		if (!lastSettings.getParentFile().exists()) lastSettings.getParentFile().mkdirs();
	
		XMLEncoder encoder=null;
		try {
			encoder = new XMLEncoder(new FileOutputStream(lastSettings));
			encoder.writeObject(this.plotType);
			if (dimsDataList!=null) {
				for (int i = 0; i < dimsDataList.size(); i++) {
					encoder.writeObject(dimsDataList.getDimsData(i));
				}
			}
		} catch (Throwable ne) {
			logger.error("Cannot save slice data from last settings!", ne);
		} finally  {
			if (encoder!=null) encoder.close();
		}
	}
	
	private String getSafeFileName(String name) {
		return name.replaceAll("[^a-zA-Z0-9_\\-]", "");
	}

	public void setSliceObject(SliceObject sliceObject) {
		this.sliceObject = sliceObject;
	}

	public void setDataShape(int[] shape) {
		this.dataShape = shape;
	}

	/**
	 * Normally call before createPartControl(...)
	 * @param plotWindow
	 */
	public void setPlottingSystem(IPlottingSystem plotWindow) {
		this.plottingSystem = plotWindow;
	}

	/**
	 * Throws exception if GUI disposed.
	 * @param vis
	 */
	public void setVisible(final boolean vis) {
		area.setVisible(vis);
		area.getParent().layout(new Control[]{area});
		if (plottingSystem!=null && !vis) plottingSystem.setPlotType(PlotType.XY);
		if (!vis) {
			sliceJob.cancel();
			saveSettings();
		}
	}

	public void setSliceIndex(int dimension, int index, boolean doSlice) {
		viewer.cancelEditing();
		this.dimsDataList.getDimsData(dimension).setSlice(index);
		viewer.refresh();
		if (doSlice) slice(true);
	}
	
	public DimsDataList getDimsDataList() {
		return dimsDataList;
	}
	public Map<Integer,String> getAxesNames() {
		return sliceObject.getNexusAxes();
	}

	public void setDimsDataList(DimsDataList dimsDataList) {
		this.dimsDataList = dimsDataList;
		viewer.refresh();
	}

	private class SliceJob extends Job {
		
		private SliceObject slice;
		public SliceJob() {
			super("Slice");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			if (slice==null) return Status.CANCEL_STATUS;
			monitor.beginTask("Slice "+slice.getName(), 10);
			try {
				monitor.worked(1);
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			
				SliceUtils.plotSlice(lazySet,
						             slice, 
						             dataShape, 
						             plotType, 
						             plottingSystem, 
						             monitor);
			} catch (Exception e) {
				logger.error("Cannot slice "+slice.getName(), e);
				System.out.println(slice);
			} finally {
				monitor.done();
			}	
			
			return Status.OK_STATUS;
		}

		public void schedule(SliceObject cs) {
			if (slice!=null && slice.equals(cs)) return;
			cancel();
			this.slice = cs;
			schedule();
		}	
	}

	public void addCustomAction(IAction customAction) {
		if (customActions == null)customActions = new ArrayList<IAction>();
		customActions.add(customAction);
	}

	public void refresh() {
		viewer.refresh();
	}

	public ILazyDataset getLazyDataset() {
		return lazySet;
	}

	public SliceObject getCurrentSlice() {
		return sliceObject;
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

}
