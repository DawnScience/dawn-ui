package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class StitchingOverlapProcessForUI{

	public static IDataset[] StitchingOverlapProcessMethod(ArrayList<ILazyDataset> arrayILDy,
			ArrayList<ILazyDataset> arrayILDx, ReflectivityUIModel model) {
			
			
		IDataset[][] output1= AttenuationCorrectedOutput.StitchingOverlapProcessMethod(arrayILDy, arrayILDx, model);
			

		IDataset[] xArrayCorrected = output1[1];
		IDataset[] yArrayCorrected = output1[0];
		
		IDataset[] xArrayCorrTrunc = new IDataset[xArrayCorrected.length];
		IDataset[] yArrayCorrTrunc = new IDataset[xArrayCorrected.length]; 
		
		
		double[][] maxMinArray = new double[xArrayCorrected.length][2];
				
				
		for (int k=0; k<(xArrayCorrected.length-1);k++){
				
			IRectangularROI box;
//			System.out.println("k:  "+ k);				
			box = model.getROIListElement(k).getBounds();
					
					
			maxMinArray[k][0] = box.getPointX()+ box.getLength(0);
			maxMinArray[k][1] = box.getPointX();
		}
		
		
		//new IDataset[1+2*list.size()];
	
		//int r= 0;
	
		for (int k=0; k<xArrayCorrected.length-1;k++){
			
			ArrayList<Integer> overlapLowerCorr = new ArrayList<Integer>();
			ArrayList<Integer> overlapHigherCorr = new ArrayList<Integer>();
			
			if (k==2){
//				System.out.println("k is two!!!!");
			}
			
			
					for(int l=0; l<xArrayCorrected[k].getSize();l++){
						if (xArrayCorrected[k].getDouble(l)>=maxMinArray[k][1]){
							overlapLowerCorr.add(l);
						}
					}
					for(int m=0; m<xArrayCorrected[k+1].getSize();m++){
						if (xArrayCorrected[k+1].getDouble(m)<maxMinArray[k][0]){
							overlapHigherCorr.add(m);
						}
					}
	
					IDataset xtemp = DatasetFactory.ones(xArrayCorrected[k].getSize()-overlapLowerCorr.size());
					IDataset ytemp = DatasetFactory.ones(xArrayCorrected[k].getSize()-overlapLowerCorr.size());
					
				
					
					int bottomOfLower = Collections.min(overlapLowerCorr);
					
					
					
				for(int c = 0; c < bottomOfLower;c++){
					
					xtemp.set(xArrayCorrected[k].getObject(c), c);
					ytemp.set(yArrayCorrected[k].getObject(c), c);
				}
				xArrayCorrTrunc[k] = xtemp;
				yArrayCorrTrunc[k] = ytemp;

		
			}
		

		xArrayCorrTrunc[xArrayCorrected.length-1] = xArrayCorrected[xArrayCorrected.length-1];
		yArrayCorrTrunc[yArrayCorrected.length-1] = yArrayCorrected[yArrayCorrected.length-1];
				
				
				
				
		
//////////////////////////////////////////////////////////////		
		
		Dataset xCorrectedTrunc = DatasetUtils.concatenate(xArrayCorrTrunc, 0);
		Dataset yCorrectedTrunc = DatasetUtils.concatenate(yArrayCorrTrunc, 0);
		
//		xCorrectedTrunc.setName("x");
//		yCorrectedTrunc.setName("y");
		
		IDataset[] output = new IDataset[2];
		
		
		
		output[0] = xCorrectedTrunc;
		output[1] = yCorrectedTrunc;
//		List<IContain1DData> output = new ArrayList<IContain1DData>();
//		output.add(new Contain1DDataImpl(xCorrectedTrunc, Arrays.asList(new IDataset[]{yCorrectedTrunc}), list.get(0).getName() + getAppendingName(), list.get(0).getLongName() + getAppendingName()));
		
		return output;
	}
	

}
