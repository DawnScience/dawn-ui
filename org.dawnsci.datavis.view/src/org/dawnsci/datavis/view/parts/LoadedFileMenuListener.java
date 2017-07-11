package org.dawnsci.datavis.view.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IDataObject;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class LoadedFileMenuListener implements IMenuListener {


	private StructuredViewer viewer;
	private IFileController fileController;

	public LoadedFileMenuListener(IFileController fileController, StructuredViewer viewer) {
		this.fileController = fileController;
		this.viewer = viewer;
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		if (viewer.getSelection().isEmpty())
			return;
		if (viewer.getSelection() instanceof IStructuredSelection) {

			manager.add(new CheckAction(fileController, viewer));
			manager.add(new UncheckAction(fileController, viewer));
			manager.add(new Separator());
			manager.add(new DeselectAction(fileController,viewer));
			manager.add(new Separator());
			manager.add(new CloseAction(fileController, viewer));
		}
	}

	
	private abstract class LoadedFileMenuAction extends Action {
		
		private StructuredViewer viewer;
		private IFileController fileController;
		
		public LoadedFileMenuAction(String text, ImageDescriptor image, IFileController fileController, StructuredViewer viewer) {
			super(text,image);
			this.viewer = viewer;
			this.fileController = fileController;
		}
		
		protected List<LoadedFile> getLoadedFiles(){
			if (viewer.getSelection() instanceof IStructuredSelection) {
				final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();


				return Arrays.stream(selection.toArray())
						.filter(LoadedFile.class::isInstance)
						.map(LoadedFile.class::cast)
						.collect(Collectors.toList());

			}
			
			return new ArrayList<>();
		}
		
		
	}
	
	private class CheckAction extends LoadedFileMenuAction {

		public CheckAction(IFileController fileController, StructuredViewer viewer) {
			super("Check",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png"), fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			fileController.selectFiles(loadedFiles, true);
			viewer.refresh();
		}
	}
	
	private class UncheckAction extends LoadedFileMenuAction {

		public UncheckAction(IFileController fileController, StructuredViewer viewer) {
			super("Uncheck",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif"), fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			fileController.selectFiles(loadedFiles, false);
			viewer.refresh();
		}
	}
	
	private class DeselectAction extends LoadedFileMenuAction {

		public DeselectAction(IFileController fileController, StructuredViewer viewer) {
			super("Deselect datasets",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			
			List<IDataObject> options = loadedFiles.stream().flatMap(l -> l.getDataOptions().stream())
					.filter(DataOptions::isSelected).map(IDataObject.class::cast).collect(Collectors.toList());;

			if (options.isEmpty()) return;

			fileController.deselect(options);
			
			viewer.refresh();
		}
	}
	
	private class CloseAction extends LoadedFileMenuAction {

		public CloseAction(IFileController fileController, StructuredViewer viewer) {
			super("Close",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			fileController.unloadFiles(loadedFiles);
			viewer.refresh();
		}
	}
}

