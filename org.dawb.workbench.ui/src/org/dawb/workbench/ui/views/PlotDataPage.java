/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IVariableManager;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.ui.data.PlotDataComponent;
import org.dawb.workbench.ui.transferable.TransferableDataObject;
import org.dawnsci.slicing.api.SlicingFactory;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.dawnsci.slicing.api.editor.ISlicablePlottingPart;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotDataPage extends Page implements IAdaptable {

	private final static Logger logger = LoggerFactory.getLogger(PlotDataPage.class);
	
	private ISlicablePlottingPart          editor;
	private PlotDataComponent       dataSetComponent;
	private IResourceChangeListener resourceListener;
	private ISliceSystem            sliceComponent;
	private Composite               content;
	
	private static final Collection<String> INACTIVE_PERSPECTIVES;
	static {
		INACTIVE_PERSPECTIVES = Arrays.asList("uk.ac.diamond.scisoft.ncd.rcp.ncdperspective",
				                              "uk.ac.diamond.scisoft.ncd.rcp.ncdcalibrationperspective",
				                              "uk.ac.diamond.scisoft.dataexplorationperspective");
	}
	/**
	 * Checks perspective to see if a 'Data' page is required.
	 * @param ed
	 * @return
	 */
	public static PlotDataPage getPageFor(ISlicablePlottingPart ed) {
		// Fix http://jira.diamond.ac.uk/browse/DAWNSCI-273
		// for DExplore and NCD to not show 'Data' pages
		try {
			final String id = EclipseUtils.getActivePage().getPerspective().getId();
			if (INACTIVE_PERSPECTIVES.contains(id)) return null;
		} catch (Throwable ne) {
			// ignored
		}
		return new PlotDataPage(ed);
	}

	private PlotDataPage(ISlicablePlottingPart ed) {
		this.editor = ed;
	}

	@Override
	public void createControl(Composite parent) {
		
		try {
			this.content = new Composite(parent, SWT.NONE);
			content.setLayout(new GridLayout(1, true));
			
			final SashForm form = new SashForm(content, SWT.VERTICAL);
			form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			this.dataSetComponent = new PlotDataComponent(editor);		
			if (editor!=null && dataSetComponent!=null) {
				final IFile file = (IFile)editor.getAdapter(IFile.class);
				if (file!=null) dataSetComponent.setFileName(file.getName());
			}
			dataSetComponent.createPartControl(form, getSite().getActionBars());
			
			if (dataSetComponent.getDataReductionAction()!=null) {
				getSite().getActionBars().getToolBarManager().add(dataSetComponent.getDataReductionAction());
				getSite().getActionBars().getToolBarManager().add(new Separator("data.reduction.separator"));
			}

			final List<IAction> extras = new ArrayList<IAction>(7);
			extras.addAll(dataSetComponent.getDimensionalActions());
			for (IAction iAction : extras) {
				getSite().getActionBars().getToolBarManager().add(iAction);
				
				// Stinky warning, we do not know which actions are menu bar stuff, so 
				// we add any action with 'preference' in the text.
				if (iAction.getText()!=null&&iAction.getText().toLowerCase().contains("preference")) {
					getSite().getActionBars().getMenuManager().add(iAction);
				}
			}
			getSite().setSelectionProvider(dataSetComponent.getViewer());
					
			dataSetComponent.addSelectionListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					
					@SuppressWarnings("unchecked")
					final List<TransferableDataObject> sels = ((StructuredSelection)event.getSelection()).toList();
					if (sels!=null) editor.updatePlot(sels.toArray(new TransferableDataObject[sels.size()]), getSliceComponent(), true);

				}
			});
			
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			this.resourceListener = new IResourceChangeListener() {
				public void resourceChanged(IResourceChangeEvent event) {
					
					if (event==null || event.getDelta()==null) return;
					
					final IFile content = (IFile)editor.getAdapter(IFile.class);
					if (content==null) return;
					
					if (event.getDelta().findMember(content.getFullPath())!=null) {
						getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									content.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
								} catch (CoreException e) {
									logger.error("Cannot refresh "+content, e);
								}
								if (editor instanceof IReusableEditor) {
								    ((IReusableEditor)editor).setInput(new FileEditorInput(content));
								}
								final List<ITransferableDataObject> sels = dataSetComponent.getSelections();
								if (sels!=null) editor.updatePlot(sels.toArray(new ITransferableDataObject[sels.size()]), (ISliceSystem)getAdapter(ISliceSystem.class), false);
							}
						});
					}
				}
			};
			workspace.addResourceChangeListener(resourceListener);
			
			this.sliceComponent = SlicingFactory.createSliceSystem("org.dawb.workbench.views.h5GalleryView");
			sliceComponent.setPlottingSystem(this.dataSetComponent.getPlottingSystem());
			sliceComponent.addCustomAction(dataSetComponent.getDataReductionAction());
			sliceComponent.createPartControl(form);
			sliceComponent.setVisible(false);
	
			form.setWeights(new int[] {40, 60});
		} catch (Exception ne) {
			logger.error("Cannot create "+getClass().getName(), ne);
		}
	}

	@Override
	public Control getControl() {
		return content;
	}

	@Override
	public void setFocus() {
		dataSetComponent.setFocus();
	}

	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace!=null && resourceListener!=null) {
            workspace.removeResourceChangeListener(resourceListener);
		}
		if (dataSetComponent!=null) dataSetComponent.dispose();
		if (sliceComponent!=null)   sliceComponent.dispose();
 		super.dispose();
	}

	public IVariableManager getDataSetComponent() {
		return dataSetComponent;
	}

	public ISliceSystem getSliceComponent() {
		return sliceComponent;
	}
	
	@Override
	public Object getAdapter(Class type) {
		if (type == String.class) {
			return "Data";
		} else if (type == List.class) {
			final List<IExpressionObject> exprs = new ArrayList<IExpressionObject>();
			for (ITransferableDataObject ob : dataSetComponent.getData()) {
				if (ob.getExpression()!=null) {
					exprs.add(ob.getExpression());
				}
			}
			return exprs;
		} else if (type == IFile.class) {
			return dataSetComponent.getIFile(false);
		}else if (type == ISliceSystem.class) {
			return sliceComponent;
		} else if (type == IVariableManager.class) {
			return dataSetComponent;
		}
		return null;
	}

}
