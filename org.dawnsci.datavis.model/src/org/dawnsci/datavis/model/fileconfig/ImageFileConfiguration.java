package org.dawnsci.datavis.model.fileconfig;

import java.util.List;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;

public class ImageFileConfiguration implements ILoadedFileConfiguration {

	@Override
	public boolean configure(LoadedFile f) {
		List<DataOptions> d = f.getDataOptions();
		if (d.size()==1 && d.get(0).getLazyDataset().getShape().length == 2) {
			d.get(0).setSelected(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void setCurrentState(List<DataOptions> state) {
		//doesn't need state
	}

}
