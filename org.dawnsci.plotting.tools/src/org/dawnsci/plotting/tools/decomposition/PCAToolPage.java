package org.dawnsci.plotting.tools.decomposition;


import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.scisoft.analysis.decomposition.PCA;
import uk.ac.diamond.scisoft.analysis.decomposition.PCAResult;

public class PCAToolPage extends AbstractToolPage {

	IPlottingSystem<Composite> system;
	
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			
			return;
		}
		
		parent.setLayout(new GridLayout(1,true));
		
		Button calculate = new Button(parent, SWT.PUSH);
		calculate.setLayoutData(new GridData());
		
		calculate.setText("Run PCA");
		
		system.createPlotPart(parent, 
				getTitle(), 
				null, 
				PlotType.XY,
				this.getViewPart());
		
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		calculate.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IImageTrace t = getImageTrace();
				IDataset data = t.getData();
				long start = System.currentTimeMillis();
				PCAResult fit = PCA.fit(DatasetUtils.convertToDataset(data),10);
				System.out.println("PCA in: " + (System.currentTimeMillis()-start));
				IDataset loadings = fit.getLoadings();
				SliceND s = new SliceND(loadings.getShape());
				s.setSlice(0, 0, 1, 1);
				IDataset pc1 = loadings.getSlice(s);
				pc1.setName("PC1");
				pc1.squeeze();
				s.setSlice(0, 1, 2, 1);
				IDataset pc2 = loadings.getSlice(s);
				pc2.setName("PC2");
				pc2.squeeze();
				System.out.println(fit.getVarianceRatio().toString());
				
				IDataset scores = fit.getScores();
				SliceND ss = new SliceND(scores.getShape());
				ss.setSlice(1, 0, 1, 1);
				IDataset pc1s = scores.getSlice(ss);
				pc1s.setName("PC1");
				pc1s.squeeze();
				ss.setSlice(1, 1, 2, 1);
				IDataset pc2s = scores.getSlice(ss);
				pc2s.setName("PC2");
				pc2s.squeeze();
				System.out.println(fit.getVarianceRatio().toString());
				
				ss.setSlice(1, 2, 3, 1);
				IDataset pc3s = scores.getSlice(ss);
				pc3s.setName("PC3");
				pc3s.squeeze();
				System.out.println(fit.getVarianceRatio().toString());
				
				pc1 = DatasetUtils.convertToDataset(pc1).imultiply(pc1s.mean());
				pc2 = DatasetUtils.convertToDataset(pc2).imultiply(pc2s.mean());
				
				IDataset m = fit.getMean();
				
				m.setName("Mean");
				
				system.createPlot1D(t.getAxes().get(0), Arrays.asList(new IDataset[]{m,pc1,pc2}), null);
//				system.createPlot1D(null, Arrays.asList(new IDataset[]{pc1s,pc2s,pc3s}), null);
				
			}
			
		});
		
		
		super.createControl(parent);
	}
	
	@Override
	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
