package org.dawnsci.spectrum.ui.dialogs;


import java.io.File;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.spectrum.ui.Activator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
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


	private IDataset data;
	private IPlottingSystem<Composite> system;
	private Image image;
	private double[] globalRange;
	private String lastPath;

	public CombineDialog(Shell shell, IDataset x, IDataset data){
		super(shell);
		this.data = data;
		try {
			AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ax.setAxis(1, x);
			Dataset range = DatasetFactory.createRange(data.getShape()[0], Dataset.INT32);
			range.setName("Range");
			
			globalRange = new double[]{x.min().doubleValue(),x.max().doubleValue(),range.min().doubleValue(),range.max().doubleValue(), 
					};
			
			ax.setAxis(0, range);
			
			data.setMetadata(ax);
		} catch (MetadataException e) {
			logger.error("Could not create axes metadata", e);
		}

		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating Combine plotting system:", e);
		}
	}

	@Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Combine Dialog");
	    newShell.setImage(image = Activator.getImageDescriptor("icons/spectrum.png").createImage());
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
		system.createPlotPart(topPane, "Combine Plot", actionBarWrapper, PlotType.IMAGE, null);
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
		
//		Button loadButton = createButton(container, IDialogConstants.CLIENT_ID+1, "Load Y Axis...", false);
		
		return container;
	}
	
	
	protected void createButtonsForButtonBar(Composite parent)
	{
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
			if (dialog.open() == Dialog.CANCEL ) return;
			lastPath = dialog.getPath();
			
			try {
				IDataHolder dh = LoaderFactory.getData(lastPath);
				IDataset dataset = dh.getDataset(0);
				dataset = dataset.getSliceView().squeeze();
				if (dataset.getSize() != data.getShape()[0]) {
					throw new IllegalArgumentException("Dataset sizes not compatible :" + data.getShape()[0] + " and "  + dataset.getSize());
				}
				AxesMetadata ax = data.getFirstMetadata(AxesMetadata.class);
				ax.setAxis(0, dataset);
				globalRange[2] = dataset.min().doubleValue();
				globalRange[3] = dataset.max().doubleValue();
				system.clear();
				IImageTrace t = MetadataPlotUtils.buildTrace(data, system);
				t.setGlobalRange(globalRange);
				system.addTrace(t);
				
				
			} catch (Exception e1) {
				MessageDialog.openError(getShell(), "Error", "Error importing y-axis :" + e1.getMessage());
			}
			
		}
		
	});

	  // Create a spacer label
	  Label spacer = new Label(parent, SWT.NONE);
	  spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	  // Update layout of the parent composite to count the spacer
	  GridLayout layout = (GridLayout)parent.getLayout();
	  layout.numColumns++;
	  layout.makeColumnsEqualWidth = false;

	  createButton(parent, IDialogConstants.OK_ID,"OK", false);
	  createButton(parent, IDialogConstants.CANCEL_ID,"Cancel", true);
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
