package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean to hold Nexus tagged axes and uncertainties
 *  
 */
public class NexusSignal {
	
	String name;
	private AxisSet[] axes;
	private String uncertainties = null;
	
	public NexusSignal(String name, int rank) {
		this.name = name;
		axes = new AxisSet[rank];
		
		for (int i = 0; i < rank; i++) {
			axes[i] = new AxisSet();
		}
	}
	
	public NexusSignal(NexusSignal toCopy) {
		this.name = toCopy.name;
		this.axes = toCopy.axes.clone();
		this.uncertainties = toCopy.uncertainties;
	}
	
	public void addAxis(int dimension, String path) {
		axes[dimension].addAxis(path);
	}
	
	public void setUncertainties(String uncert) {
		this.uncertainties = uncert;
	}
	
	public String getUncertainties() {
		return this.uncertainties;
	}
	
	public String[] getPrimaryAxes() {
		String[] out = new String[axes.length];
		
		for (int i = 0; i < axes.length; i++) {
			out[i] = axes[i].getPrimaryAxis();
		}
		
		return out;
	}
	
	public String[][] getTaggedAxes() {
		String[][] out = new String[axes.length][];
		
		for (int i = 0; i < axes.length; i++) {
			List<String> a = axes[i].getAxes();
			out[i] = axes[i].getAxes().toArray(new String[a.size()]);
		}
		
		return out;
	}
	
	public List<String> getAxes(int dimension) {
		return axes[dimension].getAxes();
	}
	
	public int getRank( ) {
		return axes.length;
	}
	
	private class AxisSet {
		
		private Set<String> axisSet = new LinkedHashSet<>();
		
		public void addAxis(String axis) {
			axisSet.add(axis);
		}
		
		public List<String> getAxes() {
			ArrayList<String> a = new ArrayList<String>(axisSet);
			return a;
		}
		
		public String getPrimaryAxis() {
			
			return axisSet.isEmpty() ? null : axisSet.iterator().next();
		}
		
	}
	

}
