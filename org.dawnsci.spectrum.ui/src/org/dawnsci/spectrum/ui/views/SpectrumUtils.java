package org.dawnsci.spectrum.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.eclipse.jface.viewers.IStructuredSelection;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

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
	
	public static ISpectrumFile averageSpectrumFiles(List<ISpectrumFile> files, IPlottingSystem system) {
		ISpectrumFile output = null;
		
		IDataset x0 = files.get(0).getxDataset();
		
		for (ISpectrumFile file : files) {
			if (!x0.equals(file.getxDataset())) return output;
		}
		
		Map<String, MultivariateSummaryStatistics> mapmss = new HashMap<String, MultivariateSummaryStatistics>();
		
		for (String name : files.get(0).getyDatasetNames()) {
			mapmss.put(name, new MultivariateSummaryStatistics(x0.getSize(),false));
		}
		
		
		for (ISpectrumFile file : files) {
			for (String name : file.getyDatasetNames()) {
				MultivariateSummaryStatistics ms = mapmss.get(name);
				IDataset ds = file.getDataset(name);
				DoubleDataset dd;
				if (ds instanceof DoubleDataset) dd = (DoubleDataset)ds;
				else {
					dd = (DoubleDataset)DatasetUtils.cast((AbstractDataset)ds, AbstractDataset.FLOAT64);
				}
				double[] raw = dd.getData();
				ms.addValue(raw);
			}
		}
		
		List<IDataset> sets = new ArrayList<IDataset>();
		
		for (String key : mapmss.keySet()) {
			MultivariateSummaryStatistics ms = mapmss.get(key);
			
			DoubleDataset dd = new DoubleDataset(ms.getMean(), x0.getShape());
			dd.setName(key);
			sets.add(dd);
		}

		return new SpectrumInMemory("Average "+ sets.hashCode(), x0, sets, system);
	}
	
	public static ISpectrumFile subtractSpectrumFiles(List<ISpectrumFile> files, IPlottingSystem system) {
		
		if (files.size() != 2) return null;
		
		IDataset x0 = files.get(0).getxDataset();
		IDataset x1 = files.get(1).getxDataset();
		
		if (!x0.equals(x1)) return null;
		
		List<IDataset> sets = new ArrayList<IDataset>();
		
		for (String name : files.get(0).getyDatasetNames()) {
			IDataset ds0 = files.get(0).getDataset(name);
			IDataset ds1 = files.get(1).getDataset(name);
			IDataset dif = Maths.subtract(ds0,ds1);
			
			dif.setName(name);
			sets.add(dif);
		}
		
		return new SpectrumInMemory("Difference"+ sets.hashCode(), x0, sets, system);
		
	}
	
	
}
