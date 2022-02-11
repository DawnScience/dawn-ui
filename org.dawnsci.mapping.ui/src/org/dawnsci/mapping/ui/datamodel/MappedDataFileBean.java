package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MappedDataFileBean {

	private List<MappedBlockBean> blocks = new ArrayList<MappedBlockBean>();
	private List<MapBean> maps = new ArrayList<MapBean>();
	private List<AssociatedImageBean> images = new ArrayList<AssociatedImageBean>();
	private List<AssociatedImageStackBean> imageStacks = new ArrayList<AssociatedImageStackBean>();
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
	
	public void addImageStack(AssociatedImageStackBean bean) {
		imageStacks.add(bean);
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
	public List<AssociatedImageStackBean> getImageStacks() {
		return imageStacks;
	}
	
	public boolean isEmpty(){
		return blocks.isEmpty() && maps.isEmpty() && images.isEmpty();
	}
	
	public boolean checkValid() {
		
		//valid files will either have a block or image stack
		if (!imageStacks.isEmpty() && blocks.isEmpty()) {
			for (AssociatedImageStackBean b : imageStacks) {
				if (!b.checkValid()) return false;
			}
			return true;
		}
		
		if (blocks.isEmpty()) return false;
		
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
