package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.util.ArrayList;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ReflectivityDialog extends Dialog {
	
	private String[] filepaths;
	private String title;
	
	public ReflectivityDialog(Shell parentShell, String[] datFilenames) {
		super(parentShell);
		this.filepaths = datFilenames;

	}

	@Override
	  protected Control createDialogArea(Composite parent) {
	    final Composite container = (Composite) super.createDialogArea(parent);
	    GridLayout gridLayout = new GridLayout(3, false);
	    container.setLayout(gridLayout);			

		ArrayList<ILazyDataset> arrayILDy = new ArrayList<ILazyDataset>();
		ArrayList<ILazyDataset> arrayILDx = new ArrayList<ILazyDataset>();
		
		
		for (String fpath : filepaths){
			try {
				IDataHolder dh1 =LoaderFactory.getData(fpath);
				ILazyDataset ild =dh1.getLazyDataset("/entry/auxiliary/3-Final extraction for reflectivity/data"); 
				ILazyDataset ild1 =dh1.getLazyDataset("/entry/auxiliary/3-Final extraction for reflectivity/qsdcd"); 
				
				//DatasetUtils.
				arrayILDy.add(ild);
				arrayILDx.add(ild1);
				
				//{
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
//		auxiliary/3-Final extraction for reflectivity/data
		
		
//		ILazyDataset[] shouldntneedthisarray = new ILazyDataset[arrayILD.size()];
//		
//		Iterator<ILazyDataset> itr =arrayILD.iterator();
//		int i=0;
//		while (itr.hasNext()){
//			shouldntneedthisarray[i] = itr.next();
//			i++;
//		}
//		
//		final AggregateDataset aggDat = new AggregateDataset(false, shouldntneedthisarray);
//		
		ReflectivityUIModel model = new ReflectivityUIModel();
		
		
		
		//model.setData(aggDat);
		
		

	    title = filepaths[0];
//		try {
//			DropDownMenuTestComposite dropDown = new DropDownMenuTestComposite(container, SWT.NONE);
//			dropDown.setLayout(new GridLayout(2,true));
//			dropDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//			test0 = dropDown.getFitDirection();
//			test1 = dropDown.getFitPower();
//		    
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	    ReflectivityCurves customComposite = new ReflectivityCurves(container, SWT.NONE, arrayILDy,arrayILDx,filepaths,title, model);
	    customComposite.setLayout(new GridLayout());
	    customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//	    
//	    PlotSystem1Composite customComposite1 = new PlotSystem1Composite(container, 
//	    		SWT.NONE, aggDat, test0, test1, model);
//	    customComposite1.setLayout(new GridLayout());
//	    customComposite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//	    		
//	    try {
//			PaddingClass padField = new PaddingClass(container, SWT.NONE);
//			padField.setLayout(new GridLayout());
//			padField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    
//	    
//	    
//	    try {
//			PlotSystem2Composite customComposite2 = new PlotSystem2Composite(container, SWT.NONE, 
//					aggDat, model);
//		    customComposite2.setLayout(new GridLayout());
//		    customComposite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		    PlotSystem3Composite customComposite3 = new PlotSystem3Composite(container, SWT.NONE, 
//		    		aggDat, model);
//		    customComposite3.setLayout(new GridLayout());
//		    customComposite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		    
//	    } catch (Exception e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		
		return container;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ReflectivityDialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
}

