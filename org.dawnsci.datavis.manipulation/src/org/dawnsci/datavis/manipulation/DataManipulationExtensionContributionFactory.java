package org.dawnsci.datavis.manipulation;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.api.utils.XYDataImpl;
import org.dawnsci.datavis.model.IFileController;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
//import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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

						CombineDialog c = new CombineDialog(Display.getDefault().getActiveShell(), comb);
						c.open();
					}
				};

				if (data == null || data.isEmpty()) {
					a.setEnabled(false);
				}

				Action average = new Action("Average"){
					@Override
					public void run() {

						IDataset comb = buildCombined();

						Dataset d = DatasetUtils.convertToDataset(comb);
						Dataset mean = d.mean(0, true);
						mean.squeeze();

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

						File f;
						try {
							f = File.createTempFile(name, ".nxs");
						} catch (IOException e) {
							
							return;
						}
						
						BundleContext bundleContext =
				                FrameworkUtil.
				                getBundle(this.getClass()).
				                getBundleContext();
						
						INexusFileFactory fileFactory = (INexusFileFactory)bundleContext.getService(bundleContext.getServiceReference(INexusFileFactory.class));
						

						try (NexusFile nexus = fileFactory.newNexusFile(f.getAbsolutePath())) {
							nexus.openToWrite(true);
							mean.setName(NexusConstants.DATA_DATA);
							GroupNode nxdata = nexus.getGroup("/entry/average", true);
							nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
							GroupNode nxentry = nexus.getGroup("/entry", true);
							nexus.addAttribute(nxentry,TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
							nexus.createData(nxdata, mean);
							nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, NexusConstants.DATA_DATA));

							AxesMetadata md = d.getFirstMetadata(AxesMetadata.class);

							ILazyDataset[] axes = md.getAxes();

							String axName = null;
							
							if (axes[1] != null) {
								IDataset y = axes[1].getSlice();
								y = y.squeeze();
								if (y.getName() != null) {
									axName = MetadataPlotUtils.removeSquareBrackets(y.getName());
									y.setName(axName);
								} else {
									axName = "y_axis";
								}
								nexus.createData(nxdata, y);
								nexus.addAttribute(nxdata, TreeFactory.createAttribute(axName + NexusConstants.DATA_INDICES_SUFFIX, 0));
							}

							nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.DATA_AXES, axName));

						} catch (NexusException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DatasetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						IFileController fc = (IFileController)bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
						
						fc.loadFile(f.getAbsolutePath());
					}
				};
				

				if (data == null || data.isEmpty()) {
					a.setEnabled(false);
					average.setEnabled(false);
				}

				search.add(a);
				search.add(average);


			}


		});

		search.add(new Action("") {

			@Override
			public void run() {

			}
		});

		additions.addContributionItem(search, null);

	}


	private List<IDataFilePackage> getData(){
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection("org.dawnsci.datavis.view.parts.LoadedFilePart");
		return getSuitableData(selection);
	}


	private IDataset buildCombined() {

		List<IDataFilePackage> suitableData = getData();
		List<IXYData> xyData = DataPackageUtils.getXYData(suitableData, false);
		if (xyData.isEmpty()) {
			return null;
		}

		xyData = getCompatibleDatasets(xyData);

		return combine(xyData);
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
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;

		for (IDataset x : xaxis) {
			if (x.min().doubleValue() > min) min = x.min().doubleValue();
			if (x.max().doubleValue() < max) max = x.max().doubleValue();
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
