package org.dawnsci.mapping.ui.test;

import org.dawnsci.mapping.ui.datamodel.LiveRemoteAxes;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;

public class MapTestUtils {
	
	public static void enableIncrement(ILazyDataset... datasets) {
		for (ILazyDataset d : datasets) {
			
			if (d == null) {
				continue;
			}
			
			if (d instanceof DynamicRandomLazyDataset) {
				((DynamicRandomLazyDataset) d).setEnableIncrement(true);
			}
			
			AxesMetadata ax = d.getFirstMetadata(AxesMetadata.class);
			
			if (ax != null && ax.getAxes() != null) {
				ILazyDataset[] axes = ax.getAxes();
				enableIncrement(axes);
			}
		}
	}
	
	
	public static IDynamicDataset getLivePointDataset(){
		
		int[] maxShape = {-1,-1,1,1};
		
		int[] first = {1,5,1,1};
		int[] second = {2,7,1,1};
		int[] third = {7,7,1,1};
		
		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);
		
		return mock;
		
	}
	
	public static IDynamicDataset getLiveDataset(){

		int[] maxShape = {-1,-1,99,100};

		int[] first = {1,5,99,100};
		int[] second = {2,7,99,100};
		int[] third = {7,7,99,100};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static IDynamicDataset getLiveLinearDataset(){

		int[] maxShape = {-1,99,100};

		int[] first = {5,99,100};
		int[] second = {10,7,99,100};
		int[] third = {20,7,99,100};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}
	
	public static IDynamicDataset getLiveLinearMap(){

		int[] maxShape = {-1};

		int[] first = {5};
		int[] second = {10};
		int[] third = {20};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static LiveRemoteAxes getLiveLinearAxes() {

		int[] first = {5};
		int[] second = {10};
		int[] third = {20};

		IDynamicDataset x = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{20});
		IDynamicDataset y = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{20});
		((DynamicRandomLazyDataset)y).setEndNan(true);
		IDynamicDataset[] ax = new IDynamicDataset[]{y,null,null};
		String[] names = new String[]{"y",null,null};
		LiveRemoteAxes axes= new LiveRemoteAxes(ax, names, "host", 8690);
		axes.setxAxisForRemapping(x);
		axes.setxAxisForRemappingName("x");

		return axes;
	}
	
	
	
	public static IDynamicDataset getLiveMap(){

		int[] maxShape = {-1,-1};

		int[] first = {1,5};
		int[] second = {2,7};
		int[] third = {7,7};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static LiveRemoteAxes getLiveAxes() {

		int[] first = {4};
		int[] second = {7};
		int[] third = {7};

		IDynamicDataset x = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{7});

		first = new int[]{1};
		second = new int[]{3};
		third = new int[]{7};
		IDynamicDataset y = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{7});
		((DynamicRandomLazyDataset)y).setEndNan(true);
		IDynamicDataset[] ax = new IDynamicDataset[]{(IDynamicDataset)y.getDataset(),(IDynamicDataset)x.getDataset(),null,null};
		String[] names = new String[]{"y","x",null,null};
		LiveRemoteAxes axes= new LiveRemoteAxes(ax, names, "host", 8690);

		return axes;
	}

}
