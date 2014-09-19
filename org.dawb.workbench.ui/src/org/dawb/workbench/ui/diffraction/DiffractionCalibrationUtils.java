/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.workbench.ui.diffraction;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Vector3d;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.workbench.ui.diffraction.table.DiffractionDataManager;
import org.dawb.workbench.ui.diffraction.table.DiffractionTableData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

/**
 * Class containing static methods used in Diffraction calibration views
 *
 */
public class DiffractionCalibrationUtils {

	private static Logger logger = LoggerFactory.getLogger(DiffractionCalibrationUtils.class);

	enum ManipulateMode {
		LEFT, RIGHT, UP, DOWN, ENLARGE, SHRINK, ELONGATE, SQUASH, CLOCKWISE, ANTICLOCKWISE
	}

	private static String REGION_PREFIX = "Pixel peaks";

	/**
	 * Create a job to calibrate images 
	 * @param display
	 * @param plottingSystem
	 * @param model
	 * @param currentData
	 * @param useFixedWavelength if true then fit using a fixed global wavelength
	 * @param postFixedWavelengthFit if true and useFixedWavelength true then fit wavelength afterwards
	 * @return job that needs to be scheduled
	 */
//	public static Job calibrateImages(final Display display,
//									   final IPlottingSystem plottingSystem,
//									   final List<DiffractionTableData> model,
//									   final DiffractionTableData currentData,
//									   final boolean useFixedWavelength,
//									   final boolean postFixedWavelengthFit) {
//		Job job = new Job("Calibrate detector") {
//			@Override
//			protected IStatus run(final IProgressMonitor monitor) {
//				IStatus stat = Status.OK_STATUS;
//				final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
//				monitor.beginTask("Calibrate detector", IProgressMonitor.UNKNOWN);
//				List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
//				List<List<? extends IROI>> lROIs = new ArrayList<List<? extends IROI>>();
//				List<DetectorProperties> dps = new ArrayList<DetectorProperties>();
//				DiffractionCrystalEnvironment env = null;
//				for (DiffractionTableData data : model) {
//					IDiffractionMetadata md = data.getMetaData();
//					if (!data.isUse() || data.getNrois() <= 0 || md == null) {
//						continue;
//					}
//					if (env == null) {
//						env = md.getDiffractionCrystalEnvironment();
//					}
//					data.setQ(null);
//
//					DetectorProperties dp = md.getDetector2DProperties();
//					if (dp == null) {
//						continue;
//					}
//					dps.add(dp);
//					lROIs.add(data.getRois());
//				}
//				List<QSpace> qs = null;
//				if (useFixedWavelength) {
//					monitor.subTask("Fitting all rings");
//					try {
//						qs = PowderRingsUtils.fitAllEllipsesToAllQSpacesAtFixedWavelength(mon, dps, env, lROIs, spacings, postFixedWavelengthFit);
//					} catch (IllegalArgumentException e) {
//						logger.debug("Problem in calibrating all image: {}", e);
//					}
//				} else {
//					try {
//						qs = PowderRingsUtils.fitAllEllipsesToAllQSpaces(mon, dps, env, lROIs, spacings);
//					} catch (IllegalArgumentException e) {
//						logger.debug("Problem in calibrating all image: {}", e);
//					}
//				}
//
//				int i = 0;
//				for (DiffractionTableData data : model) {
//					IDiffractionMetadata md = data.getMetaData();
//					if (!data.isUse() || data.getNrois() <= 0 || md == null) {
//						continue;
//					}
//
//					DetectorProperties dp = md.getDetector2DProperties();
//					if (dp == null) {
//						continue;
//					}
//					data.setQ(qs.get(i++));
//					logger.debug("Q-space = {}", data.getQ());
//				}
//
//				display.syncExec(new Runnable() {
//					@Override
//					public void run() {
//						for (DiffractionTableData data : model) {
//							IDiffractionMetadata md = data.getMetaData();
//							if (data.getQ() == null || !data.isUse() || data.getNrois() <= 0 || md == null) {
//								continue;
//							}
//							DetectorProperties dp = md.getDetector2DProperties();
//							DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
//							if (dp == null || ce == null) {
//								continue;
//							}
//
//							DetectorProperties fp = data.getQ().getDetectorProperties();
//							dp.setGeometry(fp);
//							ce.setWavelength(data.getQ().getWavelength());
//						}
//
//						if (currentData == null || currentData.getMetaData() == null || currentData.getQ() == null)
//							return;
//
//						hideFoundRings(plottingSystem);
//						//drawCalibrantRings(currentData.augmenter);
//					}
//				});
//				return stat;
//			}
//		};
//		job.setPriority(Job.SHORT);
//		return job;
//	}

	/**
	 * 
	 * @param plottingSystem
	 */
	public static void hideFoundRings(IPlottingSystem plottingSystem) {
		for (IRegion r : plottingSystem.getRegions()) {
			String n = r.getName();
			if (n.startsWith(REGION_PREFIX)) {
				r.setVisible(false);
			}
		}
	}


	public static IStatus drawFoundRing(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, final IROI froi, final boolean circle) {
		final boolean[] status = {true};
		
		display.syncExec(new Runnable() {

			public void run() {
				try {
					IRegion region = plotter.createRegion(RegionUtils.getUniqueName(REGION_PREFIX, plotter), circle ? RegionType.CIRCLEFIT : RegionType.ELLIPSEFIT);
					region.setROI(froi);
					region.setRegionColor(circle ? ColorConstants.cyan : ColorConstants.orange);
					monitor.subTask("Add region");
					region.setUserRegion(false);
					plotter.addRegion(region);
					monitor.worked(1);
				} catch (Exception e) {
					status[0] = false;
				}
			}
		});
		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	/**
	 * 
	 * @param currentData
	 * @param mode
	 * @param fast
	 */
	public static void changeRings(DiffractionTableData currentData, ManipulateMode mode, boolean fast) {
		if (currentData == null || currentData.getMetaData() == null)
			return;

		DetectorProperties detprop = currentData.getMetaData().getDetector2DProperties();
		if (detprop == null)
			return;

		if (mode == ManipulateMode.UP) {
			Vector3d orig = detprop.getOrigin();
			Vector3d col = detprop.getPixelColumn();
			if (fast)
				col.scale(10);
			orig.add(col);
			detprop.setOrigin(orig);
		} else if (mode == ManipulateMode.DOWN) {
			Vector3d orig = detprop.getOrigin();
			Vector3d col = detprop.getPixelColumn();
			if (fast)
				col.scale(10);
			orig.sub(col);
			detprop.setOrigin(orig);
		} else if (mode == ManipulateMode.LEFT) {
			Vector3d orig = detprop.getOrigin();
			Vector3d row = detprop.getPixelRow();
			if (fast)
				row.scale(10);
			orig.add(row);
			detprop.setOrigin(orig);
		} else if (mode == ManipulateMode.RIGHT) {
			Vector3d orig = detprop.getOrigin();
			Vector3d row = detprop.getPixelRow();
			if (fast)
				row.scale(10);
			orig.sub(row);
			detprop.setOrigin(orig);
		} else if (mode == ManipulateMode.ENLARGE) {
			Vector3d norm = new Vector3d(detprop.getNormal());
			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
			double[] bc = detprop.getBeamCentreCoords();
			Vector3d orig = detprop.getOrigin();
			orig.sub(norm);
			detprop.setOrigin(orig);
			if (!Double.isNaN(bc[0])) { // fix on beam centre
				detprop.setBeamCentreCoords(bc);
			}
		} else if (mode == ManipulateMode.SHRINK) {
			Vector3d norm = new Vector3d(detprop.getNormal());
			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
			double[] bc = detprop.getBeamCentreCoords();
			Vector3d orig = detprop.getOrigin();
			orig.add(norm);
			detprop.setOrigin(orig);
			if (!Double.isNaN(bc[0])) { // fix on beam centre
				detprop.setBeamCentreCoords(bc);
			}
		} else if (mode == ManipulateMode.ELONGATE) {
			double tilt = Math.toDegrees(detprop.getTiltAngle());
			double[] angle = detprop.getNormalAnglesInDegrees();
			tilt += fast ? 2 : 0.2;
			if (tilt > 90)
				tilt = 90;
			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
			logger.trace("p: {}", tilt);
		} else if (mode == ManipulateMode.SQUASH) {
			double tilt = Math.toDegrees(detprop.getTiltAngle());
			double[] angle = detprop.getNormalAnglesInDegrees();
			tilt -= fast ? 2 : 0.2;
			if (tilt < 0)
				tilt = 0;
			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
			logger.trace("o: {}", tilt);
		} else if (mode == ManipulateMode.ANTICLOCKWISE) {
			double[] angle = detprop.getNormalAnglesInDegrees();
			angle[2] -= fast ? 2 : 0.5;
			if (angle[2] < 0)
				angle[2] += 360;
			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
			logger.trace("a: {}", angle[2]);
		} else if (mode == ManipulateMode.CLOCKWISE) {
			double[] angle = detprop.getNormalAnglesInDegrees();
			angle[2] += fast ? 2 : 0.5;
			if (angle[2] > 360)
				angle[2] = 360;
			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
			logger.trace("c: {}", angle[2]);
		}
	}

	/**
	 * 
	 * @param display
	 * @param model
	 * @param currentData
	 */
	public static void calibrateWavelength(final Display display, final List<DiffractionTableData> model, final DiffractionTableData currentData) {
		Job job = new Job("Calibrate wavelength") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				IStatus stat = Status.OK_STATUS;
				monitor.beginTask("Calibrate wavelength", IProgressMonitor.UNKNOWN);
				List<Double> odist = new ArrayList<Double>();
				List<Double> ndist = new ArrayList<Double>();
				for (DiffractionTableData data : model) {
					if (data.getQ() == null || !data.isUse() || data.getNrois() <= 0 || data.getMetaData() == null) {
						continue;
					}

					if (Double.isNaN(data.getOd())) {
						continue;
					}
					odist.add(data.getOd());
					ndist.add(data.getQ().getDetectorProperties().getDetectorDistance());
				}
				if (odist.size() < 3) {
					logger.warn("Need to use three or more images");
					return Status.CANCEL_STATUS;
				}
				Polynomial p = new Polynomial(1);
				try {
					Fitter.polyFit(new Dataset[] {DatasetFactory.createFromList(odist)}, DatasetFactory.createFromList(ndist), 1e-15, p);
				} catch (Exception e) {
					logger.error("Problem with fit", e);
					return Status.CANCEL_STATUS;
				}
				logger.debug("Straight line fit: {}", p);

				final double f = p.getParameterValue(0);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						for (final DiffractionTableData data : model) {
							if (!data.isUse() || data.getNrois() <= 0 || data.getMetaData() == null) {
								continue;
							}

							final DiffractionCrystalEnvironment ce = data.getMetaData().getDiffractionCrystalEnvironment();
							if (ce != null) {
								double ow = ce.getWavelength();
								ce.setWavelength(ow * f);
							}
						}
					}
				});

				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						//drawCalibrantRings(currentData.augmenter);
					}
				});
				return stat;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	
	/**
	 * 
	 * @param system
	 * @return an IImageTrace
	 */
	public static IImageTrace getImageTrace(IPlottingSystem system) {
		Collection<ITrace> traces = system.getTraces();
		if (traces != null && traces.size() > 0) {
			ITrace trace = traces.iterator().next();
			if (trace instanceof IImageTrace) {
				return (IImageTrace) trace;
			}
		}
		return null;
	}

	/**
	 * List of names to save
	 */
	public static final String[] NAMES = new String[]{ "Image", 
		"Distance (mm)", "X beam centre (pixels)", "Y beam centre (pixels)", "Wavelength (Angstrom)",
		"Energy (keV)", "Yaw (degrees)", "Pitch (degrees)", "Roll (degrees)", "Residual"};

	/**
	 * 
	 * @param model
	 * @param filepath
	 */
	public static void saveModelToCSVFile(DiffractionDataManager manager, String filepath) {
		String[][] values = new String[manager.getSize()][NAMES.length];
		int i = 0;
		for (DiffractionTableData model : manager.iterable()) {
			DetectorProperties dp = model.getMetaData().getDetector2DProperties();
			double wavelength = model.getMetaData().getDiffractionCrystalEnvironment().getWavelength();
			//wavelength = DiffractionCalibrationUtils.setPrecision(wavelength, 5);
			double residual = model.getResidual();
			// image
			values[i][0] = model.getName();
			// distance
			values[i][1] = String.valueOf(dp.getBeamCentreDistance());
			// X beam centre
			values[i][2] = String.valueOf(dp.getBeamCentreCoords()[0]);
			// Y beam centre
			values[i][3] = String.valueOf(dp.getBeamCentreCoords()[1]);
			// wavelength
			values[i][4] = String.valueOf(wavelength);
			// energy
			values[i][5] = String.valueOf(DiffractionCalibrationUtils.getWavelengthEnergy(wavelength));
			// Orientation Yaw
			values[i][6] = String.valueOf(dp.getNormalAnglesInDegrees()[0]);
			// Orientation Pitch
			values[i][7] = String.valueOf(dp.getNormalAnglesInDegrees()[1]);
			// Orientation Roll
			values[i][8] = String.valueOf(dp.getNormalAnglesInDegrees()[2]);
			// Orientation Roll
			values[i][9] = String.valueOf(residual);
			++i;
		}
		saveToCsvFile(filepath, NAMES, values);
	}

	/**
	 * Saves to a csv file a table of arrays
	 * @param filename
	 *           The filename to save the data to
	 * @param names
	 *           names of each column
	 * @param values
	 *           values for each columns and rows
	 */
	public static void saveToCsvFile(String filename, String[] names, Object[][] values) {
		if (names.length != values[0].length) {
			logger.error("The names and values arrays don't have the same size");
			return;
		}
		try {
			FileWriter writer = new FileWriter(filename);
			// write names
			for (int i = 0; i < names.length; i++) {
				writer.append(names[i]);
				writer.append(',');
			}
			writer.append('\n');
			// write values
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[0].length; j++) {
					writer.append((String)values[i][j]);
					writer.append(',');
				}
				writer.append('\n');
			}
			writer.flush();
			writer.close();
			logger.debug("Metadata saved to file");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error saving metadata:"+e);
		}
	}
	
	public static void saveToNexusFile(DiffractionDataManager manager, String filepath) throws Exception {
		
		DiffractionTableData cd = manager.getCurrentData();
		
		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
		IPersistentFile file = service.createPersistentFile(filepath);
		try {
			if (cd.getCalibrationInfo()!= null) {
				file.setPowderCalibrationInformation(cd.getImage(), cd.getMetaData(), cd.getCalibrationInfo());
			} else {
				file.setData(cd.getImage());
				file.setDiffractionMetadata(cd.getMetaData());
			}
			
			
		} catch(Exception e) {
			//do nothing
		} finally {
			file.close();
		}
	}

	/**
	 * Returns the wavelength/energy given the energy/wavelength
	 * with the same precision as the value entered
	 * @param value
	 * @return a double value with the same precision number as the value entered as parameter
	 */
	public static double getWavelengthEnergy(double value) {
		BigDecimal valueBd = BigDecimal.valueOf(value);
		int precision = valueBd.precision();

		double result = 1. / (0.0806554465 * value); // constant from NIST CODATA 2006

		return setPrecision(result, precision);
	}

	/**
	 * Sets a double with the wanted precision
	 * @param value
	 * @param precision
	 * @return a double value with the wanted precision
	 */
	public static double setPrecision(double value, int precision) {
		int decimal = 0;
		if (value < 1) {
			for (int i = 0; i < precision; i ++) {
				decimal ++;
			}
		} else {
			int resultInt = BigDecimal.valueOf(value).intValue();
			int numberOfDigit = String.valueOf(resultInt).length();
			for (int i = 0; i < precision - numberOfDigit; i ++) {
				decimal ++;
			}
		}

		BigDecimal bd = new BigDecimal(value).setScale(decimal, RoundingMode.HALF_EVEN);
		value = bd.doubleValue();
		return value; 
	}

	/**
	 * Returns a with a specific precision number given a source value and a result value.<br>
	 * The format returned would look like the following: 00,000.####<br>
	 * Used for wavelength/energy
	 * @param sourceValue
	 * @param resultValue
	 * @return a format mask
	 */
	public static String getFormatMask(double sourceValue, double resultValue) {
		BigDecimal sourceBd = BigDecimal.valueOf(sourceValue);
		int precisionNumber = sourceBd.precision();

		String result = "";
		if (resultValue < 1) {
			for (int i = 0; i < precisionNumber; i ++) {
				result += "#";
			}
		} else {
			int resultInt = BigDecimal.valueOf(resultValue).intValue();
			int numberOfDigit = String.valueOf(resultInt).length();
			for (int i = 0; i < precisionNumber - numberOfDigit; i ++) {
				result += "#";
			}
		}
		return "##,##0." + result;
	}

	/**
	 * Updates the Diffraction tool
	 * @param nodePath
	 *         node to update
	 * @param value
	 *         new value of the node
	 * @param toolSystem
	 *         ToolPage system of the tool
	 */
//	public static void updateDiffTool(String nodePath, double value, IToolPageSystem toolSystem) {
//		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DiffractionCalibrationConstants.DIFFRACTION_ID);
//		DiffractionTreeModel treeModel = diffTool.getModel();
//
//		NumericNode<Length> distanceNode = getDiffractionTreeNode(nodePath, toolSystem);
//		distanceNode.setDoubleValue(value);
//		treeModel.setNode(distanceNode, nodePath);
//
//		diffTool.refresh();
//	}

	/**
	 * Gets a Diffraction tree node
	 * @param nodePath
	 *         node to retrieve
	 * @param toolSystem
	 *         ToolPage system of the tool
	 * @return
	 */
//	@SuppressWarnings("unchecked")
//	public static NumericNode<Length> getDiffractionTreeNode(String nodePath, IToolPageSystem toolSystem) {
//		NumericNode<Length> node = null;
//		if (toolSystem == null)
//			return node;
//		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DiffractionCalibrationConstants.DIFFRACTION_ID);
//		DiffractionTreeModel treeModel = diffTool.getModel();
//		if (treeModel == null)
//			return node;
//		node = (NumericNode<Length>) treeModel.getNode(nodePath);
//		return node;
//	}
}
