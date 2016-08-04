package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public class StitchingOverlapProcess extends AbstractProcess {

	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {
		
		Iterator<IContain1DData> itr = list.listIterator();
		
		IDataset[] xArray = new IDataset[list.size()];
		IDataset[] yArray = new IDataset[list.size()];
		
		int k =0;
		
		while (itr.hasNext()){
			xArray[k] =list.get(k).getxDataset();
			yArray[k] =list.get(k).getyDatasets().get(0);
			k++;
		}
		
		double[][] maxMinArray = new double[list.size()][];
		
		for (k=0; k<list.size();k++){
			
			maxMinArray[k][0] = (double) xArray[k].max(true, true);
			maxMinArray[k][1] = (double) xArray[k].min(true, true);
			
		}
		
		
		
		ArrayList<Double> overlapLower = new ArrayList<Double>();
		ArrayList<Double> overlapHigher = new ArrayList<Double>();
		
		for (k=0; k<list.size();k++){
			
			if (xArray[k+1] != null){
				if (maxMinArray[k][0]<maxMinArray[k+1][1]){
					for(int l=0; l<xArray[k].getSize();l++){
						if (xArray[k].getDouble(l)<maxMinArray[k+1][1]){
							overlapLower.add((double) l);
						}
					for(int l=0; l<xArray[k+1].getSize();l++){
						if (xArray[k+1].getDouble(l)<maxMinArray[k+1][0]){
							overlapHigher.add((double) l);
						}
					}	
					
					
				}
		
				
		Dataset x = DatasetUtils.concatenate(xArray, 0);
		Dataset y = DatasetUtils.concatenate(yArray, 0);
		
		
		
		
		List<IContain1DData> output = new ArrayList<IContain1DData>();
		output.add(new Contain1DDataImpl(x, Arrays.asList(new IDataset[]{y}), list.get(0).getName() + getAppendingName(), list.get(0).getLongName() + getAppendingName()));
		
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
