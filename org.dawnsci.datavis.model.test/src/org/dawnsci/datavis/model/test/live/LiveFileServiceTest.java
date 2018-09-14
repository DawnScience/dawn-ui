package org.dawnsci.datavis.model.test.live;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveFileService;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.dawnsci.datavis.model.LoadedFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

public class LiveFileServiceTest {

	private static FileController fileController;
	
	@BeforeClass
	public static void buildData() throws Exception {
		fileController = new FileController();
	}
	
	@AfterClass
	public static void clean() throws Exception {
		new LiveServiceManager().setILiveFileService(null);
	}
	
	@Test
	public void testBasicServiceMethods() throws Exception {
		
		String path = "/tmp/myfile.test";
		
		ILiveFileService liveFileService = mock(ILiveFileService.class);
		new LiveServiceManager().setILiveFileService(liveFileService);
		ArgumentCaptor<ILiveFileListener> listener = ArgumentCaptor.forClass(ILiveFileListener.class);
		
		assertTrue(fileController.getLoadedFiles().isEmpty());
		fileController.attachLive();
		verify(liveFileService).addLiveFileListener(listener.capture());
		ILiveFileListener captured = listener.getValue();
		
		assertNotNull(captured);
		
		List<LoadedFile> loadedFiles = fileController.getLoadedFiles();
		assertTrue(loadedFiles.isEmpty());

		LoadedFile f = mock(LoadedFile.class, withSettings().extraInterfaces(IRefreshable.class));
		
		when(f.getFilePath()).thenReturn(path);
		
		captured.fileLoaded(f);
		assertFalse(fileController.getLoadedFiles().isEmpty());
		
		captured.refreshRequest();
		verify(liveFileService,times(1)).runUpdate(any(Runnable.class),Matchers.eq(false));
		
		captured.localReload(path);
		verify(liveFileService,times(1)).runUpdate(any(Runnable.class),Matchers.eq(true));
		
	}

}
