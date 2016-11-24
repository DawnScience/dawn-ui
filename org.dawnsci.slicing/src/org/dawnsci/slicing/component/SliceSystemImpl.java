/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.DawbUtils;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.dawnsci.slicing.Activator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.SliceObject;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.slicing.api.AbstractSliceSystem;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.RangeMode;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


/**
 * Dialog to slice multi-dimensional data to images and 1D plots.
 * 
 * Copied from nexus tree viewer but in a simpler to use UI.
 *  
 *
 */
public class SliceSystemImpl extends AbstractSliceSystem {

	private static final List<String> COLUMN_PROPERTIES = Arrays.asList(new String[]{"Dimension","Axis","Slice","Axis Data","Span"});
	
	private ILazyDataset    lazySet; // The dataset that we are slicing.
	private int[]           dataShape;

	private TableViewer     viewer;

	private CLabel          errorLabel, explain, infoLabel;
	private Composite       area;
	private boolean         isErrorCondition=false;
    private SliceJob        sliceJob;
     
    private Action          updateAutomatically;

	private ITraceListener.Stub traceListener;
	
	private TypeEditingSupport  typeEditingSupport;
	private SliceEditingSupport sliceEditingSupport;
	private AxisEditingSupport  axisEditingSupport;
	
	public SliceSystemImpl() {
		this.sliceJob        = new SliceJob(this);
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
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =5;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;
		area.setLayout(layout);
		
		this.explain = new CLabel(area, SWT.WRAP);
		final GridData eData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		eData.heightHint=44;
		explain.setLayoutData(eData);
		GridUtils.setVisible(explain, false);
	
		this.sliceToolbar = createSliceTools();
		final ToolBar        tool    = ((ToolBarManager)sliceToolbar).createControl(area);
		tool.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		this.viewer = new TableViewer(area, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				event.doit=false;
				// Do nothing disabled
			}
		});		
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = 45;
			}
		});

		createColumns(viewer);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(COLUMN_PROPERTIES.toArray(new String[COLUMN_PROPERTIES.size()]));			
		
		final Composite bottom = new Composite(area, SWT.NONE);
		bottom.setLayout(new GridLayout(1, false));
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.errorLabel = new CLabel(bottom, SWT.WRAP);
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		errorLabel.setImage(Activator.getImageDescriptor("icons/error.png").createImage());
		GridUtils.setVisible(errorLabel,         false);
		
		this.infoLabel = new CLabel(bottom, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.setVisible(infoLabel,         false);

		// Something to tell them their image orientation (X and Y may be mixed up!)
		if (plottingSystem!=null) {
			final StyledText imageOrientation = new StyledText(area, SWT.NONE);
			imageOrientation.setEditable(false);
			imageOrientation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			imageOrientation.setBackground(area.getBackground());
			GridUtils.setVisible(imageOrientation, plottingSystem.is2D());
			
			addImageOrientationListener(imageOrientation);
			
			this.traceListener = new ITraceListener.Stub() {
				protected void update(TraceEvent evt) {
					if( imageOrientation.isDisposed()) return;
					GridUtils.setVisible(imageOrientation, plottingSystem.is2D()&&plottingSystem.getPlotType()!=null&&plottingSystem.getPlotType().is2D());
					setImageOrientationText(imageOrientation);
					area.layout();
					addImageOrientationListener(imageOrientation);
				}
			};
			imageOrientation.setToolTipText("The image orientation currently set by the plotting.");
			plottingSystem.addTraceListener(traceListener);
		}
		
		for (String id : sliceTools.keySet()) {
			sliceTools.get(id).createToolComponent(area);
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
		setAdvancedColumnsVisible(isAdvanced());	
   	
		return area;
	}
	
	@Override
	public void setToolbarVisible(boolean vis) {
		GridUtils.setVisible(((ToolBarManager)sliceToolbar).getControl(), vis);
		sliceToolbar.update(true);
	}
	
	@Override
	public ISelectionProvider getSelectionProvider() {
		return viewer;
	}
	
	@Override
	public void setSliceMetadata(final IMetadata sliceMeta) {
		
        super.setSliceMetadata(sliceMeta);
        
        Display.getDefault().syncExec(new Runnable() {
        	public void run() {
               viewer.setSelection(viewer.getSelection());
				String path = null;
				if (sliceMeta != null) {
					path = sliceMeta.getFilePath();
				}

				if (path == null && sliceObject != null) {
					path = sliceObject.getPath();
				}

				if (path != null) {
	               final IWorkbenchPart part = getPlottingSystem() != null ? getPlottingSystem().getPart() : null;
	               if (part!=null && part instanceof ITitledEditor) {
	            	   final File file = new File(path);
	            	   ((ITitledEditor)part).setPartTitle(file.getName());
	               }
               }
        	}
        });
	}

	@Override
	public void setSliceTypeInfo(String label, ImageDescriptor icon) {
		if (label==null && icon==null) {
			GridUtils.setVisible(infoLabel,         false);
		} else {
		    GridUtils.setVisible(infoLabel,         true);
		    infoLabel.setText(" "+label);
		    if (icon==null) icon = getActionByPlotType(sliceType).getImageDescriptor();
		    infoLabel.setImage(icon.createImage(Display.getDefault()));
		    infoLabel.setText(" "+label);
		}
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
	public boolean isAxesVisible() {
		return axesVisible;
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
	
	private Action                reverse;

	/**
	 * Creates the actions for 
	 * @return
	 */
	protected IToolBarManager createSliceTools() {
		
		final ToolBarManager man = new ToolBarManager(SWT.FLAT|SWT.RIGHT|SWT.WRAP);
		man.add(new Separator("sliceTools"));

		final Action showTips = new Action("Show tooltips while editing the slice setup", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(SliceConstants.SHOW_HINTS, isChecked());
			}
		};
		showTips.setImageDescriptor(Activator.getImageDescriptor("icons/wand.png"));
		showTips.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(SliceConstants.SHOW_HINTS));
        man.add(showTips);		
		man.add(new Separator("group0"));

		// Add action for Setting the tools into advanced mode.
		advanced = new Action("Advanced slicing.\nFor instance, shows extra options for 'Type' including mean and median.", IAction.AS_CHECK_BOX) {
			public void run() {
				setAdvanced(isChecked());
				viewer.cancelEditing();
			}
		};
		advanced.setImageDescriptor(Activator.getImageDescriptor("icons/graduation-hat.png"));
		advanced.setChecked(isAdvanced());
        man.add(advanced);		
		man.add(new Separator("group1"));

		super.createSliceTools(man);
		
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
		
		final Action asScale = new Action("Sliding scale", IAction.AS_CHECK_BOX) {
			public void run () {
				
				viewer.cancelEditing();
				Activator.getDefault().getPreferenceStore().setValue(SliceConstants.SLICE_EDITOR, 0);
			}
		};
		grp2.add(asScale);
		asScale.setChecked(Activator.getDefault().getPreferenceStore().getInt(SliceConstants.SLICE_EDITOR)==0);
		
		final Action asSpinner = new Action("Slice index (only)", IAction.AS_CHECK_BOX) {
			public void run () {
				viewer.cancelEditing();
				Activator.getDefault().getPreferenceStore().setValue(SliceConstants.SLICE_EDITOR, 1);
			}
		};
		grp2.add(asSpinner);
		asSpinner.setChecked(Activator.getDefault().getPreferenceStore().getInt(SliceConstants.SLICE_EDITOR)==1);
				
				
		final MenuAction editorMenu = new MenuAction("Edit the slice with different editors.");
		man.add(editorMenu);
		editorMenu.setImageDescriptor(Activator.getImageDescriptor("icons/spinner_buttons.png"));
		editorMenu.add(asScale);
		editorMenu.add(asSpinner);
		
		
		createCustomActions(man);

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
	
	private Map<Enum, DimsDataList> sliceSettings;
	
	@Override
	protected void saveSliceSettings() {
		if (dimsDataList==null || dimsDataList.isEmpty()) return;
		if (sliceSettings == null) sliceSettings = new HashMap<Enum, DimsDataList>(3);
		final DimsDataList ddl = dimsDataList.clone();
		sliceSettings.put(sliceType, ddl);
	}

	
	@Override
	public void update(boolean disable) {
		
		viewer.cancelEditing();
		if (sliceSettings!=null && sliceSettings.containsKey(sliceType) && !dimsDataList.isEmpty()) {
			this.dimsDataList = sliceSettings.get(sliceType);
		}
		
		typeEditingSupport.updateChoices();
		
		viewer.refresh();
		reverse.setEnabled(sliceType==PlotType.IMAGE||sliceType==PlotType.SURFACE);

		// Save preference
		Activator.getDefault().getPreferenceStore().setValue(SliceConstants.PLOT_CHOICE, sliceType.toString());
   		boolean isOk = updateErrorLabel();
   		if (isOk) {
   			if (disable) setEnabled(false);
   			slice(true);
   		}
   	}

	private void setImageOrientationText(final StyledText text) {
		text.setText("");
		text.append(" Image Orientation: ");
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

	public void fireDimensionalListeners() {
        super.fireDimensionalListeners();
	}
	public void fireAxisChoiceListeners(AxisChoiceEvent evt) {
        super.fireAxisChoiceListeners(evt);
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
						this.sliceType = (PlotType)firstObject;
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
			if (sliceType==null) {
				try {
				    sliceType = PlotType.valueOf(Activator.getDefault().getPreferenceStore().getString(SliceConstants.PLOT_CHOICE));
				    if (dimsDataList.getAxisCount()<2) sliceType = PlotType.XY;
				} catch (Throwable ignored) {
					// Ok then
				}
			}

			if (sliceType==null) sliceType = dimsDataList.getAxisCount()>1 ? PlotType.IMAGE : PlotType.XY;
			final IAction action = getActionByPlotType(sliceType);
			if (action!=null) action.setChecked(true);
			
			// We make sure that the size is not outside
			for (int i = 0; i < dims; i++) {
				DimsData dd = dimsDataList.getDimsData(i);
				if (dd!=null) {
					if (dd.getSlice()>=dataShape[i]) {
						dd.setSlice(0);
					}
				}
 				if (sliceSource!=null && sliceSource.getLazySet()!=null) {
 					final int max = LazyDataset.getMaxSliceLength(sliceSource.getLazySet(), i);
 					dd.setSliceSpan(Math.min(dd.getSliceSpan(), max));
 				}
			}

		}

		if (sliceType==null) sliceType = PlotType.XY;
		reverse.setEnabled(sliceType==PlotType.IMAGE||sliceType==PlotType.SURFACE);
		
		// Parse if ranges allowed to try to assign at least one dims data to a range
		if (getRangeMode().isRange() && lazySet!=null) {
			final int[] shape = this.lazySet.getShape();
			for (int dim = 0; dim < shape.length; dim++) {
				DimsData dd = dimsDataList.getDimsData(dim);
			    if (dd.isSlice() && shape[dim]>1) { // Slice found
			    	dd.setPlotAxis(AxisType.RANGE);
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
		
		// SLICE is currently the only PlotAxis type which can be set on multiple
		// different axes. Range can be multiple if the RangeMode is set to MULTI
		final AxisType usedAxis = data!=null ? data.getPlotAxis() : AxisType.NONE;		
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).equals(data)) continue;
			if (dimsDataList.getDimsData(i).getPlotAxis()==usedAxis) {
				if (getRangeMode()!=RangeMode.MULTI_RANGE) {
				    dimsDataList.getDimsData(i).setPlotAxis(AxisType.SLICE);
				} else if (data.getPlotAxis() != AxisType.RANGE && data.getPlotAxis() != AxisType.SLICE) {
					dimsDataList.getDimsData(i).setPlotAxis(AxisType.RANGE);
				}
			}
		}
		
		Display.getCurrent().syncExec(new Runnable() {
			public void run() {
		        updateErrorLabel();
			}
		});
		return !errorLabel.isVisible();
	}
	
	private boolean updateErrorLabel() {
        final String errorMessage = checkErrors();
        return updateErrorLabel(errorMessage);
	}
		
	/**
	 * returns true if there is no error
	 * @return
	 */
	private boolean updateErrorLabel(String errorMessage) {
				
		boolean ok = errorMessage==null;
		if (!ok) {
			errorLabel.setText(" "+errorMessage);
		}
		GridUtils.setVisible(errorLabel,         !(ok||getRangeMode().isRange()));
		isErrorCondition = errorLabel.isVisible();
		updateAutomatically.setEnabled(ok&&plottingSystem!=null);
		errorLabel.getParent().layout(new Control[]{errorLabel});

		return ok;
	}	

	private List<TableViewerColumn> advancedColumns;

	private void createColumns(final TableViewer viewer) {
		
		final TableViewerColumn dim   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		dim.getColumn().setText("Dim");
		dim.getColumn().setWidth(42);
		dim.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(this, viewer, 0)));
		
		final TableViewerColumn type   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		type.getColumn().setText("Type");
		type.getColumn().setWidth(65);
		type.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(this, viewer,1)));
		this.typeEditingSupport = new TypeEditingSupport(this, viewer);
		type.setEditingSupport(typeEditingSupport);

		final TableViewerColumn slice   = new TableViewerColumn(viewer, SWT.LEFT, 2);
		slice.getColumn().setText("Slice Value");
		slice.getColumn().setWidth(140);
		slice.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(this, viewer,2)));
		this.sliceEditingSupport = new SliceEditingSupport(this, viewer);
		slice.setEditingSupport(sliceEditingSupport);
		
		if (axesVisible) {
			final TableViewerColumn axis   = new TableViewerColumn(viewer, SWT.LEFT, 3);
			axis.getColumn().setText("Axis Data");
			axis.getColumn().setWidth(140);
			axis.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(this, viewer,3)));
			this.axisEditingSupport = new AxisEditingSupport(this, viewer);
			axis.setEditingSupport(axisEditingSupport);
		
			advancedColumns = new ArrayList<TableViewerColumn>();
			final TableViewerColumn span   = new TableViewerColumn(viewer, SWT.LEFT, 4);
			span.getColumn().setText("Span");
			span.getColumn().setWidth(0);
			span.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(this, viewer,4)));
			span.setEditingSupport(new SpanEditingSupport(this, viewer));
			advancedColumns.add(span);
		}
	}
	
	protected void setAdvancedColumnsVisible(boolean isVis) {
		
		if (advancedColumns==null) return;
		for (TableViewerColumn col : advancedColumns) {
			col.getColumn().setWidth(isVis?80:0);
			col.getColumn().setResizable(isVis?true:false);
			if (isVis) {
				col.getColumn().setWidth(80);
			} else {
				col.getColumn().setWidth(0);
			}
		}
	}

	protected void setAdvanced(boolean advanced) {
		setAdvancedColumnsVisible(advanced);
		super.setAdvanced(advanced);
	}

	/**
	 * Update slice
	 * @param data
	 * @param enabled - can be set to false to grey out table during slice.
	 */
	protected void update(DimsData data, boolean enabled) {
		final boolean isValidData = synchronizeSliceData(data);
		viewer.cancelEditing();
		viewer.refresh();
		
		if (isValidData) {
			setEnabled(enabled);
			slice(false);
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
		if (getActiveTool()!=null) getActiveTool().militarize(true);
		
		final SliceObject object = new SliceObject();
		object.setPath(source.getFilePath());
		object.setName(source.getDataName());
		setSliceObject(object);
		if (lazySet!=null) {
			setDataShape(lazySet.getShape());
			explain.setText("Create a slice of "+sliceObject.getName()+".\nIt has the shape "+Arrays.toString(dataShape));
		}
		
		createDimsData(source.isExpression());
        if (axisEditingSupport!=null) axisEditingSupport.updateAxesChoices();
		viewer.refresh();
    	
		synchronizeSliceData(null);
		slice(true);
		
		if (plottingSystem==null) {
			updateAutomatically.setEnabled(false);
			viewer.getTable().getColumns()[2].setText("Start Index or Slice Range");
		}
		
		if (typeEditingSupport!=null) typeEditingSupport.updateChoices();

		checkToolDimenionsOk();
	}


	public void setLabel(final String text) {
		GridUtils.setVisible(explain, true);
		explain.setText(text);
		explain.getParent().layout();
	}



	protected boolean isReversedImage() {
		if (plottingSystem==null) return false;
		final Collection<ITrace> traces = plottingSystem.getTraces(IImageTrace.class);
		if (traces == null) return false;
		final Iterator<ITrace> it = traces.iterator();
		if (it.hasNext()) {
			final IImageTrace trace = (IImageTrace) it.next();
			return trace.getImageOrigin().isOnLeadingDiagonal();
		} else {
			try {
				final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
				ImageOrigin origin = ImageOrigin.forLabel(store.getString(PlottingConstants.ORIGIN_PREF));
				return origin.isOnLeadingDiagonal();
			} catch (Throwable e) {
				return true;
			}
		}
	}
	/**
	 * Does slice in monitored job
	 */
	protected void slice(final boolean force) {
		if (plottingSystem==null) return;
		if (!force) {
		    if (updateAutomatically!=null && !updateAutomatically.isChecked()) return;
		}

		try {
			SliceObject cs = SliceUtils.createSliceObject(dimsDataList, getData(), sliceObject);
			sliceJob.schedule(sliceType, cs, force);
		} catch (Exception e) {
			logger.error("Cannot create a slice object!", e);
			updateErrorLabel(e.getMessage());
			setEnabled(true);
		}
	}
	
	public void dispose() {
		if (plottingSystem!=null && traceListener!=null) {
			plottingSystem.removeTraceListener(traceListener);	
		}
		advancedColumns.clear();
		super.dispose();
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
			encoder.writeObject(this.sliceType);
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

	private void setDataShape(int[] shape) {
		this.dataShape = shape;
	}

	/**
	 * Throws exception if GUI disposed.
	 * @param vis
	 */
	public void setVisible(final boolean vis) {
		if (getActiveTool()!=null) getActiveTool().demilitarize();
		if (Display.getDefault().getThread()!=Thread.currentThread()) {
			throw new RuntimeException("Thread '"+Thread.currentThread()+"' is not the UI thread!");
		}
		area.setVisible(vis);
		area.getParent().layout();
		//if (plottingSystem!=null && !vis) plottingSystem.setPlotType(PlotType.XY);
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
		return sliceObject.getAxisNames();
	}

	public void setDimsDataList(DimsDataList dimsDataList) {
		this.dimsDataList = dimsDataList;
		viewer.refresh();
	}


    @Override
	public void refresh() {
		viewer.refresh();
		axisEditingSupport.updateAxesChoices();
	}

	public ILazyDataset getLazyDataset() {
		return lazySet;
	}


	@Override
	public void setSlicingEnabled(boolean enabled) {
		viewer.getTable().setEnabled(enabled);
	}


	public boolean isErrorVisible() {
		return errorLabel.isVisible();
	}


	public void updateAxesChoices() {
		if (axisEditingSupport!=null) axisEditingSupport.updateAxesChoices();
	}

	private boolean enabled = true;
	public void setEnabled(boolean enabled) {
		
		if (getPlottingSystem()==null) return;
		this.enabled = enabled;
		if (!enabled) {
			viewer.getTable().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
			viewer.getControl().getParent().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
		} else {
			viewer.getTable().setCursor(null);
			viewer.getControl().getParent().setCursor(null);
		}
		((ToolBarManager)sliceToolbar).getControl().setEnabled(enabled);
		viewer.getTable().setEnabled(enabled);
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Utility method used to save out 3D datasets as double[][][]
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Throwable {
		
		final ILoaderService service = (ILoaderService)Activator.getService(ILoaderService.class);
		final IDataHolder holder = service.getData("C:/Users/fcp94556/Desktop/test.nxs", new IMonitor.Stub());
		final IDataset    set    = holder.getLazyDataset("/entry1/data/data").getSlice();
		final double[][][] da    = new double[set.getShape()[0]][set.getShape()[1]][set.getShape()[2]];
		for (int i = 0; i < set.getShape()[0]; i++) {
			for (int j = 0; j < set.getShape()[1]; j++) {
				for (int k = 0; k < set.getShape()[2]; k++) {
					da[i][j][k] = set.getDouble(i,j,k);
				}
			}
		}
		
		FileOutputStream fout = new FileOutputStream("C:\\Users\\fcp94556\\Desktop\\test.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		try {
			oos.writeObject(da);
		} finally {
			oos.close();
		}
		
		
	}
}
