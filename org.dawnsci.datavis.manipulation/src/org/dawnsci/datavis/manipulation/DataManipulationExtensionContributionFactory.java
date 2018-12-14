package org.dawnsci.datavis.manipulation;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.api.utils.XYDataImpl;
import org.dawnsci.datavis.model.FileControllerUtils;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
//import org.dawnsci.datavis.api.IDataPackage;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import uk.ac.diamond.scisoft.analysis.dataset.function.Interpolation1D;

public class DataManipulationExtensionContributionFactory extends ExtensionContributionFactory {

	public DataManipulationExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

		MenuManager search = new MenuManager("XY Data",
				"org.dawnsci.datavis.tools.data");

		search.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				search.removeAll();

				List<IDataFilePackage> data = getData();

				Action a = new Action("Concatenate"){
					@Override
					public void run() {

						IDataset comb = buildCombined();

						if (comb != null ) {
							CombineDialog c = new CombineDialog(Display.getDefault().getActiveShell(), comb);
							c.open();
						} else {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "No valid data.", "No valid data was found to concatenate.\n(1D slices are currently not supported).");
						}
					}
				};

				if (data == null || data.isEmpty()) {
					a.setEnabled(false);
				}

				Action average = new Action("Average"){
					@Override
					public void run() {

						IDataset comb = buildCombined();

						if (comb == null) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "No valid data.", "No valid data was found to average.\n(1D slices are currently not supported).");
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

						FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
						String[] exts = new String[] {".dat",".xye",".nxs",".h5"};
						fd.setFilterExtensions(exts);
						fd.setFileName(name);

						BundleContext bundleContext =
								FrameworkUtil.
								getBundle(this.getClass()).
								getBundleContext();

						IRecentPlaces recentPlaces = (IRecentPlaces)bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));

						fd.setFilterPath(recentPlaces.getRecentDirectories().get(0));

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

							INexusFileFactory fileFactory = (INexusFileFactory)bundleContext.getService(bundleContext.getServiceReference(INexusFileFactory.class));

							success = FileWritingUtils.writeNexus(open, fileFactory, mean);
							
						}
						else {
							success = FileWritingUtils.writeText(open, mean);
						}
						
						if (success) {
							IFileController fc = (IFileController)bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
							FileControllerUtils.loadFile(fc,open);
						}
						
					}
				};
				
				
				
				Action sub = new Action("Subtract"){
					@Override
					public void run() {

						List<IXYData> xy = getCompatibleXY();
						
						ListDialog d = new ListDialog(Display.getDefault().getActiveShell());
						d.setTitle("Select file to subtract");
						d.setContentProvider(new ArrayContentProvider());
						d.setLabelProvider(new LabelProvider());

						if (xy == null) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "No valid data.", "No valid data was found to subtract.\n(1D slices are currently not supported).");
							return;
						}
						
						d.setInput(xy.toArray());
						
						if (Dialog.OK != d.open()) {
							return;
						}
						
						if (d.getResult().length == 0) return;
						
						BundleContext bundleContext =
								FrameworkUtil.
								getBundle(this.getClass()).
								getBundleContext();

						IRecentPlaces recentPlaces = (IRecentPlaces)bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));

						DirectoryDialog dirDi = new DirectoryDialog(Display.getDefault().getActiveShell());
						dirDi.setFilterPath(recentPlaces.getRecentDirectories().get(0));
						
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
							IFileController fc = (IFileController)bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
							FileControllerUtils.loadFiles(fc,paths.toArray(new String[paths.size()]), null);
						}
						
					}
				};
				
				
				Action xmcd = new Action("XMCD"){
					@Override
					public void run() {

						List<IXYData> data = getCompatibleXY();
						
						if (data != null && data.size() == 2) {
							RegionNormalisedDifferenceDialog d = new RegionNormalisedDifferenceDialog(Display.getDefault().getActiveShell(), data.get(0), data.get(1));
							if (d.open() == Dialog.OK) {
								BundleContext bundleContext =
										FrameworkUtil.
										getBundle(this.getClass()).
										getBundleContext();
								IRecentPlaces recentPlaces = bundleContext.getService(bundleContext.getServiceReference(IRecentPlaces.class));
								FileDialog f = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
								f.setFilterPath(recentPlaces.getCurrentDefaultDirectory());
								f.setFileName(d.getTemplateName() + ".dat");
								String open = f.open();
								
								if (open != null && FileWritingUtils.writeText(open, d.getData())) {
									IFileController fc = bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
									FileControllerUtils.loadFile(fc,open);
								}
							}
						} else {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", "XMCD requires two files to be selected in the table");
						}
						
					}
				};
				
				
				if (data == null || data.isEmpty()) {
					a.setEnabled(false);
					average.setEnabled(false);
					sub.setEnabled(false);
					xmcd.setEnabled(false);
				}
				

				search.add(a);
				search.add(average);
				search.add(sub);
				search.add(xmcd);

			}


		});

		search.add(new Action("") {

			@Override
			public void run() {

			}
		});

		additions.addContributionItem(search, new Expression() {
			
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				Object variable = context.getVariable("activeWorkbenchWindow.activePerspective");
				//probably shouldn't be needed, but doesn't work without it.
				search.setVisible(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
				return EvaluationResult.valueOf(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
			}
		});
	}


	private List<IDataFilePackage> getData(){
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(DataVisConstants.FILE_PART_ID);
		return getSuitableData(selection);
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

	private List<IXYData> getCompatibleXY(){
		List<IDataFilePackage> suitableData = getData();
		List<IXYData> xyData = DataPackageUtils.getXYData(suitableData, false);
		if (xyData.isEmpty()) {
			return null;
		}

		return getCompatibleDatasets(xyData);
	}
	
	private IDataset buildCombined() {

		List<IXYData> compatibleXY = getCompatibleXY();
		
		if (compatibleXY == null) return null;

		return combine(compatibleXY);
	}

	private List<IDataFilePackage> getSuitableData(ISelection selection){

		if (selection instanceof StructuredSelection) {

			return Arrays.stream(((StructuredSelection)selection).toArray())
					.filter(IDataFilePackage.class::isInstance)
					.map(IDataFilePackage.class::cast).collect(Collectors.toList());

		}

		return Collections.emptyList();
	}

	public static List<IXYData> getCompatibleDatasets(List<IXYData> data){

		IDataset[] xall = new IDataset[data.size()];

		for (int i = 0; i < data.size(); i++) {
			xall[i] = data.get(i).getX();
		}

		boolean dataAndNull = false;
		boolean needsChecks = false;

		IDataset test = xall[0];

		for (int i = 1; i < xall.length; i++) {
			if (test == null && xall[i] != null) dataAndNull = true;
			if (test != null && xall[i] == null) dataAndNull = true;

			if (test != null && !test.equals(xall[i])) needsChecks = true;
		}

		//xdata and no xdata not supported
		if (dataAndNull) return null;

		if (test == null) {
			//TODO make sure yDatasets != null
			int size = data.get(0).getY().getSize();

			for (IXYData d : data) {
				if (d.getY().getSize() != size) return null;
			}

			return data;
		}

		if (!needsChecks) {
			return data;
		}

		int[] commonRange = checkXaxisHasCommonRange(xall);

		if (commonRange != null) { 
			//TODO slice to common range
		}

		double[] commonValues = checkXaxisHasCommonRangeForInterpolation(xall);

		if (commonValues == null) return null;

		List<IXYData> output = new ArrayList<IXYData>();

		int maxpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[1])-1;
		int minpos = ROISliceUtils.findPositionOfClosestValueInAxis(test, commonValues[0])+1;

		IDataset xnew =  test.getSlice(new int[] {minpos},new int[]{maxpos},null);
		xnew.setName(test.getName());

		IDataset y = data.get(0).getY().getSlice(new int[] {minpos},new int[]{maxpos},null);

		XYDataImpl d = new XYDataImpl(xnew, y, data.get(0).getFileName(), data.get(0).getDatasetName(), new SliceND(y.getShape()));

		output.add(d);

		for (int i = 1; i < data.size(); i++) {

			IDataset x = data.get(i).getX();
			IDataset y1 = data.get(i).getY();

			output.add(new XYDataImpl(xnew, Interpolation1D.splineInterpolation(x, y1, xnew),data.get(i).getFileName(),data.get(i).getDatasetName(), new SliceND(xnew.getShape())));
		}

		return output;
	}

	private static int[] checkXaxisHasCommonRange(IDataset[] xaxis) {
		return null;
	}

	private static double[] checkXaxisHasCommonRangeForInterpolation(IDataset[] xaxis) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (IDataset x : xaxis) {
			if (x.min().doubleValue() < min) min = x.min().doubleValue();
			if (x.max().doubleValue() > max) max = x.max().doubleValue();
		}

		if (min > max) return null;

		return new double[] {min, max};
	}

	private static IDataset combine(List<IXYData> list) {
		if (list == null || list.isEmpty()) return null;

		IDataset x0 = list.get(0).getX();

		IDataset[] all = new IDataset[list.size()];
		IDataset names = DatasetFactory.zeros(StringDataset.class, new int[] {list.size()});

		int count = 0;
		for (IXYData file : list) {

			names.set(file.getFileName() + "-" + file.getDatasetName(), count);
			IDataset ds1 = file.getY().getSliceView().squeeze();
			ds1.setShape(new int[]{1,ds1.getShape()[0]});
			all[count++] = ds1;

		}

		Dataset conc = DatasetUtils.concatenate(all, 0);

		conc.setName("Combination");

		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			md.setAxis(1, x0);
			md.addAxis(0, names);
			conc.setMetadata(md);
		} catch (MetadataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return conc;
	}
}
