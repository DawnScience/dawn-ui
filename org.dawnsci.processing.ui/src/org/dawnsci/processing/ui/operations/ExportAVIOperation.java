/*-
 * Copyright 2020 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.processing.ui.operations;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.dawnsci.conversion.Activator;
import org.dawnsci.processing.ui.converters.avi.AVIOutputStream;
import org.eclipse.dawnsci.analysis.api.processing.IExportOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.image.IPlotImageService;
import org.eclipse.dawnsci.plotting.api.image.PlotImageData;
import org.eclipse.dawnsci.plotting.api.image.PlotImageData.PlotImageType;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.metadata.OperationMetadata;

public class ExportAVIOperation extends AbstractOperation<ExportAVIModel,OperationData> implements IExportOperation{

	private static final Logger logger = LoggerFactory.getLogger(ExportAVIOperation.class);
	
	private AVIOutputStream  out;
	
	@Override
	public void init() {
		out = null;
	}
	
	@Override
	public String getId() {
		return "org.dawnsci.conversion.operations.ExportAVIOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (out == null) {
			
			String filePath = model.getFilePath();
			
			if (filePath == null || filePath.isEmpty()) {
				OperationMetadata md = input.getFirstMetadata(OperationMetadata.class);
				
				if (md == null || md.getOutputFilename() == null) throw new OperationException(this, "Could not get source filename from metadata");
				
				String filename = md.getOutputFilename();
				
				File f = new File(filename);
				String parent = f.getParent();
				
				int i = md.getCurrentOperationPosition();
				
				filePath = parent + File.separator + "movie_" + i + ".avi";
			}
			
			final File outputFile = new File(filePath);
			File parent = outputFile.getParentFile();
			
			if (!parent.exists()) {
				boolean mkdirs = parent.mkdirs();
				
				if (!mkdirs) {
					throw new OperationException(this, "Could not make avi output directory");
				}
			}
			
			try {
				out = new AVIOutputStream(outputFile, AVIOutputStream.VideoFormat.JPG, 24);
			} catch (IOException e) {
				throw new OperationException(this, "Could not create AVI file", e);
			}
			out.setVideoCompressionQuality(1f);
			out.setTimeScale(1);
			out.setFrameRate(model.getFrameRate());
		}
		
		IImageService imageService = Activator.getService(IImageService.class);
		
		PlotImageData plotImageData = new PlotImageData();
		plotImageData.setConstantRange(true);
		ImageServiceBean bean = imageService.createBeanFromPreferences();
		
		if (model.getColormapMinMax() != null) {
			double[] mm = model.getColormapMinMax();
			
			if (mm.length < 2) throw new OperationException(this, "Colourmap min/max array must contain 2 values");
			
			bean.setMin(mm[0]);
			bean.setMax(mm[1]);
		}
		
		plotImageData.setImageServiceBean(bean);
			
		
		try {
			ImageData data = getImageData(input, plotImageData);
			BufferedImage   img  = imageService.getBufferedImage(data);
	        out.writeFrame(img);
		} catch (Exception e) {
			throw new OperationException(this, "Could not create image", e);
		}
		
		return new OperationData(input);
	}
	
	private ImageData getImageData(IDataset slice, PlotImageData pdata) throws Exception {
		
		pdata.setData(slice);
		
		// We override the slice name so that update works.
		final String title = slice.getName(); // Contains information about slice.
		pdata.setPlotTitle(title);
		slice.setName("slice");

		pdata.setWidth(slice.getShape()[1]);
		pdata.setHeight(slice.getShape()[0]);
		pdata.setType(PlotImageType.IMAGE_ONLY);

		
		final Image     image = Activator.getService(IPlotImageService.class).getImage(pdata);
		final ImageData data  = image.getImageData();
		image.dispose();
		return data;
	}
	
	@Override
	public void dispose() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				logger.error("Error closing stream",e);
			}
		}
		
		out = null;
	}

}
