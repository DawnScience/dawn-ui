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

	private LabelNode   root;

	private NumericNode<Dimensionless> max;
	private NumericNode<Dimensionless> min;
	private NumericNode<Dimensionless> mean;
	
	public DiffractionTreeModel(IMetaData metaData) throws Exception {
		this.root     = new LabelNode();
		createDiffractionModel(metaData);
	}

	private void createDiffractionModel(IMetaData metaData) throws Exception {

		DiffractionCrystalEnvironment dce = (metaData instanceof IDiffractionMetadata)
				? ((IDiffractionMetadata)metaData).getDiffractionCrystalEnvironment()
						: null;
				
	    DetectorProperties detprop = (metaData instanceof IDiffractionMetadata)
	    		? ((IDiffractionMetadata)metaData).getDetector2DProperties()
	    				: null;
		
	    // Experimental Info
        final LabelNode experimentalInfo = new LabelNode("Experimental Information", root);
        experimentalInfo.setDefaultExpanded(true);
       
        NumericNode<Length> lambda = new NumericNode<Length>("Wavelength", experimentalInfo, NonSI.ANGSTROM);
        if (dce!=null) lambda.setDefault(dce.getWavelength(), NonSI.ANGSTROM);
        lambda.setEditable(true);
        lambda.setIncrement(0.01);
        lambda.setFormat("#0.##");
        lambda.setLowerBound(0);
        lambda.setUpperBound(1000);
        lambda.setUnits(NonSI.ANGSTROM, NonSI.ELECTRON_VOLT);
        
        final NumericNode<Length> dist   = new NumericNode<Length>("Distance", experimentalInfo, SI.MILLIMETER);
        if (detprop!=null) dist.setDefault(detprop.getOrigin().z, SI.MILLIMETER);
        dist.setEditable(true);
        dist.setIncrement(1);
        dist.setFormat("#0.#");
        dist.setLowerBound(0);
        dist.setUpperBound(1000);
     
        NumericNode<Angle> start = new NumericNode<Angle>("Oscillation Start", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  start.setDefault(dce.getPhiStart(), NonSI.DEGREE_ANGLE);
       
        NumericNode<Angle> stop = new NumericNode<Angle>("Oscillation Stop", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  stop.setDefault(dce.getPhiStart()+dce.getPhiRange(), NonSI.DEGREE_ANGLE);

        NumericNode<Angle> osci = new NumericNode<Angle>("Oscillation Range", experimentalInfo, NonSI.DEGREE_ANGLE);
        if (dce!=null)  osci.setDefault(dce.getPhiRange(), NonSI.DEGREE_ANGLE);
        
	    // Beam Center
        final LabelNode beamCen = new LabelNode("Beam Center", experimentalInfo);
        beamCen.setDefaultExpanded(true);
      
        NumericNode<Length> beamX = new NumericNode<Length>("X", beamCen, SI.MILLIMETER);
        beamX.setEditable(true);
        if (detprop!=null) beamX.setDefault(getBeamX(detprop, SI.MILLIMETER), SI.MILLIMETER);
       
        NumericNode<Length> beamY = new NumericNode<Length>("Y", beamCen, SI.MILLIMETER);
        beamY.setEditable(true);
        if (detprop!=null) beamY.setDefault(getBeamY(detprop, SI.MILLIMETER), SI.MILLIMETER);

        // Pixel Info
        final LabelNode pixelValue = new LabelNode("Intensity", root);
        pixelValue.setDefaultExpanded(true);
				                 
        this.max  = new NumericNode<Dimensionless>("Visible Maximum", pixelValue, Dimensionless.UNIT);
        this.min  = new NumericNode<Dimensionless>("Visible Minimum", pixelValue, Dimensionless.UNIT);
        this.mean = new NumericNode<Dimensionless>("Mean", pixelValue, Dimensionless.UNIT);
       
        
        // Detector Meta
        final LabelNode detectorMeta = new LabelNode("Detector", root);
        detectorMeta.setDefaultExpanded(true);
        
        final NumericNode<Duration> exposure   = new NumericNode<Duration>("Exposure Time", detectorMeta, SI.SECOND);
        if (dce!=null) exposure.setDefault(dce.getExposureTime(), SI.SECOND);
        
        final LabelNode size = new LabelNode("Size", detectorMeta);
        NumericNode<Length> x  = new NumericNode<Length>("x", size, SI.MILLIMETER);
        if (detprop!=null) x.setDefault(detprop.getDetectorSizeH(), SI.MILLIMETER);
        NumericNode<Length> y  = new NumericNode<Length>("y", size, SI.MILLIMETER);
        if (detprop!=null) y.setDefault(detprop.getDetectorSizeV(), SI.MILLIMETER);

        final LabelNode pixel = new LabelNode("Pixel", detectorMeta);
        x  = new NumericNode<Length>("x", pixel, SI.MILLIMETER);
        if (detprop!=null) x.setDefault(detprop.getHPxSize(), SI.MILLIMETER);
        x.setEditable(true);
        x.setIncrement(0.01);
        x.setFormat("#0.##");
        x.setLowerBound(0);
        x.setUpperBound(1000);

        y  = new NumericNode<Length>("y", pixel, SI.MILLIMETER);
        if (detprop!=null) y.setDefault(detprop.getVPxSize(), SI.MILLIMETER);
        y.setEditable(true);
        y.setIncrement(0.01);
        y.setFormat("#0.##");
        y.setLowerBound(0);
        y.setUpperBound(1000);
      
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

	public void setIntensityValues(IImageTrace image) throws Exception {
		
		if (image==null) return;
		max.setDefault(image.getImageServiceBean().getMax().doubleValue(), Dimensionless.UNIT);
		min.setDefault(image.getImageServiceBean().getMin().doubleValue(), Dimensionless.UNIT);

		IImageService service = (IImageService)ServiceManager.getService(IImageService.class);
		float[] fa = service.getFastStatistics(image.getImageServiceBean());
		mean.setDefault(fa[2], Dimensionless.UNIT);
        mean.setLabel(image.getImageServiceBean().getHistogramType().getLabel());
	}

	public LabelNode getRoot() {
		return root;
	}

	public void dispose() {
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
