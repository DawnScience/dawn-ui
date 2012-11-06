package org.dawb.workbench.plotting.tools.diffraction;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.tree.TreeNode;

import org.dawb.common.services.IImageService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.IPaletteListener.Stub;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.eclipse.jface.viewers.Viewer;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * Holds data for the Diffraction model.
 * 
 * @author fcp94556
 *
 */
public class DiffractionTreeModel {

	private static Logger logger = LoggerFactory.getLogger(DiffractionTreeModel.class);
	
	private LabelNode   root;
	private Stub        paletteListener;
	private IImageTrace image;
	private Viewer      viewer;
	
	public DiffractionTreeModel(IMetaData metaData, IImageTrace image, Viewer viewer) throws Exception {
		this.image    = image;
		this.viewer   = viewer;
		this.root     = new LabelNode();
		createDiffractionModel(metaData, image);
	}

	private void createDiffractionModel(IMetaData metaData, final IImageTrace image) throws Exception {

		DiffractionCrystalEnvironment dce = (metaData instanceof IDiffractionMetadata)
				? ((IDiffractionMetadata)metaData).getDiffractionCrystalEnvironment()
						: null;
				
	    DetectorProperties detprop = (metaData instanceof IDiffractionMetadata)
	    		? ((IDiffractionMetadata)metaData).getDetector2DProperties()
	    				: null;
		
	    // Experimental Info
        final LabelNode experimentalInfo = new LabelNode("Experimental Information", root);
       
        NumericNode<Length> lambda = new NumericNode<Length>("Wavelength", experimentalInfo, NonSI.ANGSTROM);
        if (dce!=null) lambda.setDefault(Amount.valueOf(dce.getWavelength(), NonSI.ANGSTROM));
       
        NumericNode<Angle> start = new NumericNode<Angle>("Start", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  start.setDefault(Amount.valueOf(dce.getPhiStart(), NonSI.DEGREE_ANGLE));
       
        NumericNode<Angle> stop = new NumericNode<Angle>("Stop", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  stop.setDefault(Amount.valueOf(dce.getPhiStart()+dce.getPhiRange(), NonSI.DEGREE_ANGLE));

        NumericNode<Angle> osci = new NumericNode<Angle>("Oscillation Range", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  osci.setDefault(Amount.valueOf(dce.getPhiRange(), NonSI.DEGREE_ANGLE));
        
	    // Beam Center
        final LabelNode beamCen = new LabelNode("Beam Center", root);
        
        NumericNode<Length> beamX = new NumericNode<Length>("X", beamCen, SI.MILLIMETER);
        beamX.setEditable(true);
        if (detprop!=null) beamX.setDefault(Amount.valueOf(getBeamX(detprop, SI.MILLIMETER), SI.MILLIMETER));
       
        NumericNode<Length> beamY = new NumericNode<Length>("Y", beamCen, SI.MILLIMETER);
        beamY.setEditable(true);
        if (detprop!=null) beamY.setDefault(Amount.valueOf(getBeamY(detprop, SI.MILLIMETER), SI.MILLIMETER));

        // Pixel Info
        final LabelNode pixelValue = new LabelNode("Intensity", root);
				                 
        final NumericNode<Dimensionless> max  = new NumericNode<Dimensionless>("Visible Maximum", pixelValue, Dimensionless.UNIT);
        final NumericNode<Dimensionless> min  = new NumericNode<Dimensionless>("Visible Minimum", pixelValue, Dimensionless.UNIT);
        final NumericNode<Dimensionless> mean = new NumericNode<Dimensionless>("Mean", pixelValue, Dimensionless.UNIT);
        setIntensityValues(max, min, mean);
        
        this.paletteListener = new IPaletteListener.Stub() {
        	protected void updateEvent(PaletteEvent evt) {
        		try {
					setIntensityValues(max, min, mean);
					viewer.refresh();
				} catch (Exception e) {
					logger.error("Updating intensity values!", e);
				}
        	}
        };
        image.addPaletteListener(paletteListener);

        
        // Detector Meta
        final LabelNode detectorMeta = new LabelNode("Detector", root);

        final NumericNode<Length> dist   = new NumericNode<Length>("Distance", detectorMeta, SI.MILLIMETER);
        if (detprop!=null) dist.setDefault(Amount.valueOf(detprop.getOrigin().z, SI.MILLIMETER));
        
        final NumericNode<Duration> exposure   = new NumericNode<Duration>("Exposure Time", detectorMeta, SI.SECOND);
        if (dce!=null) exposure.setDefault(Amount.valueOf(dce.getExposureTime(), SI.SECOND));
        
        final LabelNode size = new LabelNode("Size", detectorMeta);
        NumericNode<Length> x  = new NumericNode<Length>("x", size, SI.MILLIMETER);
        if (detprop!=null) x.setDefault(Amount.valueOf(detprop.getDetectorSizeH(), SI.MILLIMETER));
        NumericNode<Length> y  = new NumericNode<Length>("y", size, SI.MILLIMETER);
        if (detprop!=null) y.setDefault(Amount.valueOf(detprop.getDetectorSizeV(), SI.MILLIMETER));

        final LabelNode pixel = new LabelNode("Pixel", detectorMeta);
        x  = new NumericNode<Length>("x", pixel, SI.MILLIMETER);
        if (detprop!=null) x.setDefault(Amount.valueOf(detprop.getHPxSize(), SI.MILLIMETER));
        y  = new NumericNode<Length>("y", pixel, SI.MILLIMETER);
        if (detprop!=null) y.setDefault(Amount.valueOf(detprop.getVPxSize(), SI.MILLIMETER));
        
        if (metaData!=null && metaData.getMetaNames()!=null && metaData.getMetaNames().size()>0) {
            final LabelNode rawMeta = new LabelNode("Raw Meta", root);
        	for (String name : metaData.getMetaNames()) {
				new ObjectNode(name, metaData.getMetaValue(name), rawMeta);
			}
        }

	}
	
	private double getBeamY(DetectorProperties dce, Unit<Length> unit) {
		final double[] beamCen = dce.getBeamLocation();
		return getPosition(beamCen[0], dce.getHPxSize(), unit);
	}

	private double getBeamX(DetectorProperties dce, Unit<Length> unit) {
		final double[] beamCen = dce.getBeamLocation();
		return getPosition(beamCen[1], dce.getVPxSize(), unit);
	}

	private double getPosition(double value, double size, Unit<Length> unit) {
		return unit==SI.MILLIMETER ? value*size : value;
	}

	private void setIntensityValues(NumericNode<Dimensionless> max,
			                        NumericNode<Dimensionless> min, 
			                        NumericNode<Dimensionless> mean) throws Exception {
		
		max.setDefault(Amount.valueOf(image.getImageServiceBean().getMax().doubleValue(), Dimensionless.UNIT));
		min.setDefault(Amount.valueOf(image.getImageServiceBean().getMin().doubleValue(), Dimensionless.UNIT));

		IImageService service = (IImageService)ServiceManager.getService(IImageService.class);
		float[] fa = service.getFastStatistics(image.getImageServiceBean());
		mean.setDefault(Amount.valueOf(fa[2], Dimensionless.UNIT));
        mean.setLabel(image.getImageServiceBean().getHistogramType().getLabel());
	}

	public LabelNode getRoot() {
		return root;
	}

	public void dispose() {
		viewer = null;
		image.removePaletteListener(paletteListener);
		image = null;
		paletteListener = null;
		root  = null;
	}

	public void reset() {
		reset(root);
	}

	private void reset(TreeNode node) {
		if (node instanceof NumericNode) {
			((NumericNode)node).reset();
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			reset(node.getChildAt(i));
		}
	}
}
