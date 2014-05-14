package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.tree.TreeNode;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEvent;
import org.dawnsci.common.widgets.tree.UnitListener;
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

	private boolean isRegionDragged = false;

	private boolean isTreeModified = false;
	private Collection<IRegion> regions;
	private List<RegionNode> regionNodes = new ArrayList<RegionNode>();
	private IPlottingSystem plottingSystem;

	public RegionEditorTreeModel(IPlottingSystem plottingSystem, Collection<IRegion> regions) throws Exception {
		this.plottingSystem = plottingSystem;
		this.regions = regions;
		for (IRegion region : regions) {
			addRegion(region, 0, 0);
		}
	}

	public void addRegion(IRegion region, double maxIntensity, double sum) {
		String name = region.getName();
		if (!nodeExist(regionNodes, name)) {
			regions.add(region);
			regionNodes.add(createRegion(region, maxIntensity, sum));
		}
	}

	private boolean nodeExist(List<RegionNode> node, String name) {
		for (RegionNode regionNode : node) {
			if(regionNode.getLabel().equals(name))
				return true;
		}
		return false;
	}

	private RegionNode createRegion(IRegion region, double maxIntensity, double sum) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		String intensityFormat = store.getString(RegionEditorConstants.INTENSITY_FORMAT);
		String sumFormat = store.getString(RegionEditorConstants.SUM_FORMAT);
		double increment = getDecimal(pointFormat);

		final RegionNode node = new RegionNode(region.getName(), root);
		node.setTooltip(region.getLabel());
		node.setEditable(true);
		node.setVisible(region.isVisible());
		node.setActive(region.isActive());
		node.setMobile(region.isMobile());
		registerNode(node);
		node.setDefaultExpanded(true);

		IROI roi = region.getROI();
		Map<String, Double> roiInfos = RegionNodeFactory.getRegionNodeInfos(roi);
		if (roiInfos == null)
			return null;
		Set<Entry<String,Double>> set = roiInfos.entrySet();
		for (Entry<String, Double> entry : set) {
			String key = entry.getKey();
			if (key.contains("Angle")) {
				if (node.isAngleInRadian())
					createAngleNode(node, entry.getKey(), true, increment, pointFormat, SI.RADIAN, Math.toRadians(entry.getValue()));
				else
					createAngleNode(node, entry.getKey(), true, increment, pointFormat, NonSI.DEGREE_ANGLE, entry.getValue());
			} else if (key.equals("Max Intensity"))
				createLengthNode(node, key, false, increment, intensityFormat, Dimensionless.UNIT, maxIntensity);
			else if (key.equals("Sum"))
				createLengthNode(node, key, false, increment, sumFormat, Dimensionless.UNIT, sum);
			else
				createLengthNode(node, key, true, increment, pointFormat, NonSI.PIXEL, entry.getValue());
		}
		return node;
	}

	private void createAngleNode(final RegionNode labelNode, String nodeName, boolean editable,
			double increment, String pointFormat, Unit<Angle> unit, double value) {
		final NumericNode<Angle> node = new NumericNode<Angle>(nodeName, labelNode, unit);
		registerNode(node);
		node.setEditable(editable);
		node.setValue(value, unit);
		node.setFormat(pointFormat);
		if (editable) {
			node.setLowerBound(Double.MIN_VALUE);
			node.setUpperBound(Double.MAX_VALUE);
			node.setIncrement(increment);
			node.addAmountListener(new AmountListener<Angle>() {
				@Override
				public void amountChanged(AmountEvent<Angle> evt) {
					try {
						isTreeModified = true;
						if(!isRegionDragged)
							setValue(labelNode);
					} finally {
						isTreeModified = false;
					}
				}
			});
			node.setUnits(NonSI.DEGREE_ANGLE, SI.RADIAN);
			node.addUnitListener(new UnitListener() {
				@Override
				public void unitChanged(UnitEvent<? extends Quantity> evt) {
					if (evt.getUnit().equals(NonSI.DEGREE_ANGLE)) {
						labelNode.setAngleInRadian(false);
					} else {
						labelNode.setAngleInRadian(true);
					}
					viewer.refresh();
				}
			});
		}
	}

	private void createLengthNode(final RegionNode labelNode, String nodeName, boolean editable,
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
						try {
							isTreeModified = true;
							if(!isRegionDragged)
								setValue(labelNode);
						} finally {
							isTreeModified = false;
						}
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

	protected void setValue(RegionNode regionNode) {
		IRegion region = plottingSystem.getRegion(regionNode.getLabel());
		if (region == null)
			return;
		IROI roi = region.getROI();
		if (roi instanceof RectangularROI) {
			double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
			double width = ((NumericNode<?>)regionNode.getChildAt(2)).getDoubleValue();
			double height = ((NumericNode<?>)regionNode.getChildAt(3)).getDoubleValue();
			double angle = ((NumericNode<?>)regionNode.getChildAt(4)).getDoubleValue();
			((RectangularROI) roi).setPoint(new double[] { xStart, yStart });
			((RectangularROI) roi).setLengths(new double[] { width, height });
			if (regionNode.isAngleInRadian())
				((RectangularROI) roi).setAngle(angle);
			else
				((RectangularROI) roi).setAngledegrees(angle);
		} else if (roi instanceof LinearROI) {
			double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
			double xEnd = ((NumericNode<?>)regionNode.getChildAt(2)).getDoubleValue();
			double yEnd = ((NumericNode<?>)regionNode.getChildAt(3)).getDoubleValue();
			double angle = ((NumericNode<?>)regionNode.getChildAt(4)).getDoubleValue();
			((LinearROI) roi).setPoint(new double[] { xStart, yStart });
			((LinearROI) roi).setEndPoint(new double[] { xEnd, yEnd });
			if (regionNode.isAngleInRadian())
				((LinearROI) roi).setAngle(angle);
			else
				((LinearROI) roi).setAngledegrees(angle);
		} else if (roi instanceof SectorROI) {
			if (regionNode.getChildCount() == 6) {
				double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
				double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
				double innerRadius = ((NumericNode<?>)regionNode.getChildAt(2)).getDoubleValue();
				double outerRadius = ((NumericNode<?>)regionNode.getChildAt(3)).getDoubleValue();
				double angle1 = ((NumericNode<?>)regionNode.getChildAt(4)).getDoubleValue();
				double angle2 = ((NumericNode<?>)regionNode.getChildAt(5)).getDoubleValue();
				((SectorROI) roi).setPoint(new double[] { xStart, yStart });
				((SectorROI) roi).setRadii(innerRadius, outerRadius);
				if (regionNode.isAngleInRadian())
					((SectorROI) roi).setAngles(new double[] {angle1, angle2});
				else
					((SectorROI) roi).setAnglesdegrees(new double[] {angle1, angle2});
			}
		} else if (roi instanceof RingROI) {
			double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
			double innerRadius = ((NumericNode<?>)regionNode.getChildAt(2)).getDoubleValue();
			double outerRadius = ((NumericNode<?>)regionNode.getChildAt(3)).getDoubleValue();
			((RingROI) roi).setPoint(new double[] { xStart, yStart });
			((RingROI) roi).setRadii(innerRadius, outerRadius);
		} else if (roi instanceof CircularROI) {
			double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
			double radius = ((NumericNode<?>)regionNode.getChildAt(2)).getDoubleValue();
			((CircularROI) roi).setPoint(new double[] { xStart, yStart });
			((CircularROI) roi).setRadius(radius);
		}
		region.setROI(roi);
		plottingSystem.repaint(false);
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
	}

	public void updateRegion(IRegion region, double maxIntensity, double sum) {
		for (RegionNode node : regionNodes) {
			String label = node.getLabel();
			if (label.equals(region.getName())) {
				List<TreeNode> children = node.getChildren();
				IROI roi = region.getROI();
				Map<String, Double> roiInfos = RegionNodeFactory.getRegionNodeInfos(roi);
				Set<Entry<String,Double>> set = roiInfos.entrySet();
				int i = 0;
				for (Entry<String, Double> entry : set) {
					NumericNode<?> aChild = (NumericNode<?>) children.get(i);
					String key = entry.getKey();
					if (key.equals("Max Intensity"))
						aChild.setDoubleValue(maxIntensity);
					else if (key.equals("Sum"))
						aChild.setDoubleValue(sum);
					else if (key.contains("Angle") && node.isAngleInRadian())
						aChild.setDoubleValue(Math.toRadians(entry.getValue()));
					else
						aChild.setDoubleValue(entry.getValue());
					i++;
				}
			}
		}
	}

	public void removeRegion(RegionNode regionNode) {
		int childrenNumber = root.getChildCount();
		regionNodes.remove(regionNode);
		root.removeChild(regionNode);
		regionNode.dispose();
		if (childrenNumber < 2) {
			viewer.remove(root);
		}
		viewer.refresh();
	}

	public boolean isRegionDragged() {
		return isRegionDragged;
	}

	public void setRegionDragged(boolean isRegionDragged) {
		this.isRegionDragged = isRegionDragged;
	}

	public boolean isTreeModified() {
		return isTreeModified;
	}

	public void setTreeModified(boolean isTreeModified) {
		this.isTreeModified = isTreeModified;
	}
}
