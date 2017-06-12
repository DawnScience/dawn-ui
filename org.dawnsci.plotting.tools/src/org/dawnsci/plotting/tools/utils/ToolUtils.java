/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.utils;

import java.util.Collection;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.spi.ServiceProvider;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.roi.data.LinearROIData;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.uom.NonSI;
import tec.units.ri.unit.MetricPrefix;
import tec.units.ri.unit.Units;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * Utility class for tools
 * @author wqk87977
 *
 */
public class ToolUtils {

	private static Logger logger = LoggerFactory.getLogger(ToolUtils.class);

	/**
	 * Millimetre unit
	 */
	public static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(Units.METRE);

	/**
	 * Centimetre unit
	 */
	public static final Unit<Length> CENTIMETRE = MetricPrefix.CENTI(Units.METRE);

	/**
	 * Nanometre unit
	 */
	public final static Unit<Length> NANO = MetricPrefix.NANO(Units.METRE);

	/**
	 * Micrometre unit
	 */
	public final static Unit<Length> MICRO = MetricPrefix.MICRO(Units.METRE);

	/**
	 * kev unit
	 */
	public static final Unit<Energy> KILO_ELECTRON_VOLT = MetricPrefix.KILO(NonSI.ELECTRON_VOLT);

	/**
	 * Dimensionless unit
	 */
	public static final Unit<Dimensionless> DIMENSIONLESS_UNIT = ServiceProvider.current().getQuantityFactory(Dimensionless.class).getSystemUnit();

	/**
	 * 
	 * @param im
	 * @param bounds
	 * @return a Slice of image given a ROI and taking into account out of bounds cases
	 */
	public static IDataset getClippedSlice(IDataset im, RectangularROI bounds) {
		final int yInc = bounds.getPoint()[1] < bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0] < bounds.getEndPoint()[0] ? 1 : -1;
		try {
			IDataset slice = null;
			double xStart = bounds.getPoint()[0];
			double yStart = bounds.getPoint()[1];
			double xEnd = bounds.getEndPoint()[0];
			double yEnd = bounds.getEndPoint()[1];
			// shape[0] = y, shape[1] = x
			int[] shape = im.getShape();
			// Check for most of the possible cases where a region might be out of bounds
			if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[0] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { shape[1], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if (xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
					new int[] { (int) yEnd, (int) xEnd },
					new int[] {yInc, xInc});
			return slice;
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger .trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image", ne);
		}
		return null;
	}

	/**
	 * 
	 * @param rectangleSlice
	 * @return the max intensity (pixel) of the rectangle slice
	 */
	public static double getRectangleMaxIntensity(IDataset rectangleSlice) {
		if (rectangleSlice == null)
			return Double.NaN;
		int[] shape = rectangleSlice.getShape();
		if (shape[0] > 0 && shape[1] > 0) {
			int[] maxPos = rectangleSlice.maxPos();
			return rectangleSlice.getDouble(maxPos[0], maxPos[1]);
		}
		return 0;
	}

	/**
	 * 
	 * @param im
	 * @param lroi
	 * @return the line intensity of the linearROI given an image dataset
	 */
	public static double getLineIntensity(IDataset im, LinearROI lroi) {
		LinearROIData lrd = new LinearROIData(lroi, DatasetUtils.convertToDataset(im), 1d);
		try {
			double max2 = lrd.getProfileData().length > 1
					&& lrd.getProfileData()[1] != null
					? lrd.getProfileData()[1].max().doubleValue()
					: -Double.MAX_VALUE;
			return Math.max(lrd.getProfileData()[0].max().doubleValue(), max2);
		} catch (Throwable ne) {
			return Double.NaN;
		}
	}

	/**
	 * 
	 * @param rectangleSlice
	 * @return the sum of intensities of the rectangle slice
	 */
	public static double getRectangleSum(IDataset rectangleSlice) {
		if (rectangleSlice == null)
			return Double.NaN;
		return (double) DatasetUtils.convertToDataset(rectangleSlice).sum(true);
	}

	/**
	 * 
	 * @param format
	 * @return the increment value given a String format
	 */
	public static double getDecimal(String format) {
		int decimal = format.split("\\.").length > 1 ? format.split("\\.")[1].length() : 0;
		double increment = 1;
		for (int i = 0; i < decimal; i++) {
			increment = increment / 10;
		}
		return increment;
	}

	/**
	 * Tries to get the meta from the editor part or uses the one in AbtractDataset of the image
	 * @return IMetadata, may be null
	 */
	public static IMetadata getMetaData(IImageTrace trace, IWorkbenchPart part) {
		//Changed to try and get the metadata from the image first
		//This works around issues that were arrising when the loader factory
		// and image traces were returning different metadata
		IMetadata metaData = null;
		if (trace != null && trace.getData() != null)
			metaData = trace.getData().getFirstMetadata(IMetadata.class);

		if (metaData != null) return metaData;
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			try {
				metaData = LoaderFactory.getMetadata(EclipseUtils.getFilePath(editor
								.getEditorInput()), null);
				return metaData;
			} catch (Exception e) {
				logger.error(
						"Cannot get meta data for "
								+ EclipseUtils.getFilePath(editor
										.getEditorInput()), e);
			}
		}
		return metaData;
	}

	/**
	 * Used for Masking tool and Sector Profile tool
	 * 
	 * @return beam centre
	 */
	public static double[] getBeamCenter(IImageTrace trace,
			IWorkbenchPart editor) {
		IMetadata meta = getMetaData(trace, editor);
		if (!(meta instanceof IDiffractionMetadata)) {
			return getImageCenter(trace);
		}
		IDiffractionMetadata dm = (IDiffractionMetadata) meta;

		if (dm.getDetector2DProperties() == null)
			return getImageCenter(trace);
		try {
			return dm.getDetector2DProperties().getBeamCentreCoords();
		} catch (NullPointerException npe) {
			return getImageCenter(trace);
		}
	}

	public static double[] getImageCenter(IImageTrace trace) {
		final IDataset image = trace.getData();
		return new double[] { image.getShape()[1] / 2d,
				image.getShape()[0] / 2d };
	}

	public static void updateSectorsMenu(IPlottingSystem<?> system, final IImageTrace image, final MenuAction menu, final IWorkbenchPart part) {
		if (system == null)
			return;
		menu.clear();

		final Collection<IRegion> regions = system.getRegions();
		if (regions!=null) for (final IRegion region : regions) {
			if (isRegionFindCentreSupported(region.getRegionType())) {
				final Action centerRegion = new Action("Center region '"+region.getName()+"'") {
					@Override
					public void run() {
						menu.setSelectedAction(this);
						final double[] cen = ToolUtils.getBeamCenter(image, part);
						if (cen!=null) {
							final RingROI roi = (RingROI)region.getROI();
							roi.setPoint(cen);
							region.setROI(roi);
							menu.setSelectedAction(this);
						}
					}
				};
				centerRegion.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-action.png"));
				menu.add(centerRegion);
			}
		}
		
		if (menu.size()>0) menu.setSelectedAction(0);
	}

	private static boolean isRegionFindCentreSupported(RegionType type) {
		return type==RegionType.SECTOR || type==RegionType.RING;
	}
}
