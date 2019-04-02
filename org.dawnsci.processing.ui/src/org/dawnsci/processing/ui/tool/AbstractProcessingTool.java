package org.dawnsci.processing.ui.tool;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.model.OperationModelViewer;
import org.dawnsci.processing.ui.model.OperationModelWizard;
import org.dawnsci.processing.ui.model.OperationModelWizardDialog;
import org.dawnsci.processing.ui.processing.OperationTableUtils;
import org.dawnsci.processing.ui.slice.DataFileSliceView;
import org.dawnsci.processing.ui.slice.EscapableSliceVisitor;
import org.dawnsci.processing.ui.slice.ExtendedFileSelectionDialog;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.dawnsci.analysis.api.processing.Atomic;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.ISavesToFile;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromLiveSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.ISeriesValidator;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.visitor.NexusFileExecutionVisitor;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public abstract class AbstractProcessingTool extends AbstractToolPage {

	private IPlottingSystem<Composite> system;
	private SashForm sashForm;
	private Label statusMessage;
	private SeriesTable  seriesTable;

	private OperationModelViewer modelEditor;
	private IOperation<?, ?> selection;
	private ProcessingJob job;
	private ITraceListener listener;
	private IOperationErrorInformer informer;
	private Action run;
	private Action configure;
	private SliceFromSeriesMetadata parentMeta;
	private IOperationInputData inputData;
	private ProcessingOutputType outputType = ProcessingOutputType.PROCESSING_ONLY;
	
	private final static String PROCESSED = "_processed";
	private final static String EXT= ".nxs";
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractProcessingTool.class);
	
	public AbstractProcessingTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Could not create plotting system",e);
			return;
		}
		this.seriesTable    = new SeriesTable(){
			protected void checkValid(List<ISeriesItemDescriptor> series) {
				if (series.contains(ISeriesItemDescriptor.INSERT)) {
					statusMessage.setText("Please choose an operation to insert");
					return;
				}
				
				ISeriesValidator validator = getValidator();
				if (validator==null) return;
				final String errorMessage = validator.getErrorMessage(series);
				if (errorMessage == null && (statusMessage.getText() == null  || statusMessage.getText().isEmpty())) return;
				statusMessage.setText(errorMessage!=null ? errorMessage : "");
				
				statusMessage.getParent().layout();
			}
		};
		
		job = new ProcessingJob();
		
		listener = new ITraceListener.Stub() {
			@Override
			protected void update(TraceEvent evt) {
				updateData();
			}
		};
	}

	
	@Override
	public void createControl(Composite parent) {
		
		sashForm = new SashForm(parent, SWT.VERTICAL);
		
		run = new Action("Run", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				
				if (parentMeta instanceof SliceFromLiveSeriesMetadata) {
					try {
						Display.getDefault().asyncExec(() -> MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Live data!", "Cannot use tool to process running data!"));
					} catch (Exception e) {
						logger.error("Could not set-up live processing", e);
					}
					
					return;
				}
				
				if (parentMeta instanceof SliceFromSeriesMetadata) {
					setUpRun();
				}
			}
		};
		run.setEnabled(false);
		
		final Action showOptions = new Action("Maximise Plot", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					sashForm.setWeights(new int[]{100,0});
				} else {
					sashForm.setWeights(new int[]{50,50});
				}
			}
		};
		
		configure = new Action("Live setup", Activator.getImageDescriptor("icons/application-dialog.png")) {
			@SuppressWarnings("unchecked")
			public void run() {
				IOperationModel model = modelEditor.getModel();
				if (inputData == null) return;
				if (!inputData.getCurrentOperations().get(0).getModel().equals(model)) return;
				
				IOperationSetupWizardPage wizardPage = ServiceHolder.getOperationUIService().getWizardPage(inputData.getCurrentOperations().get(0));
				
				OperationModelWizard wizard = new OperationModelWizard(inputData.getInputData(), wizardPage);
				wizard.setWindowTitle("Operation Model Configuration");
				OperationModelWizardDialog dialog = new OperationModelWizardDialog(getSite().getShell(), wizard);
				dialog.create();
				if (dialog.open() == Dialog.OK) {
					modelEditor.refresh();
					updateData();
				}
				
			}
		};
		configure.setEnabled(false);
		
		showOptions.setImageDescriptor(Activator.getImageDescriptor("icons/maximize.png"));
		run.setImageDescriptor(Activator.getImageDescriptor("icons/run_workflow.gif"));
		getSite().getActionBars().getToolBarManager().add(run);
		getSite().getActionBars().getToolBarManager().add(showOptions);
		getSite().getActionBars().getMenuManager().add(showOptions);
		getSite().getActionBars().getToolBarManager().add(configure);
		
		Composite base = new Composite(sashForm, SWT.NONE);
		base.setLayout(new GridLayout(1,true));

		
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;

		system.createPlotPart(base, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		system.getSelectedYAxis().setAxisAutoscaleTight(true);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		statusMessage = new Label(base, SWT.WRAP);
		statusMessage.setText("Status...");
		statusMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		Composite lower = new Composite(sashForm, SWT.FILL);
		lower.setLayout(new GridLayout(2, false));
		informer = OperationTableUtils.initialiseOperationTable(seriesTable, lower);
		
		modelEditor = new OperationModelViewer(false);
		modelEditor.createPartControl(lower);
		seriesTable.addSelectionListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				modelEditor.selectionChanged(null, event.getSelection());
				try {
					IStructuredSelection ss = (IStructuredSelection)event.getSelection();
					if (ss.isEmpty()) return;
					Object ob = ((IStructuredSelection)ss).getFirstElement();
					if (ob instanceof ISeriesItemDescriptor) {
						try {
							selection = ((IOperation<?, ?> )((ISeriesItemDescriptor)ob).getSeriesObject());
							if (selection == null) return;
							updateData();
						} catch (Exception e) {
//							selection = null;
						}
					}

				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
			}
		});
		
		final MenuManager click = new MenuManager("#PopupMenu");
		click.setRemoveAllWhenShown(true);
		//createActions(Click);
		click.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				OperationTableUtils.addMenuItems(manager, seriesTable, getViewPart().getSite().getShell());
			}
		});
		
		seriesTable.setMenuManager(click);
		
		OperationTableUtils.setupPipelinePaneDropTarget(seriesTable, null, logger, null);
		
		
		sashForm.setWeights(new int[]{50,50});
		
		super.createControl(parent);
		
		
	}
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		super.activate();
		getPlottingSystem().addTraceListener(listener);
		if (informer != null) informer.setTestData(getData());
		updateData();
	}
	
	@Override
	public void deactivate(){
		super.deactivate();
		getPlottingSystem().removeTraceListener(listener);
	}
	
	@Override 
	public void dispose(){
		try {
			system.dispose();
		} catch (Exception e) {
			logger.error("Error disposing plotting system",e);
		}
		super.dispose();
		
	}
	
	protected abstract IDataset getData();
	
	protected void updateData() {
		inputData =null;
		configure.setEnabled(false);
		IDataset ds = getData();
		if (ds == null) return;
		IOperation<?, ?>[] operations = OperationTableUtils.getOperations(logger, seriesTable.getSeriesItems());
		SliceFromSeriesMetadata meta = ds.getFirstMetadata(SliceFromSeriesMetadata.class);
		if (ds.getFirstMetadata(SliceFromSeriesMetadata.class) != null){
			run.setEnabled(true);
			if(meta.getSourceInfo().getFilePath() != null && !meta.getSourceInfo().getFilePath().isEmpty()) parentMeta = meta;
		}
		else {
			run.setEnabled(false);
			parentMeta = null;
		}
		
		ProcessingInfo info = new ProcessingInfo();
		info.data = ds;
		info.operations = operations;
		info.parentMetadata = parentMeta;
		
		job.update(info);
		job.schedule();

	}

	@Override
	public Control getControl() {
		return sashForm;
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private class ProcessingInfo {
		public IDataset data;
		public IOperation<?, ?>[] operations;
		public SliceFromSeriesMetadata parentMetadata;
		
	}
	
	private class ProcessingJob extends Job {

		private ProcessingInfo info;
		
		public ProcessingJob() {
			super("Processing");
		}
		
		public void update(ProcessingInfo info) {
			this.info = info;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IDataset ds = info.data.getSliceView();
			IOperation<?, ?>[] operations = info.operations;
			
			SliceFromSeriesMetadata sliceMeta;
			
			if (info.parentMetadata == null) {
				SliceND slice = new SliceND(ds.getShape());
				int[] dataDims = new int[]{0, 1};
				SliceInformation si = new SliceInformation(slice, slice, slice, dataDims, 1, 0);
				SourceInformation so = new SourceInformation("", "", ds);
				sliceMeta = new SliceFromSeriesMetadata(so, si);
			} else {
				sliceMeta = info.parentMetadata;
				int[] shape = info.parentMetadata.getParent().getShape();
				int[] s = shape.clone();
				Arrays.fill(s, 1);
				int[] dd = info.parentMetadata.getDataDimensions();
				for (int i = 0; i < dd.length; i++) s[dd[i]] = shape[dd[i]];
				ds.setShape(s);
			}
			
			
			ds.setMetadata(sliceMeta);
			
			EscapableSliceVisitor vis = new EscapableSliceVisitor(null, sliceMeta.getDataDimensions(),operations, operations,null,null,system);
			vis.setEndOperation(selection);
			
			try {
				vis.visit(ds);
				informer.setInErrorState(null);
			} catch (Exception e) {
				if (e instanceof OperationException) {
					if (informer.getInErrorState() == null) informer.setInErrorState((OperationException)e);
					
				}
			}
			inputData = vis.getOperationInputData();
			if (inputData != null) configure.setEnabled(true);
			return Status.OK_STATUS;
		}
		
		
	}
	@Override
	public <T> void setPlottingSystem(IPlottingSystem<T> system) {
		super.setPlottingSystem(system);
		if (system == null) System.out.println("Plot null");
	}
	
	private void setUpRun() {
		
		if (parentMeta == null) return;
	
		String p = getPathNoExtension(parentMeta.getFilePath());

		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
		String timeStamp = "_" +dateFormat.format(date);
		String full = p + PROCESSED+ timeStamp + EXT;
		
		boolean isHDF5 = false;
		try {
			Tree tree = ServiceHolder.getLoaderService().getData(parentMeta.getFilePath(), null).getTree();
			isHDF5 = tree != null;
		} catch (Exception e2) {
			logger.error("Could not read tree",e2);
		}
		
		ExtendedFileSelectionDialog fsd = new ExtendedFileSelectionDialog(Display.getCurrent().getActiveShell(),isHDF5,false, false, outputType);
		
		fsd.setPath(full);
		fsd.setFolderSelector(false);
		fsd.setNewFile(true);
		fsd.create();
		if (fsd.open() == Dialog.CANCEL) return;
		outputType = fsd.getProcessingOutputType();
		
		final String path = fsd.getPath();
		File fh = new File(path);
		fh.getParentFile().mkdirs();
		
		ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

		try {
			dia.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					
					try {
						runProcessing(parentMeta, path, monitor);
						Map<String,String> props = new HashMap<>();
						props.put(PlottingEventConstants.SINGLE_FILE_PROPERTY, path);
						EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
						eventAdmin.postEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
						parentMeta.toString();
					} catch (final Exception e) {
						
						logger.error(e.getMessage(), e);
					}
				}
			});
		} catch (InvocationTargetException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (InterruptedException e1) {
			logger.error(e1.getMessage(), e1);
		}
		
	}
	
	private void runProcessing(SliceFromSeriesMetadata meta, String outputFile, IProgressMonitor monitor){
		
		try {
			IMonitor mon = new ProgressMonitorWrapper(monitor);
			IOperationService service = ServiceHolder.getOperationService();
			IOperationContext cc = service.createContext();
			ILazyDataset local = meta.getParent().getSliceView();
			SliceFromSeriesMetadata ssm = new SliceFromSeriesMetadata(meta.getSourceInfo());
			local.setMetadata(ssm);
			cc.setData(local);
			
			NexusFileExecutionVisitor vis = new NexusFileExecutionVisitor(outputFile);
			
			if (outputType == ProcessingOutputType.LINK_ORIGINAL && vis instanceof ISavesToFile) {
				((ISavesToFile)vis).includeLinkTo(meta.getFilePath());
			} else if (outputType == ProcessingOutputType.ORIGINAL_AND_PROCESSED) {
				File source = new File(meta.getFilePath());
				File dest = new File(outputFile);
				logger.debug("Copying original data ("+source.getAbsolutePath()+") to output file ("+dest.getAbsolutePath()+")");
				long start = System.currentTimeMillis();
				FileUtils.copyNio(source, dest);
				logger.debug("Copy ran in: " +(System.currentTimeMillis()-start)/1000. + " s : Thread" +Thread.currentThread().toString());
			}
			
			cc.setVisitor(vis);
			cc.setDataDimensions(meta.getDataDimensions());
			cc.setSeries(OperationTableUtils.getOperations(logger, seriesTable.getSeriesItems()));
			cc.setMonitor(mon);
			
			monitor.beginTask("Processing", DataFileSliceView.getWork(new SliceND(local.getShape()), meta.getDataDimensions()));
			
			if (canRunParallel()) cc.setExecutionType(ExecutionType.PARALLEL);
			else cc.setExecutionType(ExecutionType.SERIES);
			
			service.execute(cc);
		} catch (Exception e) {
			logger.error("Could no run processing",e);
		}
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system;
		} else {
			return super.getAdapter(clazz);
		}
	}
	
	private boolean canRunParallel() {
		IOperation<?, ?>[] operationSeries = OperationTableUtils.getOperations(logger, seriesTable.getSeriesItems());
		for (IOperation<?, ?> op : operationSeries) {
			Atomic atomic = op.getClass().getAnnotation(Atomic.class);
			if (atomic == null) {
				return false;
			}
		}
		
		return true;
	}

	private String getPathNoExtension(String path) {
		int posExt = path.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? path : path.substring(0, posExt);
	}
}
