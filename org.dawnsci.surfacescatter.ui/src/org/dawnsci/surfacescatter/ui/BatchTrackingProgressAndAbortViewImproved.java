package org.dawnsci.surfacescatter.ui;

import java.util.concurrent.Future;

import org.dawnsci.surfacescatter.BatchRodModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class BatchTrackingProgressAndAbortViewImproved extends Dialog {

	private ProgressBar progress;
	private BatchRodModel brm;
	private BatchRunner br;

	public BatchTrackingProgressAndAbortViewImproved(Shell parentShell, BatchRodModel brm) {

		super(parentShell);
		this.brm = brm;

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
		int max = (brm.getBrdtoList().size());
		progress.setMaximum(max);

		Button abort = new Button(container, SWT.PUSH);
		abort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		abort.setText("Abort");

		final Display display = Display.getCurrent();

		abort.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				br.getExecutor().shutdownNow();
				getShell().close();

			}
		});

		br = new BatchRunner(brm, progress, this, display);

		
//		br.getBatch().
//		
//		for (Future<Boolean> future : br.getBatch()) {
//			try{
//				if(future.get()) {
//
//							if (progress.isDisposed() != true) {
//								progress.setSelection(progress.getSelection() + 1);
//			
//								if (progress.getSelection() == progress.getMaximum()) {
//									getShell().close();
//								}
//			
////							}
////							return;
//						}
////					});
//				}
//			}catch (Exception e) {
//				System.out.println(e.getMessage());
//				throw new RuntimeException(e);
//			}
//		}
		
		
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

		return new Point((int) Math.round(0.3 * w), (int) Math.round(0.2 * h));
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);
		c.setVisible(true);
		return c;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public ProgressBar getProgressBar() {
		return progress;
	}
}