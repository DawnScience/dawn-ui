package org.dawnsci.surfacescatter.ui;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.PlatformUI;

public class TrackingProgressAndAbortView extends Dialog {
	
	
	private Button abort;
	private ProgressBar progress;
	private int maximum;
	private SurfaceScatterPresenter ssp;
	private IPlottingSystem<Composite> subImage;
	private IPlottingSystem<Composite> outputCurvesPlotSystem;
	private IPlottingSystem<Composite> mainImagePlotSytem;
	private TabFolder backgroundTabFolder;
	private IPlottingSystem<Composite> subBgImage;
	private Shell parentShell;
	private TrackingProgressAndAbortView tpaav;
	
	
	public TrackingProgressAndAbortView(Shell parentShell, 
										int maximum,
										SurfaceScatterPresenter ssp,
										IPlottingSystem<Composite> subImage,
										IPlottingSystem<Composite> outputCurvesPlotSystem,
										IPlottingSystem<Composite> mainImagePlotSytem,
										TabFolder backgroundTabFolder,
										IPlottingSystem<Composite> subBgImage) {
		
		
		super(parentShell);
		this.parentShell = parentShell;
		this.ssp =ssp;
		this.maximum = maximum;		
		this.subImage = subImage;
		this.outputCurvesPlotSystem =outputCurvesPlotSystem;
		this.mainImagePlotSytem = mainImagePlotSytem;
		this.backgroundTabFolder= backgroundTabFolder;
		this.subBgImage = subBgImage;
		this.tpaav = this;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);		

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		progress = new ProgressBar(container, SWT.HORIZONTAL | SWT.SMOOTH);
		progress.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progress.setMinimum(0);
		progress.setMaximum(maximum);
		
		abort = new Button (container, SWT.PUSH);
		abort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		abort.setText("Abort");		
		
		abort.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		try{
		
			ssp.runTrackingJob(subImage,
							   outputCurvesPlotSystem, 
							   mainImagePlotSytem,
							   backgroundTabFolder, 
							   subBgImage, 
							   progress,
							   tpaav
							  );
		}
		catch(IndexOutOfBoundsException d){
			ssp.boundariesWarning();
		}
		
		catch(java.lang.OutOfMemoryError e){
			ssp.boundariesWarning();
		}
		
	

		
		
		
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Processing...");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(0.2*w), (int) Math.round(0.15*h));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	
//	@Override
//    protected void createButtonsForButtonBar(Composite parent) {
//            createButton(parent, IDialogConstants.OK_ID, "OK", true);
//            override = createButton(parent, IDialogConstants.CANCEL_ID,
//                           "Override", false);
//    }
//	
//	public Button getOverride(){
//		return override;
//	}
//	
	
	
	
	public ProgressBar getProgressBar(){
		return progress;
	}
}