package org.dawnsci.plotting.jreality;

import java.util.Arrays;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Random;

public class TestView extends ViewPart {

	public static final String ID = "org.dawnsci.plotting.jreality.TestView"; //$NON-NLS-1$

	public TestView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		final HardwarePlotting plotting = new HardwarePlotting();
		plotting.createControl(container);
		
    	AbstractDataset data = Random.rand(0, 256, new int[]{256, 256});
        AxisValues x = new AxisValues("X-Axis", DoubleDataset.arange(256, IntegerDataset.FLOAT64));
        AxisValues y = new AxisValues("Y-Axis", DoubleDataset.arange(256, IntegerDataset.FLOAT64));
        AxisValues z = new AxisValues("Z-Axis", null);
		
		try {
			plotting.plot(data, Arrays.asList(x,y,z), PlottingMode.SURF2D);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

}
