package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.dialog.ImageGridDialog;
import org.dawnsci.mapping.ui.dialog.MapPropertiesDialog;
import org.dawnsci.mapping.ui.dialog.RGBMixerDialog;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

public class MapActionUtils {

	public static IAction getRGBDialog(final List<MappedData> maps, final MappedDataFile mdf, final TreeViewer viewer) {
		final List<IDataset> dataList = new ArrayList<IDataset>(maps.size());
		for (MappedData map : maps) {
			IDataset data = map.getMap();
			data.setName(map.toString());
			dataList.add(map.getMap());
		}
		IAction action = new Action("RGB Mixer...") {
			@Override
			public void run() {
				RGBMixerDialog dialog;
				try {
					dialog = new RGBMixerDialog(dataList);
					dialog.createContents();
					if (dialog.open() == RGBMixerDialog.CANCEL_ID)
						return;
					IDataset rgb = dialog.getRGBDataset();
					if (rgb == null) return;
					rgb.addMetadata(maps.get(0).getMap().getMetadata(AxesMetadata.class).get(0));
					MappedData m = maps.get(0).makeNewMapWithParent("RGB", rgb);
					mdf.addMapObject("RGB", m);
					viewer.refresh();
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error opening RGB Mixer",
							"The following error occured while opening the RGB Mixer dialog: " + e);
				}
				
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/rgb.png"));
		return action;
	}
	
	public static IAction getComparisonDialog(final List<MappedData> maps) {
		final List<IDataset> dataList = new ArrayList<IDataset>(maps.size());
		for (MappedData map : maps) {
			IDataset data = map.getMap();
			data.setName(map.toString());
			dataList.add(map.getMap());
		}
		IAction action = new Action("Comparison Viewer...") {
			@Override
			public void run() {
				ImageGridDialog dialog;
				try {
					dialog = new ImageGridDialog(dataList);
					dialog.createContents();
					dialog.open();
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error Comparison Viewer",
							"The following error occured while opening the Comparison Viewer dialog: " + e);
				}
				
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/images-stack.png"));
		return action;
	}
	
	public static IAction getMapPropertiesAction(final MappedData map, final MapPlotManager manager, final MappedDataFile file) {
		IAction trans = new Action("Properties...") {
			@Override
			public void run() {
				
				MapPropertiesDialog dialog;
				try {
					dialog = new MapPropertiesDialog(Display.getDefault().getActiveShell(), map, file,manager);
					dialog.open();
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error Properties Viewer",
							"The following error occured while opening the Properties Viewer dialog: " + e);
				}
				
			}
		};
		
		return trans;
		
	}
	
	public static IAction getFileRemoveAction(final MappedFileManager manager, final MappedDataFile file) {
		IAction trans = new Action("Clear") {
			@Override
			public void run() {
				manager.removeFile(file);
			}
		};
		
		return trans;
		
	}
}
