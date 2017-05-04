package org.dawnsci.surfacescatter.ui;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class RodAnalysisWindow {

	private SashForm analysisSash;
	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem3Composite ssps3c;
	private int numberOfImages;
	private Dataset nullImage;
	private TabFolder tabFolder;
	private IPlottingSystem<Composite> plotSystem;
	
	public RodAnalysisWindow(CTabFolder folder,
							SurfaceScatterPresenter ssp,
							SurfaceScatterViewStart ssvs){
		
		CTabItem analysis = new CTabItem(folder, SWT.NONE);
		analysis.setText("Analysis");
		analysis.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite analysisComposite = new Composite(folder, SWT.FILL);
		analysisComposite.setLayout(new GridLayout());
		analysisComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		analysis.setControl(analysisComposite);

		analysisSash = new SashForm(analysisComposite, SWT.FILL);
		analysisSash.setLayout(new GridLayout());
		analysisSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm anaLeft = new SashForm(analysisSash, SWT.FILL);
		anaLeft.setLayout(new GridLayout());
		anaLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));

		SashForm anaRight = new SashForm(analysisSash, SWT.FILL);
		anaRight.setLayout(new GridLayout());
		anaRight.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));

		analysisSash.setWeights(new int[] { 40, 60 });

		////////////////////// Analysis Left//////////////////////////////
		///////////////// anaLeft Window 3/////////////////////////////////

		@SuppressWarnings("deprecation")
		Dataset noImage = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
		
		customComposite = new PlotSystemCompositeView(anaLeft, 
													  SWT.FILL, 
													  noImage, 
													  1, 
													  numberOfImages, 
													  nullImage,
													  ssp, 
													  ssvs);

		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		//////////////////////// Analysis Right//////////////////////////////
		///////////////// anaRight Window 4/////////////////////////////////
		
		tabFolder = new TabFolder(anaRight, SWT.BORDER | SWT.CLOSE);
		tabFolder.setLayout(new GridLayout());
	    		
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        	
		TabItem ssps3cTab = new TabItem(tabFolder, SWT.NONE);
		ssps3cTab.setText("Slice Output");
		ssps3cTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite ssps3cTabComposite = new Composite(tabFolder, SWT.NONE | SWT.FILL);
		ssps3cTabComposite.setLayout(new GridLayout());
		ssps3cTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
		ssps3cTab.setControl(ssps3cTabComposite);
		
		try {
			ssps3c = new SuperSashPlotSystem3Composite(ssps3cTabComposite, 
													   SWT.FILL, 
													   ssvs, 
													   ssp);

			ssps3c.setLayout(new GridLayout());
			ssps3c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			ssps3c.setSsp(ssp);

		} catch (Exception d) {

		}
		
		TabItem reviewTab = new TabItem(tabFolder, SWT.NONE);
		reviewTab.setText("Stored Curves");
		reviewTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		Composite reviewTabComposite = new Composite(tabFolder, SWT.NONE | SWT.FILL);
		reviewTabComposite.setLayout(new GridLayout());
		reviewTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	       
		reviewTab.setControl(reviewTabComposite);
		
		Group storedCurves = new Group(reviewTabComposite, SWT.NONE);
        GridLayout storedCurvesLayout = new GridLayout();
        storedCurves.setLayout(storedCurvesLayout);
        
        final GridData storedCurvesData = new GridData(SWT.FILL, SWT.FILL, true, true);
        storedCurvesData.grabExcessVerticalSpace = true;
        storedCurvesData.heightHint = 100;
        storedCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
	       
		try {
			plotSystem = PlottingFactory.createPlottingSystem();
				
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	        
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(storedCurves, 
																				null);
		  
	    plotSystem.createPlotPart(storedCurves, 
	        					  "Stored Curves", 
	        					  actionBarComposite, 
	        					  PlotType.IMAGE, 
	        					  null);
	        
//	    ILineTrace lt = plotSystem.createLineTrace("Blank Curve");
//		IDataset backup = DatasetFactory.createRange(0, 200, 1, Dataset.FLOAT64);
//		lt.setData(backup, backup);		
//
//		plotSystem.addTrace(lt);
	    
	    plotSystem.getPlotComposite().setLayoutData(storedCurvesData);
	    		
//	    		new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
	       
//	    plotSystem.createPlot1D(null, null, null, null);    
	    
	    tabFolder.getTabList()[1].setEnabled(false);
		
	}
	
	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	public SuperSashPlotSystem3Composite getSsps3c(){
		return ssps3c;
	}
	
	public PlotSystemCompositeView getCustomComposite(){
		return customComposite;
	}
		
	private void setNumberOfImages(int n){
		this.numberOfImages = n;
	}
}
