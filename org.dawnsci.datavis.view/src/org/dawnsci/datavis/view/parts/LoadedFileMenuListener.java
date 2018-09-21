package org.dawnsci.datavis.view.parts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.conversion.ui.api.IFileOverrideWizard;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileController;
import org.dawnsci.datavis.model.FileJoining;
import org.dawnsci.datavis.model.IDataObject;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.view.Activator;
import org.dawnsci.datavis.view.parts.LoadedFilePart.LabelEditingSupport;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.wizards.IWizardDescriptor;


public class LoadedFileMenuListener implements IMenuListener {


	private TableViewer viewer;
	private IFileController fileController;
	private LabelEditingSupport editColumn;
	private String id;
	private ImageDescriptor convertImage = Activator.getImageDescriptor("icons/convert.png");

	public LoadedFileMenuListener(IFileController fileController, TableViewer viewer,LabelEditingSupport editColumn) {
		this.fileController = fileController;
		this.viewer = viewer;
		this.editColumn = editColumn;
		id = fileController.getID();
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
			menuDisplay.add(new SetLabelAction(fileController, viewer, editColumn));
			menuDisplay.add(new ClearLabelAction(fileController, viewer, editColumn));
			menuDisplay.add(new EditLabelAction(fileController,viewer, editColumn));
			manager.add(menuDisplay);
			manager.add(new Separator());
			manager.add(new JoinFilesAction(fileController,viewer));
			manager.add(new Separator());
			manager.add(new DeselectAction(fileController,viewer));
			manager.add(new Separator());
			manager.add(new CloseAction(fileController, viewer));
			manager.add(new Separator());
			if (getWiz()!=null) manager.add(new ConvertFilesAction(fileController, viewer));
			
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
		
		protected List<LoadedFile> getFileSelection() {
			if (LoadedFileMenuListener.this.id.equals(fileController.getID()) && view.getSelection() instanceof IStructuredSelection) {
				final IStructuredSelection selection = (IStructuredSelection) view.getSelection();
				return Arrays.stream(selection.toArray())
						.filter(LoadedFile.class::isInstance)
						.map(LoadedFile.class::cast)
						.collect(Collectors.toList());

			}
			
			return Collections.emptyList();
		}
	}
	
	private class CheckAction extends LoadedFileMenuAction {

		public CheckAction(IFileController fileController, TableViewer viewer) {
			super("Check",AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png"), fileController, viewer);
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getFileSelection();
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
			List<LoadedFile> loadedFiles = getFileSelection();
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
			List<LoadedFile> loadedFiles = getFileSelection();
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
			List<LoadedFile> loadedFiles = getFileSelection();
			if (loadedFiles.isEmpty()) return;
			
			List<IDataObject> options = loadedFiles.stream()
					.flatMap(l -> l.getDataOptions().stream())
					.filter(DataOptions::isSelected)
					.map(IDataObject.class::cast)
					.collect(Collectors.toList());

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
			List<LoadedFile> deselected = getFileSelection();
			if (deselected.isEmpty()) return;

			file.unloadFiles(deselected);
			
			List<LoadedFile> files = file.getLoadedFiles();

			if (!files.isEmpty()) {
				viewer.setSelection(new StructuredSelection(files.get(0)), true);
			}
			view.refresh();
		}
	}
	
	private class SetLabelAction extends LoadedFileMenuAction {

		private LabelEditingSupport editColumn;
		
		public SetLabelAction(IFileController fileController, TableViewer viewer, LabelEditingSupport editColumn) {
			super("Set Label",null, fileController, viewer);
			this.editColumn = editColumn;
		}

		@Override
		public void run() {
			List<LoadedFile> loadedFiles = getFileSelection();
			if (loadedFiles.isEmpty()) return;
			
			Collection<String> options = loadedFiles.get(0).getLabelOptions();
			
			if (options.isEmpty()) {
				return;
			}
			
			List<String> copyOptions = new ArrayList<>(options);
			Collections.sort(copyOptions);
			
			ListDialog d = new ListDialog(Display.getDefault().getActiveShell());
			d.setTitle("Select item for label");
			d.setContentProvider(new ArrayContentProvider());
			d.setLabelProvider(new LabelProvider());

			d.setInput(copyOptions);
			
			if (Dialog.OK != d.open()) {
				return;
			}
			
			String labelName = d.getResult()[0].toString();
			
			fileController.setLabelName(labelName);
			
			Layout layout = view.getTable().getParent().getLayout();
			
			if (layout instanceof TableColumnLayout) {
				TableColumn column = view.getTable().getColumn(2);
				column.setText(labelName);
				((TableColumnLayout)layout).setColumnData(column, new ColumnWeightData(50,20));
			}
			
			editColumn.setCanEdit(false);
			
			view.refresh();
			view.getTable().getParent().layout();
		}
	}
	
	private class EditLabelAction extends LoadedFileMenuAction {

		private LabelEditingSupport editColumn;
		
		public EditLabelAction(IFileController fileController, TableViewer viewer, LabelEditingSupport editColumn) {
			super("User Editable Labels",null, fileController, viewer);
			this.editColumn = editColumn;
		}

		@Override
		public void run() {
			fileController.setLabelName("User Editable Label");
			
			Layout layout = view.getTable().getParent().getLayout();
			
			if (layout instanceof TableColumnLayout) {
				TableColumn column = view.getTable().getColumn(2);
				column.setText("User Editable Label");
				((TableColumnLayout)layout).setColumnData(column, new ColumnWeightData(50,20));
			}
			
			editColumn.setCanEdit(true);
			
			view.refresh();
			view.getTable().getParent().layout();
		}
	}
	
	private class ClearLabelAction extends LoadedFileMenuAction {

		private LabelEditingSupport editColumn;
		
		public ClearLabelAction(IFileController fileController, TableViewer viewer, LabelEditingSupport editColumn) {
			super("Clear",null, fileController, viewer);
			this.editColumn = editColumn;
		}

		@Override
		public void run() {
			
			fileController.setLabelName("");
			
			Layout layout = view.getTable().getParent().getLayout();
			
			if (layout instanceof TableColumnLayout) {
				TableColumn column = view.getTable().getColumn(2);
				((TableColumnLayout)layout).setColumnData(column, new ColumnWeightData(0,0));
			}
			
			editColumn.setCanEdit(false);
			view.refresh();
			view.getTable().getParent().layout();
		}
	}
	
	private class JoinFilesAction extends LoadedFileMenuAction {

		public JoinFilesAction(IFileController fileController, TableViewer viewer) {
			super("Create joined file",null, fileController, viewer);
		}

		@Override
		public void run() {
			String joinedFilePath = FileJoining.autoFileJoiner(filepathGenerator(getFileSelection()));
			if (fileController instanceof FileController) {
				((FileController)fileController).loadFiles(new String[]{joinedFilePath}, null, false);
			}
			view.refresh();
		}
		
		private List<String> filepathGenerator(List<LoadedFile> loadedFileList) {
			Iterator<LoadedFile> fileIterator = loadedFileList.iterator();
			List<String> filePaths = new ArrayList<>();
			
			while (fileIterator.hasNext()) {
				LoadedFile currentFile = fileIterator.next();
				filePaths.add(currentFile.getFilePath());
			}
			
			return filePaths;
		}
	}

	private class ConvertFilesAction extends LoadedFileMenuAction {
		public ConvertFilesAction(IFileController fileController, TableViewer viewer) {
			super("Convert", convertImage, fileController, viewer);
		}

		@Override
		public void run() {
			
			IWizardDescriptor wiz = getWiz();
			IWorkbenchWizard wizard = null;
			try {
				wizard = wiz.createWizard();
			} catch (CoreException e) {
				//TODO log
			}
			
			if (wizard == null || !(wizard instanceof IFileOverrideWizard)) {
				//log
				return;
			}
			
			List<File> selFiles = getFileSelection()
								.stream()
								.map(loadedFile -> new File(loadedFile.getFilePath()))
								.collect(Collectors.toList());
			((IFileOverrideWizard)wizard).setFileSelectionOverride(selFiles);
			WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
			dialog.setPageSize(new Point(400, 450));
			dialog.create();
			dialog.open();
		}
	}
	
	private static IWizardDescriptor getWiz() {
		return PlatformUI.getWorkbench().getExportWizardRegistry().findWizard("org.dawnsci.conversion.ui.convertExportWizard");
	}
}

