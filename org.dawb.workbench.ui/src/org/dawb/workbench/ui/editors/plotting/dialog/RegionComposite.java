package org.dawb.workbench.ui.editors.plotting.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.swt.xygraph.figures.Trace;
import org.dawb.workbench.ui.editors.plotting.swtxy.BoxSelection;
import org.dawb.workbench.ui.editors.plotting.swtxy.LineSelection;
import org.dawb.workbench.ui.editors.plotting.swtxy.Region;
import org.dawb.workbench.ui.editors.plotting.swtxy.XYRegionGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RegionComposite extends Composite {

	private XYRegionGraph xyGraph;

	private Text   nameText;
	private CCombo regionType;
	private CCombo traceCombo;
	private Button showPoints;

	public RegionComposite(final Composite parent, final int style, final XYRegionGraph xyGraph, final int defaultSelection) {
		
		super(parent, SWT.NONE);
		this.xyGraph = xyGraph;
		
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));

		final Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name   ");
		nameLabel.setLayoutData(new GridData());

		nameText = new Text(this, SWT.BORDER | SWT.SINGLE);
		nameText.setToolTipText("Region name");
		nameText.setLayoutData(new GridData(SWT.FILL, 0, true, false));

		final Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setText("Type");
		typeLabel.setLayoutData(new GridData());

		regionType = new CCombo(this, SWT.NONE);
		regionType.setEditable(false);
		regionType.setToolTipText("Region type");
		regionType.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		regionType.setItems(new String[]{"Line", "Box"});
		regionType.select(defaultSelection);

		final Label traceLabel = new Label(this, SWT.NONE);
		traceLabel.setText("Trace");
		traceLabel.setLayoutData(new GridData());

		traceCombo = new CCombo(this, SWT.NONE);
		traceCombo.setEditable(false);
		traceCombo.setToolTipText("Existing plot on the graph ");
		traceCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));

		for(Trace trace : xyGraph.getPlotArea().getTraceList()) traceCombo.add(trace.getName());
		traceCombo.select(0);
		
		this.showPoints = new Button(this, SWT.CHECK);
		showPoints.setText("Show vertex values");		
		showPoints.setToolTipText("When on this will show the actual value of the point in the axes it was added.");
		showPoints.setLayoutData(new GridData(0, 0, false, false, 2, 1));

		nameText.setText(getDefaultName(defaultSelection));

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


	public Region createRegion() {
		
		final Trace trace = xyGraph.getPlotArea().getTraceList().get(traceCombo.getSelectionIndex());
		Region region=null;
		
		final String txt = nameText.getText();
		final Pattern pattern = Pattern.compile(".* (\\d+)");
		final Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			final int count = Integer.parseInt(matcher.group(1));
			countMap.put(regionType.getSelectionIndex(), count);
		}
		
		if (regionType.getSelectionIndex()==0) region = new LineSelection(txt, trace);
		if (regionType.getSelectionIndex()==1) region = new BoxSelection(txt, trace);
		
		region.setShowPosition(showPoints.getSelection());
		
        return region;
	}
	
	
	public void editRegion(Region region) {
        throw new RuntimeException("Please implement editRegion!");
	}
}
