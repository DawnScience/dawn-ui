package org.dawnsci.plotting.tools.reduction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;

class DataReduction2DToolSpectraRegionDataNode extends DataReduction2DToolObservableModel implements IROIListener {

	protected final IRegion plotRegion;
	private final DataReduction2DToolModel toolModel;
	
	public static final String START = "start";
	public static final String END = "end";
	public static final String SPECTRA_CHANGED = "spectra";
	private List<DataReduction2DToolSpectrumDataNode> spectraList;

	protected final List<ITrace> regionTraces = new ArrayList<>();

	protected DataReduction2DToolRegionData regionData;
	
	protected boolean adjusting;
	
	public DataReduction2DToolSpectraRegionDataNode(IRegion plotRegion, final DataReduction2DToolModel toolModel, final DataReduction2DToolRegionData regionData) {
		this.plotRegion = plotRegion;
		this.toolModel = toolModel;
		this.plotRegion.addROIListener(this);
		this.regionData = regionData;
		findSpectra();
		plotRegion.setUserObject(this);
	}
	
	private void findSpectra() {
		adjusting = true;
		IROI roi = plotRegion.getROI();
		if (!RectangularROI.class.isInstance(roi))
			return;
		RectangularROI boxRoi = (RectangularROI) roi;
		int firstIndex;
		int lastIndex;
		if (regionData != null) {
			firstIndex = regionData.getStartIndex();
			lastIndex = regionData.getEndIndex();
		} else {
			firstIndex = (int) Math.round(boxRoi.getPointY());
			if (spectraList != null) {
				lastIndex = firstIndex + spectraList.size();
			} else {
				lastIndex = firstIndex + (int) Math.round(boxRoi.getLength(1));
			}
			firstIndex = Math.max(firstIndex, 0);
			if (lastIndex > firstIndex) {
				lastIndex--;
			}
		}
		boolean started = false;
		boolean ended = false;
		int index = 0;
		ArrayList<DataReduction2DToolSpectrumDataNode> tempSpectraList = new ArrayList<>();
		for (DataReduction2DToolSpectrumDataNode spectrumDataNode : toolModel.getSpectrumDataNodes()) {
			if (spectrumDataNode.getIndex() >= firstIndex) {
				started = true;
			}
			if (started && !ended) {
				tempSpectraList.add(spectrumDataNode);
				if (spectrumDataNode.getIndex() == lastIndex) {
					ended = true;
				}
			}
			if (!ended && spectrumDataNode == toolModel.getSpectrumDataNodes().get(toolModel.getSpectrumDataNodes().size() - 1)) {
				ended = true;
				lastIndex = index;
			}
			if (started && ended) {
				firePropertyChange(SPECTRA_CHANGED, spectraList, spectraList = tempSpectraList);
				firePropertyChange(START, null, this.getStart());
				firePropertyChange(END, null, this.getEnd());
				roi.setPoint(0, firstIndex);
				((RectangularROI) roi).setLengths(new double[]{boxRoi.getLength(0), lastIndex - firstIndex + 1});
				System.err.println("Point: " + firstIndex);
				System.err.println("Length " + (lastIndex - firstIndex + 1));
				plotRegion.setROI(roi);
				break;
			}
			index++;
		}
		adjusting = false;
	}
	
	public List<DataReduction2DToolSpectrumDataNode> getSpectra() {
		return spectraList;
	}
	
	public IRegion getRegion() {
		return plotRegion;
	}
	
	public DataReduction2DToolSpectrumDataNode getStart() {
		return spectraList.get(0);
	}

	public DataReduction2DToolSpectrumDataNode getEnd() {
		return spectraList.get(spectraList.size() - 1);
	}
	
	public int getTotalSpectra() {
		return regionData.getNSpectra();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		// Do nothing
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		if (!adjusting)
			findSpectra();
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// Do nothing
	}

	public Dataset getDataset(Dataset fullData) {
		Dataset result = DatasetFactory.zeros(fullData.getClass(), 0, fullData.getShapeRef()[1]);
		for (DataReduction2DToolSpectrumDataNode spectrum : this.getSpectra()) {
			Dataset data = fullData.getSliceView(new int[]{spectrum.getIndex(), 0}, new int[]{spectrum.getIndex() + 1, fullData.getShape()[1]}, new int[]{1,1});
			result = DatasetUtils.append(result, data, 0);
		}
		return result;
	}

	public ITrace[] getTraces() {
		return regionTraces.toArray(new ITrace[]{});
	}

	public void addTrace(ITrace trace) {
		regionTraces.add(trace);
	}


	public void clearTrace() {
		regionTraces.clear();
	}

	@Override
	public String toString() {
		return this.getStart() + ":" + this.getEnd();
	}

	public DataReduction2DToolRegionData getRegionData() {
		return regionData;
	}
	
}
