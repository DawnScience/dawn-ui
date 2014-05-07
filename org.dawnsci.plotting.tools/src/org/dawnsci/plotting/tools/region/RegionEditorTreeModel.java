package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.tree.TreeNode;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * Holds data for the Region Editor model.
 * 
 * @author wqk87977
 *
 */
public class RegionEditorTreeModel extends AbstractNodeModel {

	private boolean isActive = false;
	private boolean isDragged = false;
	private Collection<IRegion> regions;
	private List<LabelNode> regionNodes = new ArrayList<LabelNode>();
	private IPlottingSystem plottingSystem;

	public RegionEditorTreeModel(IPlottingSystem plottingSystem, Collection<IRegion> regions) throws Exception {
		this.plottingSystem = plottingSystem;
		this.regions = regions;
		for (IRegion region : regions) {
			addRegion(region);
		}
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = false;
	}

	public LabelNode createRectangularRegion(IRegion region) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		String intensityFormat = store.getString(RegionEditorConstants.INTENSITY_FORMAT);
		String sumFormat = store.getString(RegionEditorConstants.SUM_FORMAT);
		double increment = getDecimal(pointFormat);

		final LabelNode rectangularLabelNode = new LabelNode(region.getName(), root);
		rectangularLabelNode.setTooltip("Box region selection");
		rectangularLabelNode.setEditable(true);
		registerNode(rectangularLabelNode);
		rectangularLabelNode.setDefaultExpanded(true);

		double xStartPt = region.getROI().getPointX();
		double yStartPt = region.getROI().getPointY();
		double width = ((RectangularROI)region.getROI()).getLengths()[0];
		double height = ((RectangularROI)region.getROI()).getLengths()[1];
		double angle = ((RectangularROI)region.getROI()).getAngle();

		createLengthNode(rectangularLabelNode, "X Start Point", true, increment, pointFormat, NonSI.PIXEL, xStartPt);
		createLengthNode(rectangularLabelNode, "Y Start Point", true, increment, pointFormat, NonSI.PIXEL, yStartPt);

		createLengthNode(rectangularLabelNode, "Width", true, increment, pointFormat, NonSI.PIXEL, width);
		createLengthNode(rectangularLabelNode, "Height", true, increment, pointFormat, NonSI.PIXEL, height);

		createAngleNode(rectangularLabelNode, "Angle", true, increment, pointFormat, NonSI.DEGREE_ANGLE, angle);

		createLengthNode(rectangularLabelNode, "Max Intensity", false, increment, intensityFormat, Dimensionless.UNIT, 0);
		createLengthNode(rectangularLabelNode, "Sum", false, increment, sumFormat, Dimensionless.UNIT, 0);
		return rectangularLabelNode;
	}

	public LabelNode createLineRegion(IRegion region) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		String intensityFormat = store.getString(RegionEditorConstants.INTENSITY_FORMAT);
		double increment = getDecimal(pointFormat);

		final LabelNode lineLabelNode = new LabelNode(region.getName(), root);
		lineLabelNode.setTooltip("Line region selection");
		lineLabelNode.setEditable(true);
		registerNode(lineLabelNode);
		lineLabelNode.setDefaultExpanded(true);

		double xStartPt = region.getROI().getPointX();
		double yStartPt = region.getROI().getPointY();
		double xEndPt = ((LinearROI)region.getROI()).getEndPoint()[0];
		double yEndPt = ((LinearROI)region.getROI()).getEndPoint()[1];
		double angle = ((LinearROI)region.getROI()).getAngle();

		createLengthNode(lineLabelNode, "X Start Point", true, increment, pointFormat, NonSI.PIXEL, xStartPt);
		createLengthNode(lineLabelNode, "Y Start Point", true, increment, pointFormat, NonSI.PIXEL, yStartPt);

		createLengthNode(lineLabelNode, "X End Point", true, increment, pointFormat, NonSI.PIXEL, xEndPt);
		createLengthNode(lineLabelNode, "Y End Point", true, increment, pointFormat, NonSI.PIXEL, yEndPt);

		createAngleNode(lineLabelNode, "Rotation", true, increment, pointFormat, NonSI.DEGREE_ANGLE, angle);

		createLengthNode(lineLabelNode, "Max Intensity", false, increment, intensityFormat, Dimensionless.UNIT, 0);
		return lineLabelNode;
	}

	public LabelNode createSectorRegion(IRegion region) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		double increment = getDecimal(pointFormat);

		final LabelNode sectorLabelNode = new LabelNode(region.getName(), root);
		sectorLabelNode.setTooltip("Sector region selection");
		sectorLabelNode.setEditable(true);
		registerNode(sectorLabelNode);
		sectorLabelNode.setDefaultExpanded(true);

		createLengthNode(sectorLabelNode, "X Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);
		createLengthNode(sectorLabelNode, "Y Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);

		createLengthNode(sectorLabelNode, "Inner Radius", true, increment, pointFormat, NonSI.PIXEL, 0);
		createLengthNode(sectorLabelNode, "Outer Radius", true, increment, pointFormat, NonSI.PIXEL, 0);

		createAngleNode(sectorLabelNode, "Angle 1", true, increment, pointFormat, NonSI.DEGREE_ANGLE, 0);
		createAngleNode(sectorLabelNode, "Angle 2", true, increment, pointFormat, NonSI.DEGREE_ANGLE, 0);
		return sectorLabelNode;
	}

	public LabelNode createCircleRegion(IRegion region) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		double increment = getDecimal(pointFormat);

		final LabelNode circleLabelNode = new LabelNode(region.getName(), root);
		circleLabelNode.setTooltip("Circle region selection");
		circleLabelNode.setEditable(true);
		registerNode(circleLabelNode);
		circleLabelNode.setDefaultExpanded(true);

		createLengthNode(circleLabelNode, "X Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);
		createLengthNode(circleLabelNode, "Y Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);

		createLengthNode(circleLabelNode, "Radius", true, increment, pointFormat, NonSI.PIXEL, 0);
		return circleLabelNode;
	}

	public LabelNode createRingRegion(IRegion region) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		double increment = getDecimal(pointFormat);

		// RingROI Node
		final LabelNode ringLabelNode = new LabelNode(region.getName(), root);
		ringLabelNode.setTooltip("Ring region selection");
		ringLabelNode.setEditable(true);
		registerNode(ringLabelNode);
		ringLabelNode.setDefaultExpanded(true);

		createLengthNode(ringLabelNode, "X Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);
		createLengthNode(ringLabelNode, "Y Centre Point", true, increment, pointFormat, NonSI.PIXEL, 0);

		createLengthNode(ringLabelNode, "Inner Radius", true, increment, pointFormat, NonSI.PIXEL, 0);
		createLengthNode(ringLabelNode, "Outer Radius", true, increment, pointFormat, NonSI.PIXEL, 0);

		createAngleNode(ringLabelNode, "Angle 1", false, increment, pointFormat, NonSI.DEGREE_ANGLE, 0);
		createAngleNode(ringLabelNode, "Angle 2", false, increment, pointFormat, NonSI.DEGREE_ANGLE, 360);
		return ringLabelNode;
	}

	private void createAngleNode(final LabelNode labelNode, String nodeName, boolean editable,
			double increment, String pointFormat, Unit<Angle> unit, double value) {
		NumericNode<Angle> node = new NumericNode<Angle>(nodeName, labelNode, unit);
		registerNode(node);
		node.setEditable(editable);
		node.setValue(value, unit);
		if (editable) {
			node.setLowerBound(Double.MIN_VALUE);
			node.setUpperBound(Double.MAX_VALUE);
			node.setIncrement(increment);
			node.setFormat(pointFormat);
			node.addAmountListener(new AmountListener<Angle>() {
				@Override
				public void amountChanged(AmountEvent<Angle> evt) {
					if(!isDragged)
						setValue(labelNode);
				}
			});
		}
	}

	private void createLengthNode(final LabelNode labelNode, String nodeName, boolean editable,
			double increment, String pointFormat, Unit<?> unit, double value) {
		if (unit.isCompatible(NonSI.PIXEL)) {
			NumericNode<Length> node = new NumericNode<Length>(nodeName, labelNode, (Unit<Length>) unit);
			registerNode(node);
			node.setEditable(editable);
			node.setFormat(pointFormat);
			node.setValue(value, (Unit<Length>)unit);
			if (editable) {
				node.setIncrement(increment);
				node.setLowerBound(Double.MIN_VALUE);
				node.setUpperBound(Double.MAX_VALUE);
				node.addAmountListener(new AmountListener<Length>() {
					@Override
					public void amountChanged(AmountEvent<Length> evt) {
						if(!isDragged)
							setValue(labelNode);
					}
				});
			}
		} else if (unit.isCompatible(Dimensionless.UNIT)) {
			NumericNode<Dimensionless> node = new NumericNode<Dimensionless>(nodeName, labelNode, (Unit<Dimensionless>) unit);
			registerNode(node);
			node.setEditable(editable);
			node.setFormat(pointFormat);
			node.setValue(value, (Unit<Dimensionless>)unit);
		}
	}

	protected void setValue(LabelNode regionNode) {
		IRegion region = plottingSystem.getRegion(regionNode.getLabel());
		if (region == null)
			return;
		IROI roi = region.getROI();
		if (roi instanceof RectangularROI) {
			double xStart = ((NumericNode<Length>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<Length>)regionNode.getChildAt(1)).getDoubleValue();
			double width = ((NumericNode<Length>)regionNode.getChildAt(2)).getDoubleValue();
			double height = ((NumericNode<Length>)regionNode.getChildAt(3)).getDoubleValue();
			double angle = ((NumericNode<Angle>)regionNode.getChildAt(4)).getDoubleValue();
			((RectangularROI) roi).setPoint(new double[] { xStart, yStart });
			((RectangularROI) roi).setLengths(new double[] { width, height });
			((RectangularROI) roi).setAngledegrees(angle);
			
		} else if (roi instanceof LinearROI) {
			double xStart = ((NumericNode<Length>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<Length>)regionNode.getChildAt(1)).getDoubleValue();
			double xEnd = ((NumericNode<Length>)regionNode.getChildAt(2)).getDoubleValue();
			double yEnd = ((NumericNode<Length>)regionNode.getChildAt(3)).getDoubleValue();
			double angle = ((NumericNode<Angle>)regionNode.getChildAt(4)).getDoubleValue();
			((LinearROI) roi).setPoint(new double[] { xStart, yStart });
			((LinearROI) roi).setEndPoint(new double[] { xEnd, yEnd });
			((LinearROI) roi).setAngledegrees(angle);
		} else if (roi instanceof SectorROI) {
		} else if (roi instanceof RingROI) {
		} else if (roi instanceof CircularROI) {
		}
		region.setROI(roi);
		plottingSystem.repaint(false);
	}

	private void updateCircleRegion(IRegion region) {
		// TODO Auto-generated method stub
		
	}

	private void updateRingRegion(IRegion region) {
		// TODO Auto-generated method stub
		
	}

	private void updateLineRegion(IRegion region, double maxIntensity) {
		for (LabelNode node : regionNodes) {
			String label = node.getLabel();
			if (label.equals(region.getName())) {
				List<TreeNode> children = node.getChildren();
				NumericNode<Length> xStartPtNode = (NumericNode<Length>) children.get(0);
				NumericNode<Length> yStartPtNode = (NumericNode<Length>) children.get(1);
				NumericNode<Length> xEndPtNode = (NumericNode<Length>) children.get(2);
				NumericNode<Length> yEndPtNode = (NumericNode<Length>) children.get(3);
				NumericNode<Angle> angleNode = (NumericNode<Angle>) children.get(4);
				NumericNode<Dimensionless> maxIntensityNode = (NumericNode<Dimensionless>) children.get(5);

				LinearROI roi = (LinearROI)region.getROI();
				xStartPtNode.setValue(roi.getPointX(), NonSI.PIXEL);
				yStartPtNode.setValue(roi.getPointY(), NonSI.PIXEL);
				xEndPtNode.setValue(roi.getEndPoint()[0], NonSI.PIXEL);
				yEndPtNode.setValue(roi.getEndPoint()[1], NonSI.PIXEL);
				angleNode.setValue(roi.getAngleDegrees(), NonSI.DEGREE_ANGLE);
				maxIntensityNode.setValue(maxIntensity, Dimensionless.UNIT);
			}
		}
	}

	private void updateSectorRegion(IRegion region) {
		// TODO Auto-generated method stub
		
	}

	private void updateRectangularRegion(IRegion region, double maxIntensity, double sum) {
		for (LabelNode node : regionNodes) {
			String label = node.getLabel();
			if (label.equals(region.getName())) {
				List<TreeNode> children = node.getChildren();
				NumericNode<Length> xStartPtNode = (NumericNode<Length>) children.get(0);
				NumericNode<Length> yStartPtNode = (NumericNode<Length>) children.get(1);
				NumericNode<Length> widthNode = (NumericNode<Length>) children.get(2);
				NumericNode<Length> heightNode = (NumericNode<Length>) children.get(3);
				NumericNode<Angle> angleNode = (NumericNode<Angle>) children.get(4);
				NumericNode<Dimensionless> maxIntensityNode = (NumericNode<Dimensionless>) children.get(5);
				NumericNode<Dimensionless> sumNode = (NumericNode<Dimensionless>) children.get(6);

				RectangularROI roi = (RectangularROI)region.getROI();
				xStartPtNode.setValue(roi.getPointX(), NonSI.PIXEL);
				yStartPtNode.setValue(roi.getPointY(), NonSI.PIXEL);
				widthNode.setValue(roi.getLengths()[0], NonSI.PIXEL);
				heightNode.setValue(roi.getLengths()[1], NonSI.PIXEL);
				angleNode.setValue(roi.getAngleDegrees(), NonSI.DEGREE_ANGLE);
				maxIntensityNode.setValue(maxIntensity, Dimensionless.UNIT);
				sumNode.setValue(sum, Dimensionless.UNIT);
			}
		}
	}

	private double getDecimal(String format) {
		int decimal = format.split(".").length > 0 ? format.split(".")[1].length() : 0;
		double increment = 1;
		for (int i = 0; i < decimal; i++) {
			increment = (double) increment / 100;
		}
		return increment;
	}

	@Override
	public void dispose() {
		super.dispose();
		deactivate();
	}

	public void addRegion(IRegion region) {
		regions.add(region);
		IROI roi = region.getROI();
		if (roi instanceof RectangularROI) {
			regionNodes.add(createRectangularRegion(region));
		} else if (roi instanceof LinearROI) {
			regionNodes.add(createLineRegion(region));
		} else if (roi instanceof SectorROI) {
			regionNodes.add(createSectorRegion(region));
		} else if (roi instanceof RingROI) {
			regionNodes.add(createRingRegion(region));
		} else if (roi instanceof CircularROI) {
			regionNodes.add(createCircleRegion(region));
		}
	}

	public void updateRegion(IRegion region, double maxIntensity, double sum) {
		IROI roi = region.getROI();
		if (roi instanceof RectangularROI) {
			updateRectangularRegion(region, maxIntensity, sum);
		} else if (roi instanceof LinearROI) {
			updateLineRegion(region, maxIntensity);
		} else if (roi instanceof SectorROI) {
			updateSectorRegion(region);
		} else if (roi instanceof RingROI) {
			updateRingRegion(region);
		} else if (roi instanceof CircularROI) {
			updateCircleRegion(region);
		}
	}

	public void removeRegion(LabelNode regionNode) {
		regionNodes.remove(regionNode);
		root.removeChild(regionNode);
		regionNode.dispose();
		if (root.getChildCount() < 2) {
			viewer.remove(root);
		}
		viewer.refresh();
	}

	public void setIsDragged(boolean isDragged) {
		this.isDragged = isDragged;
	}
}
