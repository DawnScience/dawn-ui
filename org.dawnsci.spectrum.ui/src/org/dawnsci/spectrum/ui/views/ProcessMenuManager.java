package org.dawnsci.spectrum.ui.views;

import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.spectrum.ui.Activator;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumFileManager;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.dawnsci.spectrum.ui.processing.AbstractProcess;
import org.dawnsci.spectrum.ui.processing.AverageProcess;
import org.dawnsci.spectrum.ui.processing.DerivativeProcess;
import org.dawnsci.spectrum.ui.processing.DivisionProcess;
import org.dawnsci.spectrum.ui.processing.SubtractionProcess;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;
import org.dawnsci.spectrum.ui.wizard.SpectrumSubtractionWizardPage;
import org.dawnsci.spectrum.ui.wizard.SpectrumWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

public class ProcessMenuManager {
	
	StructuredViewer viewer;
	SpectrumFileManager manager;
	IPlottingSystem system;
	
	public ProcessMenuManager(StructuredViewer viewer, SpectrumFileManager manager, IPlottingSystem system) {
		this.viewer = viewer;
		this.manager = manager;
		this.system = system;
	}
	
	public void fillProcessMenu(IMenuManager menu) {
		MenuManager menuProcess = new MenuManager("Process",
				Activator.imageDescriptorFromPlugin("org.dawnsci.spectrum.ui","icons/function.png"),
				"org.dawnsci.spectrum.ui.views.processingmenu");
		
		AbstractProcess process = new AverageProcess();
		addProcessAction(process, menuProcess, "Average",((IStructuredSelection)viewer.getSelection()).size() > 1);

		process = new DerivativeProcess();
		addProcessAction(process, menuProcess, "Derivative",((IStructuredSelection)viewer.getSelection()).size() >= 1);
		addWizardActions(menuProcess);
		addCacheArithmeticMenu(menuProcess);
		addCacheActions(menuProcess);
		
		menu.add(menuProcess);
	}
	
	private void addCacheActions(MenuManager menu) {
		
		boolean enabled = ((IStructuredSelection)viewer.getSelection()).size() == 1;
		
		MenuManager cacheMenu = new MenuManager("Cache");
		
		if (manager.getCachedFile() != null) {
			cacheMenu.add(new Action(manager.getCachedFile().getName()) {
				@Override
				public void run() {
					//does nothing
				}
			});
		}
		
		IAction setCache = new Action("Set as cached") {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				manager.setCachedFile(list.get(0));
			}
		};
		
		setCache.setEnabled(enabled);
		
		IAction clearCache = new Action("Clear cached") {
			@Override
			public void run() {
				manager.setCachedFile(null);
			}
		};
		
		cacheMenu.add(setCache);
		cacheMenu.add(clearCache);
		
		menu.add(cacheMenu);
		
	}
	
	private void addCacheArithmeticMenu(MenuManager menuManager) {
		
		boolean enabled = manager.getCachedFile() != null;
		
		MenuManager menu = new MenuManager("Arithmetic with cache");
		
		AbstractProcess process = new SubtractionProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Subtract cached",enabled);
		
		process = new DivisionProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Divied by cached",enabled);
		
		menuManager.add(menu);

		
	}
	
	private void addProcessAction(final AbstractProcess process, MenuManager manager, String name, boolean enabled) {
		//manager
		Action action = new Action(name) {
			public void run() {
				ISelection selection = viewer.getSelection();
				List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				List<IContain1DData> out = process.process(list);
				
				if (out == null) {
					showMessage("Could not process dataset, operation not supported for this data!");
					return;
				}
				
				for(IContain1DData data : out) {
					SpectrumInMemory mem = new SpectrumInMemory(data.getLongName(), data.getName(), data.getxDataset(), data.getyDatasets(), system);
					ProcessMenuManager.this.manager.addFile(mem);
				}
			}
		};
		
		action.setEnabled(enabled);
		manager.add(action);
		
	}
	
	private void addWizardActions(MenuManager menuManager) {

		boolean enabled = ((IStructuredSelection)viewer.getSelection()).size() == 1 &&
				manager.getCachedFile() != null;
		MenuManager menu = new MenuManager("Wizards");


		IAction subtractionWizard = new Action("Subtraction wizard...") {
			public void run() {
				ISelection selection = viewer.getSelection();
				SpectrumWizard sw = new SpectrumWizard();
				List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				sw.addPage(new SpectrumSubtractionWizardPage(manager.getCachedFile(),list));
				sw.setData(list);
				WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(),sw);
				if (wd.open() == WizardDialog.OK) {
					List<IContain1DData> out = sw.getOutputData();

					for(IContain1DData data : out) {
						SpectrumInMemory mem = new SpectrumInMemory(data.getLongName(), data.getName(), data.getxDataset(), data.getyDatasets(), system);
						ProcessMenuManager.this.manager.addFile(mem);
					}
				}
			}
		};
		
		subtractionWizard.setEnabled(enabled);
		menu.add(subtractionWizard);
		menuManager.add(menu);
	}
	
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

}
