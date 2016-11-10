package org.dawnsci.mapping.ui.wizards;

import java.util.Collection;

import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.Stats;

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
	public static final String I05CHECK = "/entry1/instrument/analyser/cps";
	
	public static final String I22SAXCHECK = "/entry1/detector/data";
	public static final String I22SAX = "/entry1/detector";
	public static final String I22WAX = "/entry1/Pilatus2M_WAXS";
	
	public static final String I22XMAP = "/entry1/xmap";
	public static final String I22MFX = "mfstage_x";
	public static final String I22MFY = "mfstage_y";
	public static final String I22BFX = "base_x";
	public static final String I22BFY = "base_y";
	public static final String I22I0PATH = "/entry1/I0";
	public static final String I22ITPATH = "/entry1/It";
	public static final String I22data = "data";
	
	private static final String I08COUNTER = "/entry1/Counter1";
	private static final String I08Y = "sample_y";
	private static final String I08X = "sample_x";
	private static final String I08PHOTON_ENERGY = "photon_energy";
	private static final String I08DATA = "data";
	public static final String I08CHECK = "/entry1/Counter1/data";
	
	
	
	public static MappedDataFileBean tryLegacyLoaders(IDataHolder dh) {
		MappedDataFileBean b = null;
		
		if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I18CHECK) != null) {
			try {
			b = LegacyMapBeanBuilder.buildBeani18in2015(dh.getTree());
			} catch (Exception e) {
				//ignore
			}
		}
		
		if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I05CHECK) != null) {
			try {
			b = LegacyMapBeanBuilder.buildBeani05in2015(dh.getTree());
			} catch (Exception e) {
				//ignore
			}
		}
		
		if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I22SAXCHECK) != null) {
			try {
			b = LegacyMapBeanBuilder.buildBeani22in2016(dh.getTree());
			} catch (Exception e) {
				//ignore
			}
		}
		
		if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I08CHECK) != null) {
			try {
			b = LegacyMapBeanBuilder.buildBeani08Energyin2016(dh.getTree());
			} catch (Exception e) {
				//ignore
			}
		}
		
		return b;
	}
	
public static MappedDataFileBean buildBeani08Energyin2016(Tree tree) {
		
		MappedDataFileBean fb = null;
		
		NodeLink nl = tree.findNodeLink(I08COUNTER);
		Node n = nl.getDestination();
		if (n instanceof GroupNode) {
			GroupNode gn = (GroupNode)n;
			if (!gn.containsDataNode(I08DATA)) return null;
			if (!gn.containsDataNode(I08PHOTON_ENERGY)) return null;
			if (!gn.containsDataNode(I08X)) return null;
			if (!gn.containsDataNode(I08Y)) return null;
			
			fb = new MappedDataFileBean();
			MappedBlockBean bb = new MappedBlockBean();
			bb.setName(I08COUNTER + Node.SEPARATOR + I08DATA);
			String[] ax = new String[3];
			ax[0] = I08COUNTER + Node.SEPARATOR + I08PHOTON_ENERGY;
			ax[1] = I08COUNTER + Node.SEPARATOR + I08Y;
			ax[2] = I08COUNTER + Node.SEPARATOR + I08X;
			
			bb.setAxes(ax);
			bb.setRank(3);
			bb.setxDim(2);
			bb.setyDim(1);
			fb.addBlock(bb);
			fb.setScanRank(3);
		}
		
		if (fb!= null && !fb.checkValid()) fb = null;
		return fb;
	}
	
	
	
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
		fb.setScanRank(3);
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
			
			String[] xyAxesNames = getXYAxesNames(gn);
			
			if (xyAxesNames == null) return null;
			
			String yAxis = xyAxesNames[1];
			String xAxis = xyAxesNames[0];

			fb = new MappedDataFileBean();
			MappedBlockBean bb = new MappedBlockBean();
			bb.setName(I05ANALYSER + Node.SEPARATOR + I05DATA);
			String[] ax = new String[4];
			ax[0] = I05ANALYSER + Node.SEPARATOR + yAxis;
			ax[1] = I05ANALYSER + Node.SEPARATOR + xAxis;
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
			fb.setScanRank(3);

		}

		
		if (fb!= null && !fb.checkValid()) fb = null;
		return fb;
	}
	
	private static String[] getXYAxesNames(GroupNode gn) {
		
		String yAxis = null;
		String xAxis = null;
		
		Collection<String> names = gn.getNames();
		
		for (String name : names) {
			
			if (gn.containsDataNode(name)) {
				DataNode dataNode = gn.getDataNode(name);
				if (dataNode.containsAttribute("axis")){
					String at = dataNode.getAttribute("axis").getFirstElement();
					if ("1,2".equals(at)) {
						if (xAxis == null) xAxis = name;
						else yAxis = name;
					}
				}
			}
			
			if (xAxis != null && yAxis != null) break;
			
		}
		
		try {
			
			DataNode dataNode = gn.getDataNode(xAxis);
			Dataset x = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			Dataset m0 = Stats.median(x, 0);
			Dataset m1 = Stats.median(x, 1);
			
			double max0 = m0.max(true).doubleValue();
			double min0 = m0.min(true).doubleValue();
			
			double max1 = m1.max(true).doubleValue();
			double min1 = m1.min(true).doubleValue();
			
			double p0 = max0-min0;
			double p1 = max1-min1;
			
			if (p1 > p0) {
				String tmp = yAxis;
				yAxis = xAxis;
				xAxis = tmp;
			}
			
		} catch (Exception e) {
			//cant read data?
		}
		
		if (xAxis == null || yAxis == null) return null;
		
		return new String[]{xAxis,yAxis};
	}
	
	public static MappedDataFileBean buildBeani22in2016(Tree tree) {
		
		MappedDataFileBean fb = null;
		
		MappedBlockBean sax = buildI22Block(I22SAX, tree.findNodeLink(I22SAX));
		MappedBlockBean wax = buildI22Block(I22SAX, tree.findNodeLink(I22WAX));
		MappedBlockBean xrf = buildI22Block(I22XMAP, tree.findNodeLink(I22XMAP));
		
		if (sax == null && wax == null) return null;
		
		fb = new MappedDataFileBean();
		
		if (sax != null) fb.addBlock(sax);
		if (wax != null) fb.addBlock(wax);
		if (xrf != null) fb.addBlock(xrf);
		
		NodeLink nl = tree.findNodeLink(I22I0PATH);
		
		if (nl != null) {
			Node destination = nl.getDestination();
			if (destination instanceof GroupNode) {
				if (((GroupNode)destination).containsDataNode(I22data)) {
					MapBean b = new MapBean();
					b.setName(I22I0PATH + Node.SEPARATOR + I22data);
					b.setParent(sax == null ? I22WAX + Node.SEPARATOR + I22data : I22SAX + Node.SEPARATOR + I22data );
					fb.addMap(b);
				}
			}
		}
		
		NodeLink nl2 = tree.findNodeLink(I22ITPATH);
		if (nl == null && nl2 == null) return null;
		
		if (nl2 != null) {
			Node destination = nl2.getDestination();
			if (destination instanceof GroupNode) {
				if (((GroupNode)destination).containsDataNode(I22data)) {
					MapBean b = new MapBean();
					b.setName(I22ITPATH + Node.SEPARATOR + I22data);
					b.setParent(sax == null ? I22WAX + Node.SEPARATOR + I22data : I22SAX + Node.SEPARATOR + I22data );
					fb.addMap(b);
				}
			}
		}	
		fb.setScanRank(3);
		return fb.checkValid() ? fb : null;
	}
	
	private static MappedBlockBean buildI22Block(String name, NodeLink link) {
		if (link == null) return null;
		Node n = link.getDestination();
		if (n instanceof GroupNode) {
			GroupNode gn = (GroupNode)n;
			DataNode dataNode = gn.getDataNode(I22data);
			int rank = dataNode.getDataset().getRank();
			String[] xyAxesNames = getXYAxesNames(gn);
			
			if (xyAxesNames == null) return null;
			
			String yAxis = xyAxesNames[1];
			String xAxis = xyAxesNames[0];
			
			MappedBlockBean mbb = new MappedBlockBean();
			mbb.setName(name + Node.SEPARATOR + I22data);
			mbb.setRank(rank);
			mbb.setxDim(1);
			mbb.setyDim(0);
			String[] axes = new String[rank];
			axes[0] = name + Node.SEPARATOR + yAxis;
			axes[1] = name + Node.SEPARATOR + xAxis;
			mbb.setAxes(axes);
			return mbb;
		}
		
		return null;
	}
	
}
