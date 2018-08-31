package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.datamodel.IMapFileEventListener;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;
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
		
	}
	
	
	@Test
	public void test() throws InterruptedException {
		MappedFileManager manager = new MappedFileManager();
		manager.setLoaderService(new LoaderServiceImpl());
		
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
		
	}
	
	@Test
	public void testLiveStream() throws InterruptedException {
		MappedFileManager manager = new MappedFileManager();
		manager.addLiveStream(new LiveStreamMapObject() {
			
			@Override
			public boolean hasChildren() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public double[] getRange() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object[] getChildren() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void update() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setPlotted(boolean plot) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isPlotted() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public boolean isLive() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getTransparency() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public IDataset getSpectrum(double x, double y) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getPath() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IDataset getMap() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getLongName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void removeAxisListener(IAxisMoveListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public List<IDataset> getAxes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void disconnect() throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public IDynamicShape connect() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void addAxisListener(IAxisMoveListener listener) {
				// TODO Auto-generated method stub
				
			}
		});
		
		assertFalse(manager.getPlottedObjects().isEmpty());
		
	}

}
