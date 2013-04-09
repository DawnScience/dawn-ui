/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.composites;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.roi.ROIEditTable;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.draw2d.swtxy.AspectAxis;
import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.RegionCoordinateSystem;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.dawnsci.plotting.system.PlottingSystemImpl;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * General composite for editing ROIs.
 * 
 * @author fcp94556
 *
 */
public class ROIComposite extends Composite {

	private XYRegionGraph xyGraph;
	private Text nameText;
	private CCombo regionType;
	private AbstractSelectionRegion editingRegion;
	private ColorSelector colorSelector;
	private Spinner alpha;
	// private Button showLabel;
	private ROIEditTable roiViewer;
	private Label symmetryLabel;
	private CCombo symmetry;

	/**
	 * Create a Region composite
	 * 
	 * @param parent
	 * @param style
	 * @param plottingSystem
	 * @param defaultRegion
	 */
	public ROIComposite(final Composite parent, final int style,
			final AbstractPlottingSystem plottingSystem,
			final RegionType defaultRegion) {

		super(parent, SWT.NONE);

		this.xyGraph = ((PlottingSystemImpl) plottingSystem).getLightWeightGraph();

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));

		final Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name   ");
		nameLabel.setLayoutData(new GridData());

		nameText = new Text(this, SWT.BORDER | SWT.SINGLE);
		nameText.setToolTipText("Region name");
		nameText.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		nameText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				AbstractSelectionRegion region = getEditingRegion();
				region.repaint();
			}
		});

		final Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setText("Type");
		typeLabel.setLayoutData(new GridData());
		regionType = new CCombo(this, SWT.NONE);
		regionType.setEditable(false);
		regionType.setToolTipText("Region type");
		regionType.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		for (RegionType type : RegionType.ALL_TYPES) {
			regionType.add(type.getName());
		}
		regionType.select(defaultRegion.getIndex());

		final Label horiz = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		horiz.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));

		Label colorLabel = new Label(this, 0);
		colorLabel.setText("Selection Color");
		colorLabel.setLayoutData(new GridData());

		colorSelector = new ColorSelector(this);
		colorSelector.getButton().setLayoutData(new GridData());
		colorSelector.setColorValue(defaultRegion.getDefaultColor().getRGB());

		colorSelector.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				AbstractSelectionRegion region = getEditingRegion();
				region.repaint();
			}
		});

		Label alphaLabel = new Label(this, 0);
		alphaLabel.setText("Alpha level");
		alphaLabel.setLayoutData(new GridData());

		alpha = new Spinner(this, SWT.NONE);
		alpha.setToolTipText("Alpha transparency level of the shaded shape");
		alpha.setLayoutData(new GridData());
		alpha.setMinimum(0);
		alpha.setMaximum(255);
		alpha.setSelection(80);
		alpha.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractSelectionRegion region = getEditingRegion();
				region.repaint();
			}
		});

		this.symmetryLabel = new Label(this, SWT.NONE);
		symmetryLabel.setText("Symmetry");
		symmetryLabel.setToolTipText("Set the symmetry of the region.");
		symmetryLabel.setLayoutData(new GridData(0, 0, false, false, 1, 1));

		this.symmetry = new CCombo(this, SWT.NONE);
		for (int index : SectorROI.getSymmetriesPossible().keySet()) {
			symmetry.add(SectorROI.getSymmetryText(index));
		}
		symmetry.setLayoutData(new GridData(SWT.FILL, 0, true, false));

		final Label spacer = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));

		// We add a composite for setting the region location programmatically.
		final Label location = new Label(this, SWT.NONE);
		location.setText("Region Location");
		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		this.roiViewer = new ROIEditTable();
		roiViewer.createPartControl(this);

		// Should be last
		nameText.setText(getDefaultName(defaultRegion.getIndex()));

	}

	private static Map<Integer, Integer> countMap;

	private String getDefaultName(int sel) {
		if (countMap == null)
			countMap = new HashMap<Integer, Integer>(5);
		if (!countMap.containsKey(sel)) {
			countMap.put(sel, 0);
		}
		int count = countMap.get(sel);
		count++;
		return regionType.getItem(sel) + " " + count;
	}

	public AbstractSelectionRegion createRegion() throws Exception {

		final AspectAxis xAxis = getAxis(xyGraph.getXAxisList(), xIndex);
		final AspectAxis yAxis = getAxis(xyGraph.getYAxisList(), yIndex);

		AbstractSelectionRegion region = null;

		final String txt = nameText.getText();
		final Pattern pattern = Pattern.compile(".* (\\d+)");
		final Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			final int count = Integer.parseInt(matcher.group(1));
			countMap.put(regionType.getSelectionIndex(), count);
		}

		region = xyGraph.createRegion(txt, xAxis, yAxis,
				RegionType.getRegion(regionType.getSelectionIndex()), true);

		this.editingRegion = region;
		getEditingRegion();

		return region;
	}

	private IROIListener roiListener;
	private int xIndex;
	private int yIndex;

	public void setEditingRegion(final AbstractSelectionRegion region) {

		this.editingRegion = region;
		this.roiViewer.setRegion(region.getROI(), region.getRegionType(), region.getCoordinateSystem());
		this.roiListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				region.setROI(evt.getROI());
			}
		};
		roiViewer.addROIListener(roiListener);

		Range range = xyGraph.primaryXAxis.getRange();
		roiViewer.setXLowerBound(Math.min(range.getUpper(), range.getLower()));
		roiViewer.setXUpperBound(Math.max(range.getUpper(), range.getLower()));

		range = xyGraph.primaryYAxis.getRange();
		roiViewer.setYLowerBound(Math.min(range.getUpper(), range.getLower()));
		roiViewer.setYUpperBound(Math.max(range.getUpper(), range.getLower()));

		nameText.setText(region.getName());
		regionType.select(region.getRegionType().getIndex());
		regionType.setEnabled(false);
		regionType.setEditable(false);

		xIndex = xyGraph.getXAxisList().indexOf(
				region.getCoordinateSystem().getX());

		yIndex = xyGraph.getYAxisList().indexOf(
				region.getCoordinateSystem().getY());

		colorSelector.setColorValue(region.getRegionColor().getRGB());
		alpha.setSelection(region.getAlpha());
		// showLabel.setSelection(region.isShowLabel());

		if (region.getRegionType() != RegionType.SECTOR) {
			GridUtils.setVisible(this.symmetryLabel, false);
			GridUtils.setVisible(this.symmetry, false);
		} else {
			SectorROI sroi = (SectorROI) editingRegion.getROI();
			if (sroi != null) {
				int sym = sroi.getSymmetry();
				symmetry.select(sym);
			}
		}
	}

	public void dispose() {
		if (roiListener != null) {
			try {
				roiViewer.removeROIListener(roiListener);
			} catch (Exception ne) {
				logger.error("Cannot remove roi listener", ne);
			}
		}
		super.dispose();
	}

	public AbstractSelectionRegion getEditingRegion() {

		final String txt = nameText.getText();
		xyGraph.renameRegion(editingRegion, txt);
		editingRegion.setName(txt);

		final AspectAxis x = getAxis(xyGraph.getXAxisList(), xIndex);
		final AspectAxis y = getAxis(xyGraph.getYAxisList(), yIndex);
		RegionCoordinateSystem sys = new RegionCoordinateSystem(
				getImageTrace(), x, y);
		editingRegion.setCoordinateSystem(sys);
		editingRegion.setRegionColor(new Color(getDisplay(), colorSelector
				.getColorValue()));
		editingRegion.setAlpha(alpha.getSelection());
		// editingRegion.setShowLabel(showLabel.getSelection());

		if (editingRegion.getRegionType() == RegionType.SECTOR) {
			SectorROI sroi = (SectorROI) editingRegion.getROI();
			if (sroi != null) {
				sroi = sroi.copy();
				sroi.setSymmetry(symmetry.getSelectionIndex());
				editingRegion.setROI(sroi);
			}
		}

		return editingRegion;
	}

	private AspectAxis getAxis(List<Axis> xAxisList, int selectionIndex) {
		if (selectionIndex < 0)
			selectionIndex = 0;
		return (AspectAxis) xAxisList.get(selectionIndex);
	}

	public IImageTrace getImageTrace() {
		return ((RegionArea) xyGraph.getPlotArea()).getImageTrace();
	}

	public void applyChanges() {
		// this.roiViewer.cancelEditing();
		AbstractSelectionRegion region = getEditingRegion();
		region.repaint();
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ROIComposite.class);

	public void cancelChanges() {
		try {
			editingRegion.setROI(roiViewer.getOriginalRoi());
		} catch (Throwable ne) {
			logger.error(
					"Problem reverting region  " + editingRegion.getName(), ne);
		}
	}

	public void disposeRegion(AbstractSelectionRegion region) {
		xyGraph.disposeRegion(region);
	}
}
