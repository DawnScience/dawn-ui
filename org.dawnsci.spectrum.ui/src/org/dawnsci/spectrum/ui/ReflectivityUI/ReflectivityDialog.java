package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ReflectivityDialog extends Dialog {

	private String[] filepaths;
	private String title;
	private List<IContain1DData> output;
	private List<IContain1DData> list;


	public ReflectivityDialog(Shell parentShell, String[] datFilenames,List<IContain1DData> input) {
		super(parentShell);
		this.filepaths = datFilenames;
		this.list = input;

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout(3, false);
		container.setLayout(gridLayout);			

		ArrayList<ILazyDataset> arrayILDy = new ArrayList<ILazyDataset>();
		ArrayList<ILazyDataset> arrayILDx = new ArrayList<ILazyDataset>();


		int k=0;

		for(k =0;k<list.size();k++){
			arrayILDx.add(list.get(k).getxDataset());
			arrayILDy.add(list.get(k).getyDatasets().get(0));
		}

		ReflectivityUIModel model = new ReflectivityUIModel();

		model.setFilepaths(filepaths);
		title = filepaths[0];


		ReflectivityCurves customComposite = new ReflectivityCurves(container, SWT.NONE, arrayILDy,arrayILDx,filepaths,title, model);
		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		StitchedReflectivityCurves stitchedCurves = new StitchedReflectivityCurves(container, 
				SWT.NONE, arrayILDy, arrayILDx,"Overlap Test", model);
		stitchedCurves.setLayout(new GridLayout());
		stitchedCurves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		output  = stitchedCurves.getOutput();

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ReflectivityDialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public List<IContain1DData> getResult(){		
		return output;
	}


}

