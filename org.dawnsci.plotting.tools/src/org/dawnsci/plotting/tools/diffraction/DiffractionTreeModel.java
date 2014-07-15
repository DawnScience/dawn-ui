package org.dawnsci.plotting.tools.diffraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.ComboNode;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.ObjectNode;
import org.dawnsci.common.widgets.tree.UnitEvent;
import org.dawnsci.common.widgets.tree.UnitListener;
import org.dawnsci.common.widgets.tree.ValueEvent;
import org.dawnsci.common.widgets.tree.ValueListener;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorHelper;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.jscience.physics.amount.Amount;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * Holds data for the Diffraction model.
 * 
 * Use getNode(String labelPath)  to get a node for use in calculation actions.
 * 
 * The label path is the path to the value in label strings. It is not case
 * sensitive. For instance '/experimental information/beam center/X'  or
 * '/Experimental Information/Beam Center/X'
 * 
 * 
 * 
 * @author fcp94556
 *
 */
public class DiffractionTreeModel extends AbstractNodeModel {

    	
	private Unit<Length>               xpixel, ypixel;
	private NumericNode<Dimensionless> max,min;
	private NumericNode<Length>        beamX, beamY, dist, xPixelSize,yPixelSize;
	private NumericNode<Length>        lambda,xSize,ySize;
	private final IDiffractionMetadata metaData;
	private static final String metadataName = "From metadata";
	private static final String defValues = "Default";
	private static final double EPSILON = 0.0001;
	
	private boolean isActive=false;
	private NumericNode<Angle> yaw, pitch, roll;

	public DiffractionTreeModel(IDiffractionMetadata metaData) throws Exception {
		this(metaData, false);
	}

	public DiffractionTreeModel(IDiffractionMetadata metaData, boolean powderMode) throws Exception {
		super();
		this.metaData = metaData;
		if (metaData.getDetector2DProperties()==null) throw new Exception("Must have detector properties!");
		if (metaData.getDiffractionCrystalEnvironment()==null) throw new Exception("Must have crystal environment!");
		createDiffractionModel(metaData, powderMode);
	}
	
	public void activate() {
		this.isActive = true;
		DetectorProperties detector = getDetectorProperties();
		if (detector != null) {
			detector.addDetectorPropertyListener(detectorListener);
		}
		
		DiffractionCrystalEnvironment dce = getCrystalEnvironment();
		if (dce != null) {
			if (environmentListener != null)
				dce.addDiffractionCrystalEnvironmentListener(environmentListener);
		}
	}
	
	public void deactivate() {
		this.isActive = false;
		DetectorProperties detector = getDetectorProperties();
		if (detector != null) {
			if (detectorListener != null)
				detector.removeDetectorPropertyListener(detectorListener);
		}
		
		DiffractionCrystalEnvironment dce = getCrystalEnvironment();
		if (dce != null) {
			if (environmentListener != null)
				dce.removeDiffractionCrystalEnvironmentListener(environmentListener);
		}
	}

	private void createDiffractionModel(IMetaData metaData, boolean powderMode) throws Exception {
		final DiffractionCrystalEnvironment  dce = getCrystalEnvironment();
		final DiffractionCrystalEnvironment odce = getOriginalCrystalEnvironment();
		final DetectorProperties         detprop = getDetectorProperties();
	    final DetectorProperties        odetprop = getOriginalDetectorProperties();
	    
	    //Check metadata for user objects - default metadata will contain a detector object
	    Collection<Serializable> userObjects = metaData.getUserObjects();
	    DiffractionDetector det = null;
	    if (userObjects != null) {
	    	for (Serializable ob: userObjects) {
	    		if (ob instanceof DiffractionDetector) {
	    			det = (DiffractionDetector)ob;
	    			break;
	    		}
	    	}
	    }

	    createExperimentalInfo(dce, odce, detprop, odetprop, powderMode);

        createDetector(dce, odce, detprop, odetprop, det);
        createIntensity();
        createRaw(metaData);
        
        createUnitsListeners(detprop, odetprop);
        
        // TODO listen to other things, for instance refine when it
        // is available may change other values.
        createDetectorListener(detprop);
        createEnvironmentListener(dce);
	}
	
	private DetectorProperties getDetectorProperties() {
		return metaData.getDetector2DProperties();
	}

	private DetectorProperties getOriginalDetectorProperties() {
		return metaData.getOriginalDetector2DProperties();
	}

	private DiffractionCrystalEnvironment getCrystalEnvironment() {
		return metaData.getDiffractionCrystalEnvironment();
	}
	private DiffractionCrystalEnvironment getOriginalCrystalEnvironment() {
		return metaData.getOriginalDiffractionCrystalEnvironment();
	}

	private void createUnitsListeners(final DetectorProperties detprop, DetectorProperties odetprop) {
        if (detprop!=null) {
        	beamX.setDefault(getBeamX(odetprop));
        	beamX.setValue(getBeamX(detprop));
        	beamX.addAmountListener(new AmountListener<Length>() {		
    			@Override
    			public void amountChanged(AmountEvent<Length> evt) {
    				setBeamX(detprop, evt.getAmount());
    			}
    		});
        }
        beamX.setIncrement(0.01);
		beamX.setFormat("#0.##");
		beamX.setLowerBound(-100000);
		beamX.setUpperBound(100000);
        beamX.addUnitListener(createPixelFormatListener(beamX));
        
        if (detprop!=null) {
        	beamY.setDefault(getBeamY(odetprop));
        	beamY.setValue(getBeamY(detprop));
        	beamY.addAmountListener(new AmountListener<Length>() {		
    			@Override
    			public void amountChanged(AmountEvent<Length> evt) {
    				setBeamY(detprop, evt.getAmount());
    			}
    		});
        }
        beamY.setIncrement(0.01);
        beamY.setFormat("#0.##");
		beamY.setLowerBound(-100000);
		beamY.setUpperBound(100000);
        beamY.addUnitListener(createPixelFormatListener(beamY));		
	}

	private void createRaw(IMetaData metaData) throws Exception {
		
        if (metaData!=null && metaData.getMetaNames()!=null && metaData.getMetaNames().size()>0) {
            final LabelNode rawMeta = new LabelNode("Raw Meta", root);
	        registerNode(rawMeta);
        	for (String name : metaData.getMetaNames()) {
        		ObjectNode on = new ObjectNode(name, metaData.getMetaValue(name), rawMeta);
		        registerNode(on);
			}
        }		
	}

	private void createDetector(final DiffractionCrystalEnvironment dce, final DiffractionCrystalEnvironment odce,
			final DetectorProperties detprop, final DetectorProperties odetprop, final DiffractionDetector det) {

		// Detector Meta
		final LabelNode detectorMeta = new LabelNode("Detector", root);
		registerNode(detectorMeta);
		detectorMeta.setDefaultExpanded(true);
		
		//Make a combo node to show the possible detectors
		List<String> names = DiffractionDetectorHelper.getDiffractionDetectorNames();
		names.add(defValues);
		int pos = setDetectorList(names,  det,  detprop);
		
		final ComboNode detectorName = new ComboNode("Type", names.toArray(new String[names.size()]), detectorMeta);
		detectorName.isEditable();
		detectorName.setTooltip("Detector type determined from the detector preferences.");
		detectorName.setValue(pos);
		detectorName.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(ValueEvent evt) {
				String name = detectorName.getStringValue();
				
				List<Amount<Length>> px = new ArrayList<Amount<Length>>(2);
				
				if (name.equals(metadataName)) {
					px.add(Amount.valueOf(odetprop.getHPxSize(),SI.MILLIMETRE));
					px.add(Amount.valueOf(odetprop.getVPxSize(),SI.MILLIMETRE));				
				} else if (name.equals(defValues)) {
					DetectorProperties dp = DiffractionDefaultMetadata.getPersistedDetectorProperties(new int[]{100,100});
					px.add(Amount.valueOf(dp.getHPxSize(),SI.MILLIMETRE));
					px.add(Amount.valueOf(dp.getVPxSize(),SI.MILLIMETRE));
				} else if (det != null && name.equals(det.getDetectorName())){
					px.add(det.getxPixelSize());
					px.add(det.getPixelSize());
				} else {
					px =DiffractionDetectorHelper.getXYPixelSizeAmount(name);
				}
				
				if (px != null && !px.isEmpty()) {
					updateXPixelSize(detprop,px.get(0));
					updateYPixelSize(detprop,px.get(1));
				}
			}
		});
		registerNode(detectorName);
		
		final NumericNode<Duration> exposure = new NumericNode<Duration>("Exposure Time", detectorMeta, SI.SECOND);
		registerNode(exposure);
		if (dce != null) {
			exposure.setDefault(odce.getExposureTime(), SI.SECOND);
			exposure.setValue(dce.getExposureTime(), SI.SECOND);
		}

		final LabelNode size = new LabelNode("Size", detectorMeta);
		registerNode(size);
		xSize = new NumericNode<Length>("x", size, SI.MILLIMETRE);
		registerNode(xSize);
		if (detprop != null) {
			xSize.setDefault(odetprop.getDetectorSizeH(), SI.MILLIMETRE);
			xSize.setValue(detprop.getDetectorSizeH(), SI.MILLIMETRE);
		}
		ySize = new NumericNode<Length>("y", size, SI.MILLIMETRE);
		registerNode(ySize);
		if (detprop != null) {
			ySize.setDefault(odetprop.getDetectorSizeV(), SI.MILLIMETRE);
			ySize.setValue(detprop.getDetectorSizeV(), SI.MILLIMETRE);
		}

		final LabelNode pixel = new LabelNode("Pixel", detectorMeta);
		registerNode(pixel);

		xPixelSize = new NumericNode<Length>("x-size", pixel, SI.MILLIMETRE);
		registerNode(xPixelSize);
		if (detprop != null) {
			xPixelSize.setDefault(odetprop.getHPxSize(), SI.MILLIMETRE);
			xPixelSize.setValue(detprop.getHPxSize(), SI.MILLIMETRE);
			xPixelSize.addAmountListener(new AmountListener<Length>() {
				@Override
				public void amountChanged(AmountEvent<Length> evt) {
					setDetectorNameToDefault(detectorName);
					setXDetectorSize(detprop, evt.getAmount());
				}
			});
		}
		xPixelSize.setEditable(true);
		xPixelSize.setIncrement(0.01);
		xPixelSize.setFormat("#0.###");
		xPixelSize.setLowerBound(0.001);
		xPixelSize.setUpperBound(1000);

		yPixelSize = new NumericNode<Length>("y-size", pixel, SI.MILLIMETRE);
		registerNode(yPixelSize);
		if (detprop != null) {
			yPixelSize.setDefault(odetprop.getVPxSize(), SI.MILLIMETRE);
			yPixelSize.setValue(detprop.getVPxSize(), SI.MILLIMETRE);
			yPixelSize.addAmountListener(new AmountListener<Length>() {
				@Override
				public void amountChanged(AmountEvent<Length> evt) {
					setDetectorNameToDefault(detectorName);
					setYDetectorSize(detprop, evt.getAmount());
					
				}
			});
		}
		yPixelSize.setEditable(true);
		yPixelSize.setIncrement(0.01);
		yPixelSize.setFormat("#0.###");
		yPixelSize.setLowerBound(0.001);
		yPixelSize.setUpperBound(1000);

		// Beam Centre
		final LabelNode beamCen = new LabelNode("Beam Centre", detectorMeta);
		beamCen.setTooltip("The beam centre is the intersection of the direct beam with the detector in terms of image coordinates. Can be undefined when there is no intersection.");
		registerNode(beamCen);
		beamCen.setDefaultExpanded(true);

		beamX = new NumericNode<Length>("X", beamCen, SI.MILLIMETRE);
		registerNode(beamX);
		beamX.setEditable(true);

		beamY = new NumericNode<Length>("Y", beamCen, SI.MILLIMETRE);
		registerNode(beamY);
		beamY.setEditable(true);

		// Listeners
		xpixel = setBeamCenterUnit(xPixelSize, beamX, "pixel");
		xPixelSize.addAmountListener(new AmountListener<Length>() {
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				xpixel = setBeamCenterUnit(xPixelSize, beamX, "pixel");
			}
		});

		ypixel = setBeamCenterUnit(yPixelSize, beamY, "pixel");
		yPixelSize.addAmountListener(new AmountListener<Length>() {
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				ypixel = setBeamCenterUnit(yPixelSize, beamY, "pixel");
			}
		});

		final LabelNode normal = new LabelNode("Normal (Orientation)", detectorMeta);
		normal.setTooltip("Detector orientation is defined by the normal to its face outward and towards the scattering sample. Rotations are centred on the beam centre");
		registerNode(normal);
		normal.setDefaultExpanded(true);

		final double[] oorientation = odetprop != null ? odetprop.getNormalAnglesInDegrees() : null;
		final double[] orientation = detprop != null ? detprop.getNormalAnglesInDegrees() : null;
		yaw = createOrientationNode("Yaw", -180, 180, oorientation, orientation, 0, normal);
		yaw.setTooltip("Rotation about vertical axis, in degrees (positive is to the right).");
		pitch = createOrientationNode("Pitch", -90, 90, oorientation, orientation, 1, normal);
		pitch.setTooltip("Rotation about horizontal axis, in degrees (positive is upwards).");
		roll = createOrientationNode("Roll", -180, 180, oorientation, orientation, 2, normal);
		roll.setTooltip("Rotation about normal, in degrees (positive is clockwise).");
	}

	private void createIntensity() {
		// Pixel Info
		final LabelNode pixelValue = new LabelNode("Intensity", root);
		registerNode(pixelValue);
		pixelValue.setDefaultExpanded(true);

		this.max = new NumericNode<Dimensionless>("Data Maximum", pixelValue, Dimensionless.UNIT);
		registerNode(max);
		this.min = new NumericNode<Dimensionless>("Data Minimum", pixelValue, Dimensionless.UNIT);
		registerNode(min);

	}

	private void createExperimentalInfo(final DiffractionCrystalEnvironment dce,
			                                 DiffractionCrystalEnvironment odce, 
			                                 final DetectorProperties detprop, 
			                                 DetectorProperties odetprop,
			                                 boolean powderMode) {
	    // Experimental Info
        final LabelNode experimentalInfo = new LabelNode("Experimental Information", root);
        registerNode(experimentalInfo);
        experimentalInfo.setDefaultExpanded(true);
       
        lambda = new NumericNode<Length>("Wavelength", experimentalInfo, NonSI.ANGSTROM);
        registerNode(lambda);
        if (dce!=null) {
           	lambda.setDefault(odce.getWavelength(), NonSI.ANGSTROM);
           	lambda.setValue(dce.getWavelength(), NonSI.ANGSTROM);
        	lambda.addAmountListener(new AmountListener<Length>() {		
				@Override
				public void amountChanged(AmountEvent<Length> evt) {
					setWavelength(dce);
				}
			});
        }
        lambda.setEditable(true);
        lambda.setIncrement(0.01);
        lambda.setFormat("#0.####");
        lambda.setLowerBound(0);
        lambda.setUpperBound(1000); // It can be ev as well.
        lambda.setUnits(NonSI.ANGSTROM, NonSI.ELECTRON_VOLT, SI.KILO(NonSI.ELECTRON_VOLT));
        lambda.addUnitListener(new UnitListener() {	
			@Override
			public void unitChanged(UnitEvent<? extends Quantity> evt) {
				if (evt.getUnit().equals(NonSI.ANGSTROM)) {
			        lambda.setIncrement(0.01);
			        lambda.setFormat("#0.####");
			        lambda.setLowerBound(0);
			        lambda.setUpperBound(1000);

				} else {
					lambda.setIncrement(0.01);
					lambda.setFormat("#0.##");
					lambda.setLowerBound(0);
					lambda.setUpperBound(100000);

				}
			}
		});

		// Moved node SCI-775
		dist = new NumericNode<Length>("Distance", experimentalInfo, SI.MILLIMETRE);
		dist.setTooltip("Distance from sample to beam centre");
		registerNode(dist);
		if (detprop != null) {
			if (odetprop != null) 
				dist.setDefault(odetprop.getBeamCentreDistance(), SI.MILLIMETRE);
			dist.setValue(detprop.getBeamCentreDistance(), SI.MILLIMETRE);
			dist.addAmountListener(new AmountListener<Length>() {
				@Override
				public void amountChanged(AmountEvent<Length> evt) {
					setDistance(detprop, evt.getAmount());
				}
			});
		}
		dist.setEditable(true);
		dist.setIncrement(0.01);
		dist.setFormat("#0.##");
		dist.setLowerBound(0);
		dist.setUpperBound(1000000);
		dist.setUnits(SI.MILLIMETRE, SI.CENTIMETRE, SI.METRE);

		// if in powder mode, do not show the oscillation nodes
		if (!powderMode) {
			NumericNode<Angle> start = new NumericNode<Angle>("Oscillation Start", experimentalInfo, NonSI.DEGREE_ANGLE);
			registerNode(start);
			if (dce!=null)  {
				start.setDefault(odce.getPhiStart(), NonSI.DEGREE_ANGLE);
				start.setValue(dce.getPhiStart(), NonSI.DEGREE_ANGLE);
			}
			NumericNode<Angle> stop = new NumericNode<Angle>("Oscillation Stop", experimentalInfo, NonSI.DEGREE_ANGLE);
			registerNode(stop);
			if (dce!=null)  {
				stop.setDefault(odce.getPhiStart()+dce.getPhiRange(), NonSI.DEGREE_ANGLE);
				stop.setValue(dce.getPhiStart()+dce.getPhiRange(), NonSI.DEGREE_ANGLE);
			}

			NumericNode<Angle> osci = new NumericNode<Angle>("Oscillation Range", experimentalInfo, NonSI.DEGREE_ANGLE);
			registerNode(osci);
			if (dce!=null)  {
				osci.setDefault(odce.getPhiRange(), NonSI.DEGREE_ANGLE);
				osci.setValue(dce.getPhiRange(), NonSI.DEGREE_ANGLE);
			}
		}
	}

	private NumericNode<Angle> createOrientationNode(String label, 
			                                     int lower,
                                                 int upper,
			                                     double[] oorientation, 
			                                     double[] orientation, 
			                                     final int index,
			                                     LabelNode normal) {
		
		NumericNode<Angle> node = new NumericNode<Angle>(label, normal, NonSI.DEGREE_ANGLE);
        registerNode(node);
        if (orientation!=null)  {
        	node.setDefault(oorientation[index], NonSI.DEGREE_ANGLE);
        	node.setValue(orientation[index], NonSI.DEGREE_ANGLE);
        }
        node.setIncrement(1);
        node.setFormat("#0.##");
        node.setLowerBound(lower);
        node.setUpperBound(upper);
        node.setUnits(NonSI.DEGREE_ANGLE, SI.RADIAN);	
        node.setEditable(true);
        
        node.addAmountListener(new AmountListener<Angle>() {		
			@Override
			public void amountChanged(AmountEvent<Angle> evt) {
				double[] ori = getDetectorProperties().getNormalAnglesInDegrees();
				ori[index]   = evt.getAmount().doubleValue(NonSI.DEGREE_ANGLE);
//				System.err.printf("Node %d amount set: %f\n", index, ori[index]);
				getDetectorProperties().setNormalAnglesInDegrees(ori);
			}
		});

        return node;
	}

	private Amount<Length> getBeamX(DetectorProperties dce) {
		final double[] beamCen = dce.getBeamCentreCoords();
		return Amount.valueOf(beamCen[0], xpixel);
	}

	private Amount<Length> getBeamY(DetectorProperties dce) {
		final double[] beamCen = dce.getBeamCentreCoords();
		return Amount.valueOf(beamCen[1], ypixel);
	}

	private void setBeamX(DetectorProperties dce, Amount<Length> beamX) {
		final double[] beamCen = dce.getBeamCentreCoords();
		beamCen[0] = beamX.doubleValue(xpixel);
		try {
			canUpdate = false;
			dce.setBeamCentreCoords(beamCen);
		} finally {
			canUpdate = true;
		}
	}

	private void setBeamY(DetectorProperties dce, Amount<Length> beamY) {
		final double[] beamCen = dce.getBeamCentreCoords();
		beamCen[1] = beamY.doubleValue(ypixel);
		try {
			canUpdate = false;
			dce.setBeamCentreCoords(beamCen);
		} finally {
			canUpdate = true;
		}
	}

	private void setDistance(DetectorProperties dce, Amount<Length> distAmount) {
		double beamDist = distAmount.doubleValue(SI.MILLIMETRE);
		try {
			canUpdate = false;
			dce.setBeamCentreDistance(beamDist);
		} finally {
			canUpdate = true;
		}
	}
	
	private void updateYPixelSize(DetectorProperties dce, Amount<Length> pixAmount) {
		double yPixel = pixAmount.doubleValue(SI.MILLIMETRE);
		try {
			canUpdate = false;
			yPixelSize.setValueQuietly(yPixel, SI.MILLIMETRE);
			if (viewer != null)
				viewer.update(yPixelSize, null);
		} finally {
			canUpdate = true;
		}
		
		setYDetectorSize(dce, pixAmount);
	}
	
	private void updateXPixelSize(DetectorProperties dce, Amount<Length> pixAmount) {
		double xPixel = pixAmount.doubleValue(SI.MILLIMETRE);
		try {
			canUpdate = false;
			xPixelSize.setValueQuietly(xPixel, SI.MILLIMETRE);
			if (viewer != null)
				viewer.update(xPixelSize, null);
		} finally {
			canUpdate = true;
		}
		
		setXDetectorSize(dce, pixAmount);
	}

	private void setYDetectorSize(DetectorProperties dce, Amount<Length> pixAmount) {
		double yPixel = pixAmount.doubleValue(SI.MILLIMETRE);
		try {
			canUpdate = false;
			dce.setVPxSize(yPixel);
			ySize.setValue(dce.getDetectorSizeV(), SI.MILLIMETRE);
			if (viewer != null)
				viewer.update(ySize, null);
		} finally {
			canUpdate = true;
		}
	}

	private void setXDetectorSize(DetectorProperties dce, Amount<Length> pixAmount) {
		double xPixel = pixAmount.doubleValue(SI.MILLIMETRE);
		try {
			canUpdate = false;
			dce.setHPxSize(xPixel);
			xSize.setValue(dce.getDetectorSizeH(), SI.MILLIMETRE);
			if (viewer != null)
				viewer.update(xSize, null);
		} finally {
			canUpdate = true;
		}
	}
	
	private void setDetectorNameToDefault(ComboNode node) {
		try {
			canUpdate = false;
			String[] names = node.getStringValues();
			
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(defValues)) {
					node.setValueQuietly(i);
					viewer.update(node, null);
					break;
				}
			}
		}finally {
			canUpdate = true;
		}
	}

	private void setWavelength(DiffractionCrystalEnvironment dce) {
		double wave = lambda.getValue(NonSI.ANGSTROM);
		try {
			canUpdate = false;
			dce.setWavelength(wave);
		} finally {
			canUpdate = true;
		}
	}

	private IDetectorPropertyListener detectorListener;
	private boolean					  canUpdate = true;
	private void createDetectorListener(final DetectorProperties detprop) {
		if (detectorListener==null) this.detectorListener = new IDetectorPropertyListener() {
			
			@Override
			public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
				if (!isActive)         return;
				if (!canUpdate)        return;
				if (evt.hasNormalChanged()) {
					double[] angles = detprop.getNormalAnglesInDegrees();
//					System.err.printf("Detector: %f, %f, %f\n", angles[0], angles[1], angles[2]);
					yaw.setValueQuietly(angles[0], NonSI.DEGREE_ANGLE);
					pitch.setValueQuietly(angles[1], NonSI.DEGREE_ANGLE);
					roll.setValueQuietly(angles[2], NonSI.DEGREE_ANGLE);
					if (viewer != null) {
						viewer.update(new Object[] {yaw, pitch, roll}, null);
					}
				} else if (evt.hasBeamCentreChanged()) {
					updateBeamCentre(detprop);
				}
				if (evt.hasOriginChanged()) {
					dist.setValueQuietly(detprop.getBeamCentreDistance(), SI.MILLIMETRE);
					updateBeamCentre(detprop);
					if (viewer!=null) viewer.update(dist, null);
				}
				if (evt.hasHPxSizeChanged()) {
					xPixelSize.setValueQuietly(detprop.getVPxSize(), SI.MILLIMETRE);
					if (viewer!=null) viewer.refresh(xPixelSize);
				}
				if (evt.hasVPxSizeChanged()) {
					yPixelSize.setValueQuietly(detprop.getVPxSize(), SI.MILLIMETRE);
					if (viewer!=null) viewer.refresh(yPixelSize);
				}
			}
		};
	}
	
	private IDiffractionCrystalEnvironmentListener environmentListener;
	private void createEnvironmentListener(final DiffractionCrystalEnvironment dce) {
		if (environmentListener==null) this.environmentListener = new IDiffractionCrystalEnvironmentListener() {
			@Override
			public void diffractionCrystalEnvironmentChanged(
					DiffractionCrystalEnvironmentEvent evt) {
				if (!isActive)         return;
				if (!canUpdate)        return;
				if (evt.hasWavelengthChanged()) {
					int unit = lambda.getUnitIndex();
					lambda.setValueQuietly(dce.getWavelength(), NonSI.ANGSTROM);
					lambda.setUnitIndex(unit);
					
					if (viewer!=null) viewer.refresh(lambda);
				}
			}
		};
	}
	
	private void updateBeamCentre(DetectorProperties detprop) {
		double[]     cen = detprop.getBeamCentreCoords();
		Amount<Length> x = Amount.valueOf(cen[0], xpixel);
		beamX.setValueQuietly(x.to(beamX.getValue().getUnit()));
		if (viewer!=null) viewer.update(beamX, null); // Cancels cell editing.
		
		Amount<Length> y = Amount.valueOf(cen[1], ypixel);
		beamY.setValueQuietly(y.to(beamY.getValue().getUnit()));
		if (viewer!=null) viewer.update(beamY, null);  // Cancels cell editing.
	}

	private UnitListener createPixelFormatListener(final NumericNode<Length> node) {
		return new UnitListener() {			
			@Override
			public void unitChanged(UnitEvent<? extends Quantity> evt) {
				if (evt.getUnit().toString().equals("pixel")) {
					node.setIncrement(1);
					node.setFormat("#0");
					node.setLowerBound(-100000);
					node.setUpperBound(100000);
				} else {
					node.setIncrement(0.01);
					node.setFormat("#0.##");
					node.setLowerBound(-1000);
					node.setUpperBound(1000);
				}
			}
		};	
	}

	protected Unit<Length> setBeamCenterUnit(NumericNode<Length> size,
			                                 NumericNode<Length> coord,
			                                 String unitName) {

		Unit<Length> unit = SI.MILLIMETRE.times(size.getValue(SI.MILLIMETRE));
		UnitFormat.getInstance().label(unit, unitName);
		coord.setUnits(SI.MILLIMETRE, unit);
		if (viewer != null)
			viewer.update(coord, new String[] { "Value", "Unit" });
		return unit;
	}

	public void setIntensityValues(IImageTrace image) throws Exception {
		
		if (image==null)  return;
		if (isDisposed)   return;
		if (image.getData()==null) return;
		max.setDefault(image.getData().max().doubleValue(), Dimensionless.UNIT);
		min.setDefault(image.getData().min().doubleValue(), Dimensionless.UNIT);

	}
	
	private int setDetectorList(List<String> names, DiffractionDetector det, DetectorProperties detprop) {
		
		int pos = 0;
		
		//From metadata - check to see if we know the detector name
		if (det == null) {
			DiffractionDetector metaDet = DiffractionDetectorHelper.getMatchingDetector(new int[] {detprop.getPx(),detprop.getPy()});
			if (metaDet != null) {
				if (inexactEquals(metaDet.getXPixelMM(),detprop.getHPxSize()) && inexactEquals(metaDet.getYPixelMM(),detprop.getVPxSize())) {
					det = metaDet;
				}
			}
		}
		
		if (det == null) {
			names.add(metadataName);
			return names.size() -1;
		}
		
		if (names.contains(det.getDetectorName())) {
			for (int i = 0; i < names.size(); i++) {
				if (names.get(i).equals(det.getDetectorName())) {
					pos = i;
					break;
				}
			}
		} else {
			names.add(det.getDetectorName());
			pos = names.size() -1;
		}
		return pos;
	}
	
	private boolean inexactEquals(double a, double b) {
		return (Math.abs(a-b) < EPSILON);
	}

	public void dispose() {
		
		super.dispose();
		deactivate();
		
		final DetectorProperties detprop = getDetectorProperties();
		if (detprop != null) {
			if (detectorListener != null)
				detprop.removeDetectorPropertyListener(detectorListener);
		}
		final DiffractionCrystalEnvironment dce = getCrystalEnvironment();
		if (dce != null) {
			if (environmentListener != null)
				dce.removeDiffractionCrystalEnvironmentListener(environmentListener);
		}
			
	}

}
