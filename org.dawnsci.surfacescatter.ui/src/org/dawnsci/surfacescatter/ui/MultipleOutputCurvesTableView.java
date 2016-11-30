package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class MultipleOutputCurvesTableView extends Composite {

	private IPlottingSystem<Composite> plotSystem4;
	private IRegion imageNo;
	private ILineTrace lt;
	private ExampleModel model;
	private DataModel dm;
//	private ArrayList<TableItem> datSelector;
	private Button sc;
	private Button save;
	private Button intensitySelect;
	private Group overlapSelection;
	private Button errors;
	private Button overlapZoom;
	private int extra = 0;
	private IRegion marker;
	
	public MultipleOutputCurvesTableView (Composite parent, 
										  int style, 
										  ArrayList<ExampleModel> models, 
										  ArrayList<DataModel> dms,
										  SuperModel sm,
										  int extra) {

		super(parent, style);

		new Label(this, SWT.NONE).setText("Output Curves");

		this.model = models.get(sm.getSelection());
		this.dm = dms.get(sm.getSelection());
		this.extra = extra;
	
		
		try {
			plotSystem4 = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		this.createContents(model, sm, dm);

	}

	public void createContents(ExampleModel model, SuperModel sm, DataModel dm) {
		
		SashForm sashForm= new SashForm(this, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		overlapSelection = new Group(sashForm, SWT.NULL);
		GridLayout overlapSelectionLayout = new GridLayout(3, true);
		GridData overlapSelectionData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		overlapSelectionData.minimumWidth = 50;
		overlapSelection.setLayout(overlapSelectionLayout);
		overlapSelection.setLayoutData(overlapSelectionData);
		overlapSelection.setText("Controls");
		
		errors = new Button(overlapSelection, SWT.PUSH);
		errors.setText("Errors");
		
		intensitySelect = new Button(overlapSelection, SWT.PUSH);
		intensitySelect.setText("Intensity?");
		
		save = new Button(overlapSelection, SWT.PUSH);
		save.setText("Save Spliced");
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		setLayout(gridLayout);
		
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(sashForm, null);
		
		final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_secondField.grabExcessVerticalSpace = true;
		gd_secondField.grabExcessVerticalSpace = true;
		gd_secondField.heightHint = 100;

		plotSystem4.createPlotPart(sashForm, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
		
		String[] t = CurveStateIdentifier.CurveStateIdentifier1(plotSystem4);
		
		if(t[0] == "f"){
			lt = plotSystem4.createLineTrace(dm.getName() + "_Fhkl");
		}
		else{
			lt = plotSystem4.createLineTrace(dm.getName() + "_Intensity");
		}
		
		if (dm.getyList() == null || dm.getxList() == null) {
			lt.setData(dm.backupDataset(), dm.backupDataset());
		} else {
			if (t[0] == "f"){
				lt.setData(dm.yIDatasetFhkl(), dm.xIDataset());
			}
			else{
				lt.setData(dm.yIDataset(), dm.xIDataset());
			}
		}

		plotSystem4.addTrace(lt);
		try {
			imageNo = plotSystem4.createRegion("Image", RegionType.XAXIS_LINE);
			imageNo.setShowPosition(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		plotSystem4.getPlotComposite().setLayoutData(gd_secondField);

		overlapZoom = new Button(sashForm, SWT.PUSH);
		overlapZoom.setText("Overlap Zoom");
		

		
		sashForm.setWeights(new int[]{10,5,80,5});
				
		
		
		
	}

	public Composite getComposite() {

		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem4;
	}

	public IRegion getRegionNo() {
		return imageNo;
	}

	public void resetCurve() {
	

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				plotSystem4.clear();
				lt = plotSystem4.createLineTrace("Output Curve");
				lt.setData(dm.backupDataset(), dm.backupDataset());
				plotSystem4.addTrace(lt);
				plotSystem4.repaint();
			}
		});

	}

	public void updateCurve(DataModel dm1, Boolean intensity, SuperModel sm) {

		if (lt.getDataName() == null) {
			lt = plotSystem4.createLineTrace("Output Curve");
		}

		if (dm1.getyList() == null || dm1.getxList() == null) {
			lt.setData(dm1.backupDataset(), dm1.backupDataset());
		} else if (intensity == true) {
			lt.setData(dm1.xIDataset(), dm1.yIDataset());
			lt.setName(dm1.getName()+ "_Intensity");
		
		}else{
			lt.setData(dm1.xIDataset(), dm1.yIDatasetFhkl());
			lt.setName(dm1.getName()+ "_Fhkl");
		}
		
		plotSystem4.clear();
		plotSystem4.addTrace(lt);
		plotSystem4.repaint();

	}
		
	public void addToDatSelector(){
		if(this.getSc() == null){
			sc = new Button(overlapSelection, SWT.CHECK);
			sc.setText("Spliced Curve");
			sc.setSelection(true);
			overlapSelection.layout(true, true);
			overlapSelection.setSize(overlapSelection.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			this.redraw();
			this.update();
		}
	}
	
	public Button getSc(){
		return sc;
	}
	
	public Group getOverlapSelectionGroup(){
		return overlapSelection;
	}
	
	public Button getIntensity(){
		return intensitySelect;
	}
	
	public Button getOverlapZoom(){
		return overlapZoom;
	}
	
	public Button getSave(){
		return save;
	}
		
	public Button getErrors(){
		return errors;
	}
	
	public IRegion getMarker(){
		return marker;
	}
}
