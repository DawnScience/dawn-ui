package org.dawnsci.datavis.manipulation;

import java.time.Instant;
import java.util.Arrays;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.RawTextSaver;

public class FileWritingUtils {
	private static Logger logger = LoggerFactory.getLogger(FileWritingUtils.class);

	
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
		if (logger.isDebugEnabled()) {
			logger.debug("Data's first dimension axes:");
			for (ILazyDataset a: axes) {
				logger.debug("{}", a == null ? "null" : Arrays.toString(a.getShape()));
			}
		}

		IDataset[] dArray = null;
		if (axes[0] != null) {
			
			dArray = new IDataset[axes.length < 2 || axes[1] == null ? 2 : 3];
			
			try {
				
				dArray[0] = getAxisPadded(axes[0]);
				data.setShape(data.getSize(),1);
				dArray[1] = data;
				if (dArray.length > 2) {
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
	
	public static boolean writeDat(String path, Dataset data) {
		String name = "data";
		if (data.getName() != null) {
			name = data.getName();
		}
		
		RawTextSaver saver = new RawTextSaver(path);
		DataHolder dh = new DataHolder();
		AxesMetadata md = data.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] axes = md.getAxes();
		IDataset[] dArray = new IDataset[4];
		int shape = data.getShapeRef()[0];
		data.setShape(shape, 1);
		dArray[1] = data;
		if (axes[0] != null) {
			try {
				dArray[0] = getAxisPadded(axes[0]);
			} catch (DatasetException e) {
				return false;
			}
		} else {
			return false;
		}
		if (data.hasErrors()) {
			dArray[2] = data.getErrors();
		} else {
			dArray[2] = DatasetFactory.zeros(shape, 1);
		}
		if (axes[0].hasErrors()) {
			try {
				dArray[3] = getAxisPadded(axes[0].getErrors());
			} catch (DatasetException e) {
				return false;
			}
		} else {
			dArray[3] = DatasetFactory.zeros(shape, 1);
		}
		
		data = DatasetUtils.concatenate(dArray, 1);
		dh.addDataset(name, data);
		
		try {
			saver.saveFile(dh);
		} catch (ScanFileHolderException e) {
			return false;
		}
		return true;
	}
	
	private static IDataset getAxisPadded(ILazyDataset l) throws DatasetException {
		Dataset y = DatasetUtils.sliceAndConvertLazyDataset(l);
		y.setShape(y.getSize(), 1);
		return y;
	}

	/**
	 * Write processed data in Nexus format
	 * @param fileFactory
	 * @param shell (can be null if no error dialog wanted)
	 * @param dawnClass
	 * @param filePath
	 * @param entryName
	 * @param dataset
	 * @return true if 
	 */
	public static boolean writeProcessedData(INexusFileFactory fileFactory, Shell shell, String dawnClass, String filePath, String entryName, Dataset dataset) {
		try (NexusFile nexus = fileFactory.newNexusFile(filePath)) {
			nexus.createAndOpenToWrite();
			GroupNode entry = NexusUtils.writeNXclass(nexus, null, entryName, NexusConstants.ENTRY);

			GroupNode data = NexusUtils.writeNXdata(nexus, entry, NexusConstants.DATA_DATA, dataset);
			NexusUtils.writeStringAttribute(nexus, null, NexusConstants.DEFAULT, entryName);
			NexusUtils.writeStringAttribute(nexus, entry, NexusConstants.DEFAULT, NexusConstants.DATA_DATA);
	
			// add NXprocess info
			GroupNode process = NexusUtils.writeNXclass(nexus, entry, "process", NexusConstants.PROCESS);
			NexusUtils.writeString(nexus, process, "program", "DAWN." + dawnClass);
			NexusUtils.writeString(nexus, process, "version", BundleUtils.getDawnVersion());
			NexusUtils.writeString(nexus, process, "date", Instant.now().toString());
			NexusUtils.writeStringAttribute(nexus, process, NexusConstants.DEFAULT, NexusConstants.DATA_DATA);
			nexus.link(nexus.getPath(data), nexus.getPath(process));
			// NXnote or NXparameters
			return true;
		} catch (Exception e) {
			if (shell != null) {
				MessageDialog.openError(shell, "Error", "Writing processed data:" + e.getMessage());
			}
		}
		return false;
	}
}
