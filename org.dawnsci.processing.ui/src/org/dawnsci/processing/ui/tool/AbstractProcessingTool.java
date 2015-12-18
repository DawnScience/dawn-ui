package org.dawnsci.processing.ui.tool;

import java.util.Iterator;
import java.util.List;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.dawnsci.processing.ui.model.OperationModelViewer;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.dawnsci.processing.ui.processing.OperationTableUtils;
import org.dawnsci.processing.ui.slice.EscapableSliceVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

public abstract class AbstractProcessingTool extends AbstractToolPage {

	private IPlottingSystem<Composite> system;
	private SashForm sashForm;
	private Label statusMessage;
	private SeriesTable  seriesTable;

	private OperationModelViewer modelEditor;
	private IOperation selection;
	private ProcessingJob job;
	private ITraceListener listener;
	
	public AbstractProcessingTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		this.seriesTable    = new SeriesTable();
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
		OperationTableUtils.initialiseOperationTable(seriesTable, lower);
		
		modelEditor = new OperationModelViewer(true);
		modelEditor.createPartControl(lower);
		seriesTable.addSelectionListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				modelEditor.selectionChanged(null, event.getSelection());
				try {
					selection = (IOperation)((ISeriesItemDescriptor)((IStructuredSelection)event.getSelection()).getFirstElement()).getSeriesObject();
					updateData();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		sashForm.setWeights(new int[]{50,50});

		super.createControl(parent);
		
		
	}
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		super.activate();
		getPlottingSystem().addTraceListener(listener);
		updateData();
	}
	
	@Override public void deactivate(){
		super.deactivate();
		getPlottingSystem().removeTraceListener(listener);
	}
	
	protected abstract IDataset getData();
	
	protected void updateData() {
		IDataset ds = getData();
		if (ds == null) return;
		IOperation[] operations = getOperations();
		
		ProcessingInfo info = new ProcessingInfo();
		info.data = ds;
		info.operations = getOperations();
		
		job.update(info);
		job.schedule();

//		SliceND slice = new SliceND(ds.getShape());
//		int[] dataDims = new int[]{0, 1};
//		
//		SliceInformation si = new SliceInformation(slice, slice, slice, dataDims, 1, 1);
//		SourceInformation so = new SourceInformation("", "", ds);
//		ds.setMetadata( new SliceFromSeriesMetadata(so, si));
//		
//		EscapableSliceVisitor vis = new EscapableSliceVisitor(null, dataDims,operations,null,null,system);
//		vis.setEndOperation(selection);
//		
//		try {
//			vis.visit(ds);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private IOperation[] getOperations() {
		final List<ISeriesItemDescriptor> desi = seriesTable.getSeriesItems();
		
		if (desi != null) {
			Iterator<ISeriesItemDescriptor> it = desi.iterator();
			while (it.hasNext()) if ((!(it.next() instanceof OperationDescriptor))) it.remove();
		}
		
		if (desi==null || desi.isEmpty()) return null;
		final IOperation<? extends IOperationModel, ? extends OperationData>[] pipeline = new IOperation[desi.size()];
		for (int i = 0; i < desi.size(); i++) {
			try {
				pipeline[i] = (IOperation<? extends IOperationModel, ? extends OperationData>)desi.get(i).getSeriesObject();
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			}
			}
		return pipeline;
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
		public IOperation[] operations;
		
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
			ProcessingInfo local = info;
			IDataset ds = info.data;
			IOperation[] operations = info.operations;
			
			SliceND slice = new SliceND(ds.getShape());
			int[] dataDims = new int[]{0, 1};
			
			SliceInformation si = new SliceInformation(slice, slice, slice, dataDims, 1, 1);
			SourceInformation so = new SourceInformation("", "", ds);
			ds.setMetadata( new SliceFromSeriesMetadata(so, si));
			
			EscapableSliceVisitor vis = new EscapableSliceVisitor(null, dataDims,operations,null,null,system);
			vis.setEndOperation(selection);
			
			try {
				vis.visit(ds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Status.OK_STATUS;
		}
		
		
	}
	@Override
	public <T> void setPlottingSystem(IPlottingSystem<T> system) {
		super.setPlottingSystem(system);
		if (system == null) System.out.println("Plot null");
	}

}
