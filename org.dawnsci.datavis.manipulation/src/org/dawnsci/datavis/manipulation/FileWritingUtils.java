package org.dawnsci.datavis.manipulation;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.RawTextSaver;

public class FileWritingUtils {

	
	public static boolean writeNexus(String path, INexusFileFactory fileFactory, Dataset data) {
		try (NexusFile nexus = fileFactory.newNexusFile(path)) {
			String name = "data";
			if (data.getName() != null) {
				name = data.getName();
			}
			nexus.openToWrite(true);
//			data.setName(NexusConstants.DATA_DATA);
			GroupNode nxdata = nexus.getGroup("/entry/"+name, true);
			nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
			GroupNode nxentry = nexus.getGroup("/entry", true);
			nexus.addAttribute(nxentry,TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			data.setName(NexusConstants.DATA_DATA);
			nexus.createData(nxdata, data);
			nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, NexusConstants.DATA_DATA));

			AxesMetadata md = data.getFirstMetadata(AxesMetadata.class);

			ILazyDataset[] axes = md.getAxes();

			String axName = null;

			if (axes[0] != null) {
				IDataset y = axes[0].getSlice();
				y = y.squeeze();
				if (y.getName() != null) {
					axName = MetadataPlotUtils.removeSquareBrackets(y.getName());
					y.setName(axName);
				} else {
					axName = "y_axis";
				}
				nexus.createData(nxdata, y);
				nexus.addAttribute(nxdata, TreeFactory.createAttribute(axName + NexusConstants.DATA_INDICES_SUFFIX, 0));
			}

			nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.DATA_AXES, axName));
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public static boolean writeText(String path, Dataset data) {
		
		String name = "data";
		
		if (data.getName() != null) {
			name = data.getName();
		}
		
		RawTextSaver saver = new RawTextSaver(path);
		DataHolder dh = new DataHolder();
		AxesMetadata md = data.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] axes = md.getAxis(0);
		IDataset[] dArray = null;
		if (axes[0] != null) {
			
			dArray = new IDataset[axes[1] == null ? 2 : 3];
			
			try {
				
				dArray[0] = getAxisPadded(axes[0]);
				data.setShape(data.getShape()[0],1);
				dArray[1] = data;
				if (axes[1] != null) {
					dArray[2] = getAxisPadded(axes[1]);
				}
				data = DatasetUtils.concatenate(dArray, 1);
			} catch (DatasetException e) {
				return false;
			}
		}

		dh.addDataset(name, data);
		
		try {
			saver.saveFile(dh);
		} catch (ScanFileHolderException e) {
			return false;
		}
		
		return true;
	}
	
	private static IDataset getAxisPadded(ILazyDataset l) throws DatasetException {
		IDataset y = l.getSlice();
		y = y.squeeze();
		y.setShape(y.getShape()[0],1);
		return y;
	}
		
		
	
}
