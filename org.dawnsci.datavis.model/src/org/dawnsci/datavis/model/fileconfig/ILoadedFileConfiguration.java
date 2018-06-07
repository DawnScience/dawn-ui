package org.dawnsci.datavis.model.fileconfig;

import java.util.List;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;

public interface ILoadedFileConfiguration {
	
	public boolean configure(LoadedFile f);
	
	public void setCurrentState(List<DataOptions> state);

}
