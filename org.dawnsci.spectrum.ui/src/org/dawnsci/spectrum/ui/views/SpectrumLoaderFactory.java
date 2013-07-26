package org.dawnsci.spectrum.ui.views;

import java.util.Collection;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SpectrumLoaderFactory {
	
	public static SpectrumFile loadSpectrumFile(String path) {
		
			IMetaData meta;
			try {
				meta = LoaderFactory.getMetaData(path, null);
				if (meta==null || meta.getDataNames()==null) {
					DataHolder dh = LoaderFactory.getData(path, null);
					if (dh==null) dh= new DataHolder();
					Collection<String> dataNames = dh.getMap().keySet();
					return new SpectrumFile(path, meta, dataNames);
					
				} else {
					return new SpectrumFile(path, meta, meta.getDataNames());
				}
			} catch (Exception e) {
				return null;
			}
			
		
	}

}
