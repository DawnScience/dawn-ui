package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;

public class RGBMapData extends MappedData {

	AbstractMapData[] rgb;
	int[] redPercent;
	int[] greenPercent;
	int[] bluePercent;
	double[] redRange;
	double[] blueRange;
	double[] greenRange;
	boolean redLog;
	boolean greenLog;
	boolean blueLog;
	RGBDataset data;
	
	public RGBMapData(String name, AbstractMapData parent, String path) {
		super(name, parent.map, parent.parent, parent.path);
		this.rgb = new AbstractMapData[3];
		data = new RGBDataset(parent.map.getShape());
		AxesMetadata ax = parent.getData().getFirstMetadata(AxesMetadata.class);
		data.setMetadata(ax);
	}
	
	@Override
	public IDataset getData() {
		return data;
	}
	
	public void updateRedPercentRange(int[] range, boolean log) {
		updatePercentRange(0, range, log);
		
	}
	
	public void updateGreenPercentRange(int[] range, boolean log) {
		updatePercentRange(1, range, log);
	}
	
	public void updateBluePercentRange(int[] range, boolean log) {
		updatePercentRange(2, range, log);
	}
	
	private void updatePercentRange(int channel, int[] range, boolean log) { 
		AbstractMapData m = rgb[channel];
		if (m != null && m.getData() != null) {
			Dataset update = update(DatasetUtils.convertToDataset(m.getData()), range[0], range[1], log);
			data.setElements(update, channel);
		}
	}
	
	public void clearRed(){
		clearChannel(0);
	}
	
	public void clearGreen(){
		clearChannel(1);
	}
	
	public void clearBlue(){
		clearChannel(2);
	}
	
	private void clearChannel(int channel){
		data.setElements(DatasetFactory.zeros(data.getShape(), Dataset.INT16), channel);
		
	}
	
	public void switchRed(AbstractMapData red, int[] range, boolean log) {
		rgb[0] = red;
		updateRedPercentRange(range, log);
	}
	
	public void switchBlue(AbstractMapData blue, int[] range, boolean log) {
		rgb[2] = blue;
		updateBluePercentRange(range, log);
	}
	
	public void switchGreen(AbstractMapData green, int[] range, boolean log) {
		rgb[1] = green;
		updateGreenPercentRange(range, log);
	}
	
	private Dataset updateDataset(Dataset ds, double min, double max, boolean log) {
		Dataset out = ds.getSlice();
		Dataset shortData = DatasetFactory.zeros(out, Dataset.INT16);
		
		if (log) {
			out = Maths.log10(out);
			min = Math.log10(min);
			max = Math.log10(max);
		}
		
		out.isubtract(min).idivide(max-min).imultiply(255);
		IndexIterator it = out.getIterator();
		while (it.hasNext()) {
			double val = out.getElementDoubleAbs(it.index);
			if (val < 0) shortData.setObjectAbs(it.index, 0);
			else if (val > 255) shortData.setObjectAbs(it.index, 255);
			else if (Double.isNaN(val)) shortData.setObjectAbs(it.index, 0);
			else shortData.setObjectAbs(it.index,val);
		}
		
		return shortData;
	}
	
	private Dataset update(Dataset ds, int lower, int upper, boolean log) {
		double dMin = ds.min().doubleValue();
		double dMax = ds.max().doubleValue();
		
		double dRange = dMax - dMin;
		int min = lower;
		int max = upper;

		double mi = dRange*((double)min/100)+dMin;
		double ma = dMax - dRange*(100-max)/100;
		
		return updateDataset(ds, mi, ma, log);
	}

	@Override
	public String toString() {
		
		StringBuilder b = new StringBuilder();
		
		for (AbstractMapData m : rgb) {
			if (m == null) b.append(".,");
			else {
				b.append(m.toString());
				b.append(",");
			}
			
		}
		b.deleteCharAt(b.length()-1);
		
		return b.toString();
	}
}
