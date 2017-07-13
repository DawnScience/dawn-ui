package org.dawnsci.datavis.model.test.live;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.ServiceManager;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class LiveFileServiceTest {

	private static IFileController fileController;
	
	@BeforeClass
	public static void buildData() throws Exception {
		ServiceManager.setLoaderService(new LoaderServiceImpl());
		fileController = new FileController();
	}
	
	@Test
	public void testBasicServiceMethods() throws Exception {
		MockLiveFileService mock = new MockLiveFileService();
		
		LiveServiceManager.setILiveFileService(mock);
		
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
				fired.set(true);
			}
		});
		
		next.refreshRequest();
		//happens in a separate thread so sleep for a bit
		Thread.sleep(100);
		assertTrue(fired.get());
		
	}

}
