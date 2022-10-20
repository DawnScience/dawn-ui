package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.ByteDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.metadata.AxesMetadata;

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
	RGBByteDataset data;
	
	public RGBMapData(String name, AbstractMapData parent, String path) {
		super(name, parent.cachedMap, parent.parent, parent.path, false);
		this.rgb = new AbstractMapData[3];
		data = DatasetFactory.zeros(RGBByteDataset.class, parent.cachedMap.getShape());
		AxesMetadata ax = parent.getMap().getFirstMetadata(AxesMetadata.class);
		data.setMetadata(ax);
	}
	
	@Override
	public IDataset getMap() {
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
		if (m != null && m.getMap() != null) {
			Dataset update = update(DatasetUtils.convertToDataset(m.getMap()), range[0], range[1], log);
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
		data.setElements(DatasetFactory.zeros(ByteDataset.class, data.getShapeRef()), channel);
		
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
		ByteDataset byteData = DatasetFactory.zeros(out, ByteDataset.class);
		
		if (log) {
			out = Maths.log10(out);
			min = Math.log10(min);
			max = Math.log10(max);
		}
		
		out.isubtract(min).idivide(max-min).imultiply(255);
		IndexIterator it = out.getIterator();
		while (it.hasNext()) {
			double val = out.getElementDoubleAbs(it.index);
			byte v;
			if (val < 0) {
				v = 0;
			} else if (val > 255) {
				v = (byte) 255;
			} else if (Double.isNaN(val)) {
				v = 0;
			} else {
				v = (byte) val;
			}
			byteData.setAbs(it.index, v);
		}
		
		return byteData;
	}
	
	private Dataset update(Dataset ds, int lower, int upper, boolean log) {
		double dMin = ds.min(true).doubleValue();
		double dMax = ds.max(true).doubleValue();
		
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
