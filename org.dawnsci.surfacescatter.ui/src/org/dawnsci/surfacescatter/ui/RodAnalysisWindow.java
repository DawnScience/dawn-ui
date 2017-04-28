package org.dawnsci.surfacescatter.ui;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class RodAnalysisWindow {

	private SashForm analysisSash;
	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem3Composite ssps3c;
	private int numberOfImages;
	private Dataset nullImage;
	
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
		try {
			ssps3c = new SuperSashPlotSystem3Composite(anaRight, 
													   SWT.FILL, 
													   ssvs, 
													   ssp);

			ssps3c.setLayout(new GridLayout());
			ssps3c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			ssps3c.setSsp(ssp);

		} catch (Exception d) {

		}
		
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
