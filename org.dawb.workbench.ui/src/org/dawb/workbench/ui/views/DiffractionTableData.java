package org.dawb.workbench.ui.views;

import java.util.List;

import org.dawb.workbench.ui.views.DiffractionCalibrationView.DiffractionTraceListener;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * Data item used in the table viewer of the Diffraction calibration views
 * @author wqk87977
 *
 */
public class DiffractionTableData {
	IPlottingSystem system;
	String path;
	String name;
	DiffractionImageAugmenter augmenter;
	IDiffractionMetadata md;
	IDataset image;
	DetectorProperties properties;
	DiffractionCrystalEnvironment crystalEnvironment;
	DiffractionTraceListener listener;
	List<IROI> rois;
	QSpace q;
	double ow = Double.NaN;
	double od = Double.NaN;
	int nrois = -1;
	boolean use = false;
}
