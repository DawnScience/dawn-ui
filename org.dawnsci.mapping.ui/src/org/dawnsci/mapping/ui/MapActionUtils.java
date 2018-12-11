package org.dawnsci.mapping.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.datavis.api.IFileOpeningController;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.dawnsci.mapping.ui.datamodel.RGBMapData;
import org.dawnsci.mapping.ui.datamodel.VectorMapData;
import org.dawnsci.mapping.ui.dialog.DynamicDialog;
import org.dawnsci.mapping.ui.dialog.ImageGridDialog;
import org.dawnsci.mapping.ui.dialog.MapPropertiesDialog;
import org.dawnsci.mapping.ui.dialog.RGBMixerDialog;
import org.dawnsci.mapping.ui.dialog.VectorMixerDialog;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapActionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(MapActionUtils.class);

	private static final ImageDescriptor datavisImage = Activator.getImageDescriptor("icons/color-swatch.png");
	
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
	
	public static IAction getVectorDialog(final List<AbstractMapData> maps, final MappedDataFile mdf, final TreeViewer viewer) {
		final List<AbstractMapData> dataList = new ArrayList<AbstractMapData>(maps.size());
		for (AbstractMapData map : maps) {
			dataList.add(map);
		}
		IAction action = new Action("Vector Plotter...") {
			@Override
			public void run() {
				VectorMixerDialog dialog;
				try {
					dialog = new VectorMixerDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),dataList);
					if (dialog.open() == Dialog.CANCEL)
						return;

					VectorMapData vectorMap = dialog.getVectorMap();
					if (vectorMap == null) return;

					mdf.addMapObject(vectorMap.toString(), vectorMap);
					viewer.refresh();
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Error opening Vector Mixer",
							"The following error occured while opening the Vector Mixer dialog: " + e);
				}
				
			}
		};
		action.setImageDescriptor(Activator.getImageDescriptor("icons/map.png"));
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
					dialog = new ImageGridDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), maps);
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
	
	public static IAction getMapPropertiesAction(final AbstractMapData map, final IMapPlotController manager, final MappedDataArea area) {
		return new Action("Properties...") {
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
		
		
	}
	
	public static IAction getFileRemoveAction(final IMapFileController manager, final MappedDataFile file) {
		return new Action("Clear") {
			@Override
			public void run() {
				manager.removeFile(file);
			}
		};
		
		
	}
	
	public static IAction transferToDataVisAction(final List<MappedDataFile> files) {
		
		return new Action("DataVis",datavisImage) {
			@Override
			public void run() {
				List<String> collect = files.stream().map(MappedDataFile::getPath).collect(Collectors.toList());
				if (collect.isEmpty()) return;
				
				IFileOpeningController fs = Activator.getService(IFileOpeningController.class);
				
				fs.loadFiles(collect.toArray(new String[collect.size()]), false);
				
				try {
					PlatformUI.getWorkbench().showPerspective("org.dawnsci.datavis.DataVisPerspective",PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					logger.error("Could not switch to DataVis perspective", e);
				}
			}
		};
		
	}
	
	public static IAction getFilesRemoveAction(final IMapFileController manager, final List<MappedDataFile> files) {
		return new Action("Clear files") {
			@Override
			public void run() {
				for (MappedDataFile file :files) manager.removeFile(file);
			}
		};
		
	}
	
	public static IAction getNonLiveFilesRemoveAction(final IMapFileController manager) {
		return new Action("Clear all finished scans") {
			@Override
			public void run() {
				manager.clearNonLiveFiles();
			}
		};
		
	}
	
	public static IAction getFilesRemoveAllAction(final IMapFileController manager) {
		return new Action("Clear all files") {
			@Override
			public void run() {
				manager.clearAll();
			}
		};
		
	}
	
	public static IAction getFilesRemoveAction(final IMapFileController manager) {
		return new Action("Clear all") {
			@Override
			public void run() {
				manager.clearAll();
			}
		};
		
	}
	
	public static IAction getUnPlotAllAction(final IMapPlotController manager, final TreeViewer viewer, IMapFileController controller) {
		return new Action("Clear plot") {
			@Override
			public void run() {
				controller.removeAllFromDisplay();
			}
		};

	}
	
	public static IAction getDynamicViewerAction(final MappedDataBlock block) {
		return new Action("Dynamic Viewer...") {
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
		
	}
	
	public static IAction getSaveImageAction(final AssociatedImage image) {
		return new Action("Save image...") {
			@Override
			public void run() {
				
				FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);

				dialog.setPath(System.getProperty("user.home")+ File.separator + "image.nxs");
			
				dialog.create();
				
				BundleContext bundleContext =
		                FrameworkUtil.
		                getBundle(this.getClass()).
		                getBundleContext();
				
				INexusFileFactory f = bundleContext.getService(bundleContext.getServiceReference(INexusFileFactory.class));
				
				if (dialog.open() == Dialog.OK) {
					MappingUtils.saveRegisteredImage(image, dialog.getPath(), f);
				}
			}
		};
		
	}
	
	public static IAction getBringToFrontAction(final PlottableMapObject map, final IMapPlotController manager) {
		return new Action("Bring to front") {
			@Override
			public void run() {
				manager.bringToFront(map);
			}
		};
	}
	
	public static IAction getSendToBackAction(final PlottableMapObject map, final IMapPlotController manager) {
		return new Action("Send to back") {
			@Override
			public void run() {
				manager.sendToBack(map);
			}
		};
	}
}
