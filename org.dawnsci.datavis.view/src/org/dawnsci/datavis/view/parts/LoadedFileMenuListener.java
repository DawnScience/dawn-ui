package org.dawnsci.datavis.view.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.IDataObject;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ListDialog;
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
			
			MenuManager menuDisplay = new MenuManager("Display");
			menuDisplay.add(new SetLabelAction(fileController, viewer));
			menuDisplay.add(new ClearLabelAction(fileController, viewer));
			menuDisplay.add(new Separator());
			menuDisplay.add(new DisableSort(fileController, viewer));
			manager.add(menuDisplay);
			manager.add(new Separator());
			manager.add(new JoinAction(fileController,viewer));
			manager.add(new Separator());
			manager.add(new DeselectAction(fileController,viewer));
			manager.add(new Separator());
			manager.add(new CloseAction(fileController, viewer));
			
			if (((IStructuredSelection)viewer.getSelection()).size()==1) {
				manager.add(new Separator());
				manager.add(new ApplyToAllAction(fileController, viewer));
			}
			
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
	
	private class ApplyToAllAction extends LoadedFileMenuAction {

		public ApplyToAllAction(IFileController fileController,
				TableViewer viewer) {
			super("Apply to all", null, fileController, viewer);
		}
		
		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			
			fileController.applyToAll(loadedFiles.get(0));

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
	
	public class JoinAction extends LoadedFileMenuAction {

		public JoinAction(IFileController fileController, TableViewer viewer) {
			super("Create joined file",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			file.joinFiles(loadedFiles);
			view.refresh();
		}
	}
	
	private class SetLabelAction extends LoadedFileMenuAction {

		public SetLabelAction(IFileController fileController, TableViewer viewer) {
			super("Set Label",null, fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getLoadedFiles();
			if (loadedFiles.isEmpty()) return;
			
			Collection<String> options = loadedFiles.get(0).getLabelOptions();
			
			ListDialog d = new ListDialog(Display.getDefault().getActiveShell());
			d.setContentProvider(new ArrayContentProvider());
			d.setLabelProvider(new LabelProvider());
			
			if (options.isEmpty()) {
				return;
			}
			
			d.setInput(options);
			
			if (Dialog.OK != d.open()) {
				return;
			}
			
			String labelName = d.getResult()[0].toString();
			
			((FileController)fileController).setLabelName(labelName);
			
			Layout layout = view.getTable().getParent().getLayout();
			
			if (layout instanceof TableColumnLayout) {
				TableColumn column = view.getTable().getColumn(2);
				column.setText(labelName);
				((TableColumnLayout)layout).setColumnData(column, new ColumnWeightData(50,20));
			}
			
			view.refresh();
			view.getTable().getParent().layout();
		}
	}
	
	private class ClearLabelAction extends LoadedFileMenuAction {

		public ClearLabelAction(IFileController fileController, TableViewer viewer) {
			super("Clear",null, fileController, viewer);
		}

		@Override
		public void run() {
			
			((FileController)fileController).setLabelName("");
			
			Layout layout = view.getTable().getParent().getLayout();
			
			if (layout instanceof TableColumnLayout) {
				TableColumn column = view.getTable().getColumn(2);
				((TableColumnLayout)layout).setColumnData(column, new ColumnWeightData(0,0));
			}
			
			view.refresh();
			view.getTable().getParent().layout();
		}
	}
	
	private class DisableSort extends LoadedFileMenuAction {

		public DisableSort(IFileController fileController, TableViewer viewer) {
			super("Disable sort",null, fileController, viewer);
		}

		@Override
		public void run() {
			
			((FileController)fileController).setComparator(null);
			view.refresh();
		}
	}
}

