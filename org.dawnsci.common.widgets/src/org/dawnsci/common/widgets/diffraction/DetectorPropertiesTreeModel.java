package org.dawnsci.common.widgets.diffraction;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.Unit;
import javax.measure.format.UnitFormat;

import si.uom.SI;
import si.uom.NonSI;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.omg.PortableInterceptor.NON_EXISTENT;

public class DetectorPropertiesTreeModel extends AbstractNodeModel {

	private NumericNode<Length>  beamX, beamY, dist;
	private NumericNode<Angle> yaw, pitch, roll;
	
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
		dist = new NumericNode<Length>("Distance", null, SI.MILLIMETRE, "");
		dist.setTooltip("Distance from sample to beam centre");
		registerNode(dist);
		dist.setValue(100, SI.MILLIMETRE);
		dist.setEditable(true);
		dist.setIncrement(0.01);
		dist.setFormat("#0.##");
		dist.setLowerBound(0);
		dist.setUpperBound(1000000);
		dist.setUnits(SI.MILLIMETRE, SI.CENTIMETRE, SI.METRE);
		return dist;
	}
	
	private void createBeamCentre(DetectorProperties detprop) {
		double[] beamCentreCoords = detprop.getBeamCentreCoords();
		
		beamX = new NumericNode<Length>("X", null, NonSI.PIXEL, "");
		setBeamCenterUnit(detprop.getHPxSize(),beamX,"pixel");
		registerNode(beamX);
		beamX.setValue(beamCentreCoords[0], NonSI.PIXEL);
		beamX.setEditable(true);
		beamX.setFormat("#0.##");
		beamX.setUnits(NonSI.PIXEL,SI.MILLIMETRE);
		
		beamY = new NumericNode<Length>("Y", null, NonSI.PIXEL, "");
		setBeamCenterUnit(detprop.getVPxSize(),beamY,"pixel");
		registerNode(beamY);
		beamY.setValue(beamCentreCoords[1], NonSI.PIXEL);
		beamY.setEditable(true);
		beamY.setFormat("#0.##");
		beamY.setUnits(NonSI.PIXEL,SI.MILLIMETRE);
		
	}
	
	protected Unit<Length> setBeamCenterUnit(double size,
			NumericNode<Length> coord,
			String unitName) {

		Unit<Length> unit = SI.MILLIMETRE.times(size);
		UnitFormat.getInstance().label(unit, unitName);
		coord.setUnits(SI.MILLIMETRE, unit);
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
