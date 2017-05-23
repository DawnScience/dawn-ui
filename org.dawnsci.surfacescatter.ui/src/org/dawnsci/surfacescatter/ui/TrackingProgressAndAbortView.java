package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
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
import org.eclipse.ui.PlatformUI;

public class TrackingProgressAndAbortView extends Dialog {
	
	
	private Button abort;
	private ProgressBar progress;
	private int maximum;
	private SurfaceScatterViewStart ssvs;
	private SurfaceScatterPresenter ssp;
	private TrackingHandlerWithFrames tj; 
	
	
	public TrackingProgressAndAbortView(Shell parentShell, 
										int maximum,
										SurfaceScatterPresenter ssp,
										SurfaceScatterViewStart ssvs) {
		
		
		super(parentShell);
		this.ssp =ssp;
		this.maximum = maximum;		
		this.ssvs = ssvs;
		
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.APPLICATION_MODAL);		

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
				Thread t = tj.getT();
				t.interrupt();				
				getShell().close();

			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		try{
			
			tj = new TrackingHandlerWithFrames(); 
			
			
			tj.setProgress(progress);
			tj.setSsvs(ssvs);
			tj.setCorrectionSelection(MethodSetting.toInt(ssp.getCorrectionSelection()));
			tj.setOutputCurves(ssvs.getSsps3c().getOutputCurves().getPlotSystem());
//			tj.setTimeStep(Math.round((2 / ssp.getNoImages())));
			tj.setSsp(ssp);
			tj.setTPAAV(TrackingProgressAndAbortView.this);
			tj.runTJ1();
			

		}
		catch(IndexOutOfBoundsException d){
			ssp.boundariesWarning();
		}
		
		catch(OutOfMemoryError e){
			ssp.outOfMemoryWarning();
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
		
		return new Point((int) Math.round(0.2*w), (int) Math.round(0.4*h));
	}
	
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);
		c.setVisible(true);
//		c.dispose();
		return c;
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	

	
	public ProgressBar getProgressBar(){
		return progress;
	}
}