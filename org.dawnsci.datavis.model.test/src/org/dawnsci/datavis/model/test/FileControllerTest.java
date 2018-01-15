package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Optional;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class FileControllerTest extends AbstractTestModel{
	
	private static FileController fileController;
	
	@BeforeClass
	public static void buildData() throws Exception {
		AbstractTestModel.buildData();
		fileController = new FileController();
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
		
	}


	@Test
	public void testGetLoadedFiles() {
		assertNotNull(fileController.getLoadedFiles());
		fileController.loadFile(file.getAbsolutePath());
	}

	@Test
	public void testSetCurrentFile() {
		assertNull(fileController.getCurrentFile());
		Optional<LoadedFile> lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst();
		fileController.setCurrentFile(lf.get(),false);
		assertNotNull(fileController.getCurrentFile());
		fileController.setCurrentFile(null,false);
		assertNull(fileController.getCurrentFile());
		
	}

//	@Test
//	public void testSetCurrentData() {
//		assertNull(fileController.getCurrentFile());
//		fileController.setCurrentFile(fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath()),false);
//		assertNotNull(fileController.getCurrentFile());
//		fileController.setCurrentData(fileController.getCurrentFile().getDataOptions().get(0));
//		assertNotNull(fileController.getCurrentDataOption());
//		fileController.setCurrentFile(null,false);
//		assertNull(fileController.getCurrentFile());
//		assertNull(fileController.getCurrentDataOption());
//	}

	@Test
	public void testGetCurrentDataOption() {
		assertNull(fileController.getCurrentDataOption());
	}

	@Test
	public void testGetCurrentFile() {
		assertNull(fileController.getCurrentFile());
	}

//	@Test
//	public void testGetSelectedDataOptions() {
//		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
//		fileController.setCurrentFile(lf,false);
//		DataOptions dataOptions = fileController.getCurrentFile().getDataOptions().get(0);		
//		dataOptions.setSelected(true);
//		assertEquals(dataOptions, fileController.getSelectedDataOptions().get(0));
//		dataOptions.setSelected(false);
//		fileController.setCurrentFile(null,false);
//	}

}
