/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors;

import java.util.Collection;

import org.dawb.common.ui.editors.EditorExtensionFactory;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.HeaderTablePage;
import org.dawb.workbench.ui.views.PlotDataPage;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.expressions.IVariableManager;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImageEditor extends MultiPageEditorPart implements IPersistableEditor, IReusableEditor, IShowEditorInput, ITitledEditor  {

	public static final String ID = "org.dawb.workbench.editors.ImageEditor"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(ImageEditor.class);

	private PlotDataEditor plotDataEditor;

	private IMemento memento;


	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        super.init(site, input);
	    setPartName(input.getName());
    }
	@Override
	public void setPartTitle(String name) {
		super.setPartName(name);	
	}
		
	public void setInput(IEditorInput input) {
		super.setInput(input);
		for(int ied = 0; ied<getPageCount(); ++ied) {
			if (getEditor(ied) instanceof IReusableEditor) {
				((IReusableEditor)getEditor(ied)).setInput(input);
			}
		}
		try{ 
		    setPartName(input.getName());
		} catch (Exception ignored) {
			// Input maybe invalid but we do not treat this as a failure if the above methods already worked.
		}
	}
	/**
	 * It might be necessary to show the tree editor on the first page.
	 * A property can be introduced to change the page order if this is required.
	 */
	@Override
	protected void createPages() {
		try {
			
			int index = 0;
			
			boolean dataFirst = false;
			if (getEditorSite().getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView")!=null) {
				dataFirst = true;
			}

			try {
				Collection<IEditorPart> extensions = EditorExtensionFactory.getEditors(this);
				if (extensions!=null && extensions.size()>0) {
					for (IEditorPart iEditorPart : extensions) {
						addPage(index, iEditorPart,  getEditorInput());
						setPageText(index, iEditorPart.getTitle());
						index++;
					}
				}
			} catch (Exception e) {
				logger.error("Cannot read editor extensions!", e);
			}
			
			if (dataFirst && System.getProperty("org.dawb.editor.ascii.hide.diamond.image.editor")==null) {
				final uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor im = new uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor();
				addPage(index, im,       getEditorInput());
				setPageText(index, "Info");
				index++;
			}

			if(!dataFirst) {
				this.plotDataEditor = new PlotDataEditor(PlotType.IMAGE, this);
				plotDataEditor.getPlottingSystem().restorePreferences(memento);
				addPage(index, plotDataEditor,       getEditorInput());
				setPageText(index, "Image");
				index++;
			}
			
			if (!dataFirst && System.getProperty("org.dawb.editor.ascii.hide.diamond.image.editor")==null) {
				final uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor im = new uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor();
				addPage(index, im,       getEditorInput());
				setPageText(index, "Info");
			}

		} catch (PartInitException e) {
			logger.error("Cannot initiate "+getClass().getName()+"!", e);
		}
	}

	/** 
	 * No Save
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (getActiveEditor().isDirty()) {
			getActiveEditor().doSave(monitor);
		}
	}

	/** 
	 * No Save
	 */
	@Override
	public void doSaveAs() {
		if (getActiveEditor().isDirty()) {
			getActiveEditor().doSaveAs();
		}
	}

	/** 
	 * We are not saving this class
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(final Class clazz) {
		
    	// TODO FIXME for IContentProvider return a Page which shows the value
    	// of plotted data. Basically the same as the CSVPage.
    	
		if (clazz == Page.class) {
			if (plotDataEditor==null) {
				return new HeaderTablePage(EclipseUtils.getFilePath(getEditorInput()));
			} else {
			    return PlotDataPage.getPageFor(plotDataEditor);
			}
		} else if (clazz == IToolPageSystem.class || clazz == IPlottingSystem.class) {
			if (plotDataEditor != null) {
				try {
					Object system = plotDataEditor.getAdapter(clazz);
					return system;
				} catch (Throwable ne) {
					logger.error("Cannot get tool system for "+getActiveEditor(), ne);
				}
				return plotDataEditor.getPlottingSystem();
			} else {
				return null;
			}
		} else if (clazz == IVariableManager.class) {
			if (plotDataEditor==null) return null;
			return plotDataEditor.getDataSetComponent();
		}
		
		return super.getAdapter(clazz);
	}

	public String toString(){
		if (getEditorInput()!=null) return getEditorInput().getName();
		return super.toString();
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		setInput(editorInput);
	}

	@Override
	public void restoreState(IMemento memento) {
		this.memento = memento;
	}

	@Override
	public void saveState(IMemento memento) {
		plotDataEditor.getPlottingSystem().savePreferences(memento);
	}
}
