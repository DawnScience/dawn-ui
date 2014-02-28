package org.dawnsci.common.widgets.gda.views;

import org.dawnsci.common.widgets.gda.function.FunctionFittingWidget;
import org.dawnsci.common.widgets.gda.function.descriptors.DefaultFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptorProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

/**
 * This class/view exists to ease testing of the FunctionFittingTool by
 * factoring out a large part of the work.
 */
public class FunctionTreeViewerPluginTestView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.dawnsci.common.widgets.gda.views.FunctionTreeViewerPluginTestView";

	private FunctionFittingWidget viewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		IFunctionDescriptorProvider provider = new DefaultFunctionDescriptorProvider();
		viewer = new FunctionFittingWidget(parent, provider, getSite());

		CompositeFunction actual = new CompositeFunction();
		Gaussian function = new Gaussian(1, 2, 3);
		function.setParameter(0, function.getParameter(1));
		actual.addFunction(function);

		viewer.setInput(actual);
		viewer.expandAll();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.setFocus();
	}

}