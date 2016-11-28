package org.dawnsci.mapping.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.dawnsci.mapping.ui.datamodel.RGBMapData;
import org.dawnsci.mapping.ui.dialog.DynamicDialog;
import org.dawnsci.mapping.ui.dialog.ImageGridDialog;
import org.dawnsci.mapping.ui.dialog.MapPropertiesDialog;
import org.dawnsci.mapping.ui.dialog.RGBMixerDialog;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class MapActionUtils {

	public static IAction getRGBDialog(final List<AbstractMapData> maps, final MappedDataFile mdf, final TreeViewer viewer) {
		final List<AbstractMapData> dataList = new ArrayList<AbstractMapData>(maps.size());
		for (AbstractMapData map : maps) {
			dataList.add(map);
		}
		IAction action = new Action("RGB Mixer...") {
			@Override
			public void run() {
				RGBMixerDialog dialog;
				try {
					dialog = new RGBMixerDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),dataList);
					if (dialog.open() == Dialog.CANCEL)
						return;

					RGBMapData rgb = dialog.getRGBMap();
					if (rgb == null) return;

					mdf.addMapObject(rgb.toString(), rgb);
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
	
	public static IAction getComparisonDialog(final List<AbstractMapData> maps) {
		final List<IDataset> dataList = new ArrayList<IDataset>(maps.size());
		for (AbstractMapData map : maps) {
			dataList.add(map.getMap());
		}
		IAction action = new Action("Comparison Viewer...") {
			@Override
			public void run() {
				ImageGridDialog dialog;
				try {
					dialog = new ImageGridDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),dataList);
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
	
	public static IAction getMapPropertiesAction(final AbstractMapData map, final MapPlotManager manager, final MappedDataArea area) {
		IAction trans = new Action("Properties...") {
			@Override
			public void run() {
				
				MapPropertiesDialog dialog;
				try {
					dialog = new MapPropertiesDialog(Display.getDefault().getActiveShell(), map, area,manager);
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
	
	public static IAction getFilesRemoveAction(final MappedFileManager manager, final List<MappedDataFile> files) {
		IAction trans = new Action("Clear files") {
			@Override
			public void run() {
				for (MappedDataFile file :files) manager.removeFile(file);
			}
		};
		
		return trans;
	}
	
	public static IAction getFilesRemoveAction(final MappedFileManager manager) {
		IAction trans = new Action("Clear all") {
			@Override
			public void run() {
				manager.clearAll();
			}
		};
		
		return trans;
	}
	
	public static IAction getDynamicViewerAction(final MappedDataBlock block) {
		IAction trans = new Action("Dynamic Viewer...") {
			@Override
			public void run() {
				
				DynamicDialog dialog;
				try {
					dialog = new DynamicDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), block);
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
	
	public static IAction getSaveImageAction(final AssociatedImage image) {
		IAction save = new Action("Save image...") {
			@Override
			public void run() {
				
				FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);

				dialog.setPath(System.getProperty("user.home")+ File.separator + "image.nxs");
				
			
				dialog.create();
				
				if (dialog.open() == Dialog.OK) {
					MappingUtils.saveRegisteredImage(image, dialog.getPath());
				}
			}
		};
		
		return save;
		
	}
}
