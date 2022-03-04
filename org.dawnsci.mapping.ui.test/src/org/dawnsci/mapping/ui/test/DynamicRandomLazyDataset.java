package org.dawnsci.mapping.ui.test;

import java.io.IOException;

import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDynamicDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;

public class DynamicRandomLazyDataset extends LazyDynamicDataset {

	private static final long serialVersionUID = 1L;
	private int[][] shapeArrays;
	ILazyDataset dataset = null;
	private int count = 0;
	boolean endNan = false;


	public DynamicRandomLazyDataset(int[][] shapes, int[] maxShape) {
		super(null, "random_dynamic", 1, DoubleDataset.class, shapes[0], maxShape, null);

		loader = new DynamicLazyLoader();
		shapeArrays = shapes;
	}

	DynamicRandomLazyDataset(DynamicRandomLazyDataset other) {
		super(other);

		shapeArrays = other.shapeArrays;
		dataset = other.dataset;
		count   = other.count;
		endNan  = other.endNan;
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
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
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
	}

	@Override
	public void removeDataListener(IDataListener l) {
	}

	@Override
	public void fireDataListeners() {
	}

	@Override
	public DynamicRandomLazyDataset clone() {
		return new DynamicRandomLazyDataset(this);
	}

	private class DynamicLazyLoader implements ILazyLoader {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isFileReadable() {
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
