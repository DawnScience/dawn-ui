/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.RegionCoordinateSystem;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.dawnsci.plotting.roi.ROIEditTable;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionEditComposite extends Composite {

	private XYRegionGraph xyGraph;

	private Text   nameText;
	private CCombo regionType;
	private CCombo xCombo, yCombo;
	private Button showPoints;
	private IRegion editingRegion;
	private ColorSelector colorSelector;
	private Spinner alpha;
	private Button mobile;
	private Button visible;
	private Button showLabel;
	private Button fillRegion;

	private ROIEditTable roiViewer;

	private boolean isImplicit;

	private IPlottingSystem<?> plottingSystem;

	/**
	 * Throws exception if not LightWeightPlottingSystem
	 * 
	 * Can be used to edit regions from outside if required.
	 * 
	 * @param parent
	 * @param plottingSystem
	 * @param style
	 * @param sys
	 * @param defaultRegion
	 * @param isImplicit
	 */
	public RegionEditComposite(final Composite parent, final IPlottingSystem<?> plottingSystem, final int style, final IPlottingSystem<Composite> sys, final RegionType defaultRegion, final boolean isImplicit) {
     
		this(parent, plottingSystem,  style, (XYRegionGraph)sys.getAdapter(XYRegionGraph.class), defaultRegion, isImplicit);
	}
	
	/**
	 * Used internally
	 * @param parent
	 * @param plottingSystem
	 * @param style
	 * @param xyGraph
	 * @param defaultRegion
	 * @param isImplicit Flag to tell whether the RegionComposite is used in a specific window with an apply button or part of a view where the changes are implicit
	 */
	public RegionEditComposite(final Composite parent, final IPlottingSystem<?> plottingSystem, final int style, final XYRegionGraph xyGraph, final RegionType defaultRegion, final boolean isImplicit) {
		
		super(parent, SWT.NONE);
		
		this.setImplicit(isImplicit);

		this.xyGraph = xyGraph;
		this.plottingSystem = plottingSystem;

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));

		final Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name   ");
		nameLabel.setLayoutData(new GridData());

		nameText = new Text(this, SWT.BORDER | SWT.SINGLE);
		nameText.setToolTipText("Region name");
		nameText.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		if(isImplicit()){
			nameText.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}
		
		final Label horiz = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		horiz.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));

		final Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setText("Type");
		typeLabel.setLayoutData(new GridData());

		regionType = new CCombo(this, SWT.BORDER |SWT.NONE);
		regionType.setEditable(false);
		regionType.setToolTipText("Region type");
		regionType.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		for (RegionType type : RegionType.ALL_TYPES) {
			regionType.add(type.getName());
		}
		regionType.select(defaultRegion.getIndex());

		xCombo = createAxisChooser(this, "X Axis", xyGraph.getXAxisList());	
		yCombo = createAxisChooser(this, "Y Axis", xyGraph.getYAxisList());	
		
		Label colorLabel = new Label(this, 0);
		colorLabel.setText("Selection Color");		
		colorLabel.setLayoutData(new GridData());
		
		colorSelector = new ColorSelector(this);
		colorSelector.getButton().setLayoutData(new GridData());		
		colorSelector.setColorValue(defaultRegion.getDefaultColor().getRGB());
		if(isImplicit()){
			colorSelector.addListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}
		
		final Label horiz2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		horiz2.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));

		Label alphaLabel = new Label(this, 0);
		alphaLabel.setText("Alpha level");		
		alphaLabel.setLayoutData(new GridData());
		
		alpha = new Spinner(this, SWT.NONE);
		alpha.setToolTipText("Alpha transparency level of the shaded shape");
		alpha.setLayoutData(new GridData());		
		alpha.setMinimum(0);
		alpha.setMaximum(255);
		alpha.setSelection(80);
		if(isImplicit()){
			alpha.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}

		this.mobile = new Button(this, SWT.CHECK);
		mobile.setText("   Mobile   ");		
		mobile.setToolTipText("When true, this selection can be resized and moved around the graph.");
		mobile.setLayoutData(new GridData(0, 0, false, false, 2, 1));
		mobile.setSelection(true);
		if(isImplicit()){
			mobile.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}
		
		this.showPoints = new Button(this, SWT.CHECK);
		showPoints.setText("   Show vertex values");		
		showPoints.setToolTipText("When on this will show the actual value of the point in the axes it was added.");
		showPoints.setLayoutData(new GridData(0, 0, false, false, 2, 1));
		if(isImplicit()){
			showPoints.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}

		this.visible = new Button(this, SWT.CHECK);
		visible.setText("   Show region");		
		visible.setToolTipText("You may turn off the visibility of this region.");
		visible.setLayoutData(new GridData(0, 0, false, false, 2, 1));
		visible.setSelection(true);
		if(isImplicit()){
			visible.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}
		
		
		this.showLabel = new Button(this, SWT.CHECK);
		showLabel.setText("   Show name");		
		showLabel.setToolTipText("Turn on to show the name of a selection region.");
		showLabel.setLayoutData(new GridData(0, 0, false, false, 2, 1));
		showLabel.setSelection(true);
		if(isImplicit()){
			showLabel.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}

		this.fillRegion = new Button(this, SWT.CHECK);
		fillRegion.setText("   Fill region");		
		fillRegion.setToolTipText("Fill the body of an area region");
		fillRegion.setLayoutData(new GridData(0, 0, false, false, 2, 1));
		fillRegion.setSelection(true);
		if(isImplicit()){
			fillRegion.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion region = getEditingRegion();
					region.repaint();
				}
			});
		}

		final Label spacer = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		// We add a composite for setting the region location programmatically.
		final Label location = new Label(this, SWT.NONE);
		location.setText("Region Location");
		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		this.roiViewer = new ROIEditTable();
		roiViewer.createPartControl(this);

		// Should be last
		nameText.setText(getDefaultName(defaultRegion.getIndex()));

	}

	private CCombo createAxisChooser(RegionEditComposite parent,
			                         String label, List<Axis> xAxisList) {
		
		final Label xAxisLabel = new Label(parent, SWT.NONE);
		xAxisLabel.setText(label);
		xAxisLabel.setLayoutData(new GridData());

		CCombo combo = new CCombo(this, SWT.BORDER |SWT.NONE);
		combo.setEditable(false);
		combo.setToolTipText("Existing axis on the graph ");
		combo.setLayoutData(new GridData(SWT.LEFT, 0, false, false));

		for(Axis axis : xAxisList)  combo.add(axis.getTitle());
		
		combo.select(0);		
		
		return combo;
	}

	private static Map<Integer,Integer> countMap;
	private String getDefaultName(int sel) {
		if (countMap==null) countMap = new HashMap<Integer,Integer>(5);
		if (!countMap.containsKey(sel)) {
			countMap.put(sel, 0);
		}
		int count = countMap.get(sel);
		count++;
		return regionType.getItem(sel)+" "+count;
	}


	public AbstractSelectionRegion<?> createRegion() throws Exception {
		
		final IAxis xAxis = getAxis(xyGraph.getXAxisList(), xCombo.getSelectionIndex());
		final IAxis yAxis = getAxis(xyGraph.getYAxisList(), yCombo.getSelectionIndex());
		
		AbstractSelectionRegion<?> region=null;
		
		final String txt = nameText.getText();
		final Pattern pattern = Pattern.compile(".* (\\d+)");
		final Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			final int count = Integer.parseInt(matcher.group(1));
			countMap.put(regionType.getSelectionIndex(), count);
		}
		
		region = xyGraph.createRegion(txt, xAxis, yAxis, RegionType.getRegion(regionType.getSelectionIndex()), true);
		region.setPlotType(plottingSystem.getPlotType());
		this.editingRegion = region;
		getEditingRegion();
		
        return region;
	}
	
	private IROIListener roiListener;
	
	public void setEditingRegion(final IRegion region) {
		
        this.editingRegion = region;
        this.roiViewer.setRegion(region.getROI(), region.getRegionType(), region.getCoordinateSystem());
        this.roiListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				region.setROI(evt.getROI());
			}
		};
        roiViewer.addROIListener(roiListener);
       
        Range range = xyGraph.getPrimaryXAxis().getRange();
        roiViewer.setXLowerBound(Math.min(range.getUpper(), range.getLower()));
        roiViewer.setXUpperBound(Math.max(range.getUpper(), range.getLower()));
		
        range = xyGraph.getPrimaryYAxis().getRange();
        roiViewer.setYLowerBound(Math.min(range.getUpper(), range.getLower()));
        roiViewer.setYUpperBound(Math.max(range.getUpper(), range.getLower()));

        nameText.setText(region.getName());
		regionType.select(region.getRegionType().getIndex());
		regionType.setEnabled(false);
		regionType.setEditable(false);
		
		int index = getAxisIndex(xyGraph.getXAxisList(), region.getCoordinateSystem().getX());
		xCombo.select(index);
		
		index = getAxisIndex(xyGraph.getYAxisList(), region.getCoordinateSystem().getY());
		yCombo.select(index);
		
		colorSelector.setColorValue(region.getRegionColor().getRGB());
		alpha.setSelection(region.getAlpha());
		mobile.setSelection(region.isMobile());
		showPoints.setSelection(region.isShowPosition());
		visible.setSelection(region.isVisible());
		showLabel.setSelection(region.isShowLabel());
		fillRegion.setSelection(region.isFill());
		
	}
	
	public void dispose() {
		if (roiListener!=null) {
			try {
				roiViewer.removeROIListener(roiListener);
			} catch (Exception ne) {
				logger.error("Cannot remove roi listener", ne);
			}
		}
		super.dispose();
	}
	
	public IRegion getEditingRegion() {
		
		final String txt = nameText.getText();
		try {
			xyGraph.renameRegion(editingRegion, txt);
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Region Exists", 
					"The region '"+txt+"' already exists.\n\n"+
					"Please choose a unique name for regions.");
		}
		final IAxis x = getAxis(xyGraph.getXAxisList(), xCombo.getSelectionIndex());
		final IAxis y = getAxis(xyGraph.getYAxisList(), yCombo.getSelectionIndex());
		RegionCoordinateSystem sys = new RegionCoordinateSystem(getImageTrace(), x, y);
		editingRegion.setCoordinateSystem(sys);
		editingRegion.setShowPosition(showPoints.getSelection());
		editingRegion.setRegionColor(new Color(getDisplay(), colorSelector.getColorValue()));
		editingRegion.setAlpha(alpha.getSelection());
		editingRegion.setMobile(mobile.getSelection());
		editingRegion.setVisible(visible.getSelection());
		editingRegion.setShowLabel(showLabel.getSelection());
		editingRegion.setFill(fillRegion.getSelection());
		
		return editingRegion;
	}


	private IAxis getAxis(List<Axis> xAxisList, int selectionIndex) {
		if (selectionIndex<0) selectionIndex=0;
		Axis a = xAxisList.get(selectionIndex);
		return a instanceof IAxis ? (IAxis) a : null;
	}

	private int getAxisIndex(List<Axis> xAxisList, IAxis a) {
		if (a instanceof Axis) {
			return xAxisList.indexOf((Axis) a);
		}
		return -1;
	}

	public IImageTrace getImageTrace() {
		return ((RegionArea)xyGraph.getPlotArea()).getImageTrace();
	}

	public void applyChanges() {
//		this.roiViewer.cancelEditing();
		IRegion region = getEditingRegion();
		if (region.isVisible())
			region.repaint();
	}
	private static final Logger logger = LoggerFactory.getLogger(RegionEditComposite.class);
	public void cancelChanges() {
		try {
		    editingRegion.setROI(roiViewer.getOriginalRoi());
		} catch (Throwable ne) {
			logger.error("Problem reverting region  "+editingRegion.getName(), ne);
		}
	}


	public void disposeRegion(AbstractSelectionRegion<?> region) {
		 xyGraph.disposeRegion(region);
	}

	public boolean isImplicit() {
		return isImplicit;
	}

	public void setImplicit(boolean isImplicit) {
		this.isImplicit = isImplicit;
	}
}
