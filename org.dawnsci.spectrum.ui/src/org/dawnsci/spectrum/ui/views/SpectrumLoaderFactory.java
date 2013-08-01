package org.dawnsci.spectrum.ui.views;

import java.util.Collection;

import org.dawnsci.plotting.api.IPlottingSystem;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SpectrumLoaderFactory {
	
	public static SpectrumFile loadSpectrumFile(String path, IPlottingSystem system) {
		
			IMetaData meta;
			try {
				meta = LoaderFactory.getMetaData(path, null);
				if (meta==null || meta.getDataNames()==null) {
					DataHolder dh = LoaderFactory.getData(path, null);
					if (dh==null) dh= new DataHolder();
					Collection<String> dataNames = dh.getMap().keySet();
					return new SpectrumFile(path, meta, dataNames,system);
					
				} else {
					return new SpectrumFile(path, meta, meta.getDataNames(),system);
				}
			} catch (Exception e) {
				return null;
			}
			
		
	}

}
