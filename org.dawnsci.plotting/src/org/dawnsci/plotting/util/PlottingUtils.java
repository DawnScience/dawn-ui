/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.util;

import java.io.File;
import java.util.Map;

import org.dawb.common.ui.selection.SelectedTreeItemInfo;
import org.dawb.common.ui.selection.SelectionUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class PlottingUtils {
	private final static Logger logger = LoggerFactory.getLogger(PlottingUtils.class);

	/**
	 * Method that plots data to a LightWeight PlottingSystem
	 * @param plottingSystem
	 *             the LightWeight plotting system
	 * @param data
	 *             the data to plot
	 */
	public static void plotData(final IPlottingSystem<?> plottingSystem,
								final String plotTitle,
								final IDataset data){
		Job plotJob = new Job("Plotting data") {
			
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {

					plottingSystem.clear();
					if(data == null) return Status.CANCEL_STATUS;
					plottingSystem.updatePlot2D(data, null, monitor);
					plottingSystem.setTitle(plotTitle);
					plottingSystem.getAxes().get(0).setTitle("");
					plottingSystem.getAxes().get(1).setTitle("");
					plottingSystem.setKeepAspect(true);
					plottingSystem.setShowIntensity(false);
				} catch (Exception e) {
					logger.error("Error plotting data", e);
					return Status.CANCEL_STATUS;
				}
					return Status.OK_STATUS;
			}
		};
		plotJob.schedule();
	}

	/**
	 * Method that loads data given an IStructuredSelection
	 * @param selection
	 * @return IDataset
	 */
	public static IDataset loadData(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			String filename = ((IFile) item).getRawLocation().toOSString();
			return loadData(filename,
						"/entry1/instrument/analyser/data");
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				return loadData(results[0].getFile(), results[0].getNode());
			}
		}
		return null;
	}

	/**
	 * Method that gives the file name of the IStructuredSelection
	 * @param selection
	 * @return String
	 */
	public static String getFileName(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			return ((IFile) item).getName();
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				File f = new File(results[0].getFile());
				return f.getName();
			}
		}
		return null;
	}

	/**
	 * Method that gives the full file path of the IStructuredSelection
	 * @param selection
	 * @return String
	 */
	public static String getFullFilePath(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			return ((IFile) item).getRawLocation().toOSString();
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				return results[0].getFile();
			}
		}
		return null;
	}

	/**
	 * Method that loads data given a filename and a data path
	 * @param fileName
	 *             the name of the data
	 * @param dataPath
	 *             if a NXS file, the data path, otherwise can be null
	 * @return the data loaded as an Dataset, null if none or not found
	 */
	public static Dataset loadData(final String fileName, final String dataPath){
		Dataset dataset = null;
		try {
			IDataHolder data = LoaderFactory.getData(fileName, null);
			IMetadata md = data.getMetadata();
			Map<String, ILazyDataset> map = data.toLazyMap();
			ILazyDataset tmpvalue = map.get(dataPath);
			if(tmpvalue == null) tmpvalue = map.get(data.getName(0));

			ILazyDataset value = tmpvalue.squeezeEnds();
			if(value.getShape().length == 2) {
				dataset = DatasetUtils.sliceAndConvertLazyDataset(value.getSliceView());
				dataset.setMetadata(md);
				return dataset;
			}
			logger.warn("Dataset not the right shape for showing in the preview");
			return null;
		} catch (Exception e) {
			logger.error("Error loading data", e);
			return null;
		}
	}

	/**
	 * Returns the bin shape given a SurfaceROI width and height
	 * 
	 * @param width
	 * @param height
	 * @param isDrag
	 * @return binShape
	 */
	public static int getBinShape(double width, double height, boolean isDrag) {
		int binShape = 1;
		if (isDrag && 
				((width > 300 && width < 900 && height > 300 && width < 900)// size above 300x300 and below 900x900
				|| (width < 300 && height > 300)					// if width below 300 but height above
				|| (width > 300 && height < 300))) {				// if width above 300 but height below
			binShape = (int)(((width + height) / 2) / 100) - 1;
		} else if (!isDrag && 
				((width > 300 && width < 900 && height > 300 && width < 900)
						|| (width < 300 && height > 300)
						|| (width > 300 && height < 300))) {
			binShape = (int)(((width + height) / 2) / 100) - 2;
		} else if (isDrag &&					// if size is bigger than 900x900 
				((width > 900 && height > 900)
				||(width > 900 && height < 900)
				||(width < 900 && height > 900))) {
			binShape = (int)(((width + height) / 2) / 100);
		} else if (!isDrag && 
				((width > 900 && height > 900)
				||(width > 900 && height < 900)
				||(width < 900 && height > 900))) {
			binShape = (int)(((width + height) / 2) / 100) - 1;
		}
		if (binShape == 0) // reset to 1 if binShape is zero
			binShape = 1;
		return binShape;
	}
}
