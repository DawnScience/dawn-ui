package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class MappedFileManagerTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static File file = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());
		
	}
	
	
	@Test
	public void test() throws InterruptedException {
		MappedFileManager manager = new MappedFileManager();
		
		AtomicReference<MappedDataFile> af = new AtomicReference<MappedDataFile>();
		
		manager.addListener(new IMapFileEventListener() {
			
			@Override
			public void mapFileStateChanged(MappedDataFile file) {
				af.set(file);
				
			}
		});
		
		manager.loadFiles(new String[] {file.getAbsolutePath()}, null);
		
		int counter = 0;
		
		while (af.get() == null && counter < 5) {
			Thread.sleep(1000);
			counter++;
		}
		
		if (counter == 5) fail("Event not fired");
		
		MappedDataFile mappedDataFile = af.get();
		
		assertEquals(file.getAbsolutePath(),mappedDataFile.getPath());
		
		List<PlottableMapObject> plottedObjects = manager.getPlottedObjects();
		
		assertTrue("Something plotted", !plottedObjects.isEmpty());
		
	}

}
