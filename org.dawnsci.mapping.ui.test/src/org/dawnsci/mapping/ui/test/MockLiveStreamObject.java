package org.dawnsci.mapping.ui.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;

public class MockLiveStreamObject implements LiveStreamMapObject {

	private boolean plotted;
	private Set<IAxisMoveListener> listeners = new HashSet<>();
	private IDynamicShape dynamicDataset;

	@Override
	public String getLongName() {
		return "Mock";
	}

	@Override
	public IDataset getMap() {
		return null;
	}

	@Override
	public boolean isLive() {
		return false;
	}

	@Override
	public void update() {

	}

	@Override
	public int getTransparency() {
		return 0;
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		return null;
	}

	@Override
	public String getPath() {
		return "Mock";
	}

	@Override
	public boolean isPlotted() {
		return plotted;
	}

	@Override
	public void setPlotted(boolean plot) {
		plotted = plot;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}

	@Override
	public double[] getRange() {
		//TODO implement
		return null;
	}

	@Override
	public void addAxisListener(IAxisMoveListener listener) {
		listeners.add(listener);

	}

	@Override
	public void removeAxisListener(IAxisMoveListener listener) {
		listeners.remove(listener);

	}

	@Override
	public List<IDataset> getAxes() {
		// TODO implement
		return null;
	}

	@Override
	public void disconnect() throws Exception {

	}

	@Override
	public void setColorRange(double[] range) {

	}

	@Override
	public double[] getColorRange() {
		return null;
	}

	@Override
	public void connect() throws Exception {
		int[] maxShape = {100,100};
		
		dynamicDataset = new DynamicRandomLazyDataset(new int[][] {maxShape}, maxShape);
		
	}

	@Override
	public IDynamicShape getDynamicDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransparency(int transparency) {
		// TODO Auto-generated method stub
		
	}

}
