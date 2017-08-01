package org.dawnsci.datavis.model.test;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class MockAxis implements IAxis {

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitleFont(Font titleFont) {
		// TODO Auto-generated method stub

	}

	@Override
	public Font getTitleFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPrimaryAxis() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLog10() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLog10(boolean isLog10) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setForegroundColor(Color color) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getForegroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBackgroundColor(Color color) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShowMajorGrid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShowMajorGrid(boolean showMajorGrid) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isShowMinorGrid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShowMinorGrid(boolean showMinorGrid) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getMajorGridColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMajorGridColor(Color majorGridColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getMinorGridColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMinorGridColor(Color minorGridColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFormatPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFormatPattern(String formatPattern) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isYAxis() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setYAxis(boolean isYAxis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisible(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getUpper() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getLower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRange(double start, double end) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAxisListener(IAxisListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAxisListener(IAxisListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDateFormatEnabled(boolean dateEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDateFormatEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getValuePosition(double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPositionValue(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getValueFromPosition(double position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPositionFromValue(double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLabelDataAndTitle(IDataset labels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaximumRange(double lower, double upper) {
		// TODO Auto-generated method stub

	}

	@Override
	public String format(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String format(Object value, int extraDP) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getScaling() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAutoFormat(boolean autoFormat) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAutoFormat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAxisAutoscaleTight(boolean axisTight) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAxisAutoscaleTight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInverted(boolean isInverted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInverted() {
		// TODO Auto-generated method stub
		return false;
	}

}
