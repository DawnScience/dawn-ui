package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsUtils;
import org.dawnsci.datavis.model.PlotController;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataOptionsUtilsTest extends AbstractTestModel {

	private static DataOptions dataOptions;

	@BeforeClass
	public static void buildData() throws Exception {
		AbstractTestModel.buildData();
		new PlotController().initialise(loadedFile);

		dataOptions = loadedFile.getDataOption("/entry/dataset3");
	}

	@Test
	public void testSlice() throws DatasetException {

		runTest(d -> DataOptionsUtils.buildView(d));

	}

	@Test
	public void testSum() throws DatasetException {

		runTest(d -> DataOptionsUtils.sum(d,null));

	}

	@Test
	public void testAverage() throws DatasetException {

		runTest(d -> DataOptionsUtils.average(d,null));

	}

	private void runTest(Function<DataOptions, DataOptions> function) throws DatasetException {
		DataOptions local = dataOptions.clone();
		local.setAxes(new String[] {null, "/entry/dataset1b", null});
		//3D data block, so should be plotted as image
		//process should make shape [1,y,x]
		int[] shape = local.getLazyDataset().getShape();
		shape[0] = 1;

		DataOptions out = function.apply(local);

		assertEquals(local.getParent(), out.getParent());
		assertArrayEquals(shape, out.getLazyDataset().getShape());
		IDataset slice = out.getLazyDataset().getSlice();
		assertArrayEquals(shape, slice.getShape());
		AxesMetadata metadata = slice.getFirstMetadata(AxesMetadata.class);

		assertNotNull(metadata);
		ILazyDataset[] axes = metadata.getAxes();
		assertNotNull(axes);
		assertEquals(3, axes.length);
		assertNotNull(axes[1]);
	}

}
