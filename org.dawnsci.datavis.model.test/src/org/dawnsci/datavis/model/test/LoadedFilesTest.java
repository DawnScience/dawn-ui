package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.LoadedFiles;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class LoadedFilesTest extends AbstractTestModel {
	
	@Test
	public void testLoadFiles() throws Exception {
		LoadedFile f0 = new LoadedFile(LoaderFactory.getData(file.getAbsolutePath()));
		LoadedFile f1 = new LoadedFile(LoaderFactory.getData(file1.getAbsolutePath()));
		LoadedFile f2 = new LoadedFile(LoaderFactory.getData(file2.getAbsolutePath()));
		LoadedFile f3= new LoadedFile(LoaderFactory.getData(file3.getAbsolutePath()));
		
		LoadedFiles files = new LoadedFiles();
		files.addFile(f3);
		files.addFile(f2);
		files.addFile(f1);
		files.addFile(f0);
		
		List<LoadedFile> lf = files.getLoadedFiles();
		
		assertEquals(f0, lf.get(3));
		assertEquals(f1, lf.get(2));
		assertEquals(f2, lf.get(1));
		assertEquals(f3, lf.get(0));
		
		files.unloadAllFiles();
		
		lf = files.getLoadedFiles();
		
		assertTrue(lf.isEmpty());
		
		files.addFile(f3);
		files.addFile(f2);
		files.addFile(f1);
		files.addFile(f0);
		
		Comparator<LoadedFile> comparator = Comparator.comparing((LoadedFile file) -> file.getName());
		files.setComparator(comparator);
		lf = files.getLoadedFiles();
		
		assertEquals(f0, lf.get(0));
		assertEquals(f1, lf.get(1));
		assertEquals(f2, lf.get(2));
		assertEquals(f3, lf.get(3));
		
		
	}

}
