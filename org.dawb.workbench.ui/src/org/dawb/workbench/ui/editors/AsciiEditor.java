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

import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.IPlottingSystemSelection;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.slicing.ISlicablePlottingPart;
import org.dawb.common.ui.slicing.SliceComponent;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.text.StringUtils;
import org.dawb.workbench.ui.views.PlotDataPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.rcp.editors.SRSEditor;


public class AsciiEditor extends MultiPageEditorPart implements ISlicablePlottingPart, IPlottingSystemSelection {

	public static final String ID = "org.dawb.workbench.editors.AsciiEditor"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(AsciiEditor.class);

	private PlotDataEditor dataSetEditor;

	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        super.init(site, input);
	    setPartName(input.getName());
    }
	
	/**
	 * It might be necessary to show the tree editor on the first page.
	 * A property can be introduced to change the page order if this is required.
	 */
	@Override
	protected void createPages() {
		try {

			this.dataSetEditor = new PlotDataEditor(true, PlotType.PT1D);
			dataSetEditor.getPlottingSystem().setColorOption(ColorOption.BY_NAME);
			addPage(0, dataSetEditor, getEditorInput());
			setPageText(0, "Plot");

			final TextEditor textEditor = new TextEditor();
			addPage(1, textEditor,       getEditorInput());
			setPageText(1, "Text");

			final CSVDataEditor dataEditor = new CSVDataEditor();
			dataEditor.setDataProvider(dataSetEditor);
			addPage(2, dataEditor,   getEditorInput());
			setPageText(2, "Data");
			addPageChangedListener(dataEditor);
			
			if (System.getProperty("org.dawb.editor.ascii.hide.diamond.srs")==null) {
				final SRSEditor srs = new SRSEditor();
				addPage(3, srs,       getEditorInput());
				setPageText(3, "Info");
				
			}

			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (getDataSetComponent()!=null && getDataSetComponent().getMetaData()!=null ) {
						if (EclipseUtils.getPage().findView("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView")!=null &&
							getPageCount()>=4) {
							setActivePage(3);
						}
					}
				}
			});

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

	@Override
	public PlotDataComponent getDataSetComponent() {
		return ((PlotDataEditor)getEditor(0)).getDataSetComponent();
	}
	@Override
	public SliceComponent getSliceComponent() {
		return  ((PlotDataEditor)getEditor(0)).getSliceComponent();
	}
	
	@Override
	public void setActivePage(final int ipage) {
		super.setActivePage(ipage);
	}

	@Override
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}

	public IPlottingSystem getPlotWindow() {
		return ((PlotDataEditor)getEditor(0)).getPlotWindow();
	}
	public PlotDataEditor getDataSetEditor() {
		return dataSetEditor;
	}
	
    public Object getAdapter(final Class clazz) {
		
    	// TODO FIXME for IContentProvider return a Page which shows the value
    	// of plotted data. Bascially the same as the CSVPage.
    	
		if (clazz == Page.class) {
			final PlotDataEditor      ed  = getDataSetEditor();
			return new PlotDataPage(ed);
		} else if (clazz == IToolPageSystem.class) {
			return dataSetEditor.getPlottingSystem();
		}
		
		return super.getAdapter(clazz);
	}

	@Override
	public AbstractDataset setDatasetSelected(String name, boolean clearOthers) {
		return getDataSetComponent().setDatasetSelected(name, clearOthers);
	}

	@Override
	public void setAll1DSelected(boolean overide) {
		getDataSetComponent().setAll1DSelected(overide);
	}

	public String toString(){
		if (getEditorInput()!=null) return getEditorInput().getName();
		return super.toString();
	}
}
