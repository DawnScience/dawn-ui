package org.dawnsci.datavis.model.fileconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataStateObject;
import org.dawnsci.datavis.model.LoadedFile;

public class XYEFileConfiguration implements ILoadedFileConfiguration {

	@Override
	public boolean configure(LoadedFile f) {
		List<DataOptions> d = f.getDataOptions();
		Map<String, int[]> ds = f.getDataShapes();
		
		if (d.size() == 2 || d.size() == 3) {
			DataOptions d0 = d.get(0);
			DataOptions d1 = d.get(1);
			int[] s0 = ds.get(d0.getName());
			int[] s1 = ds.get(d0.getName());
			if (s0.length == 1 && Arrays.equals(s0, s1)) {
				d1.setAxes(new String[]{d0.getName()});
				d1.setSelected(true);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void setCurrentState(List<DataStateObject> state) {
		//doesn't need state
	}

}
