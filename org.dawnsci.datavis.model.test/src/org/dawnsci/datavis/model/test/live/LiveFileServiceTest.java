package org.dawnsci.datavis.model.test.live;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.dawnsci.datavis.model.LoadedFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class LiveFileServiceTest {

	private static FileController fileController;
	private static MockLiveFileService mock;
	
	@BeforeClass
	public static void buildData() throws Exception {
//		ServiceManager.setLoaderService(new LoaderServiceImpl());
		fileController = new FileController();
		fileController.setLoaderService(new LoaderServiceImpl());
		mock = new MockLiveFileService();
		new LiveServiceManager().setILiveFileService(mock);
	}
	
	@AfterClass
	public static void clean() throws Exception {
		new LiveServiceManager().setILiveFileService(null);
	}
	
	@Test
	public void testBasicServiceMethods() throws Exception {
		
		assertTrue(mock.getListeners().isEmpty());
		assertTrue(fileController.getLoadedFiles().isEmpty());
		
		fileController.attachLive();
		
		List<LoadedFile> loadedFiles = fileController.getLoadedFiles();
		assertTrue(loadedFiles.isEmpty());
		
		assertFalse(mock.getListeners().isEmpty());
		
		ILiveFileListener next = mock.getListeners().iterator().next();
		
		next.fileLoaded(new LoadedFile(new DataHolder()));
		
		assertFalse(fileController.getLoadedFiles().isEmpty());
		
		final AtomicBoolean fired = new AtomicBoolean(false);
		
		fileController.addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
			}

			@Override
			public void liveUpdate() {
				fired.set(true);
				
			}
		});
		
		next.refreshRequest();
		//happens in a separate thread so sleep for a bit
		Thread.sleep(100);
		assertTrue(fired.get());
		
	}

}
