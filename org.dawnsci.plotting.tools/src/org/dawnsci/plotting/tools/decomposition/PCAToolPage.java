package org.dawnsci.plotting.tools.decomposition;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LinearAlgebra;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import uk.ac.diamond.scisoft.analysis.decomposition.PCA;
import uk.ac.diamond.scisoft.analysis.decomposition.PCAResult;

public class PCAToolPage extends AbstractToolPage {

	private IPlottingSystem<Composite> varianceSystem;
	private IPlottingSystem<Composite> loadingSystem;
	private IPlottingSystem<Composite> scoresSystem;
	private IPlottingSystem<Composite> reconSystem;
	private PCAJob job;
	private PCAReconJob jobRecon;
	private Label status;
	private Composite control;
	
	private PCAResult currentResult;
	private PCADataPackage currentDataPackage;
	private Combo yCombo;
	private Combo xCombo;
	
	private Spinner pcRecon;
	private Spinner sampleRecon;
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		this.control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, false));
		
		job = new PCAJob();
		jobRecon = new PCAReconJob();
		
		
		try {
			varianceSystem = PlottingFactory.createPlottingSystem();
			scoresSystem = PlottingFactory.createPlottingSystem();
			loadingSystem = PlottingFactory.createPlottingSystem();
			reconSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			
			return;
		}
		
		Button calculate = new Button(control, SWT.PUSH);
		calculate.setLayoutData(new GridData());
		
		calculate.setText("Run PCA");
		
		final Spinner nComponents = new Spinner(control, SWT.READ_ONLY | SWT.BORDER);
		nComponents.setMinimum(1);
		nComponents.setSelection(10);
		
		status = new Label(control, SWT.NONE);
		status.setText("Ready");
		
		final TabFolder tabFolder = new TabFolder(control, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		TabItem var = new TabItem(tabFolder, SWT.NONE);
		var.setText("Variance Explained");
		
		Composite c = new Composite(tabFolder, SWT.NONE);
		c.setLayout(new GridLayout());
		var.setControl(c);
		
		TabItem scoresLoads = new TabItem(tabFolder, SWT.None);
		scoresLoads.setText("Scores/Loads");
		scoresLoads.setControl(makeLoadingScoresPlot(tabFolder));
		
		TabItem reconItem = new TabItem(tabFolder, SWT.NONE);
		reconItem.setText("Reconstruct");
		reconItem.setControl(makeReconstructionPanel(tabFolder));
		
		varianceSystem.createPlotPart(c, 
				"PCA_tool_variance", 
				null, 
				PlotType.XY,
				this.getViewPart());
		
		varianceSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		calculate.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				IImageTrace t = getImageTrace();
				IDataset data = t.getData();
				List<IDataset> axes = t.getAxes();
				
				IDataset[] ax = null;
				
				if (axes != null) {
					ax = axes.toArray(new IDataset[axes.size()]);
				}
				
				int nComp = nComponents.getSelection();
				
				PCADataPackage p = new PCADataPackage(data, ax, nComp);
				
				job.setDataPack(p);
				status.setText("PCA running...");
				control.layout();
				currentResult = null;
				
				String[] vals = new String[nComp];
				
				for (int i = 1; i <= nComp; i++){
					vals[i-1] = "PC"+i;
				}
				
				String[] valsx = new String[nComp+1];
				valsx[0] = "Y-axis";
				System.arraycopy(vals, 0, valsx, 1, vals.length);
				
				yCombo.setItems(vals);
				yCombo.select(0);

				xCombo.setItems(valsx);
				xCombo.select(0);
				
				pcRecon.setMinimum(1);
				pcRecon.setMaximum(nComp);
				sampleRecon.setMinimum(0);
				sampleRecon.setMaximum(data.getShape()[0]-1);
				sampleRecon.setSelection(0);
				sampleRecon.getParent().pack();
				sampleRecon.getParent().layout();
				
				
				job.schedule();
			}
			
		});
		
		
		super.createControl(control);
	}
	
	private Composite makeReconstructionPanel(TabFolder folder) {
		Composite c = new Composite(folder, SWT.NONE);
		c.setLayout(new GridLayout(1,false));
		new Label(c, SWT.None);
		
		Composite c1 = new Composite(c, SWT.None);
		c1.setLayout(new GridLayout(4,false));
		
		Label l = new Label(c1, SWT.None);
		l.setText("PCs: ");
		l.setLayoutData(new GridData());
		pcRecon = new Spinner(c1, SWT.BORDER);
		pcRecon.setLayoutData(new GridData());
		l = new Label(c1, SWT.NONE);
		l.setText("Sample: ");
		l.setLayoutData(new GridData());
		sampleRecon = new Spinner(c1, SWT.BORDER);
		sampleRecon.setLayoutData(new GridData());
		
		pcRecon.addSelectionListener(new SelectionAdapter() {
		
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateReconstruction(currentDataPackage,currentResult);
			}
			
		});
		
		sampleRecon.addSelectionListener(new SelectionAdapter() {
		
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateReconstruction(currentDataPackage,currentResult);
			}
			
		});
		
		reconSystem.createPlotPart(c, 
				"PCA_tool_reconstruction", 
				null, 
				PlotType.XY,
				this.getViewPart());
		
		reconSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		return c;
	}
	
	private Composite makeLoadingScoresPlot(TabFolder folder){
		Composite c = new Composite(folder, SWT.NONE);
		c.setLayout(new GridLayout(2,false));
		new Label(c, SWT.None);
		
		Composite c1 = new Composite(c, SWT.None);
		c1.setLayout(new GridLayout(3,false));
		
		yCombo = new Combo(c1, SWT.None);
		yCombo.setItems(new String[] {"Y-axis","PC1","PC2"});
		yCombo.select(1);
		yCombo.addSelectionListener(new SelectionAdapter() {
	
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateResult(currentDataPackage, currentResult);
				
			}
			
		});
		
		Label vs = new Label(c1, SWT.None);
		vs.setText("VS");
		
		xCombo = new Combo(c1, SWT.None);
		xCombo.setItems(new String[] {"PC1","PC2"});
		xCombo.select(1);
		
		xCombo.addSelectionListener(new SelectionAdapter() {
	
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateResult(currentDataPackage, currentResult);
				
			}
			
		});
		
		loadingSystem.createPlotPart(c, 
				"PCA_tool_loading", 
				null, 
				PlotType.XY,
				this.getViewPart());
		
		loadingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		scoresSystem.createPlotPart(c, 
				"PCA_tool_scores", 
				null, 
				PlotType.XY,
				this.getViewPart());
		
		scoresSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		return c;
	}
	
	private void updateResult(PCADataPackage data, PCAResult fit){
		
		if (data == null || fit == null) return;
		
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(() -> updateResult(data, fit));
			return;
		}
		
		status.setText("PCA done.");
		control.layout();
		
		IDataset loadings = fit.getLoadings();
		IDataset loadAx = null;
		
		if (data.getAxes() != null && data.getAxes()[0] != null) {
			loadAx = data.getAxes()[0];
		}
		
		List<IDataset> loadList = new ArrayList<IDataset>();
		IDataset mean = fit.getMean().getSliceView();
		mean.setName("Mean");
		loadList.add(mean);
		
		IDataset scoresx = null;
		IDataset scoresy = null;
		
		IDataset scores = fit.getScores();
		
		String x = xCombo.getText();
		if (x.equals("Y-axis")) {
			if (data.getAxes() != null && data.getAxes()[1] != null) {
				scoresx = data.getAxes()[1];
			}
			
		} else {
			int xpc = Integer.parseInt(x.substring(2, x.length()));
			SliceND sx = new SliceND(loadings.getShape());
			sx.setSlice(0,xpc-1, xpc, 1);
			IDataset pcx = loadings.getSlice(sx);
			pcx.setName("PC" + xpc);
			pcx.squeeze();
			loadList.add(pcx);
			
			SliceND ss = new SliceND(scores.getShape());
			ss.setSlice(1, xpc-1, xpc, 1);
			scoresx = scores.getSlice(ss);
			scoresx.setName("PC" + xpc);
			scoresx.squeeze();
		}
		
		String y = yCombo.getText();
		int ypc = Integer.parseInt(y.substring(2, y.length()));
		SliceND s = new SliceND(loadings.getShape());
		s.setSlice(0, ypc-1, ypc, 1);
		IDataset pcx = loadings.getSlice(s);
		pcx.setName("PC" + ypc);
		pcx.squeeze();
		loadList.add(pcx);
		
		SliceND ss = new SliceND(scores.getShape());
		ss.setSlice(1, ypc-1, ypc, 1);
		scoresy = scores.getSlice(ss);
		scoresy.setName("PC" + ypc);
		scoresy.squeeze();
		
		loadingSystem.clear();
		loadingSystem.createPlot1D(loadAx, loadList, null);
		loadingSystem.setShowLegend(true);
		loadingSystem.setTitle("Loadings");
	
		scoresSystem.clear();
		ILineTrace l = scoresSystem.createTrace("trace", ILineTrace.class);
		
		l.setData(scoresx, scoresy);
		
		l.setTraceType(TraceType.POINT);
		l.setPointStyle(PointStyle.FILLED_CIRCLE);
		scoresSystem.addTrace(l);
		scoresSystem.getSelectedXAxis().setTitle(scoresx == null ? "sample" : scoresx.getName());
		scoresSystem.getSelectedYAxis().setTitle(scoresy == null ? "axis" : scoresy.getName());
		scoresSystem.autoscaleAxes();
		
		updateVarianceExplained(fit.getVarianceRatio());
		updateReconstruction(data, fit);
	}
	
	private void updateReconstruction(final PCADataPackage data, final PCAResult result) {
		
		
		if (data == null || result == null) return;
		
		final int selection = sampleRecon.getSelection();
		final int nPCs = pcRecon.getSelection();
		
		Runnable r = () -> {
			
			IDataset d = data.getData();
			SliceND s = new SliceND(d.getShape());
			s.setSlice(0, selection, selection+1,1);
			
			IDataset slice = d.getSlice(s);
			IDataset sq = slice.squeeze();
			sq.setName("Sample " + selection);
			
			IDataset scores = result.getScores();
			SliceND scoresSlice = new SliceND(scores.getShape());
			scoresSlice.setSlice(1, 0, nPCs, 1);
			
			final Dataset subScores = DatasetUtils.convertToDataset(scores.getSlice(scoresSlice));
			
			IDataset loadings = result.getLoadings();
			SliceND loadSlice = new SliceND(loadings.getShape());
			loadSlice.setSlice(0, 0, nPCs, 1);
			
			final Dataset subLoads = DatasetUtils.convertToDataset(loadings.getSlice(loadSlice));
			
			Dataset dotProduct = LinearAlgebra.dotProduct(subScores,subLoads);
			
			Dataset test = dotProduct.getSlice(s);
			Dataset ts = test.squeeze();
			ts.iadd(result.getMean());
			Dataset recon = dotProduct.squeeze();
			recon.iadd(result.getMean());
			
			IDataset residual = Maths.subtract(sq, ts);
			residual.setName("Residual");
			
			IDataset loadAx = null;
			
			if (data.getAxes() != null && data.getAxes()[0] != null) {
				loadAx = data.getAxes()[0];
			}
			
			reconSystem.clear();
			reconSystem.createPlot1D(loadAx, Arrays.asList(sq,ts,residual), null);
		};
		
		jobRecon.setRunnable(r);
		jobRecon.schedule();
		
	}
	
	private void updateVarianceExplained(IDataset explained){
		
		Dataset range = DatasetFactory.createRange(IntegerDataset.class,1, explained.getSize()+1, 1);
		range.setName("Component");
		
		Dataset s = DatasetUtils.convertToDataset(explained.getSlice());
		s.imultiply(100);
		s.setName("% Variance Explained");
		
		varianceSystem.createPlot1D(range, Arrays.asList(new IDataset[]{s}), null);
	}
	
	@Override
	public void dispose() {
		if (reconSystem!=null) reconSystem.dispose();
		if (varianceSystem!=null) varianceSystem.dispose();
		if (scoresSystem!=null) scoresSystem.dispose();
		if (loadingSystem!=null) loadingSystem.dispose();
		
		super.dispose();
	}
	
	@Override
	public Control getControl() {
		
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();

	}
	
	private class PCADataPackage {
		
		private IDataset data;
		private IDataset[] axes;
		private int nComponents;
		
		public PCADataPackage(IDataset data, IDataset[] axes, int nComponents){
			this.data = data;
			this.axes = axes;
			this.nComponents = nComponents;
		}

		public IDataset getData() {
			return data;
		}

		public IDataset[] getAxes() {
			return axes;
		}

		public int getnComponents() {
			return nComponents;
		}
		
	}
	
	private class PCAJob extends Job {

		private AtomicReference<PCADataPackage> dataPack;
		
		public PCAJob() {
			super("PCA");
			dataPack = new AtomicReference<>();
		}
		
		public void setDataPack(PCADataPackage data) {
			dataPack.set(data);
		}
		

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			PCADataPackage p = dataPack.getAndSet(null);
			
			if (p == null) return Status.OK_STATUS;
			long start = System.currentTimeMillis();
			PCAResult fit = PCA.fit(DatasetUtils.convertToDataset(p.getData()),p.getnComponents());
			System.out.println("PCA in: " + (System.currentTimeMillis()-start));
			currentResult = fit;
			currentDataPackage = p;
			updateResult(p, fit);
			
			return Status.OK_STATUS;
		}
	}
	
	private class PCAReconJob extends Job {

		private AtomicReference<Runnable> runnable;
		
		public PCAReconJob() {
			super("PCA Recon");
			runnable = new AtomicReference<>();
		}
		
		public void setRunnable(Runnable data) {
			runnable.set(data);
		}
		

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			Runnable p = runnable.getAndSet(null);
			
			if (p == null) return Status.OK_STATUS;
			
			p.run();
			
			return Status.OK_STATUS;
		}
	}
}
