package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class SaveTextProcess extends AbstractSaveProcess {

	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {

		List<IDataset> datasets = new ArrayList<IDataset>();
		List<String> headings = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		
		sb.append("#Data saved from trace perspective\r\n");

		for (IContain1DData i1d : list) {
			sb.append("# " + i1d.getLongName()+"\r\n");
			if (i1d.getxDataset()!= null) {
				IDataset data = i1d.getxDataset().clone();
				data.setShape(data.getShape()[0],1);
				datasets.add(data);
				headings.add(data.getName());
			}

			for (IDataset y : i1d.getyDatasets()) {
				IDataset data = y.clone();
				data.setShape(data.getShape()[0],1);
				datasets.add(data);
				headings.add(data.getName());
			}
		}

		Dataset allTraces = DatasetUtils.concatenate(datasets.toArray(new IDataset[datasets.size()]), 1);
		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(path);
		DataHolder dh = new DataHolder();
		dh.addDataset("AllTraces", allTraces);
		saver.setHeader(sb.toString());
		saver.setHeadings(headings);
		try {
			saver.saveFile(dh);
		} catch (ScanFileHolderException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return null;
	}

	@Override
	protected String getAppendingName() {
		//Should never be called
		return "_savetext";
	}

	@Override
	public String getDefaultName() {
		// TODO Auto-generated method stub
		return "traceprocessed.dat";
	}

}
