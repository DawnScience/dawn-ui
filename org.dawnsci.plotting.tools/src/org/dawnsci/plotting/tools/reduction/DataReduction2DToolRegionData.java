package org.dawnsci.plotting.tools.reduction;

import java.text.MessageFormat;
import java.util.List;

public class DataReduction2DToolRegionData {
	private final int startIndex;
	private final int endIndex;
	private final int nSpectra;
	private final List<DataReduction2DToolSpectrumDataNode> nodes;
	
	public DataReduction2DToolRegionData(final int startIndex, final int endIndex, List<DataReduction2DToolSpectrumDataNode> nodes) {
		if (nodes.get(0).getIndex() != startIndex || nodes.get(nodes.size()-1).getIndex() != endIndex)
			throw new IllegalArgumentException(MessageFormat.format("RegionData arguments mismatch -> {0} vs {1} and {2} vs {3}", nodes.get(0).getIndex(), startIndex, nodes.get(nodes.size()-1).getIndex(), endIndex));
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.nSpectra = nodes.size();
		this.nodes = nodes;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public int getnSpectra() {
		return nSpectra;
	}
	
	public List<DataReduction2DToolSpectrumDataNode> getNodes() {
		return nodes;
	}
}
