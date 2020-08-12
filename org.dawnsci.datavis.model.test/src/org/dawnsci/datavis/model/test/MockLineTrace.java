package org.dawnsci.datavis.model.test;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Color;

public class MockLineTrace implements ILineTrace {
	
	private String name;
	private Object userObject;
	private IDataset data;

	public MockLineTrace(String name) {
		this.name = name;
	}

	@Override
	public void initialize(IAxis... axes) {
	}

	@Override
	public String getDataName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset getData() {
		return data;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisible(boolean isVisible) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isUserTrace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getUserObject() {
		// TODO Auto-generated method stub
		return userObject;
	}

	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;

	}

	@Override
	public boolean is3DTrace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public int getErrorBarWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setErrorBarWidth(int errorBarCapWidth) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getTraceColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTraceColor(Color traceColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public TraceType getTraceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTraceType(TraceType traceType) {
		// TODO Auto-generated method stub

	}

	@Override
	public PointStyle getPointStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPointStyle(PointStyle pointStyle) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLineWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLineWidth(int lineWidth) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPointSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPointSize(int pointSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAreaAlpha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAreaAlpha(int areaAlpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAntiAliasing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAntiAliasing(boolean antiAliasing) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isErrorBarEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setErrorBarEnabled(boolean errorBarEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public ErrorBarType getYErrorBarType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setYErrorBarType(ErrorBarType errorBarType) {
		// TODO Auto-generated method stub

	}

	@Override
	public ErrorBarType getXErrorBarType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXErrorBarType(ErrorBarType errorBarType) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getErrorBarColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorBarColor(Color errorBarColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset getYData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset getXData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(IDataset xData, IDataset yData) {
		data = yData;

	}

	@Override
	public void repaint() {
		// TODO Auto-generated method stub

	}

	@Override
	public IAxis getXAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAxis getYAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDrawYErrorInArea(boolean drawYErrorInArea) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDrawYErrorInArea() {
		// TODO Auto-generated method stub
		return false;
	}

}
