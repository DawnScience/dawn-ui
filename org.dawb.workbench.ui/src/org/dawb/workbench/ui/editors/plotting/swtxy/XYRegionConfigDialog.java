package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.toolbar.XYGraphConfigDialog;
import org.dawb.workbench.ui.editors.plotting.dialog.RegionComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class XYRegionConfigDialog extends XYGraphConfigDialog {

	private List<RegionComposite> regionList;
	private XYRegionGraph         regionGraph;

	public XYRegionConfigDialog(Shell parentShell, XYGraph xyGraph) {
		super(parentShell, xyGraph);
		regionList = new ArrayList<RegionComposite>();
		this.regionGraph = (XYRegionGraph)xyGraph;
	
		command = new XYRegionConfigCommand(xyGraph);
		command.savePreviousStates();

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
        final Composite parent_composite = (Composite)super.createDialogArea(parent);
        final TabFolder tabFolder = (TabFolder)parent_composite.getChildren()[0];  

        //Annotation Configure Page
        if ( ((RegionArea)regionGraph.getPlotArea()).getRegionList().size() > 0 ){
        	
        	Composite regionComposite = new Composite(tabFolder, SWT.NONE);
        	regionComposite.setLayout(new GridLayout(1, false));        	
        	TabItem regionConfigTab = new TabItem(tabFolder, SWT.NONE);
		    regionConfigTab.setText("Regions");
		    regionConfigTab.setToolTipText("Configure Selection Regions");
		    regionConfigTab.setControl(regionComposite);
		    
        	Group regionSelectGroup = new Group(regionComposite, SWT.NONE);
        	regionSelectGroup.setLayoutData(new GridData(
        			SWT.FILL, SWT.FILL,true, false));
        	regionSelectGroup.setText("Selection Region");
        	regionSelectGroup.setLayout(new GridLayout(1, false));    	        
        	final Combo regionCombo = new Combo(regionSelectGroup, SWT.DROP_DOWN);
        	regionCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
        	
        	
 	        for(Region region : ((RegionArea)regionGraph.getPlotArea()).getRegionList())
 	        	regionCombo.add(region.getName());
 	        regionCombo.select(0);
        	
 	        final Composite regionConfigComposite = new Composite(regionComposite, SWT.NONE);
 	        regionConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 	        final StackLayout stackLayout = new StackLayout();
 	        regionConfigComposite.setLayout(stackLayout);        
 	        
 	        for(Region region : ((RegionArea)regionGraph.getPlotArea()).getRegionList()){
		        
		        RegionComposite regionPage = new RegionComposite(regionConfigComposite, SWT.NONE, (XYRegionGraph)xyGraph, region.getRegionType());
		        regionList.add(regionPage);
		        regionPage.setEditingRegion(region);   	        
 	        }
 	        
 	        stackLayout.topControl = regionList.get(0);
 	        regionCombo.addSelectionListener(new SelectionAdapter(){
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			stackLayout.topControl = regionList.get(regionCombo.getSelectionIndex());
        			regionConfigComposite.layout(true, true);
        		}
        	}); 	       
        }
        
		return parent_composite;
	}
	
	protected void applyChanges(){	
 
		super.applyChanges();
		for (RegionComposite comp : regionList){
			comp.applyChanges();
		}
		
	}
}
