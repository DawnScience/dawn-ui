package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController;
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
			public List<String> getRecentPlaces() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void addPlace(String path) {
				// TODO Auto-generated method stub
				
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
