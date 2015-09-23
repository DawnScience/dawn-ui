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
	
	public boolean checkValid() {
		
		if (blocks.isEmpty()) return false;
		if (maps.isEmpty()) return false;
		
		for (MappedBlockBean b : blocks) {
			if (!b.checkValid()) return false;
		}
		
		for (MapBean b : maps) {
			if (!b.checkValid()) return false;
		}
		
		return true;
	}
	
}
