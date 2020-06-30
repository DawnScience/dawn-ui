package org.dawnsci.datavis;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IPlotController;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class DatasetContributionFactory extends ExtensionContributionFactory {

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		
		MenuManager search = new MenuManager("Dataset Options",
                "org.dawnsci.datavis.dataset");
		
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		final IFileController fileController = bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
		final IPlotController plotController = bundleContext.getService(bundleContext.getServiceReference(IPlotController.class));
		
		search.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {

				search.removeAll();

				Action a = new Action("Filter Datasets"){
					@Override
					public void run() {
						boolean onlySignals = fileController.isOnlySignals();
						fileController.setOnlySignals(!onlySignals);
					}
				};

				a.setChecked(fileController.isOnlySignals());
				
				
				Action coa = new Action("Co-slice Datasets"){
					@Override
					public void run() {
						boolean coSlice = plotController.isCoSlicingEnabled();
						plotController.setCoSlicingEnabled(!coSlice);
					}
				};

				coa.setChecked(plotController.isCoSlicingEnabled());

				search.add(a);
				search.add(coa);
			}
		});

		search.add(new Action("") {

			@Override
			public void run() {

			}
		});

		additions.addContributionItem(search, new Expression() {
			
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				Object variable = context.getVariable("activeWorkbenchWindow.activePerspective");
				//probably shouldn't be needed, but doesn't work without it.
				search.setVisible(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
				return EvaluationResult.valueOf(DataVisConstants.DATAVIS_PERSPECTIVE_ID.equals(variable));
			}
		});

	}

}
