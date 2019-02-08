package org.dawnsci.datavis.model.test;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.trace.IDownSampleListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.graphics.PaletteData;

public class MockImageTrace implements IImageTrace {

	String name;
	private Object userObject;
	private ImageServiceBean bean = new ImageServiceBean();
	private IDataset data;
	
	public MockImageTrace(String name) {
		this.name = name;
	}

	@Override
	public void initialize(IAxis... axes) {
	}

	@Override
	public PaletteData getPaletteData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPaletteData(PaletteData paletteData) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPaletteName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPaletteName(String paletteName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageServiceBean getImageServiceBean() {
		return bean;
	}

	@Override
	public void addPaletteListener(IPaletteListener pl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePaletteListener(IPaletteListener pl) {
		// TODO Auto-generated method stub

	}

	@Override
	public Number getMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HistogramBound getNanBound() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HistogramBound getMinCut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HistogramBound getMaxCut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNanBound(HistogramBound bound) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMinCut(HistogramBound bound) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxCut(HistogramBound bound) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMin(Number min) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMax(Number max) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRescaleHistogram(boolean rescaleHistogram) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRescaleHistogram() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getAxesNames() {
		// TODO Auto-generated method stub
		return null;
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
	public int getBin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addDownsampleListener(IDownSampleListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDownsampleListener(IDownSampleListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public IROI getRegionInAxisCoordinates(IROI roi) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPointInAxisCoordinates(double[] point) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getPointInImageCoordinates(double[] axisLocation) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset getRGBData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageOrigin getImageOrigin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImageOrigin(ImageOrigin origin) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setData(ILazyDataset image, List<? extends IDataset> axes, boolean performAutoScale) {
		if (image instanceof IDataset) {
			data = (IDataset) image;
		}
		return false;
	}

	@Override
	public void setDynamicData(IDynamicShape dynamic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAxes(List<? extends IDataset> axes, boolean performAutoScale) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IDataset> getAxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DownsampleType getDownsampleType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDownsampleType(DownsampleType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rehistogram() {
		// TODO Auto-generated method stub

	}

	@Override
	public HistoType getHistoType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setHistoType(HistoType type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setImageUpdateActive(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void repaint() {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset getDownsampled() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset getDownsampledMask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDownsampleBin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IDataset getMask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMask(IDataset bd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sleep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAlpha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGlobalRange(double[] globalRange) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasTrueAxes() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double[] getGlobalRange() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public double[] getGlobalRange() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
