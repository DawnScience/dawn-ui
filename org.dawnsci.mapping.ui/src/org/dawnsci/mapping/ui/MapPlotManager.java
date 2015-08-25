package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;

public class MapPlotManager {
	
	private IPlottingSystem map;
	private IPlottingSystem data;
	private MappedDataArea area;
	private List<MapObject> layers;
	
	public MapPlotManager(IPlottingSystem map, IPlottingSystem data, MappedDataArea area) {
		this.map = map;
		this.data = data;
		this.area = area;
		layers = new LinkedList<MapObject>();
	}
	
	public void plotData(double x, double y) {
		MappedData topMap = getTopMap();
		if (topMap == null) return;
		IDataset s = topMap.getSpectrum(x,y);
		
		if (s != null) MappingUtils.plotDataWithMetadata(s, data, new int[]{0});
	}
	
	public void plotMap(MappedData map) {
		plotMapData(map);
	}
	
	public MappedData getTopMap(){
		
		for (int i = layers.size()-1; i >=0 ; i--) {
			MapObject l = layers.get(i);
			if (l instanceof MappedData) return (MappedData)l;
		}
		
		return null;
	}
	
	public void clearAll(){
		map.clear();
		data.clear();
	}
	
	private void plotMapData(MappedData mapdata){
		map.clear();
		MappedDataFile dataFile = area.getDataFile(0);
		AssociatedImage image = dataFile.getAssociatedImage();
		if (mapdata == null) mapdata = dataFile.getMap();
		int count = 0;
		layers.clear();
		try {
			ICompositeTrace comp = this.map.createCompositeTrace("composite1");
			if (image != null) {
				layers.add(image);
				comp.add(MappingUtils.buildTrace(image.getImage(), this.map),count++);
			}

			layers.add(mapdata);
			comp.add(MappingUtils.buildTrace(mapdata.getMap(), this.map,mapdata.getTransparency()),count++);
			this.map.addTrace(comp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isPlotted(MapObject object) {
		return layers.contains(object);
	}
}
