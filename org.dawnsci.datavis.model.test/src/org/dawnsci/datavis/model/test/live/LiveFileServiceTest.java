package org.dawnsci.datavis.model.test.live;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.dawnsci.datavis.api.ILiveFileService;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.ILiveLoadedFileListener;
import org.dawnsci.datavis.model.ILiveLoadedFileService;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import uk.ac.diamond.osgi.services.ServiceProvider;


public class LiveFileServiceTest {

	private static FileController fileController;
	
	@BeforeClass
	public static void buildData() throws Exception {
		fileController = new FileController();
	}
	
	@AfterClass
	public static void clean() throws Exception {
		ServiceProvider.reset();
	}
	
	@Test
	public void testBasicServiceMethods() throws Exception {
		
		String path = "/tmp/myfile.test";
		
		ILiveLoadedFileService liveFileService = mock(ILiveLoadedFileService.class);
		ServiceProvider.setService(ILiveFileService.class, liveFileService);
		ArgumentCaptor<ILiveLoadedFileListener> listener = ArgumentCaptor.forClass(ILiveLoadedFileListener.class);
		
		assertTrue(fileController.getLoadedFiles().isEmpty());
		fileController.attachLive();
		verify(liveFileService).addLiveFileListener(listener.capture());
		ILiveLoadedFileListener captured = listener.getValue();
		
		assertNotNull(captured);
		
		List<LoadedFile> loadedFiles = fileController.getLoadedFiles();
		assertTrue(loadedFiles.isEmpty());

		LoadedFile f = mock(LoadedFile.class, withSettings().extraInterfaces(IRefreshable.class));
		
		when(f.getFilePath()).thenReturn(path);
		
		captured.fileLoaded(f);
		assertFalse(fileController.getLoadedFiles().isEmpty());
		
		captured.refreshRequest();
		verify(liveFileService,times(1)).runUpdate(any(Runnable.class),ArgumentMatchers.eq(false));
		
		captured.localReload(path,false);
		verify(liveFileService,times(1)).runUpdate(any(Runnable.class),ArgumentMatchers.eq(true));
		
	}

}
