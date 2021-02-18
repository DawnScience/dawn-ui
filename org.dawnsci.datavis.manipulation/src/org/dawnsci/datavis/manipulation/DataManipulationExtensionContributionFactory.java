package org.dawnsci.datavis.manipulation;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.manipulation.componentfit.ComponentFitDialog;
import org.dawnsci.datavis.manipulation.componentfit.ComponentFitModel;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import uk.ac.diamond.scisoft.analysis.utils.ReflMergeUtils;

public class DataManipulationExtensionContributionFactory extends ExtensionContributionFactory {

	public DataManipulationExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

		MenuManager xyTools = new MenuManager("XY Data", "org.dawnsci.datavis.tools.data");

		xyTools.addMenuListener(new IMenuListener() {

			private BundleContext bundleContext;

			private <T> T getService(Class<T> clazz) {
				return (T) bundleContext.getService(bundleContext.getServiceReference(clazz));
			}

			private String getLastRecentDirectory() {
				IRecentPlaces places = getService(IRecentPlaces.class);
				return places.getRecentDirectories().get(0);
			}

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				xyTools.removeAll();

				List<IDataFilePackage> data = getData();
				bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
				Shell shell = Display.getDefault().getActiveShell();

				Action a = new Action("Concatenate"){
					@Override
					public void run() {

						IDataset comb = buildCombined(data);

						if (comb != null ) {
							CombineDialog c = new CombineDialog(shell, comb);
							c.open();
						} else {
							MessageDialog.openError(shell, "No valid data.", "No valid data was found to concatenate.\n(1D slices are currently not supported).");
						}
					}
				};

				Action average = new Action("Average"){
					@Override
					public void run() {

						IDataset comb = buildCombined(data);

						if (comb == null) {
							MessageDialog.openError(shell, "No valid data.", "No valid data was found to average.\n(1D slices are currently not supported).");
							return;
						}
						
						Dataset d = DatasetUtils.convertToDataset(comb);
						Dataset mean = d.mean(0, true);
						mean.squeeze();
						mean.setName("average");
						Dataset n;
						try {
							n = DatasetUtils.sliceAndConvertLazyDataset(d.getFirstMetadata(AxesMetadata.class).getAxes()[0]);
						} catch (DatasetException e1) {
							return;
						}

						String name = "average";

						if (n instanceof StringDataset) {

							StringBuilder builder = new StringBuilder(name);
							builder.append("_");
							builder.append(new File(n.getStringAbs(0)).getName());
							builder.append("_");
							builder.append(new File(n.getStringAbs(n.getSize()-1)).getName());

							name = builder.toString();
						}
						
						transferAxisOne(d,mean);

						FileDialog fd = new FileDialog(shell, SWT.SAVE);
						String[] exts = new String[] {".dat",".xye",".nxs",".h5"};
						fd.setFilterExtensions(exts);
						fd.setFileName(name);
						fd.setFilterPath(getLastRecentDirectory());

						String open = fd.open();

						if (open == null) return;

						int filterIndex = fd.getFilterIndex();

						if (filterIndex == -1) {
							filterIndex = 0;
						}

						String ext = exts[filterIndex];

						if (!open.endsWith(ext)) {
							open = open + ext;
						}
						
						boolean success = false;

						if (ext.equals(exts[2]) || ext.equals(exts[3])) {

							INexusFileFactory fileFactory = getService(INexusFileFactory.class);

							success = FileWritingUtils.writeNexus(open, fileFactory, mean);
							
						}
						else {
							success = FileWritingUtils.writeText(open, mean);
						}
						
						if (success) {
							IFileController fc = getService(IFileController.class);
							FileControllerUtils.loadFile(fc,open);
						}
						
					}
				};

				Action sub = new Action("Subtract"){
					@Override
					public void run() {

						List<IXYData> xy = getCompatibleXY(data);
						
						ListDialog d = new ListDialog(shell);
						d.setTitle("Select file to subtract");
						d.setContentProvider(new ArrayContentProvider());
						d.setLabelProvider(new LabelProvider());

						if (xy == null) {
							MessageDialog.openError(shell, "No valid data.", "No valid data was found to subtract.\n(1D slices are currently not supported).");
							return;
						}
						
						d.setInput(xy.toArray());
						
						if (Dialog.OK != d.open()) {
							return;
						}
						
						if (d.getResult().length == 0) return;
						

						DirectoryDialog dirDi = new DirectoryDialog(shell);
						dirDi.setFilterPath(getLastRecentDirectory());
						
						String output = dirDi.open();
						
						if (output == null) {
							return;
						}
						
						IXYData subtrahend = (IXYData)d.getResult()[0];
						AxesMetadata axes = null;
						try {
							axes = MetadataFactory.createMetadata(AxesMetadata.class, 1);
							axes.setAxis(0, subtrahend.getX().getSlice());
						} catch (MetadataException e) {
						}
						
						List<String> paths = new ArrayList<>();
						
						for (IXYData ixy : xy) {
							
							String outputfile = output + File.separator + new File(ixy.getFileName()).getName() + "-" + new File(subtrahend.getFileName()).getName() + ".dat";
							
							if (ixy == subtrahend) {
								continue;
							}
							
							Dataset dif = Maths.subtract(ixy.getY(),subtrahend.getY());
							dif.setMetadata(axes.clone());
							
							if (FileWritingUtils.writeText(outputfile, dif)) {
								paths.add(outputfile);
							}
						}
						
						if (!paths.isEmpty()) {
							IFileController fc = getService(IFileController.class);
							FileControllerUtils.loadFiles(fc,paths.toArray(new String[paths.size()]), null);
						}
						
					}
				};
				
				
				Action xmcd = new Action("XMCD"){
					@Override
					public void run() {

						List<IXYData> xy = getCompatibleXY(data);
						
						if (xy != null && xy.size() == 2) {
							RegionNormalisedDifferenceDialog d = new RegionNormalisedDifferenceDialog(shell, xy.get(0), xy.get(1));
							if (d.open() == Dialog.OK) {
								FileDialog f = new FileDialog(shell, SWT.SAVE);
								f.setFilterPath(getLastRecentDirectory());
								f.setFileName(d.getTemplateName() + ".dat");
								String open = f.open();
								
								if (open != null && FileWritingUtils.writeText(open, d.getData())) {
									IFileController fc = getService(IFileController.class);
									FileControllerUtils.loadFile(fc,open);
								}
							}
						} else {
							MessageDialog.openError(shell, "Error", "XMCD requires two files to be selected in the table");
						}
						
					}
				};
				
				Action stitch = new Action("Stitch") {
					@Override
					public void run() {
						
						// Takes the list of IDataFilePackages, extracts the data and stores 
						// it as a nested array
						
						Dataset[] dataArray = new Dataset[data.size()];
						
						int counter = 0;
						String firstFile = "";
						String finalFile = "";
						// Loop through the files that are checked/highlighted
						ListIterator<IDataFilePackage> it = data.listIterator();
						while (it.hasNext()) {
							IDataPackage[] dataPacks = it.next().getDataPackages();
							
							if (counter == 0) {
								firstFile = new File(data.get(counter).getFilePath()).getName();
							}
							finalFile = new File(data.get(counter).getFilePath()).getName();
							// Extract the y-axis from the selected IDataPackage
							Dataset yTemp = null;
							for (IDataPackage i : dataPacks) {
								if (i.isSelected()) {
									try {
										yTemp = DatasetUtils.sliceAndConvertLazyDataset(i.getLazyDataset());
										break;
									} catch (DatasetException e) {
										MessageDialog.openError(shell, "Error", "There has been an error opening the selected dataset.");
									}
								}
							}
							if (yTemp == null) {
								continue;
							}
							dataArray[counter] = yTemp;
							counter += 1;
						}
						
						Dataset[] attenuationCorrected;
						Dataset joinedData; 
						Dataset normalisedToOne;
						Dataset pointsMerged = null;
						try {
							attenuationCorrected = ReflMergeUtils.correctAttenuation(dataArray);
							joinedData = ReflMergeUtils.concatenate(attenuationCorrected);
							normalisedToOne = ReflMergeUtils.normaliseTER(joinedData);
							pointsMerged = ReflMergeUtils.rebinDatapoints(normalisedToOne);
						} catch (DatasetException e) {
							MessageDialog.openError(shell, "Error", "There has been an error in the dataset concatenation.");
						}
						
						writeToFile(pointsMerged, firstFile, finalFile);
					}
					
					private void writeToFile(Dataset dataToWrite, String firstFile, String finalFile) {
						String name = "stitched";
						
						StringBuilder builder = new StringBuilder(name);
						if (firstFile.contains(".") && finalFile.contains(".")) {
							builder.append("_");
							builder.append(firstFile.substring(0, firstFile.lastIndexOf(".")));
							builder.append("_");
							builder.append(finalFile.substring(0, finalFile.lastIndexOf(".")));	
						}
						name = builder.toString();
						
						FileDialog fd = new FileDialog(shell, SWT.SAVE);
						String[] exts = new String[] {".dat"};
						fd.setFilterExtensions(exts);
						fd.setFileName(name);
						fd.setFilterPath(getLastRecentDirectory());

						String open = fd.open();
						int filterIndex = fd.getFilterIndex();

						if (filterIndex == -1) {
							filterIndex = 0;
						}

						String ext = exts[filterIndex];

						if (!open.endsWith(ext)) {
							open = open + ext;
						}

						boolean success = FileWritingUtils.writeDat(open, dataToWrite);
						
						if (success) {
							IFileController fc = getService(IFileController.class);
							FileControllerUtils.loadFile(fc,open);
						} else {
							MessageDialog.openError(shell, "Error", "It has not been possible to write the data file.");
						}
					}
				};
				
				stitch.setToolTipText("Stitch together selected dataset, and normalise the total external reflection to 1.");

				if (data == null || data.isEmpty()) {
					a.setEnabled(false);
					average.setEnabled(false);
					sub.setEnabled(false);
					xmcd.setEnabled(false);
					stitch.setEnabled(false);
				}

				Action componentFit = new Action("Component Fit"){
					@Override
					public void run() {

						try {
							IFileController fc = getService(IFileController.class);
							ComponentFitModel model = getComponentFit(fc);
							ComponentFitDialog d = new ComponentFitDialog(shell, model);
							d.open();
							
						} catch (IllegalArgumentException | DatasetException e) {
							MessageDialog.openError(shell, "Error", "Could not initialise data for component fit: " + e.getMessage());
						}
						
					}
				};

				xyTools.add(a);
				xyTools.add(average);
				xyTools.add(sub);
				xyTools.add(xmcd);
				xyTools.add(stitch);
				xyTools.add(componentFit);
			}
		});

		xyTools.add(new Action("") {
			@Override
			public void run() {
			}
		});

		additions.addContributionItem(xyTools, new Expression() {
			
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				Object variable = context.getVariable("activeWorkbenchWindow.activePerspective");
				//probably shouldn't be needed, but doesn't work without it.
				xyTools.setVisible(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
				return EvaluationResult.valueOf(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
			}
		});
	}

	private List<IDataFilePackage> getData() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(DataVisConstants.FILE_PART_ID);
		return getSuitableData(selection);
	}
	
	private ComponentFitModel getComponentFit(IFileController fc) throws IllegalArgumentException, DatasetException{
		List<DataOptions> dataOptions = fc.getImmutableFileState();
		
		return ComponentFitModel.buildModel(dataOptions);
	}

	private List<IDataFilePackage> getSuitableData(ISelection selection){

		if (selection instanceof StructuredSelection) {

			return Arrays.stream(((StructuredSelection)selection).toArray())
					.filter(IDataFilePackage.class::isInstance)
					.map(IDataFilePackage.class::cast).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}


	private List<IXYData> getCompatibleXY(List<IDataFilePackage> suitableData){
		List<IXYData> xyData = DataPackageUtils.getXYData(suitableData, false);
		if (xyData.isEmpty()) {
			return null;
		}

		return DataManipulationUtils.getCompatibleDatasets(xyData, null, null);
	}
	
	private IDataset buildCombined(List<IDataFilePackage> suitableData) {

		List<IXYData> compatibleXY = getCompatibleXY(suitableData);
		
		if (compatibleXY == null) return null;

		return DataManipulationUtils.combine(compatibleXY);
	}
	
	private void transferAxisOne(Dataset a, Dataset b) {
		AxesMetadata md = a.getFirstMetadata(AxesMetadata.class);
		if (md == null) return;

		ILazyDataset[] axes = md.getAxes();

		if (axes[1] == null) return;

		try {
			IDataset y = axes[1].getSlice();
			y = y.squeeze();
			if (y.getName() != null) {
				String axName = MetadataPlotUtils.removeSquareBrackets(y.getName());
				y.setName(axName);
			} else {
				y.setName("y_axis");
			}
			AxesMetadata m = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			m.setAxis(0, y);
			b.setMetadata(m);
		} catch (Exception e){
			//TODO log
		}

		return;
	}

	
}
