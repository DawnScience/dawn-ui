package org.dawnsci.common.live;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

public class FileBeanClassRegistry implements IClassRegistry {

	private Map<String, Class<?>> idMap;
	
	public FileBeanClassRegistry() {
		idMap = new HashMap<>();
		idMap.put(FileBean.class.getSimpleName(), FileBean.class);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idMap;
	}

}
