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
import org.dawb.workbench.ui.views.PlotDataPage;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IFileLoader;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.hdf5.editor.H5Editor;
import org.eclipse.dawnsci.hdf5.editor.H5ValuePage;
import org.eclipse.dawnsci.hdf5.editor.IH5Editor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemSelection;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.expressions.IVariableManager;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataManager;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.editors.HDF5Input;
import uk.ac.diamond.scisoft.analysis.rcp.editors.HDF5TreeEditor;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;


public class H5MultiEditor extends MultiPageEditorPart  implements IReusableEditor, IPlottingSystemSelection, IH5Editor, ITitledEditor {

	// The property org.dawb.editor.h5.use.default is set by default in dawb / dawn vanilla
	// The property org.dawb.editor.h5.use.default is not set in SDA.
	private static final String ORG_DAWB_EDITOR_H5_USE_DEFAULT = "org.dawb.editor.h5.use.default";

	private static final Logger logger = LoggerFactory.getLogger(H5MultiEditor.class);
	private PlotDataEditor dataSetEditor;
	private IReusableEditor treePage;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        super.init(site, input);
	    setPartName(input.getName());
    }

	@Override
	public void setPartTitle(String name) {
		super.setPartName(name);	
	}
	
	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		if (dataSetEditor!=null) dataSetEditor.setInput(input);
		if (treePage!=null)      treePage.setInput(input);
	}

	/**
	 * It might be necessary to show the tree editor on the first page.
	 * A property can be introduced to change the page order if this is required.
	 */
	@Override
	protected void createPages() {
		
		boolean treeOnTop = getSite().getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView")!=null;

		// TODO FIXME should not be using LoaderFactory to get individual loaders.
		IDataHolder holder   = null;
		IFileLoader loader   = null;
        IMetadata   metadata = null;
        Tree        tree     = null;
		try {
			String fileName = EclipseUtils.getFilePath(getEditorInput());
			holder = LoaderFactory.fetchData(fileName, true);
			if (holder == null) {
				final String extension = FileUtils.getFileExtension(fileName).toLowerCase();
				loader = LoaderFactory.getLoader(LoaderFactory.getLoaderClass(extension), fileName);
				loader.setAsyncLoad(treeOnTop);
				holder = loader.loadFile();
				holder.setLoaderClass(loader.getClass());
				holder.setFilePath(fileName);
				LoaderFactory.cacheData(holder);
			}
			tree = holder.getTree();
			metadata = holder.getMetadata();
		} catch (Exception e1) {
			// Allowed to have no meta data at this point.
		}

		HDF5Input input = new HDF5Input(getEditorInput(), tree);

		try {
			
			if (metadata!=null) {
				final Collection<String> names = SliceUtils.getSlicableNames(holder);
				if (names == null || names.size() < 1 || getSite().getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView") != null) {
					treeOnTop = true;
				}
			}

			int index = 0;
			String defaultEditorSetting = System.getProperty(ORG_DAWB_EDITOR_H5_USE_DEFAULT);
			boolean useH5Editor = defaultEditorSetting == null || defaultEditorSetting.equals("true");
			if (treeOnTop) {
				this.treePage = useH5Editor ? new H5Editor() : new HDF5TreeEditor();
				addPage(index, treePage, input);
				setPageText(index, "Tree");
				index++;
			}

			try {
				Collection<IEditorPart> extensions = EditorExtensionFactory.getEditors(this);
				if (extensions!=null && extensions.size()>0) {
					for (IEditorPart iEditorPart : extensions) {
						addPage(index, iEditorPart, input.getInput());
						setPageText(index, iEditorPart.getTitle());
						index++;
					}
				}
			} catch (Exception e) {
				logger.error("Cannot read editor extensions!", e);
			}

			/**
			 * TODO This list of data sets can be expensive to extract. Consider
			 * a lazy loading checkbox tree. This means a new PlotDataComponent
			 * for H5 and a new PlotDataEditor which does not extract meta data
			 * at all but loads sets as it sees them.
			 */

			if (!treeOnTop) {
				this.dataSetEditor = new PlotDataEditor(PlotType.XY);
				dataSetEditor.getPlottingSystem().setColorOption(ColorOption.BY_NAME);	
				addPage(index, dataSetEditor, input.getInput());
				setPageText(index, "Plot");
				index++;

				this.treePage = useH5Editor ? new H5Editor() : new HDF5TreeEditor();
				addPage(index, treePage, input);
				setPageText(index, "Tree");
				index++;
			}

			if (!useH5Editor) {
				((HDF5TreeEditor) treePage).startUpdateThread(holder, loader);
			}
		} catch (Exception e) {
			logger.error("Cannot initiate "+getClass().getName()+"!", e);
		}
 	}
	
	public void dispose() {
		dataSetEditor = null;
		treePage      = null;
		super.dispose();
	}

	/** 
	 * No Save
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/** 
	 * No Save
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/** 
	 * We are not saving this class
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void setActivePage(final int ipage) {
		super.setActivePage(ipage);
	}

	@Override
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}

	public PlotDataEditor getDataSetEditor() {
		if (dataSetEditor==null) return null;
		return dataSetEditor;
	}

    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		
		if (clazz == Page.class) {
			final PlotDataEditor      ed  = getDataSetEditor();
			return PlotDataPage.getPageFor(ed);
			
		} else if (clazz == IContentProvider.class) {
			return new H5ValuePage();
		} else if (clazz == IToolPageSystem.class) {
			if (dataSetEditor!=null) return dataSetEditor.getPlottingSystem();
		} else if (clazz == IPlottingSystem.class) {
			if (dataSetEditor!=null) return dataSetEditor.getPlottingSystem();
		} else if (clazz == IVariableManager.class) {
			if (dataSetEditor==null) return null;
			return dataSetEditor.getDataSetComponent();
		} else if (clazz == ISliceSystem.class) {
			if (dataSetEditor==null) return null;
			return dataSetEditor.getSliceComponent();
		} else if (clazz == ITransferableDataManager.class) {
			if (dataSetEditor==null) return null;
			return (ITransferableDataManager)dataSetEditor.getDataSetComponent();
		}
		
		return super.getAdapter(clazz);
	}
    

	@Override
	public IDataset setDatasetSelected(String name, boolean clearOthers) {
		IVariableManager man = (IVariableManager)getAdapter(IVariableManager.class);
		if (man==null) return null;
		return ((IPlottingSystemSelection)man).setDatasetSelected(name, clearOthers);
	}

	@Override
	public void setAll1DSelected(boolean overide) {
		IVariableManager man = (IVariableManager)getAdapter(IVariableManager.class);
		if (man==null) return;
		((IPlottingSystemSelection)man).setAll1DSelected(overide);
	}

	@Override
	public String getFilePath() {
		return EclipseUtils.getFilePath(getEditorInput());
	}

}
