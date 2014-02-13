package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class AverageProcess extends AbstractProcess {

	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {
		
		list = SpectrumUtils.getCompatibleDatasets(list);
		
		if (list == null) return null;
		
		IDataset x0 = list.get(0).getxDataset();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Average of: ");
		sb.append("\n");
		MultivariateSummaryStatistics ms = new MultivariateSummaryStatistics(x0.getSize(),false);
		for (IContain1DData file : list) {
			
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
		
		String shortName = "Average: " + list.get(0).getName() + " to " + list.get(list.size()-1).getName();
		
		return  Arrays.asList(new IContain1DData[] {new Contain1DDataImpl(x0, sets, shortName, sb.toString()+"["+ sets.hashCode()+"]")});
		
	}
	
	@Override
	protected AbstractDataset process(AbstractDataset x, AbstractDataset y) {
		return null;
	}

	@Override
	protected String getAppendingName() {
		//Should not be called
		return "_average";
	}

}
