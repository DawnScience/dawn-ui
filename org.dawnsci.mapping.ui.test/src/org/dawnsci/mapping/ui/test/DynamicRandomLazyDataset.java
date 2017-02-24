package org.dawnsci.mapping.ui.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.LazyDynamicDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.january.metadata.MetadataType;

public class DynamicRandomLazyDataset extends LazyDynamicDataset {

	private static final long serialVersionUID = 1L;
	private int[] maxShape;
	private int[][] shapeArrays;
	ILazyDataset dataset = null;
	private int count = 0;
	boolean endNan = false;


	public DynamicRandomLazyDataset(int[][] shapes, int[] maxShape) {
		super("random_dynamic", Dataset.FLOAT64, 1, shapes[0], maxShape, null);

		loader = new DynamicLazyLoader();
		shapeArrays = shapes;
		this.maxShape = maxShape;
	}
	
	public void setEndNan(boolean endNan) {
		this.endNan = endNan;
	}

	@Override
	public IDynamicDataset getDataset() {
		return this;
	}


	@Override
	public void setMaxShape(int... maxShape) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refreshShape() {

		if (count == 0){
			count++;
			return true;
		}

		if (count < shapeArrays.length) {
			int[] s = shapeArrays[count++];
			size = ShapeUtils.calcLongSize(s);
			shape = s.clone();
			oShape = shape;
			oShape.toString();
		}

		return true;
	}

	@Override
	public void addDataListener(IDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDataListener(IDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fireDataListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public DynamicRandomLazyDataset clone() {
		DynamicRandomLazyDataset ret = new DynamicRandomLazyDataset(shapeArrays, maxShape);
		ret.loader = this.loader;
		ret.oShape = oShape;
		ret.shape = shape;
		ret.size = size;
		ret.endNan = endNan;
		ret.prepShape = prepShape;
		ret.postShape = postShape;
		ret.begSlice = begSlice;
		ret.delSlice = delSlice;
		ret.map = map;
		ret.base = base;
		ret.metadata = copyMetadata();
		ret.oMetadata = oMetadata;
		ret.name = this.name;
		return ret;
	}

	private class DynamicLazyLoader implements ILazyLoader {

		private static final long serialVersionUID = 1L;


		@Override
		public boolean isFileReadable() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
			if (shape.length == 1) {
				DoubleDataset s = DatasetFactory.createRange(slice.getShape()[0]);
				if (endNan && slice.isAll()) s.set(Double.NaN, s.getSize()-1);
				return s;
			}
			return Random.rand(shape).getSlice(slice);
		}

	}



}

