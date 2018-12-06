package org.dawnsci.datavis.model;

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RunningAverage;

public class DataOptionsUtils {

	public static DataOptions buildView(DataOptions op) {
		return new DataOptionsSlice(op, op.getPlottableObject().getNDimensions().buildSliceND());
	}

	public static DataOptions average(DataOptions data, IMonitor monitor) {

		SliceViewIterator iterator = buildIterator(data);
		iterator.hasNext();

		try {

			IDataset dataset = iterator.next().getSlice();

			RunningAverage rav = new RunningAverage(dataset);

			while (iterator.hasNext()) {
				if (monitor != null) {
					monitor.worked(1);

					if (monitor.isCancelled()) {
						return null;
					}
				}

				dataset = iterator.next().getSlice();
				rav.update(dataset);
			}

			String name = "[average]";

			return buildNewDataOptions(data, rav.getCurrentAverage(), name);

		} catch (DatasetException e) {
			return null;
		}

	}

	public static DataOptions sum(DataOptions data, IMonitor monitor) {


		SliceViewIterator iterator = buildIterator(data);
		iterator.hasNext();

		try {

			Dataset dataset = DatasetUtils.cast(DoubleDataset.class, iterator.next().getSlice());

			while (iterator.hasNext()) {
				if (monitor != null) {
					monitor.worked(1);

					if (monitor.isCancelled()) {
						return null;
					}
				}

				IDataset  next= iterator.next().getSlice();
				dataset.iadd(next);
			}

			String name = "[sum]";

			return buildNewDataOptions(data, dataset, name);

		} catch (DatasetException e) {
			return null;
		}

	}

	public static int getNumberOfSlices(DataOptions data) {

		NDimensions nDimensions = data.getPlottableObject().getNDimensions();

		int [] dd = nDimensions.getDimensionsWithoutDescription();

		int nSlice = 1;

		for (int i = 0; i < dd.length; i++) {
			nSlice *= nDimensions.getSize(dd[i]);
		}

		return nSlice;
	}

	private static SliceViewIterator buildIterator(DataOptions data) {
		ILazyDataset lazyDataset = data.getLazyDataset().getSliceView();
		lazyDataset.clearMetadata(null);
		NDimensions nDimensions = data.getPlottableObject().getNDimensions();

		return new SliceViewIterator(lazyDataset, null, nDimensions.getDimensionsWithDescription());
	}

	private static DataOptionsDataset buildNewDataOptions(DataOptions input, Dataset output, String name) {
		NDimensions nDimensions = input.getPlottableObject().getNDimensions();
		NDimensions ndc = new NDimensions(nDimensions);
		ndc.updateShape(output.getShape());
		IPlotMode plotMode = input.getPlottableObject().getPlotMode();
		PlottableObject po = new PlottableObject(plotMode, ndc);
		DataOptionsDataset dop = new DataOptionsDataset(input.getName()+name, input.getParent(), output);
		dop.setPlottableObject(po);

		int[] dd = nDimensions.getDimensionsWithDescription();
		//data axes should be set in sum/average
		String[] ax = input.getPrimaryAxes();
		String[] inax = new String[nDimensions.getRank()];

		for (int i = 0; i < nDimensions.getRank(); i++) {
			ndc.setAxis(i, null);
		}

		for (int i = 0; i < dd.length; i++) {
			inax[dd[i]] = ax[dd[i]];
			ndc.setAxis(dd[i], ax[dd[i]]);
		}

		dop.setAxes(inax);

		return dop;
	}

}
