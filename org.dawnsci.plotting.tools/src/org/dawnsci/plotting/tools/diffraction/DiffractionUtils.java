/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.diffraction;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IParametricROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.BooleanDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionMetadataUtils;
import uk.ac.diamond.scisoft.analysis.diffraction.PeakFittingEllipseFinder;
import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;

public class DiffractionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(DiffractionUtils.class);

	/**
	 * Fetch diffraction metadata, doesn't add it to the IDataset
	 * @param image
	 * @param altPath alternative for file path if metadata is null or does not hold it
	 * @param service
	 * @param statusText returned message (can be null)
	 * @return diffraction metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(IDataset image, String altPath, ILoaderService service, String[] statusText) {
		// Now always returns IDiffractionMetadata to prevent creation of a new
		// metadata object after listeners have been added to the old metadata
		//TODO improve this section- it's pretty horrible
		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();
		
		if (image == null)
			return lockedMeta;

		int[] shape = image.getShape();
		IMetadata mdImage = null;
		try {
			mdImage = image.getMetadata();
		} catch (Exception e1) {
			// do nothing
		}
		if (lockedMeta != null) {
			if (mdImage instanceof IDiffractionMetadata) {
				IDiffractionMetadata dmd = (IDiffractionMetadata) mdImage;
				if (!dmd.getDiffractionCrystalEnvironment().equals(lockedMeta.getDiffractionCrystalEnvironment()) ||
						!dmd.getDetector2DProperties().equals(lockedMeta.getDetector2DProperties())) {
					try {
						DiffractionMetadataUtils.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
					} catch (IllegalArgumentException e) {
						if (statusText != null)
							statusText[0] = "Locked metadata does not match image dimensions!";
					}
				}
			} else {
				//TODO what if the image is rotated?
				
				if (shape[0] == lockedMeta.getDetector2DProperties().getPx() &&
					shape[1] == lockedMeta.getDetector2DProperties().getPy()) {
				} else {
					IDiffractionMetadata clone = lockedMeta.clone();
					clone.getDetector2DProperties().setPx(shape[0]);
					clone.getDetector2DProperties().setPy(shape[1]);
					if (statusText != null)
						statusText[0] = "Locked metadata does not match image dimensions!";
				}
			}
			if (statusText != null && statusText[0] == null) {
				statusText[0] = "Metadata loaded from locked version";
			}
			return lockedMeta;
		}

		//If not see if the trace has diffraction meta data
		if (mdImage instanceof IDiffractionMetadata) {
			if (statusText != null && statusText[0] == null) {
				statusText[0] = "Metadata loaded from image";
			}
			return (IDiffractionMetadata) mdImage;
		}
		
		//Try and get the filename here, it will help later on
		String filePath = mdImage == null ? null : mdImage.getFilePath();
		
		if (filePath == null) {
			filePath = altPath;
		}
		
		if (filePath != null) {
			//see if we can read diffraction info from nexus files
			NexusDiffractionMetaCreator ndmc = new NexusDiffractionMetaCreator(filePath);
			IDiffractionMetadata difMet = ndmc.getDiffractionMetadataFromNexus(shape);
			if (difMet !=null) {
				//TODO comment out
				//image.setMetadata(difMet);
				if (statusText != null && statusText[0] == null) {
					if (ndmc.isCompleteRead()){
						statusText[0] = "Metadata completely loaded from nexus tree";
						return difMet;
					}
					else if (ndmc.isPartialRead()) {
						statusText[0] = "Required metadata loaded from nexus tree";
						return difMet;
					}
					else if (ndmc.anyValuesRead())
						statusText[0] = "Incomplete metadata in nexus tree, loading from preferences";
					else
						statusText[0] = "No metadata in nexus tree, metadata loaded from preferences";
				}
				
			}
		}

		// if it is null try and get it from the loader service
		if (mdImage == null && filePath != null) {
			IMetadata md = null;
			try {
				md = service.getMetadata(filePath, null);
			} catch (Exception e) {
				logger.error("Cannot read meta data from part", e);
			}
			
			//If it is there and diffraction data return it
			if (md instanceof IDiffractionMetadata) {
				if (statusText != null && statusText[0] == null) {
					statusText[0] = "Metadata loaded from file";
				}
				return (IDiffractionMetadata) md;
			}
		}
		
		// if there is no meta or is not nexus or IDiff create default IDiff and put it in the dataset
		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(filePath, shape);
		//image.setMetadata(mdImage);
		if (statusText != null && statusText[0] == null) {
			statusText[0] = "No metadata found. Values loaded from preferences:";
		}
		return (IDiffractionMetadata) mdImage;
	}

	public static IROI runConicPeakFit(final IProgressMonitor monitor, Display display,
			final IPlottingSystem<?> plotter, IImageTrace t, IParametricROI roi, IParametricROI[] innerOuter, int nPoints) {
	
		if (roi == null)
			return null;

		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
		monitor.subTask("Find POIs near initial ellipse");
		Dataset image = (Dataset) t.getData();
		BooleanDataset mask = (BooleanDataset) t.getMask();
		PolylineROI points;
		monitor.subTask("Fit POIs");
		
		points = PeakFittingEllipseFinder.findPointsOnConic(image, mask,roi, innerOuter,nPoints, mon);
		
		if (monitor.isCanceled())
			return null;
		
		if (points == null) return null;
		
		if (roi instanceof EllipticalROI) {
			if (points.getNumberOfPoints() < 3) {
				throw new IllegalArgumentException("Could not find enough points to trim");
			}

			monitor.subTask("Trim POIs");
			EllipticalFitROI efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 5, false);
			logger.debug("Found {}...", efroi);
			monitor.subTask("");
			
			EllipticalFitROI cfroi = PowderRingsUtils.fitAndTrimOutliers(null, points, 2, true);
			
			
			double dma = efroi.getSemiAxis(0)-cfroi.getSemiAxis(0);
			double dmi = efroi.getSemiAxis(1)-cfroi.getSemiAxis(0);
			
			double crms = Math.sqrt((dma*dma + dmi*dmi)/2);
			System.err.println("Diff ax: " + (crms));
			double rms = efroi.getRMS();
			System.err.println("DRMS: " + (efroi.getRMS()));
			
			if (crms < rms) {
				efroi = cfroi;
				logger.warn("SWITCHING TO CIRCLE - RMS SEMIAX-RADIUS {} < FIT RMS {}",crms,rms);
			}
			
			return efroi;
		}
		
		return points;
	}
	
	
//	public static IROI runEllipsePeakFit(final IProgressMonitor monitor, Display display,
//			final IPlottingSystem<Composite> plotter, IImageTrace t, IROI roi, double innerRadius, double outerRadius, int nPoints) {
//		
//		if (roi == null)
//			return null;
//
//		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
//		monitor.subTask("Find POIs near initial ellipse");
//		Dataset image = (Dataset) t.getData();
//		BooleanDataset mask = (BooleanDataset) t.getMask();
//		PolylineROI points;
//		EllipticalFitROI efroi;
//		monitor.subTask("Fit POIs");
//		
//		points = PeakFittingEllipseFinder.findPointsOnEllipse(image, mask, (EllipticalROI) roi, innerRadius, outerRadius,nPoints, mon);
//		
//		if (monitor.isCanceled())
//			return null;
//		
//		if (points == null) return null;
//		
//		if (points.getNumberOfPoints() < 3) {
//			throw new IllegalArgumentException("Could not find enough points to trim");
//		}
//
//		monitor.subTask("Trim POIs");
//		efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 5, false);
//		logger.debug("Found {}...", efroi);
//		monitor.subTask("");
//
//		return efroi;
//	}

}
