package org.dawnsci.mapping.ui.test;

import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Random;

public class MockDatasetConnector implements IDatasetConnector {
	
	int[] maxShape;
	int[][] shapeArrays;
	int count = 0;


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
		return Random.lazyRand(shapeArrays[count]);
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
		if (count > shapeArrays.length-1) count++;
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
		return null;
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		return null;
	}

	@Override
	public void disconnect() throws DatasetException {
	}

}
