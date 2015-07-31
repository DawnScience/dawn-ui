package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedFileDescription {

	private Map<String,List<String>> blockToMap = new HashMap<String, List<String>>();
	private Map<String,List<String>> blockToAxes = new HashMap<String, List<String>>();
	private String xAxisName;
	private String yAxisName;
	
	
	public void addDataBlock(String blockName, List<String> axes) {
		blockToMap.put(blockName, new ArrayList<String>());
		blockToAxes.put(blockName, axes);
	}
	
	public void addMap(String parentBlock, String mapName) {
		if (!blockToMap.containsKey(parentBlock)) blockToMap.put(parentBlock,Arrays.asList(new String[]{mapName}));
		else blockToMap.get(parentBlock).add(mapName);
	}
	
	public List<String> getBlockNames() {
		return new ArrayList<String>(blockToMap.keySet());
	}
	
	public List<String> getBlockAxes(String parentBlock) {
		return blockToAxes.get(parentBlock);
	}
	
	public List<String> getMapNames(String parentBlock) {
		return blockToMap.get(parentBlock);
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
	
	
	
}
