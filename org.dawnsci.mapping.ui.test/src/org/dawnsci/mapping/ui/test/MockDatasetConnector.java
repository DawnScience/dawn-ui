package org.dawnsci.mapping.ui.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.january.metadata.MetadataType;

public class MockDatasetConnector implements IDatasetConnector {
	
	private int[] maxShape;
	private int[][] shapeArrays;
	private boolean connected = false;
	ILazyDataset dataset = null;
	

	public MockDatasetConnector(int[] maxShape, int[][]shapeArrays) {
		this.maxShape = maxShape;
		this.shapeArrays = shapeArrays;
	}
	
	@Override
	public String getPath() {
		return null;
	}

	@Override
	public void setPath(String path) {
	}

	@Override
	public ILazyDataset getDataset() {
		if (!connected) throw new RuntimeException("Not connected");
		return dataset;
	}

	@Override
	public boolean resize(int... newShape) {
		return false;
	}

	@Override
	public int[] getMaxShape() {
		return maxShape;
	}

	@Override
	public void setMaxShape(int... maxShape) {
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
	}

	@Override
	public boolean refreshShape() {
		((IDynamicDataset)dataset).refreshShape();
		return false;
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
	public String getDatasetName() {
		return null;
	}

	@Override
	public void setDatasetName(String datasetName) {

	}

	@Override
	public void setWritingExpected(boolean expectWrite) {

	}

	@Override
	public boolean isWritingExpected() {
		return false;
	}

	@Override
	public String connect() throws DatasetException {
		connected = true;
		
		dataset = new DynamicRandomLazyDataset("random", Dataset.FLOAT64, shapeArrays, maxShape);
		
		return null;
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		return null;
	}

	@Override
	public void disconnect() throws DatasetException {
		connected = false;
	}
	
	private class DynamicRandomLazyDataset extends LazyDataset implements IDynamicDataset {

		private int[] maxShape;
		int count = 1;
		
		public DynamicRandomLazyDataset(String name, int dtype, int[][] shapes, int[] maxShape) {
			super(name, dtype, shapes[0], new ILazyLoader(){

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isFileReadable() {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
					return Random.rand(slice.getShape());
				}
				
			});
			
			this.maxShape = maxShape;
		}

		@Override
		public IDynamicDataset getDataset() {
			return this;
		}

		@Override
		public boolean resize(int... newShape) {
			return false;
		}

		@Override
		public int[] getMaxShape() {
			return maxShape;
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

			if (count < shapeArrays.length) {
				int[] s = shapeArrays[count++];
				size = ShapeUtils.calcLongSize(s);
				shape = s;
				oShape = s;
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

		

	}

}
