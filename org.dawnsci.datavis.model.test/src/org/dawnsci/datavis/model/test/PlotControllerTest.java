package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.ISliceChangeListener;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.dawnsci.datavis.model.PlotController;
import org.dawnsci.datavis.model.SliceChangeEvent;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class PlotControllerTest extends AbstractTestModel {
	
	private static PlotController plotManager;
	private static FileController fileController;
	private static IPlottingSystem<?> plottingSystem;
	private static ExecutorService exService;
	
	@BeforeClass
	public static void buildData() throws Exception {
		AbstractTestModel.buildData();
		exService = Executors.newSingleThreadExecutor();
		
	}
	
	private void initialiseControllers() {
		IRecentPlaces p = new IRecentPlaces() {

			@Override
			public void addPlace(String path) {
				
			}

			@Override
			public List<String> getRecentPlaces() {
				return null;
			}
			
		};
		plottingSystem = new MockPlottingSystem();
		new LiveServiceManager().setILiveFileService(null);
		fileController = new FileController();
		fileController.setRecentPlaces(p);
		fileController.setLoaderService(new LoaderServiceImpl());
		plotManager = new PlotController(plottingSystem,fileController,exService);
		
	}
	

	
	private DataOptions setUpAndSelectFirstFile1D(){
		FileControllerUtils.loadFile(fileController,file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset1");
		fileController.setFileSelected(lf,true);
		fileController.setDataSelected(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		return dop;
	}
	
	private DataOptions setUpAndSelectFirstFile2D(){
		DataOptions d = setUpAndSelectFile2D(file.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file.getAbsolutePath());
		return d;
	}
	
	private DataOptions setUpAndSelectFirstFile3D(){
		DataOptions dop = setUpAndSelectFile3D(file.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file.getAbsolutePath());
		return dop;
	}
	
	private DataOptions setUpAndSelectFile2D(String path){
		FileControllerUtils.loadFile(fileController,path);
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(path)).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset2");
		fileController.setFileSelected(lf,true);
		fileController.setDataSelected(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		return dop;
	}
	
	private DataOptions setUpAndSelectFile3D(String path){
		FileControllerUtils.loadFile(fileController,path);
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(path)).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset3");
		fileController.setFileSelected(lf,true);
		fileController.setDataSelected(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		return dop;
	}

	@Test
	public void testPlotModeXY() {
		initialiseControllers();
		//Open file and select 1D data
		setUpAndSelectFirstFile1D();
		//Unchecks and unloads
		testSingleTraceAddRemoveUnload();
		
	}

	@Test
	public void testPlotModeXYSlice() {
		initialiseControllers();
		setUpAndSelectFirstFile1D();
		IDataset data = plottingSystem.getTraces().iterator().next().getData();
		assertEquals(10, data.getSize());
		LoadedFile lf = fileController.getLoadedFiles().iterator().next();
		DataOptions dop = lf.getDataOptions().iterator().next();
		NDimensions nDimensions = dop.getPlottableObject().getNDimensions();
		
		ISliceChangeListener sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				//respond to this happening elsewhere
				if (event.isOptionsChanged()) return;
				if (event.getParent() instanceof DataOptions) {
					DataOptions dOptions = (DataOptions)event.getParent();
					if (!dOptions.isSelected() && !dOptions.getParent().isSelected()) {
						return;
					}
				}
				plotManager.forceReplot();	
			};
		};
		
		nDimensions.addSliceListener(sliceListener);
		
		//slice
		nDimensions.setSlice(0, new Slice(5));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		data = plottingSystem.getTraces().iterator().next().getData();
		assertEquals(5, data.getSize());
		//unload file
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}

	@Test
	public void testPlotModeXYMultiDatasetFile(){
		initialiseControllers();
		setUpAndSelectFirstFile1D();
		LoadedFile lf = fileController.getLoadedFiles().iterator().next();
		DataOptions dop = lf.getDataOption("/entry/dataset1");
		DataOptions dop1 = lf.getDataOption("/entry/dataset1a");
		//select 2nd 1d dataset
		fileController.setDataSelected(dop1, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//deselect file
		fileController.setFileSelected(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//reselect file
		fileController.setFileSelected(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//deselect datasets
		fileController.setDataSelected(dop,false);
		fileController.setDataSelected(dop1,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select one
		fileController.setDataSelected(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//select two
		fileController.setDataSelected(dop1,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//de-select file again
		fileController.setFileSelected(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select
		fileController.setFileSelected(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//unload
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testMultiFilePlotModeXY() throws Exception{
		initialiseControllers();
		FileControllerUtils.loadFile(fileController,file1.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file2.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file3.getAbsolutePath());
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		LoadedFile lf3 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file3.getAbsolutePath())).findFirst().get();
		DataOptions dop1 = lf1.getDataOption("/entry/dataset1");
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setFileSelected(lf1,true);
		fileController.setDataSelected(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		DataOptions dop2 = lf2.getDataOption("/entry/dataset1");
		fileController.setFileSelected(lf2,true);
		fileController.setDataSelected(dop2, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		DataOptions dop3 = lf3.getDataOption("/entry/dataset1");
		fileController.setFileSelected(lf3,true);
		fileController.setDataSelected(dop3, true);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());
		fileController.setDataSelected(dop3, false);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setDataSelected(dop2, false);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setDataSelected(dop1, false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
		//clean up
		fileController.unloadFiles(Arrays.asList(lf1,lf2,lf3));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}

	private void testSingleTraceAddRemoveUnload(){
		LoadedFile lf = fileController.getLoadedFiles().get(0);
		DataOptions dop = lf.getSelectedDataOptions().get(0);
		//unselect data
		fileController.setDataSelected(dop,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select data
		fileController.setDataSelected(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//unselect file
		fileController.setFileSelected(lf, false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select file
		fileController.setFileSelected(lf, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//unload file
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}

	@Test
	public void testPlotModeImage() {
		initialiseControllers();
		setUpAndSelectFirstFile2D();
		testSingleTraceAddRemoveUnload();
		
	}

	@Test
	public void testPlotModeImageXYSwitchSingleFile() {
		DataOptions dop = setUpAndSelectFirstFile2D();
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		IPlotMode[] modes = plotManager.getPlotModes(dop);

		LoadedFile lf = dop.getParent();
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
		
		plotManager.switchPlotMode(modes[0],dop);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[0].getOptions()[0], nD.getDescription(1));
		
		plotManager.switchPlotMode(modes[1],dop);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
		
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}

	@Test
	public void testPlotModeImageXYSwitch2() {
		initialiseControllers();
		DataOptions dop = setUpAndSelectFirstFile1D();

		LoadedFile lf = dop.getParent();
		DataOptions dop2 = setUpAndSelectFile2D(file1.getAbsolutePath());
		LoadedFile lf2 = dop2.getParent();
		IPlotMode[] modes = plotManager.getPlotModes(dop2);
		assertFalse(lf.isSelected());
		plotManager.switchPlotMode(modes[0],dop2);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		fileController.setFileSelected(lf, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		fileController.setFileSelected(lf, true);
		fileController.setDataSelected(dop2,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);

		plotManager.switchPlotMode(modes[1],dop2);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		fileController.unloadFiles(Arrays.asList(lf,lf2));

		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}

	@Test
	public void testPlotModeImageWithSlice() {
		DataOptions dop = setUpAndSelectFirstFile3D();

		ITrace next = plottingSystem.getTraces().iterator().next();
		IDataset d0 = next.getData();
		assertTrue(next instanceof IImageTrace);

		LoadedFile lf = dop.getParent();
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		
		ISliceChangeListener sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				//respond to this happening elsewhere
				if (event.isOptionsChanged()) return;
				if (event.getParent() instanceof DataOptions) {
					DataOptions dOptions = (DataOptions)event.getParent();
					if (!dOptions.isSelected() && !dOptions.getParent().isSelected()) {
						return;
					}
				}
				plotManager.forceReplot();	
			};
		};
		
		nD.addSliceListener(sliceListener);
		
		
		nD.setSlice(0, new Slice(1,2,1));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		IDataset d1 = next.getData();
		assertTrue(!Comparisons.allTrue(Comparisons.equalTo(d0, d1)));
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}

	@Test
	public void testMultiFileImage() throws Exception{
		initialiseControllers();
		FileControllerUtils.loadFile(fileController,file1.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file2.getAbsolutePath());
		
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		
		DataOptions dop1 = lf1.getDataOption("/entry/dataset2");
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setFileSelected(lf1,true);
		fileController.setDataSelected(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		
		DataOptions dop2 = lf2.getDataOption("/entry/dataset2");
		fileController.setFileSelected(lf2,true);
		fileController.setDataSelected(dop2, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		//clean up
		fileController.unloadFiles(Arrays.asList(lf1,lf2));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
//	
//	@Test
//	public void testMultiFileXYAndImage() throws Exception{
//		initialiseControllers();
//		FileControllerUtils.loadFile(fileController,file.getAbsolutePath());
//		FileControllerUtils.loadFile(fileController,file1.getAbsolutePath());
//		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst().get();
//		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
//
//		DataOptions dop = lf.getDataOption("/entry/dataset2");
//		plotManager.waitOnJob();
//		assertEquals(0, plottingSystem.getTraces().size());
//		//Select first file and some data, check plotted as line
//		fileController.setFileSelected(lf,true);
//		fileController.setDataSelected(dop, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		ITrace next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		
//		//change current file (not checked) and check data, make sure image still plotted
//		DataOptions dop1 = lf1.getDataOption("/entry/dataset1");
//		fileController.setFileSelected(lf1,false);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		fileController.setDataSelected(dop1, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		plotManager.waitOnJob();
//		fileController.setDataSelected(dop1, false);
//	
//		//check file (with data unchecked) make sure image still plotted
//		fileController.setFileSelected(lf1,true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		
//		//check data, make sure plot switches to a line and first file is unchecked
//		fileController.setDataSelected(dop1, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof ILineTrace);
//		
//		//Need to make UI reflect the plot manager deselection
//		assertFalse(lf.isSelected());
//		//
//		fileController.setFileSelected(lf,true);
//		assertFalse(lf1.isSelected());
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		
//		//clean up
//		fileController.unloadFiles(Arrays.asList(lf1,lf));
////		fileController.unloadFile(lf2);
////		fileController.unloadFile(lf3);
//		plotManager.waitOnJob();
//		assertEquals(0, plottingSystem.getTraces().size());
//		
//	}

}
