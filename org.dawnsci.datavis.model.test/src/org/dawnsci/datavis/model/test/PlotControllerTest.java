package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.IPlotMode;
import org.dawnsci.datavis.model.LiveServiceManager;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.dawnsci.datavis.model.PlotController;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Slice;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class PlotControllerTest extends AbstractTestModel {
	
	private static PlotController plotManager;
	private static FileController fileController;
	private static IPlottingSystem<?> plottingSystem;
	
	@BeforeClass
	public static void buildData() throws Exception {
		
		IRecentPlaces p = new IRecentPlaces() {

			@Override
			public void addPlace(String path) {
				
			}

			@Override
			public List<String> getRecentPlaces() {
				return null;
			}
			
		};
			AbstractTestModel.buildData();
			plottingSystem = new MockPlottingSystem();
			LiveServiceManager.setILiveFileService(null);
			fileController = new FileController();
			fileController.setRecentPlaces(p);
			fileController.setLoaderService(new LoaderServiceImpl());
			plotManager = new PlotController(plottingSystem,fileController);
//			plotManager.setFileController();
	}

	@Before
	public void clearAllData(){
		fileController.unloadAll();
		plottingSystem.clear();
	}
	
	private void setUpAndSelectFirstFile1D(){
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset1");
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
	}
	
	private void setUpAndSelectFirstFile2D(){
		setUpAndSelectFile2D(file.getAbsolutePath());
		fileController.loadFile(file.getAbsolutePath());
	}
	
	private void setUpAndSelectFile2D(String path){
		fileController.loadFile(path);
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(path)).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset2");
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeXY() {
		//Open file and select 1D data
		setUpAndSelectFirstFile1D();
		assertNotNull(plotManager.getCurrentPlotModes());
		assertEquals(2, plotManager.getCurrentPlotModes().length);

		testSingleTraceAddRemoveUnload();
		
	}
	
	@Test
	public void testPlotModeXYSlice() {
		setUpAndSelectFirstFile1D();
		//slice
		fileController.getCurrentDataOption().getPlottableObject().getNDimensions().setSlice(0, new Slice(5));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//unload file
		LoadedFile lf = fileController.getCurrentFile();
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	
	@Test
	public void testPlotModeXYMultiDatasetFile(){
		setUpAndSelectFirstFile1D();
		LoadedFile lf = fileController.getCurrentFile();
		DataOptions dop = lf.getDataOption("/entry/dataset1");
		DataOptions dop1 = lf.getDataOption("/entry/dataset1a");
		//select 2nd 1d dataset
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//deselect file
		fileController.setCurrentFile(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//reselect file
		fileController.setCurrentFile(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//deselect datasets
		fileController.setCurrentData(dop,false);
		fileController.setCurrentData(dop1,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select one
		fileController.setCurrentData(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//select two
		fileController.setCurrentData(dop1,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//de-select file again
		fileController.setCurrentFile(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select
		fileController.setCurrentFile(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		//unload
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testMultiFilePlotModeXY() throws Exception{
		fileController.loadFile(file1.getAbsolutePath());
		fileController.loadFile(file2.getAbsolutePath());
		fileController.loadFile(file3.getAbsolutePath());
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		LoadedFile lf3 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file3.getAbsolutePath())).findFirst().get();
		DataOptions dop1 = lf1.getDataOption("/entry/dataset1");
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf1,true);
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		DataOptions dop2 = lf2.getDataOption("/entry/dataset1");
		fileController.setCurrentFile(lf2,true);
		fileController.setCurrentData(dop2, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		DataOptions dop3 = lf3.getDataOption("/entry/dataset1");
		fileController.setCurrentFile(lf3,true);
		fileController.setCurrentData(dop3, true);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop3, false);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop2, false);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop1, false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
		//clean up
		fileController.unloadFile(lf1);
		fileController.unloadFile(lf2);
		fileController.unloadFile(lf3);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}

	private void testSingleTraceAddRemoveUnload(){
		LoadedFile lf = fileController.getCurrentFile();
		DataOptions dop = fileController.getCurrentDataOption();
		//unselect data
		fileController.setCurrentData(dop,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select data
		fileController.setCurrentData(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//unselect file
		fileController.setCurrentFile(lf, false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//select file
		fileController.setCurrentFile(lf, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		//unload file
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	

	@Test
	public void testPlotModeImage() {
		
		setUpAndSelectFirstFile2D();
		testSingleTraceAddRemoveUnload();
		
		//load file
//		fileController.loadFile(file.getAbsolutePath());
//		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
//		DataOptions dop = lf.getDataOption("/entry/dataset2");
//		plotManager.waitOnJob();
//		assertEquals(0, plottingSystem.getTraces().size());
//		//set data, check line trace plotted
//		fileController.setCurrentFile(lf,true);
//		fileController.setCurrentData(dop, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		ITrace next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof ILineTrace);
//		//switch to image mode, check image is plotted
//		IPlotMode[] modes = plotManager.getCurrentPlotModes();
//		plotManager.switchPlotMode(modes[1]);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		
//		//tick different data, check line trace plotted
//		DataOptions dop1 = lf.getDataOption("/entry/dataset3");
//		fileController.setCurrentData(dop1, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof ILineTrace);
//		//switch to image mode, check image plotted
//		plotManager.switchPlotMode(modes[1]);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		//tick other data, check one image is plotted and dop1 not selected
//		fileController.setCurrentData(dop, true);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof IImageTrace);
//		assertFalse(dop1.isSelected());;
//		fileController.unloadFile(lf);
//		plotManager.waitOnJob();
//		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageXYSwitchSingleFile() {
		setUpAndSelectFirstFile2D();
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		DataOptions dop = fileController.getCurrentDataOption();
		LoadedFile lf = fileController.getCurrentFile();
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
		
		plotManager.switchPlotMode(modes[0]);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[0].getOptions()[0], nD.getDescription(1));
		
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
		
//		NDimensions nD = dop.getPlottableObject().getNDimensions();
//		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
//		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
//		
//		plotManager.switchPlotMode(modes[0]);
//		plotManager.waitOnJob();
//		assertEquals(1, plottingSystem.getTraces().size());
//		next = plottingSystem.getTraces().iterator().next();
//		assertTrue(next instanceof ILineTrace);
		
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageXYSwitch2() {
		
		setUpAndSelectFirstFile1D();
		DataOptions dop = fileController.getCurrentDataOption();
		LoadedFile lf = fileController.getCurrentFile();
		setUpAndSelectFile2D(file1.getAbsolutePath());
		DataOptions dop2 = fileController.getCurrentDataOption();
		LoadedFile lf2 = fileController.getCurrentFile();
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		assertFalse(lf.isSelected());
		plotManager.switchPlotMode(modes[0]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		fileController.setCurrentFile(lf, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		fileController.setCurrentFile(lf, true);
		fileController.setCurrentData(dop2,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);

		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		fileController.unloadFile(lf);
		fileController.unloadFile(lf2);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageWithSlice() {
		setUpAndSelectFirstFile2D();

		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		DataOptions dop = fileController.getCurrentDataOption();
		LoadedFile lf = fileController.getCurrentFile();
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		nD.setSlice(0, new Slice(1,2,1));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testMultiFileImage() throws Exception{
		fileController.loadFile(file1.getAbsolutePath());
		fileController.loadFile(file2.getAbsolutePath());
		
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		
		DataOptions dop1 = lf1.getDataOption("/entry/dataset2");
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf1,true);
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		
		DataOptions dop2 = lf2.getDataOption("/entry/dataset2");
		fileController.setCurrentFile(lf2,true);
		fileController.setCurrentData(dop2, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		//clean up
		fileController.unloadFile(lf1);
		fileController.unloadFile(lf2);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	
	@Test
	public void testMultiFileXYAndImage() throws Exception{
		fileController.loadFile(file.getAbsolutePath());
		fileController.loadFile(file1.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst().get();
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();

		DataOptions dop = lf.getDataOption("/entry/dataset2");
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//Select first file and some data, check plotted as line
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//change current file (not checked) and check data, make sure image still plotted
		DataOptions dop1 = lf1.getDataOption("/entry/dataset1");
		fileController.setCurrentFile(lf1,false);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		plotManager.waitOnJob();
		fileController.setCurrentData(dop1, false);
	
		//check file (with data unchecked) make sure image still plotted
		fileController.setCurrentFile(lf1,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//check data, make sure plot switches to a line and first file is unchecked
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		//Need to make UI reflect the plot manager deselection
		assertFalse(lf.isSelected());
		//
		fileController.setCurrentFile(lf,true);
		assertFalse(lf1.isSelected());
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//clean up
		fileController.unloadFile(lf);
		fileController.unloadFile(lf1);
//		fileController.unloadFile(lf2);
//		fileController.unloadFile(lf3);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	
//	@Test
//	public void testPlotModeXYtoImage() {
//		fileController.loadFile(file.getAbsolutePath());
//		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
//		lf.setSelected(true);
//		DataOptions dop = lf.getDataOptions().get(1);
//		dop.setSelected(true);
//		fileController.setCurrentFile(lf);
//		fileController.setCurrentData(dop, true);
//		assertNotNull(plotManager.getCurrentPlotModes());
//		assertEquals(3, plotManager.getCurrentPlotModes().length);
//		assertEquals(1, plottingSystem.getTraces().size());
//		fileController.unloadFile(lf);
////		fileController.setCurrentData(dop,false);
////		assertEquals(0, plottingSystem.getTraces().size());
////		fileController.setCurrentData(dop,true);
////		assertEquals(1, plottingSystem.getTraces().size());
////		fileController.getNDimensions().setSlice(0, new Slice(5));
////		assertEquals(1, plottingSystem.getTraces().size());
//	}

//	@Test
//	public void testRemoveFromPlot() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSwitchPlotMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddToPlot() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetCurrentMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetCurrentMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdatePlot() {
//		fail("Not yet implemented");
//	}

}
