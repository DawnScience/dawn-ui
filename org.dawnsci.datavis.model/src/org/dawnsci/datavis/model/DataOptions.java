package org.dawnsci.datavis.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IDataPackage;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

public class DataOptions implements IDataObject, IDataPackage {

	private String name;
	private LoadedFile parent;
	private String[] axes;
	private ILazyDataset data;
	private boolean selected;

	private PlottableObject plottableObject;
	
	public DataOptions(String name, LoadedFile parent) {
		this.name = name;
		this.parent = parent;
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
		ILazyDataset local = parent.getLazyDataset(name);
		AxesMetadata am = local.getFirstMetadata(AxesMetadata.class);
		if (am == null) return null;
		if (am.getAxes() == null) return null;
		String[] ax = new String[am.getAxes().length];
		ILazyDataset[] axes = am.getAxes();
		int index = name.lastIndexOf(Node.SEPARATOR);
		if (index < 0) return null;
		String sub = name.substring(0, index);
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] != null) {
				String full =  sub + Node.SEPARATOR + axes[i].getName();
				ax[i] = parent.getDataShapes().containsKey(full) ? full : null;
			} else {
				ax[i] = null;
			}
		}
		return ax;
	}
	
	public ILazyDataset getLazyDataset() {
		if (data == null || !Arrays.equals(data.getShape(), parent.getLazyDataset(name).getShape())) {
			ILazyDataset local = parent.getLazyDataset(name).getSliceView();
			if (axes != null) {
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
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						} else {
							ax.setAxis(i, parent.getLazyDataset(axes[i]));
						}
						
					}
					local.setMetadata(ax);
				} catch (MetadataException e) {
					e.printStackTrace();
				}
			}
			data = local;
		}
		return data;
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
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public NDimensions buildNDimensions() {
		NDimensions ndims = new NDimensions(getLazyDataset().getShape());
		ndims.setUpAxes((String)null, getAllPossibleAxes(), getPrimaryAxes());
		return ndims;
	}
	
	public SliceND getSlice() {
		return plottableObject == null ? null : plottableObject.getNDimensions().buildSliceND();
	}
}
