/*
 * Copyright (c) 2012, 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.swing.tree.TreeNode;

import si.uom.SI;
import si.uom.NonSI;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.ComboNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEvent;
import org.dawnsci.common.widgets.tree.UnitListener;
import org.dawnsci.common.widgets.tree.ValueEvent;
import org.dawnsci.common.widgets.tree.ValueListener;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Holds data for the Region Editor model.
 * 
 * @author wqk87977
 *
 */
public class RegionEditorTreeModel extends AbstractNodeModel {

	private boolean roiSettingAllowed = true;
	private boolean isTreeModified = false;
	private IPlottingSystem<?> plottingSystem;

	public RegionEditorTreeModel(IPlottingSystem<?> plottingSystem) throws Exception {
		this.plottingSystem = plottingSystem;
	}

	public void addRegion(IRegion region, double maxIntensity, double sum) {
		String name = region.getName();
		List<TreeNode> nodes = root.getChildren();
		if (!nodeExist(nodes, name)) {
			createRegion(region, maxIntensity, sum);
		}
	}

	private boolean nodeExist(List<TreeNode> nodes, String name) {
		if (nodes == null)
			return false;
		for (TreeNode node : nodes) {
			if (node instanceof RegionEditorNode) {
				RegionEditorNode regionNode = (RegionEditorNode) node;
				if(regionNode.getLabel().equals(name))
					return true;
			}
		}
		return false;
	}

	private RegionEditorNode createRegion(IRegion region, double maxIntensity, double sum) {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
		String angleFormat = store.getString(RegionEditorConstants.ANGLE_FORMAT);
		String intensityFormat = store.getString(RegionEditorConstants.INTENSITY_FORMAT);
		String sumFormat = store.getString(RegionEditorConstants.SUM_FORMAT);
		double increment = ToolUtils.getDecimal(pointFormat);
		double incrementAngle = ToolUtils.getDecimal(angleFormat);

		final RegionEditorNode node = new RegionEditorNode(plottingSystem, region, root);
		node.setTooltip(region.getName());
		node.setEditable(true);
		node.setVisible(region.isVisible());
		node.setActive(region.isActive());
		node.setMobile(region.isMobile());
		registerNode(node);
		node.setDefaultExpanded(true);

		IROI roi = region.getROI();
		Map<String, Object> roiInfos = RegionEditorNodeFactory.getRegionNodeInfos(roi);
		if (roiInfos == null)
			return null;
		Set<Entry<String,Object>> set = roiInfos.entrySet();
		for (Entry<String, Object> entry : set) {
			String key = entry.getKey();
			if (key.contains(RegionEditorNodeFactory.ANGLE)) {
				if (node.isAngleInRadian())
					createAngleNode(node, entry.getKey(), true, incrementAngle, pointFormat, SI.RADIAN, Math.toRadians((Double)entry.getValue()));
				else
					createAngleNode(node, entry.getKey(), true, incrementAngle, angleFormat, NonSI.DEGREE_ANGLE, (Double)entry.getValue());
			} else if (key.contains(RegionEditorNodeFactory.INTENSITY))
				createLengthNode(node, key, false, increment, intensityFormat, Dimensionless.UNIT, maxIntensity);
			else if (key.contains(RegionEditorNodeFactory.SUM))
				createLengthNode(node, key, false, increment, sumFormat, Dimensionless.UNIT, sum);
			else if (key.contains(RegionEditorNodeFactory.SYMMETRY))
				createSymmetryNode(node, key, true, (Integer)entry.getValue());
			else
				createLengthNode(node, key, true, increment, pointFormat, NonSI.PIXEL, (Double)entry.getValue());
		}
		return node;
	}

	private void createSymmetryNode(final RegionEditorNode regionNode, String nodeName,
			boolean editable, int value) {
		Collection<String> items = SectorROI.getSymmetriesPossible().values();
		final String[] symmetries = (String[]) items.toArray(new String[items.size()]);
		final ComboNode node = new ComboNode(nodeName, symmetries, regionNode);
		node.setEditable(editable);
		node.setTooltip("Symmetry type of sector ROI");
		node.setValue(value);
		if (editable) {
			node.addValueListener(new ValueListener() {
				@Override
				public void valueChanged(ValueEvent evt) {
					try {
						isTreeModified = true;
						setValue(regionNode);
					} finally {
						isTreeModified = false;
					}
				}
			});
		}
		registerNode(node);

	}

	private void createAngleNode(final RegionEditorNode regionNode, String nodeName, boolean editable,
			double increment, String pointFormat, Unit<Angle> unit, double value) {
		final NumericNode<Angle> node = new NumericNode<Angle>(nodeName, regionNode, unit);
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
						setValue(regionNode);
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
						regionNode.setAngleInRadian(false);
					} else {
						regionNode.setAngleInRadian(true);
					}
					viewer.refresh();
				}
			});
		}
	}

	private void createLengthNode(final RegionEditorNode regionNode, String nodeName, boolean editable,
			double increment, String pointFormat, Unit<?> unit, double value) {
		if (unit.isCompatible(NonSI.PIXEL)) {
			NumericNode<Length> node = new NumericNode<Length>(nodeName, regionNode, (Unit<Length>) unit);
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
							setValue(regionNode);
						} finally {
							isTreeModified = false;
						}
					}
				});
			}
		} else if (unit.isCompatible(Dimensionless.UNIT)) {
			NumericNode<Dimensionless> node = new NumericNode<Dimensionless>(nodeName, regionNode, (Unit<Dimensionless>) unit);
			registerNode(node);
			node.setEditable(editable);
			node.setFormat(pointFormat);
			node.setValue(value, (Unit<Dimensionless>)unit);
		}
	}

	/**
	 * TODO make this method more generic
	 * @param regionNode
	 */
	protected void setValue(RegionEditorNode regionNode) {
		
		if (!roiSettingAllowed) return;
		
		IRegion region = null;
		Collection<IRegion> regions = plottingSystem.getRegions();
		for (IRegion iRegion : regions) {
			if (iRegion.getName().equals(regionNode.getLabel()))
				region = iRegion;
		}
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
			if (regionNode.getChildCount() == 7) {
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
				((SectorROI) roi).setSymmetry((Integer) ((ComboNode)regionNode.getChildAt(6)).getValue());
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
		} else if (roi instanceof PointROI) {
			double xStart = ((NumericNode<?>)regionNode.getChildAt(0)).getDoubleValue();
			double yStart = ((NumericNode<?>)regionNode.getChildAt(1)).getDoubleValue();
			((PointROI) roi).setPoint(new double[] {xStart, yStart} );
		}
		region.setROI(roi);
		plottingSystem.repaint(false);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * Called to set the region in the model.
	 * @param roi
	 * @param maxIntensity
	 * @param sum
	 */
	void updateRegion(IROI roi, double maxIntensity, double sum) {
		
		try {
			roiSettingAllowed = false;
			
			List<TreeNode> nodes = root.getChildren();
			if (nodes == null)
				return;
			for (TreeNode node : nodes) {
				if (node instanceof RegionEditorNode) {
					RegionEditorNode regionNode = (RegionEditorNode) node;
					String label = regionNode.getLabel();
					if (label.equals(roi.getName())) {
						List<TreeNode> children = regionNode.getChildren();
						if (children == null)
							return;
						Map<String, Object> roiInfos = RegionEditorNodeFactory.getRegionNodeInfos(roi);
						Set<Entry<String,Object>> set = roiInfos.entrySet();
						int i = 0;
						for (Entry<String, Object> entry : set) {
							if (children.get(i) instanceof NumericNode<?>) {
								NumericNode<?> aChild = (NumericNode<?>) children.get(i);
								String key = entry.getKey();
								if (key.contains("Intensity"))
									aChild.setDoubleValue(maxIntensity);
								else if (key.contains("Sum"))
									aChild.setDoubleValue(sum);
								else if (key.contains("Angle") && regionNode.isAngleInRadian())
									aChild.setDoubleValue(Math.toRadians((Double)entry.getValue()));
								else
									aChild.setDoubleValue((Double)entry.getValue());
								i++;
							}
							else if (children.get(i) instanceof ComboNode) {
								ComboNode aChild = (ComboNode) children.get(i);
								String key = entry.getKey();
								if (key.contains("Symmetry"))
									aChild.setValue(entry.getValue());
								i++;
							}
						}
					}
				}
			}
		} finally {
			roiSettingAllowed = true;
		}
	}

	public void removeRegion(RegionEditorNode regionNode) {
		int childrenNumber = root.getChildCount();
		root.removeChild(regionNode);
		regionNode.dispose();
		if (childrenNumber < 2) {
			viewer.remove(root);
		}
		viewer.refresh();
	}

	public boolean isTreeModified() {
		return isTreeModified;
	}

	public void setTreeModified(boolean isTreeModified) {
		this.isTreeModified = isTreeModified;
	}
}
