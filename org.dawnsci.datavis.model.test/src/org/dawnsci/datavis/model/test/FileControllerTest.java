package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.LoadedFile;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class FileControllerTest extends AbstractTestModel{
	
	@BeforeClass
	public static void buildData() throws Exception {
		AbstractTestModel.buildData();
		
	}

	@Test
	public void testGetLoadedFiles() {
		
		FileController fileController = new FileController();
		fileController.setLoaderService(new LoaderServiceImpl());
		fileController.setRecentPlaces(new IRecentPlaces() {
			
			@Override
			public List<String> getRecentDirectories() {
				return Collections.emptyList();
			}
			
			@Override
			public void addFiles(String... path) {
				
			}

			@Override
			public String getCurrentDefaultDirectory() {
				return "";
			}

			@Override
			public List<String> getRecentFiles() {
				return Collections.emptyList();
			}
		});
		
		//Check no files
		assertNotNull(fileController.getLoadedFiles());
		assertTrue(fileController.getLoadedFiles().isEmpty());
		
		FileControllerUtils.loadFile(fileController,file.getAbsolutePath());
		
		//Check files
		assertTrue(!fileController.getLoadedFiles().isEmpty());
		LoadedFile lf= fileController.getLoadedFiles().get(0);
		assertTrue(lf.getFilePath().equals(file.getAbsolutePath()));
	}

}
