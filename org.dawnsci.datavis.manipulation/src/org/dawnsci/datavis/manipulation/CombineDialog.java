package org.dawnsci.datavis.manipulation;

import java.io.File;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Comparisons.Monotonicity;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class CombineDialog extends Dialog implements IAdaptable{

	private static Logger logger = LoggerFactory.getLogger(CombineDialog.class);


	private Dataset data;
	private IPlottingSystem<Composite> system;
	private Image image;
	private double[] globalRange;
	private String lastPath;
	private IDataset names;

	public CombineDialog(Shell shell, IDataset data) {
		super(shell);
		this.data = DatasetUtils.convertToDataset(data);
		try {
			AxesMetadata ax = data.getFirstMetadata(AxesMetadata.class);

			ILazyDataset[] xs = ax.getAxis(0);
			Dataset range = null;
			if (xs != null && xs[0] != null) {
				names = DatasetUtils.sliceAndConvertLazyDataset(xs[0]).squeeze();
				names.setName("Names");
				if (xs.length > 1 && xs[1] != null) {
					range = DatasetUtils.sliceAndConvertLazyDataset(xs[1]).squeeze();
					Monotonicity m = Comparisons.findMonotonicity(range);
					if (m == Monotonicity.NOT_ORDERED) {
						range = null;
					}
				}
			}

			if (range == null) {
				range = DatasetFactory.createRange(IntegerDataset.class, this.data.getShapeRef()[0]);
				range.setName("Range");
			}

			ILazyDataset[] axis = ax.getAxis(1);
			
			IDataset x = axis[0].getSlice();
			
			globalRange = new double[] { x.min().doubleValue(), x.max().doubleValue(),
					range.min().doubleValue(), range.max().doubleValue() };
			
			ax.setAxis(0, range);
			
			data.setMetadata(ax);
		} catch (Exception e) {
			logger.error("Could not create axes metadata", e);
		}

		try {
			system = DataVisManipulationServiceManager.getPlottingService().createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating Combine plotting system:", e);
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Combine");
	}

	/**
	 * Create the content of the Shell dialog
	 * 
	 * @return
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topPane = new Composite(container, SWT.NONE);
		topPane.setLayout(new GridLayout(1, false));
		topPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(topPane, null);
		system.createPlotPart(topPane, "Combine Plot", actionBarWrapper, PlotType.XY, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		try {
			IImageTrace t = MetadataPlotUtils.buildTrace(data, system);
			t.setGlobalRange(globalRange);
			system.addTrace(t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		system.setKeepAspect(false);

		return container;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// Change parent layout data to fill the whole bar
		parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button axisButton = createButton(parent, IDialogConstants.NO_ID, "Load Y-Axis...", false);
		axisButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
				dialog.setNewFile(false);
				dialog.setFolderSelector(false);
				if (lastPath != null) {
					File f = new File(lastPath);
					if (!f.isDirectory()) {
						lastPath = f.getParent();
					}
					dialog.setPath(lastPath);
				} else {
					dialog.setPath(System.getProperty("user.home"));
				}

				dialog.create();
				if (dialog.open() == Dialog.CANCEL)
					return;
				lastPath = dialog.getPath();

				try {
					IDataHolder dh = LoaderFactory.getData(lastPath);
					IDataset dataset = dh.getDataset(0);
					dataset = dataset.getSliceView().squeeze();
					if (dataset.getSize() != data.getShapeRef()[0]) {
						throw new IllegalArgumentException(
								"Dataset sizes not compatible :" + data.getShapeRef()[0] + " and " + dataset.getSize());
					}
					AxesMetadata ax = data.getFirstMetadata(AxesMetadata.class);
					ax.setAxis(0, dataset);
					globalRange[2] = dataset.min(true).doubleValue();
					globalRange[3] = dataset.max(true).doubleValue();
					system.clear();
					IImageTrace t = MetadataPlotUtils.buildTrace(data, system);
					t.setGlobalRange(globalRange);
					system.addTrace(t);

				} catch (Exception e1) {
					MessageDialog.openError(getShell(), "Error", "Error importing y-axis :" + e1.getMessage());
				}

			}
		});

		Button saveButton = createButton(parent, IDialogConstants.NO_ID, "Save...", false);
		saveButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);
				if (lastPath != null) {
					File f = new File(lastPath);
					if (!f.isDirectory()) {
						lastPath = f.getParent();
					}
					dialog.setPath(lastPath);
				} else {
					dialog.setPath(System.getProperty("user.home"));
				}
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL ) return;
				lastPath = dialog.getPath();
				
				try (NexusFile nexus = DataVisManipulationServiceManager.getNexusFileFactory().newNexusFile(lastPath)) {
					nexus.openToWrite(true);
					GroupNode nxentry = NexusUtils.writeNXclass(nexus, null, "entry", NexusConstants.ENTRY);
					GroupNode nxdata = NexusUtils.writeNXclass(nexus, nxentry, "data", NexusConstants.DATA);
					data.setName(NexusConstants.DATA_DATA);
					nexus.createData(nxdata, data);
					NexusUtils.writeAttribute(nexus, nxdata, NexusConstants.DATA_SIGNAL, NexusConstants.DATA_DATA);
					
					AxesMetadata md = data.getFirstMetadata(AxesMetadata.class);
					
					ILazyDataset[] axes = md.getAxes();
					
					String[] names = new String[axes.length];
					
					names[0] = ".";
					names[1] = ".";
					
					if (axes[0] != null) {
						IDataset y = axes[0].getSlice();
						y = y.squeeze();
						if (y.getName() != null) {
							names[0] = MetadataPlotUtils.removeSquareBrackets(y.getName());
							y.setName(names[0]);
						} else {
							names[0] = "y_axis";
						}
						nexus.createData(nxdata, y);
						nexus.addAttribute(nxdata, TreeFactory.createAttribute(names[0] + NexusConstants.DATA_INDICES_SUFFIX, 0));
					}
					
					if (axes[1] != null) {
						IDataset x = axes[1].getSlice();
						if (x.getName() != null) {
							names[1] = MetadataPlotUtils.removeSquareBrackets(x.getName());
							x.setName(names[1]);
						} else {
							names[1] = "x_axis";
						}
						x = x.squeeze();
						nexus.createData(nxdata, x);
						nexus.addAttribute(nxdata, TreeFactory.createAttribute(names[1] + NexusConstants.DATA_INDICES_SUFFIX, 1));
					}
					
					nexus.addAttribute(nxdata, TreeFactory.createAttribute(NexusConstants.DATA_AXES, names));
					
					if (CombineDialog.this.names != null) {
						nexus.createData(nxdata, CombineDialog.this.names);
						nexus.addAttribute(nxdata, TreeFactory.createAttribute( CombineDialog.this.names.getName() + NexusConstants.DATA_INDICES_SUFFIX, 0));
					}
					
					
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), "Error", "Error saving combined data:" + e1.getMessage());
				}
				
			}
			
		});

		// Create a spacer label
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Update layout of the parent composite to count the spacer
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		createButton(parent, IDialogConstants.OK_ID, "OK", false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 800);
	}

	@Override
	public boolean close() {
		if (image != null) image.dispose();
		return super.close();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (IPlottingSystem.class == adapter) return system;
		return null;
	}
}
