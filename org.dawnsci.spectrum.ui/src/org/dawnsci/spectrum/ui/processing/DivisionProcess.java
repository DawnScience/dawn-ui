package org.dawnsci.spectrum.ui.processing;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DivisionProcess extends AbstractProcess {

	IContain1DData oDenominator;
	IContain1DData denominator;
	
	public DivisionProcess(IContain1DData denominator) {
		this.oDenominator = denominator;
	}
	
	public List<IContain1DData> process(List<IContain1DData> list) {

		list.add(oDenominator);

		List<IContain1DData> listCom = SpectrumUtils.getCompatibleDatasets(list);

		this.denominator = listCom.remove(listCom.size()-1);

		return super.process(listCom);
	}
	
	@Override
	protected AbstractDataset process(AbstractDataset x, AbstractDataset y) {
		AbstractDataset y1 = DatasetUtils.convertToAbstractDataset(denominator.getyDatasets().get(0));
		AbstractDataset out = Maths.dividez(y, y1);
		return out;
	}

	
	@Override
	protected String getAppendingName() {
		return "_dividedBy_"+oDenominator.getName();
	}

}
