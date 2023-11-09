package org.dawnsci.datavis.model.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController.OpenMode;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlotController;
import org.dawnsci.january.model.ISliceChangeListener;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.model.SliceChangeEvent;
import org.dawnsci.january.ui.utils.DisplayWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class PlotControllerTest extends AbstractTestModel {
	
	private static PlotController plotManager;
	private static FileController fileController;
	private static IPlottingSystem<?> plottingSystem;
	private static ExecutorService exService;

	private static final int MODE_2D = PlotController.MODE_2D;

	@BeforeClass
	public static void buildData() throws Exception {
		DisplayWrapper.setTestMode();
		AbstractTestModel.buildData();
		exService = Executors.newSingleThreadExecutor();
		
	}
	
	private void initialiseControllers() {
		IRecentPlaces p = new IRecentPlaces() {

			@Override
			public void addFiles(String... path) {
				
			}

			@Override
			public List<String> getRecentDirectories() {
				return Collections.emptyList();
			}

			@Override
			public String getCurrentDefaultDirectory() {
				return "";
			}

			@Override
			public List<String> getRecentFiles() {
				return Collections.emptyList();
			}
			
		};
		plottingSystem = new MockPlottingSystem();
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
		return d;
	}
	
	private DataOptions setUpAndSelectFirstFile3D(){
		DataOptions dop = setUpAndSelectFile3D(file.getAbsolutePath());
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
			}

			@Override
			public void axisChanged(SliceChangeEvent event) {
				//do nothing
			}

			@Override
			public void optionsChanged(SliceChangeEvent event) {
				//do nothing
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
		assertEquals(modes[MODE_2D].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[MODE_2D].getOptions()[1], nD.getDescription(0));
		
		plotManager.switchPlotMode(modes[0],dop);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[0].getOptions()[0], nD.getDescription(1));
		
		plotManager.switchPlotMode(modes[MODE_2D],dop);
		plotManager.waitOnJob();

		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[MODE_2D].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[MODE_2D].getOptions()[1], nD.getDescription(0));
		
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

		plotManager.switchPlotMode(modes[MODE_2D],dop2);
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
			}

			@Override
			public void axisChanged(SliceChangeEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void optionsChanged(SliceChangeEvent event) {
				// TODO Auto-generated method stub
				
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
	
	@Ignore
	@Test
	public void testLabelPropagation() throws Exception{
		initialiseControllers();
		DataOptions dp = setUpAndSelectFirstFile1D();
		assertEquals(1, plottingSystem.getTraces().size());
		String label = "my_test_label";
		dp.getParent().setLabel(label);
		plotManager.forceReplot();
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next.getName().equals(label));
		
		fileController.setDataSelected(dp.getParent().getDataOption("/entry/dataset1a"),true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertFalse(next.getName().equals(label));
		assertTrue(next.getName().contains(label));
	}
	
	@Test
	public void testSelectNotSelected() throws Exception {
		initialiseControllers();
		setUpAndSelectFirstFile2D();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		String path = file1.getAbsolutePath();
		FileControllerUtils.loadFile(fileController,path);
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(path)).findFirst().get();
		DataOptions dop2 = lf.getDataOption("/entry/dataset2");
		fileController.setDataSelected(dop2, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
	}

	@Test
	public void testSwitchAndSliceBeforePlot() throws Exception {
		initialiseControllers();
		String path = file.getAbsolutePath();
		FileControllerUtils.loadFile(fileController,path);
		LoadedFile lf = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(path)).findFirst().get();
		DataOptions dop = lf.getDataOption("/entry/dataset3");
		IPlotMode[] plotModes = plotManager.getPlotModes(dop);
		IPlotMode newMode = plotModes[0];
		assertNotEquals(newMode, plotManager.getCurrentMode());
		//should only switch internal plot object in dataoption
		plotManager.switchPlotMode(newMode, dop);
		assertNotEquals(newMode, plotManager.getCurrentMode());
		NDimensions nDimensions = dop.getPlottableObject().getNDimensions();
		SliceND snd = nDimensions.buildSliceND();
		assertNotEquals(2, snd.getStart()[0]);

		nDimensions.setSlice(0, new Slice(2,3));
		
		fileController.setFileSelected(lf, true);
		plotManager.waitOnJob();
		assertNotEquals(newMode, plotManager.getCurrentMode());
		fileController.setDataSelected(dop, true);
		plotManager.waitOnJob();
		
		dop = lf.getDataOption("/entry/dataset3");
		nDimensions = dop.getPlottableObject().getNDimensions();
		snd = nDimensions.buildSliceND();
		assertEquals(2, snd.getStart()[0]);
		
		assertEquals(newMode, plotManager.getCurrentMode());
		
		//clean up
		fileController.unloadFiles(Arrays.asList(lf));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testCoSliceXY() throws Exception{
		initialiseControllers();
		
		//Load data and set up with three line plots
		
		FileControllerUtils.loadFile(fileController,file1.getAbsolutePath());
		
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		DataOptions dop1 = lf1.getDataOption("/entry/dataset2");
		
		IPlotMode[] plotModes = plotManager.getPlotModes(dop1);
		IPlotMode newMode = plotModes[0];
		assertTrue(newMode.supportsMultiple());
		
		plotManager.switchPlotMode(newMode, dop1);
		
		fileController.setFileSelected(lf1, true);
		fileController.setDataSelected(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		
		FileControllerUtils.loadFile(fileController,file2.getAbsolutePath());
		FileControllerUtils.loadFile(fileController,file3.getAbsolutePath());
		
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		LoadedFile lf3 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file3.getAbsolutePath())).findFirst().get();
		
		DataOptions dop2 = lf2.getDataOption("/entry/dataset2");
		DataOptions dop2a = lf3.getDataOption("/entry/dataset2a");
		DataOptions dop2aRemove = lf3.getDataOption("/entry/dataset2");
		plotManager.switchPlotMode(newMode, dop2a);
		
		fileController.setDataSelected(dop2aRemove,false);
		
		fileController.setFileSelected(lf2, true);
		fileController.setDataSelected(dop2, true);
		fileController.setFileSelected(lf3, true);
		fileController.setDataSelected(dop2a, true);
		
		plotManager.waitOnJob();
		
		assertEquals(3, plottingSystem.getTraces().size());
		
		
		IPlotMode m = dop1.getPlottableObject().getPlotMode();
		NDimensions nd1 = dop1.getPlottableObject().getNDimensions();
		NDimensions nd2 = dop2.getPlottableObject().getNDimensions();
		NDimensions nd2a = dop2a.getPlottableObject().getNDimensions();

		SliceND s1 = nd1.buildSliceND();
		SliceND s2 = nd2.buildSliceND();
		SliceND s2a = nd2a.buildSliceND();
		
		assertEquals(m, dop2.getPlottableObject().getPlotMode());
		assertEquals(m, dop2a.getPlottableObject().getPlotMode());
		
		assertArrayEquals(s1.getStart(), s2.getStart());
		assertArrayEquals(s1.getStop(), s2.getStop());
		
		assertArrayEquals(s1.getStart(), s2a.getStart());
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2a.getStop())));
		
		//change slice, make sure noting is co-slice
		nd1.setSlice(0, new Slice(1,2,1));
		
		plotManager.replotOnSlice(dop1);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());
		
		s1 = nd1.buildSliceND();
		s2 = nd2.buildSliceND();
		s2a = nd2a.buildSliceND();
		
		assertThat(s1.getStart(), IsNot.not(IsEqual.equalTo(s2.getStart())));
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2.getStop())));
		
		assertThat(s1.getStart(), IsNot.not(IsEqual.equalTo(s2a.getStart())));
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2a.getStop())));
		
		//enable co-slicing and slice
		plotManager.setCoSlicingEnabled(true);
		
		nd1.setSlice(0, new Slice(2,3,1));
		
		plotManager.replotOnSlice(dop1);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());
		
		s1 = nd1.buildSliceND();
		s2 = nd2.buildSliceND();
		s2a = nd2a.buildSliceND();
		
		assertArrayEquals(s1.getStart(), s2.getStart());
		assertArrayEquals(s1.getStop(), s2.getStop());
		
		assertThat(s1.getStart(), IsNot.not(IsEqual.equalTo(s2a.getStart())));
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2a.getStop())));
		
		//disable co-slicing and slice
		plotManager.setCoSlicingEnabled(false);
		
		nd1.setSlice(0, new Slice(3,4,1));
		
		plotManager.replotOnSlice(dop1);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());

		s1 = nd1.buildSliceND();
		s2 = nd2.buildSliceND();
		s2a = nd2a.buildSliceND();
		
		assertThat(s1.getStart(), IsNot.not(IsEqual.equalTo(s2.getStart())));
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2.getStop())));
		
		assertThat(s1.getStart(), IsNot.not(IsEqual.equalTo(s2a.getStart())));
		assertThat(s1.getStop(), IsNot.not(IsEqual.equalTo(s2a.getStop())));
		
		//clean up
		fileController.unloadFiles(Arrays.asList(lf1,lf2,lf3));
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	
	@Test
	public void testMultiFileImageSelectOnLoad() throws Exception{
		initialiseControllers();
		fileController.setOpenMode(OpenMode.SELECT);
		FileControllerUtils.loadFile(fileController,file1.getAbsolutePath());
		
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file1.getAbsolutePath())).findFirst().get();
		
		assertTrue(lf1.isSelected());
		DataOptions dop1 = lf1.getDataOption("/entry/dataset2");
		fileController.setDataSelected(dop1, true);
				
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		FileControllerUtils.loadFile(fileController,file2.getAbsolutePath());
		
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		assertTrue(lf2.isSelected());
		//even though is SELECT open mode, 2 images cannot be plotted so 1st file must deselect
		assertTrue(!lf1.isSelected());
	}
	
	
	@Test
	public void testPlotModeXYMultiSelectOnLoad() {
		initialiseControllers();
		fileController.setOpenMode(OpenMode.SELECT);
		setUpAndSelectFirstFile1D();
		
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		FileControllerUtils.loadFile(fileController,file2.getAbsolutePath());
		
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		
		LoadedFile lf1 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file.getAbsolutePath())).findFirst().get();
		LoadedFile lf2 = fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().equals(file2.getAbsolutePath())).findFirst().get();
		
		assertTrue(lf1.isSelected());
		assertTrue(lf2.isSelected());
		
	}
	
	
}
	
