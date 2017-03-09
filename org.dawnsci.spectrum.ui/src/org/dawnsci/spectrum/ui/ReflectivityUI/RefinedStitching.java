package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.util.ArrayList;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;

public class RefinedStitching {

	public static Dataset[] curveStitch3 (ArrayList<ILazyDataset> xArrayList,
			 								 ArrayList<ILazyDataset> yArrayList,
			 								 ReflectivityUIModel model){ 

		IDataset[] xArray= new IDataset[xArrayList.size()];
		IDataset[] yArray= new IDataset[yArrayList.size()];
		
		for (int b = 0; b< xArrayList.size(); b++){
			
			SliceND sliceX =new SliceND(xArrayList.get(b).getShape());
			SliceND sliceY =new SliceND(yArrayList.get(b).getShape());
			
			try {
				xArray[b] = xArrayList.get(b).getSlice(sliceX);
				yArray[b] = yArrayList.get(b).getSlice(sliceY);
			} catch (DatasetException e) {
				e.printStackTrace();
			}
				
		}
		
		IDataset[] xArrayCorrected = xArray.clone();
		IDataset[] yArrayCorrected = yArray.clone();
		
		IDataset[][] attenuatedDatasets = new IDataset[2][];
		
//		int d = xArray.length;
//		
//		double[][] maxMinArray = new double[d][2];
//		
//		for(int k =0;k<d;k++){
//			maxMinArray[k][0] = (double) xArray[k].max(null);
//			maxMinArray[k][1] = (double) xArray[k].min(null);
//		}
//		
//		
		int d =  model.getROIList().size();
		
		double[][] maxMinArray = new double[d][2];
		
		for(int k =0;k<=d-1;k++){
			
			if( model.getROIListElement(k)!= null){
				
				IRectangularROI box = model.getROIListElement(k).getBounds();////////////////////////aaaaaarrrrrrggggghhhjhj
							
				maxMinArray[k][0] = box.getPointX()+ box.getLength(0);
				maxMinArray[k][1] = box.getPointX();
			}

		}
		
		
		
		
		double attenuationFactor =1;
		
		for (int k=0; k<xArray.length-1;k++){
		
		
			ArrayList<Integer> overlapLower = new ArrayList<Integer>();
			ArrayList<Integer> overlapHigher = new ArrayList<Integer>();
			
			
			for(int l=0; l<xArrayCorrected[k].getSize();l++){
				if (xArrayCorrected[k].getDouble(l)>=maxMinArray[k][1]){
					overlapLower.add(l);
				}
			}
			
			for(int m=0; m<xArrayCorrected[k+1].getSize();m++){
				if (xArrayCorrected[k+1].getDouble(m)<maxMinArray[k][0]){
					overlapHigher.add(m);
				}
			}
			
			Dataset[] xLowerDataset =new Dataset[1];
			Dataset yLowerDataset =null;
		
			Dataset[] xHigherDataset =new Dataset[1];
			Dataset yHigherDataset =null;
			
			
			ArrayList<Double> xLowerList =new ArrayList<>();
			ArrayList<Double> yLowerList =new ArrayList<>();
			
			ArrayList<Double> xHigherList =new ArrayList<>();
			ArrayList<Double> yHigherList =new ArrayList<>();
			
			
			if (overlapLower.size() > 0 && overlapHigher.size() > 0){
			
				for (int l=0; l<overlapLower.size(); l++){
					xLowerList.add(xArray[k].getDouble(overlapLower.get(l)));
					yLowerList.add(yArray[k].getDouble(overlapLower.get(l)));
				
					xLowerDataset[0] = DatasetFactory.createFromObject(xLowerList);
					yLowerDataset = DatasetFactory.createFromObject(yLowerList);
					
				}
				
				for (int l=0; l<overlapHigher.size(); l++){
					xHigherList.add(xArray[k+1].getDouble(overlapHigher.get(l)));
					yHigherList.add(yArray[k+1].getDouble(overlapHigher.get(l)));
					
					xHigherDataset[0] = DatasetFactory.createFromObject(xHigherList);
					yHigherDataset = DatasetFactory.createFromObject(yHigherList);
				}
				
				double correctionRatio = PolynomialOverlap.correctionRatio(xLowerDataset, yLowerDataset, 
				xHigherDataset, yHigherDataset, attenuationFactor);
				
				
				attenuationFactor = correctionRatio;
			
			}
		//////////////////need to deal with the lack of overlap here
		
		yArrayCorrected[k+1] = Maths.multiply(yArray[k+1],attenuationFactor);
		
		}
		
		attenuatedDatasets[0] = yArrayCorrected;
		attenuatedDatasets[1] = xArrayCorrected;
		
		Dataset[] sortedAttenuatedDatasets = new Dataset[2];
		
		sortedAttenuatedDatasets[0]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[0], 0)); ///yArray Intensity
		sortedAttenuatedDatasets[1]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[1], 0)); ///xArray
		
		DatasetUtils.sort(sortedAttenuatedDatasets[1],
		sortedAttenuatedDatasets[0]);
		
	
		return sortedAttenuatedDatasets;
		
	}
				
}
