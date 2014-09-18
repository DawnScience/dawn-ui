/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.editors.IEditorExtension;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.HeaderTablePage;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.util.io.FileUtils;
import org.dawb.common.util.list.SortNatural;
import org.dawb.workbench.ui.Activator;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.IImageStackTrace;
import org.eclipse.dawnsci.plotting.api.trace.IStackPositionListener;
import org.eclipse.dawnsci.plotting.api.trace.StackPositionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;


/**
 * An editor which combines a plot with a graph of data sets.
 * 
 * Currently this is for 2D analysis only so if the data does not contain 1D, this
 * editor will not show.
 * 
 */
public class PlotImageEditor extends EditorPart implements IEditorExtension, IReusableEditor {
	
	public static final String ID = "org.dawb.workbench.editors.plotImageEditor";

	private static Logger logger = LoggerFactory.getLogger(PlotImageEditor.class);
	
	// This view is a composite of two other views.
	private IPlottingSystem            plottingSystem;	
	private ActionBarWrapper            wrapper;
	private final IReusableEditor       parent;
    private boolean                     plotUpdateAllowed=true;


	public PlotImageEditor() {
		this(null);
	}
	
	public PlotImageEditor(IReusableEditor parent) {
	
		this.parent = parent;
		try {
	        this.plottingSystem = PlottingFactory.createPlottingSystem();
	        plottingSystem.setColorOption(ColorOption.NONE);
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
 	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		setSite(site);
		super.setInput(input);
		setPartName(input.getName());	
	}
	
	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		createPlot();
		firePropertyChange(IEditorPart.PROP_INPUT);
	}


	@Override
	public boolean isDirty() {
		return false;
	}
	

	public void setToolbarsVisible(boolean isVisible) {
		wrapper.setVisible(isVisible);
	}

	@Override
	public void createPartControl(final Composite parent) {
		
		final Composite  main       = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		main.setLayout(gridLayout);
		
		this.wrapper = ActionBarWrapper.createActionBars(main, getEditorSite().getActionBars());

		// NOTE use name of input. This means that although two files of the same
		// name could be opened, the editor name is clearly visible in the GUI and
		// is usually short.
		final String plotName = this.getEditorInput().getName();

		final Composite plot = new Composite(main, SWT.NONE);
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plot.setLayout(new FillLayout());
		
		getPlottingSystem().createPlotPart(plot, plotName, wrapper, PlotType.IMAGE, this);
		getPlottingSystem().getSelectedXAxis().setTitle("");
        
		wrapper.update(true);

		createPlot();
		
		getEditorSite().setSelectionProvider(getPlottingSystem().getSelectionProvider());

 	}
	private void createPlot() {
		
		if (!plotUpdateAllowed) return;
		
		final Job job = new Job("Read image data") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				final String filePath = EclipseUtils.getFilePath(getEditorInput());
				
				final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
				final boolean isStackAllowed = store.getBoolean("org.dawb.workbench.plotting.preference.loadImageStacks");
				
				if (!isStackAllowed) {
					Dataset set;
					try {
						final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
						set = (Dataset)service.getDataset(filePath, null);
					} catch (Throwable e) {
						logger.error("Cannot load file "+filePath, e);
						return Status.CANCEL_STATUS;
					}
					
					set.setName(""); // Stack trace if null - stupid.
					getPlottingSystem().clear();
					getPlottingSystem().createPlot2D(set, null, monitor);
				} else {
				
					getPlottingSystem().clear();
					try {
						final IImageStackTrace stack = getPlottingSystem().createImageStackTrace("image");
						
						final List<String> imageFilenames = new ArrayList<String>();
						final File   file  = new File(filePath);
						final String ext  = FileUtils.getFileExtension(file.getName());
						final File   par = file.getParentFile();
						int selection=0;
						if (par.isDirectory()) {
							int index=0;
							for (String fName : par.list()) {
								if (fName.endsWith(ext)) {
									final File f = new File(par,fName);
									imageFilenames.add(f.getAbsolutePath());
									if (f.getAbsolutePath().equals(filePath)) {
										selection = index;
									}
									index++;
								}
							}
						}
						
						if (imageFilenames.size() > 1) {
				 		    Collections.sort(imageFilenames, new SortNatural<String>(true));
							ImageStackLoader loader = new ImageStackLoader(imageFilenames , new ProgressMonitorWrapper(monitor));
							LazyDataset lazyDataset = new LazyDataset("Folder Stack", loader.getDtype(), loader.getShape(), loader);
							
							stack.setStack(lazyDataset);
							
							if (parent!=null) stack.addStackPositionListener(new IStackPositionListener() {			
								
								@Override
								public void stackPositionChanged(StackPositionEvent evt) {
										
									try { // We do this to change the title of the editor part.
										plotUpdateAllowed = false;
										
										final String path = imageFilenames.get(evt.getPosition());
										final File file = new File(path);
										IEditorInput input = null;
										try {
											IFile ifile = (IFile)getEditorInput().getAdapter(IFile.class);
											final IFile nfile = ifile.getParent().getFile(new Path(file.getName()));
											input = new FileEditorInput(nfile);
											
										} catch (Throwable ne) {
											final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(file);
											input      = new FileStoreEditorInput(externalFile);
										}
										final IEditorInput finalInput = input;
										if (finalInput!=null) Display.getDefault().syncExec(new Runnable() {
											public void run() {
												parent.setInput(finalInput);
											}
										});
									} catch (Throwable ignored) {
										// TODO should we report problems?
									} finally {
										plotUpdateAllowed = true;
									}
								}
							});
						}
						
						final int stackIndex = selection;
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								stack.setStackIndex(stackIndex);
								getPlottingSystem().addTrace(stack);
								getPlottingSystem().repaint(true);
							}
						});
					} catch (Exception ne) {
						ne.printStackTrace();
					}
				}				
				return Status.OK_STATUS;
			}
			
		};
		job.setUser(false);
		job.setPriority(Job.BUILD);
		job.schedule();
	}
	

	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	protected void createCustomToolbarActionsRight(final ToolBarManager toolMan) {

		toolMan.add(new Separator(getClass().getName()+"Separator1"));

		final Action tableColumns = new Action("Open editor preferences.", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		};
		tableColumns.setChecked(false);
		tableColumns.setImageDescriptor(Activator.getImageDescriptor("icons/application_view_columns.png"));

		toolMan.add(tableColumns);
		
	}

	@Override
	public void setFocus() {
		if (getPlottingSystem()!=null && getPlottingSystem().getPlotComposite()!=null) {
			getPlottingSystem().setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @Override
    public void dispose() {
     	if (getPlottingSystem()!=null) getPlottingSystem().dispose();
     	super.dispose();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		if (clazz == Page.class) {
			return new HeaderTablePage(EclipseUtils.getFilePath(getEditorInput()));
		} else if (clazz == IToolPageSystem.class || clazz == IPlottingSystem.class) {
			return getPlottingSystem();
		}
		
		return super.getAdapter(clazz);
	}

    @Override
    public IPlottingSystem getPlottingSystem() {
    	return plottingSystem;
    }

	@Override
	public boolean isApplicable(String filePath, String extension,
			String perspectiveId) {
		return true;
	}

}
