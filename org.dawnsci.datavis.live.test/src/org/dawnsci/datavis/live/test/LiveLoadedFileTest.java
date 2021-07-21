package org.dawnsci.datavis.live.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.datavis.live.LiveLoadedFile;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.PlotController;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.dataset.MockDynamicLazyDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;


public class LiveLoadedFileTest {

	@Test
	public void testLoadedFile() throws Exception {
		
		String name = "/file/nothing.nxs";
		
		IRemoteDatasetService remote = mock(IRemoteDatasetService.class);
		ILoaderService local = mock(ILoaderService.class);
		
		Tree t = makeTree();
		
		Map<String, NodeLink> map = TreeUtils.treeBreadthFirstSearch(t.getGroupNode(), n -> n.getDestination() instanceof DataNode, false, null);
		
		
		MockRemoteHolder m = new MockRemoteHolder();
		
		for (Entry<String,NodeLink> e : map.entrySet()) {
			m.addDataset("/" + e.getKey(), ((DataNode)e.getValue().getDestination()).getDataset());
		}
		
		m.setTree(t);
		
		when(remote.createRemoteDataHolder(anyString(),anyString(),anyInt(),anyBoolean())).thenReturn(m);
		
		LiveLoadedFile f = new LiveLoadedFile(name, "nohost", 0, remote, local);
		
		assertEquals(2,f.getUninitialisedDataOptions().size());
		assertEquals(0,f.getDataOptions().size());
		
		new PlotController().initialise(f);
		f.setInitialised();
		
		assertEquals(2,f.getDataOptions().size());
		
		DataOptions dataOption = f.getDataOption("/entry/data/data");
		
		ILazyDataset lz = dataOption.getLazyDataset();
		
		AxesMetadata amd = lz.getFirstMetadata(AxesMetadata.class);
		
		assertNotNull(amd);
		ILazyDataset axis = amd.getAxis(0)[0];
		
		assertEquals("/entry/data/axis", axis.getName());
		
		
		f.refresh();
		
		

	}
	
	private Tree makeTree() {
		Tree t = TreeFactory.createTree(0, null);
		GroupNode r = t.getGroupNode();
		NXentry e = NexusNodeFactory.createNXentry();
		NXdata d = NexusNodeFactory.createNXdata();
		
		DataNode data = TreeFactory.createDataNode(0);
		DataNode dataax = TreeFactory.createDataNode(0);
		MockDynamicLazyDataset md = new MockDynamicLazyDataset(new int[][] {{10},{30},{60},{100}}, DatasetFactory.createRange(100));
		MockDynamicLazyDataset mdax = new MockDynamicLazyDataset(new int[][] {{10},{30},{60},{100}}, DatasetFactory.createRange(100));
		md.setName("/entry/data/data");
		mdax.setName("/entry/data/axis");
		
		data.setDataset(md);
		dataax.setDataset(mdax);
		d.addDataNode("data", data);
		d.addDataNode("axis", dataax);
		
		d.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, "data"));
		d.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, new String[] {"axis"}));
		d.addAttribute(TreeFactory.createAttribute("axis" + NexusConstants.DATA_INDICES_SUFFIX, 0));
		
		e.addGroupNode("data", d);
		r.addGroupNode("entry", e);
		return t;
	}
	
	private class MockRemoteHolder extends DataHolder implements IRemoteDataHolder {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void update() {
			List<ILazyDataset> list = getList();
			for (ILazyDataset l : list) {
				if (l instanceof IDynamicDataset) {
					((IDynamicDataset) l).refreshShape();
				}
			}
		}
	}
	
}
