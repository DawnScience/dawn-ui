package org.dawb.workbench.ui.diffraction.table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.workbench.ui.Activator;
import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ResourceTransfer;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;

/**
 * 
 * @author wqk87977
 *
 */
public class DiffCalTableViewer extends TableViewer {

	private Action deleteAction;
	private List<DiffractionTableData> model = new ArrayList<DiffractionTableData>();
	private TabFolder tabFolder;
	private Composite parent;
	private List<String> pathsList;
	private IDetectorPropertyListener detectorPropertyListener;
	private ILoaderService service;
	private DropTargetAdapter dropListener;
	
	private Table table;

	/**
	 * 
	 * @param parent
	 *           composite
	 * @param pathsList
	 *           list of all paths images to be displayed in the table viewer
	 * @param service
	 *           service loader, can be null
	 */
	public DiffCalTableViewer(Composite parent, List<String> pathsList, 
						ILoaderService service) {
		super(parent, SWT.FULL_SELECTION
				| SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.parent = parent;
		this.pathsList = pathsList;
		this.service = service;
		this.table = getTable();

		initialize();
		createColumns(this);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText(
				"Drag/drop file(s)/data to this table");
		
		setContentProvider(new DiffCalContentProvider());
		setLabelProvider(new DiffCalLabelProvider());
		setInput(model);
		getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		refresh();
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection)getSelection();
				Object[] selected = selection.toArray();
				if (selected.length > 0) {
					if (selected.length == 1) {
						deleteAction.setText("Delete "
								+ ((DiffractionTableData) selection
										.getFirstElement()).name);
						mgr.add(deleteAction);
					} else {
						deleteAction.setText("Delete " + selected.length
								+ " images selected");
						mgr.add(deleteAction);
					}
				}
			}
		});
		getControl().setMenu(
				mgr.createContextMenu(getControl()));
		// add drop support
		DropTarget dt = new DropTarget(getControl(), DND.DROP_MOVE
				| DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);

		Label infoEditableLabel = new Label(parent, SWT.NONE);
		infoEditableLabel.setText("* Click to change");
	}

	private void initialize(){
		detectorPropertyListener = new IDetectorPropertyListener() {
			@Override
			public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refresh();
					}
				});
			}
		};

		dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				DiffractionTableData good = null;
				if (dropData instanceof IResource[]) {
					IResource[] res = (IResource[]) dropData;
					for (int i = 0; i < res.length; i++) {
						DiffractionTableData d = createData(res[i].getRawLocation().toOSString(), null);
						if (d != null) {
							good = d;
//							setWavelength(d);
						}
					}
				} else if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						DiffractionTableData d = null;
						if (obj[i] instanceof HDF5NodeLink) {
							HDF5NodeLink node = (HDF5NodeLink) obj[i];
							if (node == null)
								return;
							d = createData(node.getFile().getFilename(), node.getFullName());
						} else if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							d = createData(file.getLocation().toOSString(), null);
						}
						if (d != null) {
							good = d;
//							setWavelength(d);
						}
					}
				} else if (dropData instanceof String[]) {
					String[] selectedData = (String[]) dropData;
					for (int i = 0; i < selectedData.length; i++) {
						DiffractionTableData d = createData(selectedData[i], null);
						if (d != null) {
							good = d;
//							setWavelength(d);
						}
					}
				}

				updateTableColumnsAndLayout();
				StructuredSelection select = (StructuredSelection)getSelection();
				DiffractionTableData currentData = (DiffractionTableData) select.getFirstElement();
				if (currentData == null && good != null) {
					table.deselectAll();
					setSelection(new StructuredSelection(good));
				}
//				if (model.size() > 0)
//					setXRaysModifiersEnabled(true);
			}
		};

		deleteAction = new Action("Delete item", Activator.getImageDescriptor("icons/delete_obj.png")) {
			@Override
			public void run() {
				StructuredSelection selection = (StructuredSelection) getSelection();
				Object[] selected = selection.toArray();
				for (int i = 0; i < selected.length; i++) {
					DiffractionTableData selectedData = (DiffractionTableData) selected[i];
					if (model.size() > 0) {
						if (model.remove(selectedData)) {
							if (selectedData.augmenter != null && service != null)
								selectedData.augmenter.deactivate(service.getLockedDiffractionMetaData()!=null);
							if (selectedData.md != null)
								selectedData.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
						}
					}
				}
//				if (!model.isEmpty()) {
//					drawSelectedData((DiffractionTableData) tableViewer.getElementAt(0));
//				} else {
//					currentData = null; // need to reset this
//					plottingSystem.clear();
//					setXRaysModifiersEnabled(false);
//					calibrateImagesButton.setEnabled(false);
//				}

				updateTableColumnsAndLayout();
				
			}
		};
	}

	/**
	 * Get the model of the DiffCalTableViewer
	 * @return
	 */
	public List<DiffractionTableData> getModel() {
		return model;
	}

	/**
	 * 
	 * @param tabFolder
	 */
	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}

	/**
	 * Add DetectorPropertyListener for the given DiffractionTableData
	 * @param data
	 */
	public void addDetectorPropertyListener(DiffractionTableData data) {
		data.md.getDetector2DProperties().addDetectorPropertyListener(detectorPropertyListener);
	}

	/**
	 * Remove DetectorPropertyListener for the given DiffractionTableData
	 * @param data
	 */
	public void removeDetectorPropertyListener (DiffractionTableData data) {
		data.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
	}

	
	/**
	 * Creates a {@link DiffractionTableData} object and updates the model in the table viewer
	 * @param filePath
	 * @param dataFullName
	 * @return
	 */
	public DiffractionTableData createData(String filePath, String dataFullName) {
		// Test if the selection has already been loaded and is in the model
		DiffractionTableData data = null;
		if (filePath == null)
			return data;

		for (DiffractionTableData d : model) {
			if (filePath.equals(d.path)) {
				data = d;
				break;
			}
		}

		if (data == null) {
			IDataset image = PlottingUtils.loadData(filePath, dataFullName);
			if (image == null)
				return data;
			int j = filePath.lastIndexOf(File.separator);
			String fileName = j > 0 ? filePath.substring(j + 1) : null;
			image.setName(fileName + ":" + image.getName());

			data = new DiffractionTableData();
			data.path = filePath;
			data.name = fileName;
			data.image = image;
			String[] statusString = new String[1];
			data.md = DiffractionTool.getDiffractionMetadata(image, filePath, service, statusString);
			model.add(data);
		}

		return data;
	}

	private void createColumns(TableViewer tv) {
		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 0));
		TableColumn tc = tvc.getColumn();
		tc.setText("Use");
		tc.setWidth(40);

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Image");
		tc.setWidth(200);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 1));

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("# of rings");
		tc.setWidth(0);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 2));

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Distance");
		tc.setToolTipText("in mm");
		if (pathsList != null && pathsList.size() <= 1) // if more than one image then we show the column
			tc.setWidth(0);
		else
			tc.setWidth(80);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 3));

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("X Position");
		tc.setToolTipText("in Pixel");
		tc.setWidth(0);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 4));

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Y Position");
		tc.setToolTipText("in Pixel");
		tc.setWidth(0);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 5));

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Residuals");
		tc.setToolTipText("Root mean of squared residuals from fit");
		tc.setWidth(0);
		tvc.setEditingSupport(new DiffCalEditingSupport(tv, 5));

	}

	/**
	 * Update the visibility of the Table columns and parent layout
	 */
	public void updateTableColumnsAndLayout() {
		if (tabFolder == null)
			return;
		int tabIndex = tabFolder.getSelectionIndex();
		TableColumn[] columns = table.getColumns();
		for (int i = 2; i < columns.length; i++) {
			if (tabIndex == 0) {	// auto mode
				int width = 0;
				// if more than one image and distance column index
				if (model.size() > 1 && i == 3)
					width = 80;
				table.getColumns()[i].setWidth(width);
			} else if (tabIndex == 1) {	// manual mode
				int width = 80;
				// if less than 2 images and column is distance
				if (model.size() <= 1 && i == 3)
					width = 0;
				table.getColumns()[i].setWidth(width);
			}
		}
		// update parent composite
		refresh();
		Rectangle r = parent.getClientArea();
		if (parent.getParent() instanceof ScrolledComposite) {
			ScrolledComposite scrollHolder = (ScrolledComposite)parent.getParent();
			scrollHolder.setMinSize(parent.computeSize(r.width, SWT.DEFAULT));
			scrollHolder.layout();
		}
	}
}
