package org.dawnsci.plotting.tools.window;

import java.util.Collection;

import org.dawb.common.ui.util.DisplayUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.dawnsci.plotting.tools.window.WindowTool.WindowJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.IPageSite;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Class used to create the composite with spinners to control the Window tool
 * @author wqk87977
 *
 */
public class RegionControlWindow {

	private Composite parent;
	private Spinner spnStartX;
	private Spinner spnStartY;
	private Spinner spnWidth;
	private Spinner spnHeight;
	private Button btnOverwriteAspect;
	private Spinner spnXAspect;
	private Spinner spnYAspect;
	private IPlottingSystem windowSystem;
	private SelectionListener selectionListener;
	private IPlottingSystem plottingSystem;
	private boolean isOverwriteAspect;

	public RegionControlWindow(Composite parent, 
			final IPlottingSystem plottingSystem, 
			final IPlottingSystem windowSystem, 
			final WindowJob windowJob) {
		this.parent = parent;
		this.plottingSystem = plottingSystem;
		this.windowSystem = windowSystem;
		this.selectionListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOverwriteAspect = btnOverwriteAspect.getSelection();

				int startPosX = spnStartX.getSelection();
				int startPosY = spnStartY.getSelection();
				int width = spnWidth.getSelection();
				int height = spnHeight.getSelection();
				if (startPosX + width > spnWidth.getMaximum()) {
					width = spnWidth.getMaximum() - startPosX;
				}
				if (startPosY + height > spnHeight.getMaximum()) {
					height = spnHeight.getMaximum() - startPosY;
				}
				int endPtX = width + startPosX;
				int endPtY = height + startPosY;
				IRegion region = windowSystem.getRegion("Window");
				RectangularROI rroi = new RectangularROI(startPosX, startPosY, width, height, 0);

				int xAspectRatio = 0, yAspectRatio = 0, binShape = 1, samplingMode = 0;
				if (!e.getSource().equals(btnOverwriteAspect)) {
					if (region != null)
						region.setROI(rroi);
					if (isOverwriteAspect()){
						xAspectRatio = getXAspectRatio();
						yAspectRatio = getYAspectRatio();
					}
					// size above 300x300
					if (rroi.getLengths()[0] > 300 && rroi.getLengths()[1] > 300) {
						// apply dawnsampling with bin of 3
						binShape = 3;
						// DownsampleMode.MEAN = 2
						samplingMode = 2;
					}
				} else if (e.getSource().equals(btnOverwriteAspect)) {
					if (isOverwriteAspect) {
						xAspectRatio = getXAspectRatio();
						yAspectRatio = getYAspectRatio();
					}
					spnXAspect.setEnabled(isOverwriteAspect);
					spnYAspect.setEnabled(isOverwriteAspect);
				}
				SurfacePlotROI sroi = new SurfacePlotROI(startPosX, 
						startPosY, 
						endPtX, 
						endPtY, 
						samplingMode, samplingMode, 
						xAspectRatio, 
						yAspectRatio);
				sroi.setXBinShape(binShape);
				sroi.setYBinShape(binShape);
				windowJob.schedule(sroi);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
	}

	/**
	 * Gets the trace or the first trace if there are more than one.
	 * @return
	 */
	protected ITrace getTrace() {
		if (plottingSystem == null) return null;

		final Collection<ITrace> traces = plottingSystem.getTraces();
		if (traces==null || traces.size()==0) return null;
		return traces.iterator().next();
	}

	protected ISurfaceTrace getSurfaceTrace() {
		final ITrace trace = getTrace();
		return trace instanceof ISurfaceTrace ? (ISurfaceTrace)trace : null;
	}

	public boolean isOverwriteAspect(){
		return isOverwriteAspect;
	}

	public int getXAspectRatio() {
		return spnXAspect.getSelection();
	}

	public int getYAspectRatio() {
		return spnYAspect.getSelection();
	}

	public Composite createRegionControl(String title, IPageSite site, IViewPart viewPart) {
		Composite windowComposite = new Composite(parent, SWT.NONE);
		windowComposite.setLayout(new FillLayout(SWT.VERTICAL));

		windowSystem.createPlotPart(windowComposite, title, site.getActionBars(), PlotType.IMAGE, viewPart);
		final ISurfaceTrace surface = getSurfaceTrace();

		int xStartPt = (int) (surface != null && surface.getWindow() != null ? surface.getWindow().getPoint()[0] : 0);
		int yStartPt = (int) (surface!=null && surface.getWindow() != null ? surface.getWindow().getPoint()[1] : 0);
		int width = 300;
		int height = 300;
		if(surface!=null && surface.getWindow() instanceof SurfacePlotROI){
			width = surface!=null ? ((SurfacePlotROI)surface.getWindow()).getEndX() : width;
			height = surface!=null ? ((SurfacePlotROI)surface.getWindow()).getEndY() : height;
		}
		ITrace trace = getTrace();
		int xSize = 0, ySize = 0;
		if (trace != null) {
			if (trace instanceof ISurfaceTrace) {
				xSize = getTrace().getData().getShape()[1];
				ySize = getTrace().getData().getShape()[0];
			}
		} else {
			xSize = 1000;
			ySize = 1000;
		}

		Composite bottomComposite = new Composite(windowComposite,SWT.NONE | SWT.BORDER);
		bottomComposite.setLayout(new GridLayout(1, false));
		bottomComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite spinnersComp = new Composite(bottomComposite, SWT.NONE);
		spinnersComp.setLayout(new GridLayout(4, false));
		spinnersComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		Label lblStartX = new Label(spinnersComp, SWT.RIGHT);
		lblStartX.setText("Start X:");
		
		spnStartX = new Spinner(spinnersComp, SWT.BORDER);
		spnStartX.setMinimum(0);
		spnStartX.setMaximum(xSize);
		spnStartX.setSize(62, 18);
		spnStartX.addSelectionListener(selectionListener);

		Label lblStartY = new Label(spinnersComp, SWT.RIGHT);
		lblStartY.setText("Start Y:");

		spnStartY = new Spinner(spinnersComp, SWT.BORDER);
		spnStartY.setMinimum(0);
		spnStartY.setMaximum(ySize);
		spnStartY.setSize(62, 18);
		spnStartY.addSelectionListener(selectionListener);

		Label lblEndX = new Label(spinnersComp, SWT.RIGHT);
		lblEndX.setText("Width:");

		spnWidth = new Spinner(spinnersComp, SWT.BORDER);
		spnWidth.setMinimum(0);
		spnWidth.setMaximum(xSize);
		spnWidth.setSize(62, 18);
		spnWidth.addSelectionListener(selectionListener);

		Label lblEndY = new Label(spinnersComp, SWT.RIGHT);
		lblEndY.setText("Height:");

		spnHeight = new Spinner(spinnersComp, SWT.BORDER);
		spnHeight.setSize(62, 18);
		spnHeight.setMinimum(0);
		spnHeight.setMaximum(ySize);
		spnHeight.addSelectionListener(selectionListener);

		setSpinnerValues(xStartPt, yStartPt, width, height);

		Composite aspectComp = new Composite(bottomComposite, SWT.NONE); 
		aspectComp.setLayout(new GridLayout(4, false));
		aspectComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnOverwriteAspect = new Button(aspectComp,SWT.CHECK);
		btnOverwriteAspect.setText("Override Aspect-Ratio");
		btnOverwriteAspect.addSelectionListener(selectionListener);

		spnXAspect = new Spinner(aspectComp,SWT.NONE);
		spnXAspect.setEnabled(false);
		spnXAspect.setMinimum(1);
		spnXAspect.setMaximum(10);
		spnXAspect.setSelection(1);
		spnXAspect.setIncrement(1);
		spnXAspect.addSelectionListener(selectionListener);

		Label lblDelimiter = new Label(aspectComp,SWT.NONE);
		lblDelimiter.setText(":");

		spnYAspect = new Spinner(aspectComp,SWT.NONE);
		spnYAspect.setEnabled(false);
		spnYAspect.setMinimum(1);
		spnYAspect.setMaximum(10);
		spnYAspect.setSelection(1);
		spnYAspect.setIncrement(1);
		spnYAspect.addSelectionListener(selectionListener);

		return windowComposite;
	}

	/**
	 * Set the spinner values
	 * @param startX start position in x dimension
	 * @param startY start position in y dimension
	 * @param width
	 * @param height
	 */
	protected void setSpinnerValues(final int startX, 
								 final int startY, 
								 final int width, 
								 final int height) {
		DisplayUtils.runInDisplayThread(true, parent, new Runnable() {
			@Override
			public void run() {
				spnStartX.setSelection(startX);
				spnStartY.setSelection(startY);
				spnWidth.setSelection(width);
				spnHeight.setSelection(height);
			}
		});
	}

	public void addSelectionListener() {
		if (spnStartX != null && !spnStartX.isDisposed())
			spnStartX.addSelectionListener(selectionListener);
		if (spnStartY != null && !spnStartY.isDisposed())
			spnStartY.addSelectionListener(selectionListener);
		if (spnWidth != null && spnWidth.isDisposed())
			spnWidth.addSelectionListener(selectionListener);
		if (spnHeight != null && !spnHeight.isDisposed())
			spnHeight.addSelectionListener(selectionListener);
	}

	public void removeSelectionListener() {
		if (spnStartX != null && !spnStartX.isDisposed())
			spnStartX.removeSelectionListener(selectionListener);
		if (spnStartY != null && !spnStartY.isDisposed())
			spnStartY.removeSelectionListener(selectionListener);
		if (spnWidth != null && !spnWidth.isDisposed())
			spnWidth.removeSelectionListener(selectionListener);
		if (spnHeight != null && !spnHeight.isDisposed())
			spnHeight.removeSelectionListener(selectionListener);
	}
}
