package org.dawnsci.plotting.tools.filter;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.SummedAreaTable;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.RGBByteDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fano factor filter. NOTE a 6mil pixel image takes about
 * 800ms on an i7 @see SummedAreaTableTest 
 * 
 * Therefore it may be required to run the filter asynchronously.
 * 
 * @author Matthew Gerring
 *
 */
public class FanoFilter extends AbstractDelayedFilter {

	private static Logger logger = LoggerFactory.getLogger(FanoFilter.class);
	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset data, List<IDataset> axes) throws Exception {
		int[]           box   = (int[])getConfiguration().get("box");
		if (box == null) {
			box = new int[]{3,3};
			logger.warn("Unexpected lack of box configuration parameter in "+getClass().getName());
		}
		if (data instanceof CompoundDataset && ((CompoundDataset)data).getElementsPerItem() == 3) {
			CompoundDataset cpd = (CompoundDataset) data;
			Dataset rData = cpd.getElements(0);
			Dataset gData = cpd.getElements(1);
			Dataset bData = cpd.getElements(2);
			SummedAreaTable rTable = new SummedAreaTable(rData, true);
			Dataset rFano = rTable.getFanoImage(box);
			SummedAreaTable gTable = new SummedAreaTable(gData, true);
			Dataset gFano = gTable.getFanoImage(box);
			SummedAreaTable bTable = new SummedAreaTable(bData, true);
			Dataset bFano = bTable.getFanoImage(box);
			Dataset fanoRgb = DatasetUtils.createCompoundDataset(RGBByteDataset.class, rFano, gFano, bFano);
			return new Object[] { fanoRgb, axes };
		} else {
			SummedAreaTable table = new SummedAreaTable(DatasetUtils.convertToDataset(data), true);
			IDataset fano = table.getFanoImage(box);
			return new Object[] { fano, axes };
		}
	}

}
