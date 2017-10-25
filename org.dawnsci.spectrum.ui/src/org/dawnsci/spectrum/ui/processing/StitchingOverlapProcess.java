package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

public class StitchingOverlapProcess extends AbstractProcess {

	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {
		
		Iterator<IContain1DData> itr = list.listIterator();
		
		IDataset[] xArray = new IDataset[list.size()];
		IDataset[] yArray = new IDataset[list.size()];
		
		IDataset[] xArrayCorrected = new IDataset[list.size()];
		IDataset[] yArrayCorrected = new IDataset[list.size()];

		IDataset[] xArrayCorrectedTrunc = new IDataset[1+2*list.size()];
		IDataset[] yArrayCorrectedTrunc = new IDataset[1+2*list.size()];
		
		
		int k=0;
		
		for(k =0;k<list.size();k++){
			xArray[k] =list.get(k).getxDataset();
			yArray[k] =list.get(k).getyDatasets().get(0);

			xArrayCorrected[k] =list.get(k).getxDataset();
			yArrayCorrected[k] =list.get(k).getyDatasets().get(0);
		}
		
		double[][] maxMinArray = new double[list.size()][2];
		
		
		for (k=0; k<list.size();k++){
			
			maxMinArray[k][0] = (double) xArray[k].max(null);
			maxMinArray[k][1] = (double) xArray[k].min(null);
			
		}
		
		
		
		double attenuationFactor =1;
		
		double[] correctionRatioArray = new double[list.size()];
		correctionRatioArray[0]=1;
		
		
		for (k=0; k<list.size()-1;k++){
			
			ArrayList<Integer> overlapLower = new ArrayList<Integer>();
			ArrayList<Integer> overlapHigher = new ArrayList<Integer>();
			
			
			//if (xArray[k+1] != null){
				if (maxMinArray[k][0]>maxMinArray[k+1][1]){
					for(int l=0; l<xArray[k].getSize();l++){
						if (xArray[k].getDouble(l)>maxMinArray[k+1][1]){
							overlapLower.add(l);
						}
					}
					for(int m=0; m<xArray[k+1].getSize();m++){
						if (xArray[k+1].getDouble(m)<maxMinArray[k][0]){
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
					
//					int n=0;
					
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
					
					
					
//					Polynomial polyFitLower = Fitter.polyFit(xLowerDataset, yLowerDataset, 1e-5,4);
//					Polynomial polyFitHigher = Fitter.polyFit(xHigherDataset, yHigherDataset, 1e-5,4);
					
//					Dataset calculatedValuesHigher = polyFitHigher.calculateValues(xLowerDataset);
//					Dataset calculatedValuesLower = polyFitLower.calculateValues(xLowerDataset);
					
					Dataset correctionsRatioDataset = Maths.divide(yLowerDataset.sum(), 
							yHigherDataset.sum());
					
					
					double correctionRatio = ((Number) correctionsRatioDataset.sum()).doubleValue()/((double) correctionsRatioDataset.getSize())*attenuationFactor;
					
					attenuationFactor = correctionRatio;
					
					yArrayCorrected[k+1] = Maths.multiply(yArray[k+1],attenuationFactor);
					
					
				//	}
				}
			}
					
			
//		Dataset x = DatasetUtils.concatenate(xArray, 0);
//		Dataset y = DatasetUtils.concatenate(yArray, 0);
////////////////////////////////////////////////////		


		
		double[][] maxMinArrayCorrected = new double[list.size()][2];
		
		
		for (k=0; k<list.size();k++){
			
			maxMinArrayCorrected[k][0] = (double) xArrayCorrected[k].max(null);
			maxMinArrayCorrected[k][1] = (double) xArrayCorrected[k].min(null);
			
		}
		
		

		
		IDataset[] xArrayCorrTrunc = new IDataset[list.size()];
		IDataset[] yArrayCorrTrunc = new IDataset[list.size()]; 
		
		//new IDataset[1+2*list.size()];
		
		//int r= 0;
	
		
		for (k=0; k<list.size()-1;k++){
			
			ArrayList<Integer> overlapLowerCorr = new ArrayList<Integer>();
			ArrayList<Integer> overlapHigherCorr = new ArrayList<Integer>();
			
				if (maxMinArrayCorrected[k][0]>maxMinArrayCorrected[k+1][1]){
					for(int l=0; l<xArrayCorrected[k].getSize();l++){
						if (xArrayCorrected[k].getDouble(l)>=maxMinArrayCorrected[k+1][1]){
							overlapLowerCorr.add(l);
						}
					}
					for(int m=0; m<xArrayCorrected[k+1].getSize();m++){
						if (xArrayCorrected[k+1].getDouble(m)<maxMinArrayCorrected[k][0]){
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
		}

		xArrayCorrTrunc[list.size()-1] = xArrayCorrected[list.size()-1];
		yArrayCorrTrunc[list.size()-1] = yArrayCorrected[list.size()-1];
				
				
				
				
		
//////////////////////////////////////////////////////////////		
		
		
		Dataset xCorrectedTrunc = DatasetUtils.concatenate(xArrayCorrTrunc, 0);
		Dataset yCorrectedTrunc = DatasetUtils.concatenate(yArrayCorrTrunc, 0);
		
		xCorrectedTrunc.setName("x");
		yCorrectedTrunc.setName("y");
		
		List<IContain1DData> output = new ArrayList<IContain1DData>();
		output.add(new Contain1DDataImpl(xCorrectedTrunc, Arrays.asList(new IDataset[]{yCorrectedTrunc}), list.get(0).getName() + getAppendingName(), list.get(0).getLongName() + getAppendingName()));
		
		return output;
	}
	
	@Override
	protected Dataset process(Dataset x, Dataset y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getAppendingName() {
		return "_stitched";
	}

}
