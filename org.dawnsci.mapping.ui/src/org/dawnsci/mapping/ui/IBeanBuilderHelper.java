package org.dawnsci.mapping.ui;

import java.util.Map;

import org.eclipse.january.metadata.IMetadata;

public interface IBeanBuilderHelper {

	public void build(String path, Map<String, int[]> datasetNames, IMetadata meta);
	
}
