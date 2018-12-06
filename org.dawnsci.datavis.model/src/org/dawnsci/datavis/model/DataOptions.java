package org.dawnsci.datavis.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.january.model.ISliceChangeListener;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.model.SliceChangeEvent;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a dataset in contained in a LoadedFile
 * in the IFileController
 */
public class DataOptions implements IDataObject, IDataPackage {

	private static final Logger logger = LoggerFactory.getLogger(DataOptions.class);

	private String name;
	private LoadedFile parent;
	protected String[] axes;
	private ILazyDataset data;
	private AtomicBoolean selected = new AtomicBoolean(false);

	private PlottableObject plottableObject;
	private String label;

	private ISliceChangeListener listener;

	public DataOptions(String name, LoadedFile parent) {
		this.name = name;
		this.parent = parent;

		this.listener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				//do nothing
			}

			@Override
			public void optionsChanged(SliceChangeEvent event) {
				//do nothing
			}

			@Override
			public void axisChanged(SliceChangeEvent event) {
				setAxes(event.getAxesNames());
			}
		};
	}

	public DataOptions(DataOptions toCopy) {
		this(toCopy.name,toCopy.parent);
		this.data = toCopy.data != null ? toCopy.data.getSliceView() : null;
		this.axes = axes != null ? axes.clone() : null;
		this.selected = new AtomicBoolean(toCopy.selected.get());
		this.plottableObject = new PlottableObject(toCopy.plottableObject.getPlotMode(),
				new NDimensions(toCopy.plottableObject.getNDimensions()));
		this.label = toCopy.label;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getFilePath(){
		return parent.getFilePath();
	}

	public Map<String, int[]> getAllPossibleAxes() {
		Map<String, int[]> map = new HashMap<>(parent.getDataShapes());
		map.remove(name);
		return map;
	}

	public String[] getPrimaryAxes(){
		ILazyDataset local = getLazyDataset();
		AxesMetadata am = local.getFirstMetadata(AxesMetadata.class);
		if (am == null) return null;
		if (am.getAxes() == null) return null;
		String[] ax = new String[am.getAxes().length];
		ILazyDataset[] axes = am.getAxes();
		// check for Nexus style name
		int index = name.lastIndexOf(Node.SEPARATOR);
		String sub = index < 0 ? "" : name.substring(0, index + 1);
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] != null) {
				String full = null;
				if (axes[i].getName().startsWith(Node.SEPARATOR)) {
					full = axes[i].getName();
				} else {
					full =  sub + axes[i].getName();
				}
				ax[i] = parent.getDataShapes().containsKey(full) ? full : null;
			} else {
				ax[i] = null;
			}
		}
		return ax;
	}

	public LoadedFile getParent() {
		return parent;
	}

	public ILazyDataset getLazyDataset() {
		if (data == null || !Arrays.equals(data.getShape(), parent.getLazyDataset(name).getShape())) {
			ILazyDataset local = parent.getLazyDataset(name).getSliceView();
			if (axes != null) {
				includeAxesMetadata(local);
			}
			data = local;
		}
		return data;
	}

	protected void includeAxesMetadata(ILazyDataset local) {
		AxesMetadata ax;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, axes.length);
			for (int i = 0; i < axes.length ; i++) {
				ILazyDataset lzAxes = parent.getLazyDataset(axes[i]);
				if (lzAxes == null) continue;
				if (!(lzAxes.getRank() ==1 || lzAxes.getRank() == local.getRank())) {
					int rank = local.getRank();
					int[] shape = local.getShape();
					int[] axShape = lzAxes.getShape();
					int axRank = lzAxes.getRank();
					int[] newShape = new int[local.getRank()];
					Arrays.fill(newShape, 1);

					int[] idx = new int[axRank];
					Arrays.fill(idx, -1);
					Boolean[] found = new Boolean[axRank];
					Arrays.fill(found, false);
					int max = rank;

					for (int j = axRank-1; j >= 0; j--) {
						int id = axShape[j];
						updateShape(j, max, shape, id, idx, found);

					}

					boolean allFound = !Arrays.asList(found).contains(false);
					//has it only not found size 1 dims?
					if (!allFound) {
						boolean allOnes = true;
						for (int j = 0; j < found.length; j++) {
							if (!found[j] && axShape[j] != 1) {
								allOnes = false;
								break;
							} else if (!found[j] && axShape[j] == 1) {
								idx[j] = j;
							}

						}

						allFound = allOnes;
					}

					if (!allFound) {
						continue;
					}

					for (int j = 0; j < axRank; j++) {
						newShape[idx[j]] = axShape[j];
					}

					try {
						lzAxes = lzAxes.getSliceView();
						lzAxes.setShape(newShape);
						ax.setAxis(i, lzAxes);
					} catch (Exception e) {
						logger.error("Error setting axes", e);
					}

				} else {
					ILazyDataset l = parent.getLazyDataset(axes[i]);
					ax.setAxis(i,l);
				}

			}
			local.setMetadata(ax);
		} catch (MetadataException e) {
			logger.error("Error creating axes metadata", e);
		}
	}

	private boolean updateShape(int i, int max, int[] shape, int id, int[] idx, Boolean[] found){

		int[] idxc = idx.clone();
		Arrays.sort(idxc);

		for (int j = max -1 ; j >= 0; j--) {

			if (id == shape[j] && Arrays.binarySearch(idxc, j) < 0) {
				idx[i] = j;
				found[i] = true;
				max = j;
				return true;
			}

		}

		return false;
	}

	public void setAxes(String[] axesNames) {
		data = null;
		this.axes = axesNames;
	}

	public PlottableObject getPlottableObject() {
		return plottableObject;
	}

	public void setPlottableObject(PlottableObject plottableObject) {
		this.plottableObject = plottableObject;
	}

	public boolean isSelected() {
		return selected.get();
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public NDimensions buildNDimensions() {
		NDimensions ndims = new NDimensions(getLazyDataset().getShape(), this);
		ndims.setUpAxes((String)null, getAllPossibleAxes(), getPrimaryAxes());
		if (axes != null) {
			for (int i = 0 ; i < axes.length; i++){
				ndims.setAxis(i, axes[i]);
			}
		}
		ndims.addSliceListener(listener);
		return ndims;
	}

	public SliceND getSlice() {
		return plottableObject == null ? null : plottableObject.getNDimensions().buildSliceND();
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public DataOptions clone() {
		return new DataOptions(this);
	}
}
