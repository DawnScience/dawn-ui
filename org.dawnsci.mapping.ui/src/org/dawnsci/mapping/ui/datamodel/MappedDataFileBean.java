package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MappedDataFileBean {

	private List<MappedBlockBean> blocks = new ArrayList<MappedBlockBean>();
	private List<MapBean> maps = new ArrayList<MapBean>();
	private List<AssociatedImageBean> images = new ArrayList<AssociatedImageBean>();
	private LiveDataBean liveBean = null;
	private int scanRank;
	
	public int getScanRank() {
		return scanRank;
	}

	public void setScanRank(int scanRank) {
		this.scanRank = scanRank;
	}

	public void addBlock(MappedBlockBean bean) {
		blocks.add(bean);
	}
	
	public void addMap(MapBean bean) {
		maps.add(bean);
	}
	
	public void addImage(AssociatedImageBean bean) {
		images.add(bean);
	}

	public List<MappedBlockBean> getBlocks() {
		return blocks;
	}

	public List<MapBean> getMaps() {
		return maps;
	}
	
	public List<AssociatedImageBean> getImages() {
		return images;
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
		
		for (AssociatedImageBean b : images) {
			if (!b.checkValid()) return false;
		}
		
		return true;
	}

	public LiveDataBean getLiveBean() {
		return liveBean;
	}

	public void setLiveBean(LiveDataBean live) {
		this.liveBean = live;
	}
	
}
