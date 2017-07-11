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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class LoadedFileMenuListener implements IMenuListener {


	private TableViewer viewer;
	private IFileController fileController;

	public LoadedFileMenuListener(IFileController fileController, TableViewer viewer) {
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
		
		protected TableViewer view;
		protected IFileController file;
		
		public LoadedFileMenuAction(String text, ImageDescriptor image, IFileController fileController, TableViewer viewer) {
			super(text,image);
			this.view = viewer;
			this.file = fileController;
		}
		
		protected List<LoadedFile> getLoadedFiles(){
			if (view.getSelection() instanceof IStructuredSelection) {
				final IStructuredSelection selection = (IStructuredSelection) view.getSelection();


				return Arrays.stream(selection.toArray())
						.filter(LoadedFile.class::isInstance)
						.map(LoadedFile.class::cast)
						.collect(Collectors.toList());

			}
			
			return new ArrayList<>();
		}
		
		
	}
	
	private class CheckAction extends LoadedFileMenuAction {

		public CheckAction(IFileController fileController, TableViewer viewer) {
			super("Check",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png"), fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			file.selectFiles(loadedFiles, true);
			view.refresh();
		}
	}
	
	private class UncheckAction extends LoadedFileMenuAction {

		public UncheckAction(IFileController fileController, TableViewer viewer) {
			super("Uncheck",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif"), fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			file.selectFiles(loadedFiles, false);
			view.refresh();
		}
	}
	
	private class DeselectAction extends LoadedFileMenuAction {

		public DeselectAction(IFileController fileController, TableViewer viewer) {
			super("Deselect datasets",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			
			List<IDataObject> options = loadedFiles.stream().flatMap(l -> l.getDataOptions().stream())
					.filter(DataOptions::isSelected).map(IDataObject.class::cast).collect(Collectors.toList());;

			if (options.isEmpty()) return;

			file.deselect(options);
			
			view.refresh();
		}
	}
	
	public class CloseAction extends LoadedFileMenuAction {

		public CloseAction(IFileController fileController, TableViewer viewer) {
			super("Close",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			file.unloadFiles(loadedFiles);
			
			int i = view.getTable().getItemCount();
			
			if (i > 0) {
				Object ob = viewer.getTable().getItem(i-1).getData();
				viewer.setSelection(new StructuredSelection(ob),true);
			}
			view.refresh();
		}
	}
}

