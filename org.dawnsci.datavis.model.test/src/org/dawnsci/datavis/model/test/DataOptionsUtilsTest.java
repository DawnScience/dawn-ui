package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsUtils;
import org.dawnsci.datavis.model.PlotController;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

	@Test
	public void testSave() throws DatasetException {

		runTest(d -> {
			DataOptions out = DataOptionsUtils.sum(d,null);
			
			String path = "/file.nxs";
			
			INexusFileFactory mockFactory = mock(INexusFileFactory.class);
			NexusFile mockFile = mock(NexusFile.class);
			
			when(mockFactory.newNexusFile(anyString())).thenReturn(mockFile);
			
			try {
				
				DataOptionsUtils.saveToFile(out, path, mockFactory);
				verify(mockFile,times(1)).createAndOpenToWrite();
				
				ArgumentCaptor<Node> node = ArgumentCaptor.forClass(Node.class);
				ArgumentCaptor<String> string = ArgumentCaptor.forClass(String.class);
				verify(mockFile,times(1)).addNode(string.capture(), node.capture());
				
				String value = string.getValue();
				assertEquals("/entry1", value);
				
				Node nodeCap = node.getValue();
				
				assertTrue(nodeCap instanceof NXentry);
				
				verify(mockFile,times(1)).close();

			} catch (Exception e) {
				fail("Save threw exception : " + e.getMessage());
			}
			
			return out;
		});

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
