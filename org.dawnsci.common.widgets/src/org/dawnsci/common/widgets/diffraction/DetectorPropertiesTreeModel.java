package org.dawnsci.common.widgets.diffraction;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.spi.ServiceProvider;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.eclipse.dawnsci.analysis.api.Constants;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;

import si.uom.NonSI;
import si.uom.SI;
import tec.units.ri.unit.MetricPrefix;
import tec.units.ri.unit.Units;

public class DetectorPropertiesTreeModel extends AbstractNodeModel {

	private NumericNode<Length>  beamX, beamY, dist;
	private NumericNode<Angle> yaw, pitch, roll;

	/**
	 * Millimetre unit
	 */
	private static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(Units.METRE);

	/**
	 * Centimetre unit
	 */
	private static final Unit<Length> CENTIMETRE = MetricPrefix.CENTI(Units.METRE);

	public DetectorPropertiesTreeModel(DetectorProperties dp) {
		
		getDistance(dp);
		createBeamCentre(dp);
		double[] n = dp.getNormalAnglesInDegrees();
		pitch = createOrientationNode("Pitch", n, 0);
		roll = createOrientationNode("Roll", n, 1);
		yaw = createOrientationNode("Yaw", n, 2);
	}
	
	public Object[] getNodes() {
		return new Object[]{dist, beamX, beamY, pitch, roll, yaw};
	}
	
	private NumericNode<Length> getDistance(DetectorProperties detprop) {
		dist = new NumericNode<Length>("Distance", null, MILLIMETRE, "");
		dist.setTooltip("Distance from sample to beam centre");
		registerNode(dist);
		dist.setValue(100, MILLIMETRE);
		dist.setEditable(true);
		dist.setIncrement(0.01);
		dist.setFormat("#0.##");
		dist.setLowerBound(0);
		dist.setUpperBound(1000000);
		dist.setUnits(MILLIMETRE, CENTIMETRE, SI.METRE);
		return dist;
	}
	
	private void createBeamCentre(DetectorProperties detprop) {
		double[] beamCentreCoords = detprop.getBeamCentreCoords();
		
		beamX = new NumericNode<Length>("X", null, Constants.PIXEL, "");
		setBeamCenterUnit(detprop.getHPxSize(),beamX,"pixel");
		registerNode(beamX);
		beamX.setValue(beamCentreCoords[0], Constants.PIXEL);
		beamX.setEditable(true);
		beamX.setFormat("#0.##");
		beamX.setUnits(Constants.PIXEL, MILLIMETRE);
		
		beamY = new NumericNode<Length>("Y", null, Constants.PIXEL, "");
		setBeamCenterUnit(detprop.getVPxSize(),beamY,"pixel");
		registerNode(beamY);
		beamY.setValue(beamCentreCoords[1], Constants.PIXEL);
		beamY.setEditable(true);
		beamY.setFormat("#0.##");
		beamY.setUnits(Constants.PIXEL,MILLIMETRE);
		
	}
	
	protected Unit<Length> setBeamCenterUnit(double size,
			NumericNode<Length> coord,
			String unitName) {

		Unit<Length> unit = MILLIMETRE.multiply(size);
		ServiceProvider.current().getUnitFormatService().getUnitFormat().label(unit, unitName);
		coord.setUnits(MILLIMETRE, unit);
		return unit;
	}
	
	private NumericNode<Angle> createOrientationNode(String label, 
			double[] orientation, 
			final int index) {

		NumericNode<Angle> node = new NumericNode<Angle>(label, null, NonSI.DEGREE_ANGLE, "");
		registerNode(node);

		node.setValue(orientation[index], NonSI.DEGREE_ANGLE);
		node.setIncrement(1);
		node.setFormat("#0.##");
		node.setUnits(NonSI.DEGREE_ANGLE, SI.RADIAN);	
		node.setEditable(true);

		return node;
	}
	
}
