package org.dawnsci.plotting.jreality;

import java.util.Arrays;

import org.dawb.common.ui.image.PaletteFactory;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

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
		
		final SurfaceTrace trace = plotting.createSurfaceTrace("Test Trace");
    	AbstractDataset data = Random.rand(0, 256, new int[]{256, 256});
    	AbstractDataset x = DoubleDataset.arange(256, IntegerDataset.FLOAT64);
    	AbstractDataset y = DoubleDataset.arange(256, IntegerDataset.FLOAT64);
    	AbstractDataset z = null;
    	trace.setAxesNames(Arrays.asList("X Axis", "Y Axis", "Z Axis"));
		trace.setPalette(PaletteFactory.makeRedsPalette());
		
    	try {
			trace.setData(data, Arrays.asList(x,y,z));
			plotting.addSurfaceTrace(trace);
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
