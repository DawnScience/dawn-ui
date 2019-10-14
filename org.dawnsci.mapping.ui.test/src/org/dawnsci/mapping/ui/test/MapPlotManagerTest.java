package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class MapPlotManagerTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static File file = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		
	}
	
	@Test
	public void test() {
		MappedFileManager fileManager = new MappedFileManager();
		fileManager.setLoaderService(new LoaderServiceImpl());
		
		MapPlotManager plotManager = new MapPlotManager();
		plotManager.setMapFileController(fileManager);
		
		IPlottingService mockPlotService = mock(IPlottingService.class);
		
		@SuppressWarnings("unchecked")
		IPlottingSystem<Object> mockMapPlot = mock(IPlottingSystem.class);
		
		@SuppressWarnings("unchecked")
		IPlottingSystem<Object> mockDetectorPlot = mock(IPlottingSystem.class);
		IImageTrace mockTrace = mock(IImageTrace.class);
		
		when(mockMapPlot.createImageTrace(any(String.class))).thenReturn(mockTrace);
		
		when(mockPlotService.getPlottingSystem("Map")).thenReturn(mockMapPlot);
		when(mockPlotService.getPlottingSystem("Detector Data")).thenReturn(mockDetectorPlot);
		
		plotManager.setPlotService(mockPlotService);
		plotManager.init();
		
		assertNull(plotManager.getTopMap());
		
		fileManager.loadFilesBlocking(new String[] {file.getAbsolutePath()});
		
		MappedDataFile dataFile = fileManager.getArea().getDataFile(0);
		AbstractMapData map = dataFile.getMap();
		
		fileManager.toggleDisplay(map);
		plotManager.waitOnJob();
		
		assertNotNull(plotManager.getTopMap());
		
		verify(mockMapPlot,times(1)).addTrace(any(ITrace.class));
		
	}

}
