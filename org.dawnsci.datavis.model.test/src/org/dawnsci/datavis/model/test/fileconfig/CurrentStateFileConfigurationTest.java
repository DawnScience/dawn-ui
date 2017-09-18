package org.dawnsci.datavis.model.test.fileconfig;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataStateObject;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.dawnsci.datavis.model.PlotModeXY;
import org.dawnsci.datavis.model.PlottableObject;
import org.dawnsci.datavis.model.fileconfig.CurrentStateFileConfiguration;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.Metadata;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class CurrentStateFileConfigurationTest {

	@Test
	public void test() {
		DataHolder dataHolder = new DataHolder();
		Metadata m = new Metadata();
		m.addDataInfo("x", new int[]{10});
		m.addDataInfo("y", new int[]{10});
		dataHolder.setMetadata(m);
		dataHolder.setFilePath("/nofile.nxs");
		dataHolder.addDataset("x", DatasetFactory.createRange(10));
		dataHolder.addDataset("y", DatasetFactory.createRange(10));
		LoadedFile f = new LoadedFile(dataHolder);
		DataOptions dataOption = f.getDataOption("x");
		NDimensions nd = dataOption.buildNDimensions();
		PlotModeXY mode = new PlotModeXY();
		nd.setAxis(0, "y");
		nd.setDescription(0, mode.getOptions()[0]);
		nd.setSlice(0, new Slice(0,5,1));
		PlottableObject po = new PlottableObject(mode, nd);
		dataOption.setPlottableObject(po);
		dataOption.setSelected(true);
		
		DataHolder test = new DataHolder();
		test.setFilePath("/nofile.nxs");
		test.addDataset("x", DatasetFactory.createRange(10));
		test.addDataset("y", DatasetFactory.createRange(10));
		test.setMetadata(m);
		LoadedFile ftest = new LoadedFile(test);
		DataOptions dataOptiontest = ftest.getDataOption("x");

		CurrentStateFileConfiguration c = new CurrentStateFileConfiguration();
		
		DataStateObject o = new DataStateObject(dataOption, true, po);
		
		c.setCurrentState(Arrays.asList(new DataStateObject[]{o}));
		
		assertTrue(c.configure(ftest));
		
		Slice slice = dataOptiontest.getPlottableObject().getNDimensions().getSlice(0);
		
		assertEquals(5, slice.getStop().intValue());
		String axis = dataOptiontest.getPlottableObject().getNDimensions().getAxis(0);
		assertEquals("y", axis);
		
		
	}

}
