package org.dawnsci.commandserver.processing.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.Reshapeable;
import org.eclipse.january.metadata.Sliceable;
import org.eclipse.january.metadata.Transposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicAxesMetadataImpl implements AxesMetadata {

	@Transposable
	@Reshapeable(matchRank = true)
	@Sliceable List<ILazyDataset>[] allAxes;
	
	Map<Integer,int[]> dimensionMap;
	
	private final static Logger logger = LoggerFactory.getLogger(DynamicAxesMetadataImpl.class);

	public DynamicAxesMetadataImpl() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initialize(int rank) {
		allAxes = new List[rank];
		dimensionMap = new HashMap<Integer,int[]>();
	}

	@SuppressWarnings("unchecked")
	private DynamicAxesMetadataImpl(DynamicAxesMetadataImpl axesMetadataImpl) {
		int r = axesMetadataImpl.allAxes.length;
		allAxes = new List[r];
		dimensionMap = new HashMap<Integer,int[]>();
		for (int i = 0; i < r; i++) {
			List<ILazyDataset> ol = axesMetadataImpl.allAxes[i];
			if (ol == null)
				continue;
			List<ILazyDataset> list = new ArrayList<ILazyDataset>();
			for (ILazyDataset l : ol) {
				ILazyDataset lv = l == null ? null : l.getSliceView();
				list.add(lv);
				if (lv != null) {
					int ihc = System.identityHashCode(lv);
					int iho = System.identityHashCode(l);
					if (axesMetadataImpl.dimensionMap.containsKey(iho)) dimensionMap.put(ihc, axesMetadataImpl.dimensionMap.get(iho).clone());
				}
				
			}
			allAxes[i] = list;
		}
	}

	@Override
	public void setAxis(int axisDim, ILazyDataset... axisData) {
		ArrayList<ILazyDataset> axisList = new ArrayList<ILazyDataset>(0);
		for (int i = 0; i < axisData.length; i++) {
			axisList.add(sanitizeAxisData(axisData[i],axisDim));
		}
		allAxes[axisDim] = axisList;
	}

	@Override
	public ILazyDataset[] getAxes() {
		ILazyDataset[] result = new ILazyDataset[allAxes.length];
		for (int i = 0; i < result.length; i++) {
			ILazyDataset[] ax = getAxis(i);
			if (ax != null && ax.length > 0) result[i] = ax[0];
		}
		return result;
	}

	@Override
	public ILazyDataset[] getAxis(int axisDim) {
		if (allAxes[axisDim] == null)
			return null;
		return allAxes[axisDim].toArray(new ILazyDataset[0]);
	}

	@Override
	public AxesMetadata clone() {
		return new DynamicAxesMetadataImpl(this);
	}

	/**
	 * Add axis data to given dimension. This dataset must be one dimensional or match rank
	 * with the associating dataset
	 * @param axisDim dimension (n.b. this is zero-based)
	 * @param axisData dataset for axis
	 */
	public void addAxis(int axisDim, ILazyDataset axisData) {
		if (allAxes[axisDim] == null) {
			allAxes[axisDim] = new ArrayList<ILazyDataset>();
		}
		allAxes[axisDim].add(sanitizeAxisData(axisData,axisDim));
	}
	
	public void addAxis(int primary, ILazyDataset axisData, int... axisDim) {
		if (allAxes[primary] == null) {
			allAxes[primary] = new ArrayList<ILazyDataset>();
		}
		
		ILazyDataset lz = sanitizeAxisData(axisData,axisDim);
		allAxes[primary].add(lz);
		if (lz != null) dimensionMap.put(System.identityHashCode(lz), axisDim);
	}

	private ILazyDataset sanitizeAxisData(ILazyDataset axisData, int... axisDim) {
		// remove any axes metadata to prevent infinite recursion
		// and also check rank
		if (axisData == null) return null;
		
		//Temporary fix for upstream lib issues
		if (axisData instanceof IDynamicDataset && axisDim.length == axisData.getRank()) {
			int[] maxShape = ((IDynamicDataset)axisData).getMaxShape();
			if (maxShape[maxShape.length-1] == 1) {
				
				logger.debug("Rejecting {} as an axis", axisData.getName() == null ? "unknown" : axisData.getName());
				
				return null;
			}
		}
		
		if (axisDim.length == 1) {
			int ad = axisDim[0];
			ILazyDataset view = axisData.getSliceView();
			view.clearMetadata(AxesMetadata.class);
			int r = axisData.getRank(); 
			if (r != allAxes.length) {
				if (r > 1) {
					throw new IllegalArgumentException("Given axis dataset must be zero or one dimensional, or match rank");
				}
				int[] newShape = new int[allAxes.length];
				Arrays.fill(newShape, 1);
				newShape[ad] = axisData.getSize();
				view.setShape(newShape);
			}
			return view;
		} else if (allAxes.length == axisData.getRank()){
			return axisData;
		} else {
			ILazyDataset view = axisData.getSliceView();
			view.clearMetadata(AxesMetadata.class);
			int[] newShape = new int[allAxes.length];
			Arrays.fill(newShape, 1);
			for (int i = 0 ; i < axisDim.length; i++) newShape[axisDim[i]] = axisData.getShape()[i];
			view.setShape(newShape);
			return view;
		}
	}

	@Override
	public int[] refresh(int[] shape) {
		int[] maxShape = shape.clone();

		for (int i = 0 ; i < allAxes.length; i++) {
			List<ILazyDataset> axis = allAxes[i];
			if (axis == null) continue;
			for (int j = 0; j < axis.size(); j++) {
				ILazyDataset l = axis.get(j);
				if (l == null) continue;
				int iHashCode = System.identityHashCode(l);
				int[] dims = dimensionMap.get(iHashCode);

				if (l instanceof IDynamicDataset) {
					
					if (l.getSize() == 1) {
						l.setShape(new int[]{1});
					} else {
						l = l.squeezeEnds();
					}
					
					if (dims != null && dims.length != l.getRank()) {
						int[] tempShape = new int[dims.length];
						Arrays.fill(tempShape,1);
						int[] ss = l.getShape();
						for (int k = 0 ; k < tempShape.length && k < ss.length; k++) tempShape[k] = ss[k];
						
						l.setShape(tempShape);
					}
					
					try {
						((IDynamicDataset) l).refreshShape();	
						
					} catch (Exception e) {
						String name = l.getName();
						if (name == null) name = "unknown_dataset";
						logger.error("Could not propagate " + name, e);
						dimensionMap.remove(iHashCode);
						axis.set(j,null);
						continue;
					}
					
				}
				// need to look at rank of l;
				if (dims == null || dims.length == 1) {
					int k = l.getShape()[0];
					if (k < maxShape[i]) maxShape[i] = k;
					int[] newShape = shape.clone();
					Arrays.fill(newShape, 1);
					newShape[i] = k;
					logger.debug("For null or 1 dim {} has new shape {}",l.getName(),Arrays.toString(newShape));
					l.setShape(newShape);
					axis.set(j, l);
				} else {
					int[] newShape = shape.clone();
					Arrays.fill(newShape, 1);
					for (int k = 0 ; k < dims.length; k++) {
						int[] s = l.getShape();
						if (s[dims[k]] < maxShape[k]) maxShape[k] = s[dims[k]];
						newShape[k] = s[dims[k]];
					}
					l.setShape(newShape);
					axis.set(j, l);
				}
			}

		}
		return maxShape;
	}

}
