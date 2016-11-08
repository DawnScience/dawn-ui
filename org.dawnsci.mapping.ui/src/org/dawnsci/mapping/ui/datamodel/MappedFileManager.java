package org.dawnsci.mapping.ui.datamodel;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.DatasetNameUtils;
import org.dawnsci.mapping.ui.AcquisitionServiceManager;
import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.dialog.RectangleRegistrationDialog;
import org.dawnsci.mapping.ui.wizards.ImportMappedDataWizard;
import org.dawnsci.mapping.ui.wizards.LegacyMapBeanBuilder;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedFileManager {

	private final static Logger logger = LoggerFactory.getLogger(MappedFileManager.class);
	
	private MapPlotManager plotManager;
	private MappedDataArea mappedDataArea;
	private Viewer viewer;
	private String xAxisName;
	private String yAxisName;

	public void init(MapPlotManager plotManager, MappedDataArea mappedDataArea, Viewer viewer){
		this.plotManager = plotManager;
		this.mappedDataArea = mappedDataArea;
		this.viewer = viewer;
	}
	
	public void setXandYAxesName(String xAxisName, String yAxisName){
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
	}
	
	public void removeFile(MappedDataFile file) {
		mappedDataArea.removeFile(file);
		plotManager.clearAll();
		plotManager.updateLayers(null);
		viewer.refresh();
	}
	
	public void removeFile(String path) {
		mappedDataArea.removeFile(path);
		plotManager.clearAll();
		plotManager.updateLayers(null);
		viewer.refresh();
	}
	
	public void clearAll() {
		mappedDataArea.clearAll();
		plotManager.clearAll();
		viewer.refresh();
	}
	
	public boolean contains(String path) {
		return mappedDataArea.contains(path);
	}
	
	public void locallyReloadLiveFile(final String path) {
		
		if (!mappedDataArea.contains(path)) return;
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					locallyReloadLiveFile(path);
				}
			});
		}
		
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		try {
			service.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					mappedDataArea.locallyReloadLiveFile(path);
					plotManager.plotLayers();
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
						@Override
						public void run() {
							viewer.refresh();

						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void importFile(final String path, final MappedDataFileBean bean) {
		if (contains(path)) return;
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					importFile(path, bean);
					
				}
			});
			return;
		}
		
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		try {
			service.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					IMonitor m = new ProgressMonitorWrapper(monitor);
					monitor.beginTask("Loading data...", -1);
					importFile(path, bean, m);
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	
	private void importFile(final String path, final MappedDataFileBean bean, final IMonitor monitor) {
		final MappedDataFile mdf = MappedFileFactory.getMappedDataFile(path, bean, monitor);
		if (monitor != null && monitor.isCancelled()) return;
		updateUI(mdf);
	}
	
	
	private void updateUI(final MappedDataFile mdf){
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					updateUI(mdf);
					
				}
			});
			return;
		}
		
		boolean load = true;
		if (!mappedDataArea.isInRange(mdf)) {
			load = MessageDialog.openConfirm(viewer.getControl().getShell(), "No overlap!", "Are you sure you want to load this data?");
		} 

		if (load)mappedDataArea.addMappedDataFile(mdf);
//		plotManager.clearAll();
		plotManager.updateLayers(null);
		viewer.refresh();
		if (viewer instanceof TreeViewer) {
			((TreeViewer)viewer).expandToLevel(mdf, 1);
		}
		
	}

	
	public void importLiveFile(final String path, LiveDataBean bean, String parentFile) {
		if (parentFile != null && !mappedDataArea.contains(parentFile)) return;
		IRemoteDatasetService rds = LocalServiceManager.getRemoteDatasetService();
		if (rds == null) {
			logger.error("Could not acquire remote dataset service");
			return;
		}
		
		IRemoteData rd = rds.createRemoteData(bean.getHost(), bean.getPort());
		
		if (rd == null) {
			logger.error("Could not acquire remote data on :" + bean.getHost() + ":" + bean.getPort());
			return;
		}
		
		try {
			rd.setPath(path);
			Map<String, Object> map = rd.getTree();
			map.toString();
			Tree tree = TreeToMapUtils.mapToTree(map, path);
			
			MappedDataFileBean buildBean = null; 
			if (AcquisitionServiceManager.getStageConfiguration()==null){
				buildBean = MapBeanBuilder.buildBean(tree);
			} else {
				String x = AcquisitionServiceManager.getStageConfiguration().getActiveFastScanAxis();
				String y = AcquisitionServiceManager.getStageConfiguration().getActiveSlowScanAxis();
				buildBean = MapBeanBuilder.buildBean(tree,x,y);
			}
			
			
			if (buildBean != null) {
				buildBean.setLiveBean(bean);
				importFile(path, buildBean, null);
			}
			
			return;
			
		} catch (Exception e) {
			logger.error("Could not build map bean from " + path, e);
		}
		
		logger.error("Could not build map bean from " + path);
		mappedDataArea.addMappedDataFile(new MappedDataFile(path,bean));
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();

			}
		});
		
	}
	
	public void importFile(final String path) {
		if (contains(path)) return;
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					importFile(path);
				}
			});
			
			return;
		}
		
		
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);

			try {
				service.busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						Map<String, int[]> datasetNames = DatasetNameUtils.getDatasetInfo(path, null);
						IMetadata meta = null;
						IDataHolder dh = null;
						try {
						meta = LocalServiceManager.getLoaderService().getMetadata(path, null);
						dh = LocalServiceManager.getLoaderService().getData(path, null);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						
						if (datasetNames != null && datasetNames.size() == 1 && datasetNames.containsKey("image-01")) {
							IDataset im;
							try {
								im = LocalServiceManager.getLoaderService().getDataset(path, null);
								showRegistrationWizard(path,im);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							return;
						}
						
						MappedDataFileBean b = null;
						try {
							b = MapBeanBuilder.buildBean(dh.getTree());
						} catch (Exception e) {
							//ignore
						}

						if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I18CHECK) != null) {
							try {
							b = LegacyMapBeanBuilder.buildBeani18in2015(dh.getTree());
							} catch (Exception e) {
								//ignore
							}
						}
						
						if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I05CHECK) != null) {
							try {
							b = LegacyMapBeanBuilder.buildBeani05in2015(dh.getTree());
							} catch (Exception e) {
								//ignore
							}
						}
						
						if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I22SAXCHECK) != null) {
							try {
							b = LegacyMapBeanBuilder.buildBeani22in2016(dh.getTree());
							} catch (Exception e) {
								//ignore
							}
						}
						
						if (b == null && dh.getLazyDataset(LegacyMapBeanBuilder.I08CHECK) != null) {
							try {
							b = LegacyMapBeanBuilder.buildBeani08Energyin2016(dh.getTree());
							} catch (Exception e) {
								//ignore
							}
						}
						
						
						
						
						if (b != null) {
							IMonitor m = new ProgressMonitorWrapper(monitor);
							monitor.beginTask("Loading data...", -1);
							importFile(path, b, m);
							return;
						}
						
						showWizard(path, datasetNames, meta);
						
					}
					
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
	}
		
	private void showWizard(final String path, final Map<String, int[]> datasetNames, final IMetadata meta) {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					showWizard(path, datasetNames, meta);
				}
			});
			
			return;
		}

		final ImportMappedDataWizard wiz = new ImportMappedDataWizard(path, datasetNames, meta);
		wiz.setNeedsProgressMonitor(true);
		final WizardDialog wd = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),wiz);
		wd.setPageSize(new Point(900, 500));
		wd.create();
		
		if (wd.open() == WizardDialog.CANCEL) return;
		
		importFile(path, wiz.getMappedDataFileBean());
		
	}
	
	private void showRegistrationWizard(final String path, final IDataset data) {
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					showRegistrationWizard(path, data);
				}
			});
			
			return;
		}


		RectangleRegistrationDialog dialog = new RectangleRegistrationDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), plotManager.getTopMap().getData(),data);
		if (dialog.open() != IDialogConstants.OK_ID) return;
		RGBDataset ds = (RGBDataset)dialog.getRegisteredImage();
		ds.setName("Registered");
		AssociatedImage asIm = new AssociatedImage("Registered", ds, path);
		mappedDataArea.addMappedDataFile(MappedFileFactory.getMappedDataFile(path, asIm));
		viewer.refresh();
	}
	
	
}
