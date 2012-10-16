package org.dawb.workbench.plotting.tools.fitting;

import org.eclipse.jface.viewers.TableViewer;

public class LineFittingTool extends AbstractFittingTool {

	/**
	 * This is for the data reduction tool doing line fitting over a stack.
	 */
	@Override
	public DataReductionInfo export(DataReductionSlice bean) throws Exception {
		return null;
	}

	/**
	 * Columns in the UI Table.
	 */
	@Override
	protected void createColumns(TableViewer viewer) {
		// Columns for coefficients of polynomials maybe?

	}

	/**
	 * The actual algorithm run
	 */
	@Override
	protected FittedFunctions getFittedFunctions(FittedPeaksInfo fittedPeaksInfo) throws Exception {
		// Drive the fitting, maybe exactly the same as the peak fitting or maybe different options.
		return null;
	}

	/**
	 * What happens when the line is plotted
	 */
	@Override
	protected void createFittedFunctionUI(FittedFunctions newBean) {
		// TODO Auto-generated method stub

	}

	/**
	 * Actions appearing in the tool.
	 */
	@Override
	protected void createActions() {
		// TODO Auto-generated method stub

	}

}
