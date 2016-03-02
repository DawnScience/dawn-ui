package org.dawnsci.mapping.ui.wizards;

import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;

public class LegacyMapBeanBuilder {

	public static final String I18CHECK = "/entry1/xspress3/AllElementSum";
	
	private static final String XPRESS = "/entry1/xspress3";
	private static final String ALLELEMENT = "AllElementSum";
	private static final String YI18 = "sc_MicroFocusSampleY";
	private static final String XI18 = "traj1ContiniousX";
	private static final String MCAS = "MCAs";
	private static final String READER = "traj1PositionReader";
	
	private static final String I05ANALYSER = "/entry1/analyser";
	private static final String I05DATA = "data";
	private static final String I05ANGLES = "angles";
	private static final String I05ENERGIES = "energies";
	private static final String I05SAX = "sax";
	private static final String I05SAZ = "saz";
	public static final String I05CHECK = "/entry1/instrument/analyser/cps";
	
	
	public static MappedDataFileBean buildBeani18in2015(Tree tree) {
		
		MappedDataFileBean fb = null;
		
		NodeLink nl = tree.findNodeLink(XPRESS);
		Node n = nl.getDestination();
		if (n instanceof GroupNode) {
			GroupNode gn = (GroupNode)n;
			if (!gn.containsDataNode(ALLELEMENT)) return null;
			if (!gn.containsDataNode(YI18)) return null;
			if (!gn.containsDataNode(XI18)) return null;
			
			fb = new MappedDataFileBean();
			MappedBlockBean bb = new MappedBlockBean();
			bb.setName(XPRESS + Node.SEPARATOR + ALLELEMENT);
			String[] ax = new String[3];
			ax[0] = XPRESS + Node.SEPARATOR + YI18;
			ax[1] = XPRESS + Node.SEPARATOR + XI18;
			bb.setAxes(ax);
			bb.setRank(3);
			bb.setxDim(1);
			bb.setyDim(0);
			fb.addBlock(bb);
			
			for (String name : gn.getNames()){
				if (name.equals(ALLELEMENT)) continue;
				if (name.equals(YI18)) continue;
				if (name.equals(XI18)) continue;
				if (name.equals(MCAS)) continue;
				if (name.equals(READER)) continue;
				
				MapBean mb = new MapBean();
				mb.setName(XPRESS + Node.SEPARATOR + name);
				mb.setParent(XPRESS + Node.SEPARATOR + ALLELEMENT);
				fb.addMap(mb);
				
			}
			
		}
		
		if (fb!= null && !fb.checkValid()) fb = null;
		return fb;
	}
	
	public static MappedDataFileBean buildBeani05in2015(Tree tree) {
MappedDataFileBean fb = null;
		
		NodeLink nl = tree.findNodeLink(I05ANALYSER);
		Node n = nl.getDestination();
		if (n instanceof GroupNode) {
			GroupNode gn = (GroupNode)n;
			if (!gn.containsDataNode(I05DATA)) return null;
			if (!gn.containsDataNode(I05ANGLES)) return null;
			if (!gn.containsDataNode(I05ENERGIES)) return null;
			if (!gn.containsDataNode(I05SAX)) return null;
			if (!gn.containsDataNode(I05SAZ)) return null;

			fb = new MappedDataFileBean();
			MappedBlockBean bb = new MappedBlockBean();
			bb.setName(I05ANALYSER + Node.SEPARATOR + I05DATA);
			String[] ax = new String[4];
			ax[0] = I05ANALYSER + Node.SEPARATOR + I05SAZ;
			ax[1] = I05ANALYSER + Node.SEPARATOR + I05SAX;
			ax[2] = I05ANALYSER + Node.SEPARATOR + I05ANGLES;
			ax[3] = I05ANALYSER + Node.SEPARATOR + I05ENERGIES;
			bb.setAxes(ax);
			bb.setRank(4);
			bb.setxDim(1);
			bb.setyDim(0);
			fb.addBlock(bb);



			MapBean mb = new MapBean();
			mb.setName(I05CHECK);
			mb.setParent(I05ANALYSER + Node.SEPARATOR + I05DATA);
			fb.addMap(mb);

		}

		
		if (fb!= null && !fb.checkValid()) fb = null;
		return fb;
	}
}
