package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.january.model.ISliceChangeListener;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.model.SliceChangeEvent;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.ErrorMetadata;
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
	private String shortName;
	private LoadedFile parent;
	protected String[] axes;
	private ILazyDataset data;
	private AtomicBoolean selected = new AtomicBoolean(false);
	
	private NexusSignal nexusSignal = null;

	private PlottableObject plottableObject;
	private String label;

	private ISliceChangeListener listener;

	private String process;

	private Map<Class<?>, List<?>> derivedData;
	
	protected boolean fromFile = true;
	
	public DataOptions(String name, LoadedFile parent) {
		this(name, parent, null);
	}

	public DataOptions(String name, LoadedFile parent, NexusSignal signal) {
		this.name = name;
		this.parent = parent;
		this.nexusSignal = signal;
		
		if (nexusSignal != null) {
			setAxes(getPrimaryAxes());
		}

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

		derivedData = new HashMap<>();
	}

	public DataOptions(DataOptions toCopy) {
		this(toCopy.name, toCopy.parent);
		this.data = toCopy.data != null ? toCopy.data.getSliceView() : null;
		this.axes = toCopy.axes != null ? toCopy.axes.clone() : null;
		this.selected = new AtomicBoolean(toCopy.selected.get());
		this.plottableObject = toCopy.plottableObject != null ? new PlottableObject(toCopy.plottableObject.getPlotMode(),
				new NDimensions(toCopy.plottableObject.getNDimensions())) : null;
		this.label = toCopy.label;
		this.derivedData = new HashMap<>(toCopy.derivedData);
		this.nexusSignal = toCopy.nexusSignal != null ? new NexusSignal(toCopy.nexusSignal) : null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getProcess() {
		return process;
	}

	public String getFilePath(){
		return parent.getFilePath();
	}

	public Map<String, int[]> getAllPossibleAxes() {
		Map<String, int[]> map = new LinkedHashMap<>(parent.getDataShapes());
		map.remove(name);
		return map;
	}
	
	private Map<String, int[]> getAxesMaxShapes() {
		
		if (parent instanceof IRefreshable) {
			return ((IRefreshable) parent).getDataMaxShapes();
		}
		
		return null;
	}

	public String[] getPrimaryAxes(){
		
		if (nexusSignal == null) {
			return null;
		}
		
		return nexusSignal.getPrimaryAxes();
	}

	public LoadedFile getParent() {
		return parent;
	}

	public ILazyDataset getLazyDataset() {
		if (data == null || !Arrays.equals(data.getShape(), parent.getLazyDataset(name).getShape())) {
			ILazyDataset local = parent.getLazyDataset(name).getSliceView();
			includeAxesMetadata(local);
			
			if (nexusSignal != null && nexusSignal.getUncertainties() != null) {
				ILazyDataset lze = parent.getLazyDataset(nexusSignal.getUncertainties());
				if (lze != null) {
					try {
						ErrorMetadata emd = MetadataFactory.createMetadata(ErrorMetadata.class);
						emd.setError(lze);
						local.setMetadata(emd);
					} catch (MetadataException e) {
						logger.error("Could not create errors");
					}
				}
			}
			
			data = local;
		}
		return data;
	}

	protected void includeAxesMetadata(ILazyDataset local) {
		AxesMetadata ax;
		if (axes == null) return;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, axes.length);
			for (int i = 0; i < axes.length ; i++) {
				String axName = axes[i];
				ILazyDataset lzAxes = parent.getLazyDataset(axName);
				if (lzAxes == null) continue;
				lzAxes.setName(axName);
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
	
	public String[] getCurrentAxes() {
		return this.axes == null ? null : this.axes.clone();
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
	
	public boolean isFromFile() {
		return fromFile;
	}

	public NDimensions buildNDimensions() {
		NDimensions ndims = new NDimensions(getLazyDataset().getShape(), this);
		
		String[][] tagged = null;
		
		if (nexusSignal != null) {
			tagged = nexusSignal.getTaggedAxes();
		}
		
		ndims.setUpAxes(name, getAllPossibleAxes(), tagged, getAxesMaxShapes());
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

	/**
	 * @return null or array of dimensions which omit from iteration when slicing
	 */
	public int[] getOmitDimensions() {
		if (plottableObject == null) {
			return null;
		}
		return plottableObject.getPlotMode().getDataDimensions(plottableObject.getNDimensions().getOptions());
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public IDataset getLabelValue() {
		return parent.getLabelValue();
	}

	@Override
	public DataOptions clone() {
		return new DataOptions(this);
	}

	@Override
	public String toString() {
		return (shortName == null ? name : shortName) + (selected.get() ? "*" : "");
	}

	@Override
	public void addDerivedData(List<?> derived) {
		if (derived != null) {
			if (derived.isEmpty()) {
				logger.warn("List is empty");
			} else {
				Iterator<?> it = derived.iterator();
				Object obj = null;
				while (obj == null && it.hasNext()) {
					obj = it.next();
				}

				if (obj == null) {
					logger.warn("List contains only nulls");
					return;
				}

				List<?> copy = new ArrayList<>(derived);
				Class<? extends Object> cls = obj.getClass();
				Class<?> sc = cls.getSuperclass();
				// place in superclass or interface
				if (Object.class.equals(sc)) {
					Class<?>[] ifs = cls.getInterfaces();
					if (ifs.length > 0) {
						derivedData.put(ifs[0], copy);
					} else {
						derivedData.put(sc, copy);
					}
				} else if (sc != null) {
					derivedData.put(sc, copy);
				} else {
					derivedData.put(cls, copy);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getDerivedData(Class<T> clazz) {
		if (derivedData.isEmpty()) {
			return null;
		}
		if (derivedData.containsKey(clazz)) {
			return (List<T>) derivedData.get(clazz);
		}

		// find first class that extends or implements parameter 
		for (Class<?> c : derivedData.keySet()) {
			if (clazz.isAssignableFrom(c)) {
				return (List<T>) derivedData.get(c);
			}
		}

		return null;
	}

	/**
	 * Remove derived data
	 * @param clazz
	 */
	public void removeDerivedData(Class<?> clazz) {
		derivedData.remove(clazz);
	}
	
	public void setFilterAxes(boolean filter) {
		//only filter axes if there is a signal
		if (plottableObject != null && plottableObject.getNDimensions() != null && nexusSignal != null) {
			plottableObject.getNDimensions().setFilterAxes(filter);
		}
	}
}
