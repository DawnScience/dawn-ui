package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MappedDataFileBean {

	private List<MappedBlockBean> blocks = new ArrayList<MappedBlockBean>();
	private List<MapBean> maps = new ArrayList<MapBean>();
	
	public void addBlock(MappedBlockBean bean) {
		blocks.add(bean);
	}
	
	public void addMap(MapBean bean) {
		maps.add(bean);
	}

	public List<MappedBlockBean> getBlocks() {
		return blocks;
	}

	public List<MapBean> getMaps() {
		return maps;
	}
	
//	public List<String> getBlockNames() {
//		List<String> names = new ArrayList<String>();
//		for (MappedBlockBean b : blocks) names.add(b.getName());
//		return names;
//	}
//	
//	public List<String> getMapNames() {
//		List<String> names = new ArrayList<String>();
//		for (MapBean m : maps) names.add(m.getName());
//		return names;
//	}
	
}
