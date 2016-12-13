package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.processing.AbstractProcess;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;

public class StitcherOutput extends AbstractProcess {

	public static List<IContain1DData> StitcherOutput1(ArrayList<IDataset> arrayILDy,
			ArrayList<IDataset> arrayILDx, OverlapUIModel model) {
		
		
		IDataset[][] output1= AttenuationCorrectedOutput.StitchingOverlapProcessMethod(arrayILDy, arrayILDx, model);
			

		IDataset[] xArrayCorrected = output1[1];
		IDataset[] yArrayCorrected = output1[0];
		
		
						
		
//////////////////////////////////////////////////////////////		
		
		
		Dataset xCorrected = DatasetUtils.concatenate(xArrayCorrected, 0);
		Dataset yCorrected = DatasetUtils.concatenate(yArrayCorrected, 0);
		
		xCorrected.setName("x");
		yCorrected.setName("y");
		
		List<IContain1DData> output = new ArrayList<IContain1DData>();
		output.add(new Contain1DDataImpl(xCorrected, Arrays.asList(new IDataset[]{yCorrected}), "test_stitched", "test_longname_stitched"));
		
		return output;
	}
	
	@Override
	protected Dataset process(Dataset x, Dataset y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getAppendingName() {
		// TODO Auto-generated method stub
		return null;
	}
}
//	protected static String getAppendingName() {
//		return "_stitched";
//	}
//
//}
