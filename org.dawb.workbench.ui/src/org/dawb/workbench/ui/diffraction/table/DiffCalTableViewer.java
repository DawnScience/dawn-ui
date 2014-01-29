package org.dawb.workbench.ui.diffraction.table;

import java.util.List;

import org.dawb.workbench.ui.Activator;
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

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;

/**
 * 
 * @author wqk87977
 *
 */
public class DiffCalTableViewer extends TableViewer {

	private Action deleteAction;
	//private List<DiffractionTableData> model = new ArrayList<DiffractionTableData>();
	private DiffractionDataManager manager;
	private Composite parent;
	private List<String> pathsList;
	private IDetectorPropertyListener detectorPropertyListener;
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
	public DiffCalTableViewer(Composite parent, List<String> pathsList, DiffractionDataManager manager) {
		super(parent, SWT.FULL_SELECTION
				| SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.parent = parent;
		this.pathsList = pathsList;
		this.table = getTable();
		this.manager = manager;
		
		initialize();
		createColumns(this);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText(
				"Drag/drop file(s)/data to this table");
		
		setContentProvider(new DiffCalContentProvider());
		setLabelProvider(new DiffCalLabelProvider());
		setInput(manager);
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
		infoEditableLabel.setText("* Click to change value");
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
						manager.loadData(res[i].getRawLocation().toOSString(), null);
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
							manager.loadData(node.getFile().getFilename(), node.getFullName());
						} else if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							manager.loadData(file.getLocation().toOSString(), null);
						}
					}
				} else if (dropData instanceof String[]) {
					String[] selectedData = (String[]) dropData;
					for (int i = 0; i < selectedData.length; i++) {
						manager.loadData(selectedData[i], null);
					}
				}
			}
		};

		deleteAction = new Action("Delete item", Activator.getImageDescriptor("icons/delete_obj.png")) {
			@Override
			public void run() {
				StructuredSelection selection = (StructuredSelection) getSelection();
				Object[] selected = selection.toArray();
				for (int i = 0; i < selected.length; i++) {
					DiffractionTableData selectedData = (DiffractionTableData) selected[i];
					if (manager.getModel().size() > 0) {
						if (manager.getModel().remove(selectedData)) {
							if (selectedData.md != null)
								selectedData.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
						}
					}
				}
				if (!manager.getModel().isEmpty()) {
					setSelection(new StructuredSelection((DiffractionTableData) getElementAt(0)));
				} else {
					setSelection(new StructuredSelection());
				}
			}
		};
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
	public void updateTableColumnsAndLayout(int tabIndex) {
		TableColumn[] columns = table.getColumns();
		for (int i = 2; i < columns.length; i++) {
			if (tabIndex == 0) {	// auto mode
				int width = 0;
				// if more than one image and distance column index
				if (manager.getModel().size() > 1 && i == 3)
					width = 80;
				table.getColumns()[i].setWidth(width);
			} else if (tabIndex == 1) {	// manual mode
				int width = 80;
				// if less than 2 images and column is distance
				if (manager.getModel().size() <= 1 && i == 3)
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
