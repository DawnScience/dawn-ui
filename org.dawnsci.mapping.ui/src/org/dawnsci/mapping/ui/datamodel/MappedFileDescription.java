package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedFileDescription {

	private Map<String,List<String>> dataBlockToMapMapping = new HashMap<String, List<String>>();
	private Map<String,List<String>> dataBlockToAxesMapping = new HashMap<String, List<String>>();
	private String xAxisName;
	private String yAxisName;
	private boolean remappingRequired = false;
	
	
	public void addDataBlock(String blockName, List<String> axes) {
		dataBlockToMapMapping.put(blockName, new ArrayList<String>());
		dataBlockToAxesMapping.put(blockName, axes);
	}
	
	public void addMap(String parentBlock, String mapName) {
		if (!dataBlockToMapMapping.containsKey(parentBlock)) dataBlockToMapMapping.put(parentBlock,Arrays.asList(new String[]{mapName}));
		else dataBlockToMapMapping.get(parentBlock).add(mapName);
	}
	
	public List<String> getBlockNames() {
		return new ArrayList<String>(dataBlockToMapMapping.keySet());
	}
	
	public List<String> getBlockAxes(String parentBlock) {
		return dataBlockToAxesMapping.get(parentBlock);
	}
	
	public List<String> getMapNames(String parentBlock) {
		return dataBlockToMapMapping.get(parentBlock);
	}
	
	public String getxAxisName() {
		return xAxisName;
	}
	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}
	public String getyAxisName() {
		return yAxisName;
	}
	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public boolean isRemappingRequired() {
		return remappingRequired;
	}

	public void setRemappingRequired(boolean remappingRequired) {
		this.remappingRequired = remappingRequired;
	}

	public Map<String, List<String>> getDataBlockToMapMapping() {
		return dataBlockToMapMapping;
	}

	public void setDataBlockToMapMapping(
			Map<String, List<String>> dataBlockToMapMapping) {
		this.dataBlockToMapMapping = dataBlockToMapMapping;
	}

	public Map<String, List<String>> getDataBlockToAxesMapping() {
		return dataBlockToAxesMapping;
	}

	public void setDataBlockToAxesMapping(
			Map<String, List<String>> dataBlockToAxesMapping) {
		this.dataBlockToAxesMapping = dataBlockToAxesMapping;
	}
	
	
	
}
