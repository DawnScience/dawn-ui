package org.dawnsci.spectrum.ui.views;

import java.util.List;

import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.spectrum.ui.Activator;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumFileManager;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.dawnsci.spectrum.ui.processing.AbstractProcess;
import org.dawnsci.spectrum.ui.processing.AdditionProcess;
import org.dawnsci.spectrum.ui.processing.AverageProcess;
import org.dawnsci.spectrum.ui.processing.CombineProcess;
import org.dawnsci.spectrum.ui.processing.DerivativeProcess;
import org.dawnsci.spectrum.ui.processing.DivisionProcess;
import org.dawnsci.spectrum.ui.processing.MultiplicationProcess;
import org.dawnsci.spectrum.ui.processing.MultiplyMinusOneProcess;
import org.dawnsci.spectrum.ui.processing.PolySmoothProcess;
import org.dawnsci.spectrum.ui.processing.RollingBallBaselineProcess;
import org.dawnsci.spectrum.ui.processing.SubtractionProcess;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;
import org.dawnsci.spectrum.ui.wizard.IntegerInputDialog;
import org.dawnsci.spectrum.ui.wizard.SpectrumSubtractionWizardPage;
import org.dawnsci.spectrum.ui.wizard.SpectrumWizard;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

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
		MenuAction menuProcess = new MenuAction("Process");
		menuProcess.setId("org.dawnsci.spectrum.ui.views.processingmenu");
		menuProcess.setImageDescriptor(Activator.imageDescriptorFromPlugin("org.dawnsci.spectrum.ui","icons/function.png"));
		
		AbstractProcess process = new AverageProcess();
		addProcessAction(process, menuProcess, "Average",((IStructuredSelection)viewer.getSelection()).size() > 1);
		process = new CombineProcess();
		addProcessAction(process, menuProcess, "Combine",((IStructuredSelection)viewer.getSelection()).size() > 1);

		process = new DerivativeProcess();
		addProcessAction(process, menuProcess, "Derivative",((IStructuredSelection)viewer.getSelection()).size() >= 1);
		process = new MultiplyMinusOneProcess();
		addProcessAction(process, menuProcess, "Multiply by -1",((IStructuredSelection)viewer.getSelection()).size() >= 1);
		//process = new PolySmoothProcess();
		//addProcessAction(process, menuProcess, "Polynomial smoothing",((IStructuredSelection)viewer.getSelection()).size() >= 1);
		menuProcess.addSeparator();
		addWizardActions(menuProcess);
		menuProcess.addSeparator();
		addCacheArithmeticMenu(menuProcess);
		menuProcess.addSeparator();
		addCacheActions(menuProcess);
		
		menu.add(menuProcess);
	}
	
	private void addCacheActions(MenuAction menu) {
		
		boolean enabled = ((IStructuredSelection)viewer.getSelection()).size() == 1;
		
		MenuAction cacheMenu = new MenuAction("Cache");
		cacheMenu.setImageDescriptor(Activator.imageDescriptorFromPlugin("org.dawnsci.spectrum.ui","icons/spectrumCache.png"));
		
		IAction cacheName = null; 
		
		if (manager.getCachedFile() == null) {
			cacheName = new Action("Empty") {
				
			};
		} else {
			cacheName = new Action(manager.getCachedFile().getName()) {
			};
		}
		
		cacheName.setEnabled(false);
		cacheMenu.add(cacheName);
		cacheMenu.addSeparator();
		
		
		IAction setCache = new Action("Set as cached") {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				IContain1DData d = list.get(0);
				IContain1DData cacheData = new Contain1DDataImpl(d.getxDataset(), d.getyDatasets(), d.getName(), d.getLongName());
				manager.setCachedFile(cacheData);
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
	
	private void addCacheArithmeticMenu(MenuAction menuManager) {
		
		boolean enabled = manager.getCachedFile() != null;
		
		MenuAction menu = new MenuAction("Arithmetic with cache");
		menu.setImageDescriptor(Activator.imageDescriptorFromPlugin("org.dawnsci.spectrum.ui","icons/processCache.png"));
		menu.setEnabled(enabled);
		
		AbstractProcess process = new SubtractionProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Subtract cached",enabled);
		
		process = new DivisionProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Divide by cached",enabled);
		
		process = new AdditionProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Add to cached",enabled);
		
		process = new MultiplicationProcess(manager.getCachedFile());
		addProcessAction(process, menu, "Multiply by cached",enabled);
		
		menuManager.add(menu);

		
	}
	
	private void addProcessAction(final AbstractProcess process, MenuAction manager, String name, boolean enabled) {
		//manager
		Action action = new Action(name) {
			public void run() {
				ISelection selection = viewer.getSelection();
				final List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				
				Job processJob = new Job("process") {
					
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						List<IContain1DData> out = process.process(list);
						
						if (out == null) {
							showMessage("Could not process dataset, operation not supported for this data!");
							return Status.CANCEL_STATUS;
						}
						
						for(IContain1DData data : out) {
							SpectrumInMemory mem = new SpectrumInMemory(data.getLongName(), data.getName(), data.getxDataset(), data.getyDatasets(), system);
							ProcessMenuManager.this.manager.addFile(mem);
						}
						return Status.OK_STATUS;
					}
				};
				
				processJob.schedule();
				
			}
		};
		
		action.setEnabled(enabled);
		manager.add(action);
		
	}
	
	private void addWizardActions(MenuAction menuManager) {

		boolean enabled = ((IStructuredSelection)viewer.getSelection()).size() == 1 &&
				manager.getCachedFile() != null;
		MenuAction menu = new MenuAction("Wizards");

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
		
		IAction rollingBaseline = new Action("Rolling Ball Baseline Correction...") {
			public void run() {
				ISelection selection = viewer.getSelection();
				final List<IContain1DData> list = SpectrumUtils.get1DDataList((IStructuredSelection)selection);
				
				int size =  list.get(0).getyDatasets().get(0).getSize();
				
				IntegerInputDialog id = new IntegerInputDialog(Display.getDefault().getActiveShell(),
						0, size,size/20, "Select ball radius (data points):");
				
				if (id.open() == Dialog.OK) {
					int width = id.getValue();
					
					final RollingBallBaselineProcess process = new RollingBallBaselineProcess();
					process.setWidth(width);
					
					Job processJob = new Job("process") {
						
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							List<IContain1DData> out = process.process(list);
							
							if (out == null) {
								showMessage("Could not process dataset, operation not supported for this data!");
								return Status.CANCEL_STATUS;
							}
							
							for(IContain1DData data : out) {
								SpectrumInMemory mem = new SpectrumInMemory(data.getLongName(), data.getName(), data.getxDataset(), data.getyDatasets(), system);
								ProcessMenuManager.this.manager.addFile(mem);
							}
							return Status.OK_STATUS;
						}
					};
					
					processJob.schedule();
					
				}
			}
		};
		
		subtractionWizard.setEnabled(enabled);
		rollingBaseline.setEnabled(((IStructuredSelection)viewer.getSelection()).size() >= 1);

		menu.add(subtractionWizard);
		menu.add(rollingBaseline);
		menuManager.add(menu);
	}
	
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

}
