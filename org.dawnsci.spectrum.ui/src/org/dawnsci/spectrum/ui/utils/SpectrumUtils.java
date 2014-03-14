package org.dawnsci.spectrum.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.eclipse.jface.viewers.IStructuredSelection;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;

public class SpectrumUtils {

	public static List<ISpectrumFile> getSpectrumFilesList(IStructuredSelection selection) {
		
		List<ISpectrumFile> list = new ArrayList<ISpectrumFile>(selection.size());
		
		Iterator<?> iterator = selection.iterator();
		
		while (iterator.hasNext()) {
			Object ob = iterator.next();
			
			if (ob instanceof ISpectrumFile) list.add((ISpectrumFile)ob);
		}
 		
		return list;
	}
	
	public static List<IContain1DData> get1DDataList(IStructuredSelection selection) {

		List<IContain1DData> list = new ArrayList<IContain1DData>(selection.size());

		Iterator<?> iterator = selection.iterator();

		while (iterator.hasNext()) {
			Object ob = iterator.next();

			if (ob instanceof ISpectrumFile) list.add((ISpectrumFile)ob);
		}

		return list;
	}
	
	public static ISpectrumFile averageSpectrumFiles(List<IContain1DData> files, IPlottingSystem system) {
		files = getCompatibleDatasets(files);
		
		if (files == null) return null;
		
		IDataset x0 = files.get(0).getxDataset();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Average of: ");
		sb.append("\n");
		MultivariateSummaryStatistics ms = new MultivariateSummaryStatistics(x0.getSize(),false);
		for (IContain1DData file : files) {
			
			sb.append(file.getName() +":");
			
			for (IDataset ds : file.getyDatasets()) {
				
				sb.append(ds.getName() +":");
				
				DoubleDataset dd;
				if (ds instanceof DoubleDataset) dd = (DoubleDataset)ds;
				else {
					dd = (DoubleDataset)DatasetUtils.cast((AbstractDataset)ds, AbstractDataset.FLOAT64);
				}
				double[] raw = dd.getData();
				ms.addValue(raw);
			}
			
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		List<IDataset> sets = new ArrayList<IDataset>();
		DoubleDataset dd = new DoubleDataset(ms.getMean(), ms.getDimension());
		
		dd.setName("Average");
		sets.add(dd);
		
		String shortName = "Average: " + files.get(0).getName() + " to " + files.get(files.size()-1).getName();
		
		return new SpectrumInMemory(sb.toString()+"["+ sets.hashCode()+"]", shortName, x0, sets, system);
	}
	
	private static int[] checkXaxisHasCommonRange(IDataset[] xaxis) {
		return null;
	}

	private static double[] checkXaxisHasCommonRangeForInterpolation(IDataset[] xaxis) {
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;
		
		for (IDataset x : xaxis) {
			if (x.min().doubleValue() > min) min = x.min().doubleValue();
			if (x.max().doubleValue() < max) max = x.max().doubleValue();
		}
		
		if (min > max) return null;
		
		return new double[] {min, max};
	}
	
	public static ISpectrumFile[] subtractSpectrumFiles(List<IContain1DData> files, IPlottingSystem system) {
		
		//TODO deal with single files
		
		if (files.size() != 2) return null;
		
		if (files.get(0).getyDatasets().isEmpty() || files.get(1).getyDatasets().isEmpty()) return null;
		
		files = getCompatibleDatasets(files);
		if (files == null) return null;
		
		IDataset x0 = files.get(0).getxDataset();
		IDataset x1 = files.get(1).getxDataset();
		
		if (!x0.equals(x1)) return null;
		
		List<IDataset> sets1 = new ArrayList<IDataset>();
		List<IDataset> sets2 = new ArrayList<IDataset>();
		
		IDataset ds0 = files.get(0).getyDatasets().get(0);
		IDataset ds1 = files.get(1).getyDatasets().get(0);
		IDataset dif = Maths.subtract(ds0,ds1);
		IDataset dif1 = Maths.subtract(ds1,ds0);

		dif.setName("Difference");
		dif1.setName("Difference");
		sets1.add(dif);
		sets2.add(dif1);
		
		SpectrumInMemory[] out = new SpectrumInMemory[2];
		
		String first = "Difference: " + files.get(0).getName() +"-" + files.get(1).getName();
		String second = "Difference: " + files.get(1).getName() +"-" + files.get(0).getName();
		
		String firstLong = "Difference: " + files.get(0).getName()+ ":" +files.get(0).getyDatasets().get(0).getName()
				+"\n-" + files.get(1).getName()+ ":" +files.get(1).getyDatasets().get(0).getName() + "(" + sets1.hashCode() + ")";
		String secondLong = "Difference: " + files.get(1).getName()+ ":" +files.get(1).getyDatasets().get(0).getName()
				+"\n-" + files.get(0).getName()+ ":" +files.get(0).getyDatasets().get(0).getName() + "(" + sets1.hashCode() + ")";
		
		
		out[0] = new SpectrumInMemory(firstLong,first, x0, sets1, system);
		out[1] = new SpectrumInMemory(secondLong,second, x0, sets2, system);
		
		return out;
		
	}
	
	public static List<IContain1DData> getCompatibleDatasets(List<IContain1DData> data){
		
		IDataset[] xall = new IDataset[data.size()];
		
		for (int i = 0; i < data.size(); i++) {
			xall[i] = data.get(i).getxDataset();
		}
		
		boolean dataAndNull = false;
		boolean needsChecks = false;
		
		IDataset test = xall[0];
		
		for (int i = 1; i < xall.length; i++) {
			if (test == null && xall[i] != null) dataAndNull = true;
			if (test != null && xall[i] == null) dataAndNull = true;
			
			if (test != null && !test.equals(xall[i])) needsChecks = true;
		}
		
		//xdata and no xdata not supported
		if (dataAndNull) return null;
		
		if (test == null) {
			//TODO make sure yDatasets != null
			int size = data.get(0).getyDatasets().get(0).getSize();
			
			for (IContain1DData d : data) {
				for (IDataset set : d.getyDatasets()) {
					if (set.getSize() != size) return null;
				}
			}
			
			return data;
		}
		
		if (!needsChecks) {
			return data;
		}
		
		int[] commonRange = checkXaxisHasCommonRange(xall);
		
		if (commonRange != null) { 
			//TODO slice to common range
		}
		
		double[] commonValues = checkXaxisHasCommonRangeForInterpolation(xall);
		
		if (commonValues == null) return null;
		
		List<IContain1DData> output = new ArrayList<IContain1DData>();
		
		int maxpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[1])-1;
		int minpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[0])+1;
		
		IDataset xnew =  test.getSlice(new int[] {minpos},new int[]{maxpos},null);
		xnew.setName(test.getName());
		
		List<IDataset> ynew = new ArrayList<IDataset>();
		
		for (IDataset y : data.get(0).getyDatasets()) {
			ynew.add(y.getSlice(new int[] {minpos},new int[]{maxpos},null));
		}

		output.add(new Contain1DDataImpl(xnew,ynew,data.get(0).getName(),data.get(0).getLongName()));
		
		for (int i = 1; i < data.size(); i++) {
			
			IDataset x = data.get(i).getxDataset();
			
			ynew = new ArrayList<IDataset>();
			
			for (IDataset y: data.get(i).getyDatasets()) {
				ynew.add(PolynomialInterpolator1D.interpolate(x, y, xnew));
			}
			
			output.add(new Contain1DDataImpl(xnew, ynew,data.get(i).getName(),data.get(i).getLongName()));
		}
		
		return output;
	}
	
	
}
