package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertEquals;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsSlice;
import org.dawnsci.datavis.model.PlotController;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataOptionsViewTest extends AbstractTestModel {

private static DataOptions dataOptions;
	
	@BeforeClass
	public static void buildData() throws Exception {
			AbstractTestModel.buildData();
			dataOptions = loadedFile.getDataOption("/entry/dataset3a");
	}
	
	@Test
	public void testDataOptionsView() throws DatasetException {
		PlotController p = new PlotController();
		p.initialise(loadedFile);
		
		DataOptionsSlice view = new DataOptionsSlice(dataOptions, dataOptions
				.getPlottableObject()
				.getNDimensions()
				.buildSliceND());
		
		IDataset slice = view.getLazyDataset().getSlice();
		
		assertEquals(1,slice.getShape()[0]);
	}
	
}
