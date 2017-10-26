package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.StareModeSelection;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class StareModeSelector  extends Dialog {


	private StareModeSelection bdd;

	public StareModeSelector(Shell parentShell, StareModeSelection bdd) {

		super(parentShell);
		
		this.bdd = bdd;
		
		setShellStyle(getShellStyle() | SWT.RESIZE);

		createDialogArea(parentShell.getParent());

	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label explanation = new Label(container, SWT.FILL);
		explanation.setText("There are a different number of frames in the paramter file and the rod to be built.\n Should I proceed with RoDan set at a fixed spot (Stare Mode)?\n"
				+ "Fixed spot will be set from first frame in parameter file.");
		
		
		Group proceedChoice = new Group(container, SWT.NONE);
		GridLayout proceedChoiceLayout = new GridLayout(2, true);
		GridData proceedChoiceData = new GridData((GridData.FILL_HORIZONTAL));
		proceedChoice.setLayout(proceedChoiceLayout);
		proceedChoice.setLayoutData(proceedChoiceData);
		

		Button abort = new Button(proceedChoice, SWT.PUSH);
		abort.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		abort.setText("Abort");
		
		
		Button proceed = new Button(proceedChoice, SWT.PUSH);
		proceed.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		proceed.setText("Proceed");

		
		addlisteners(abort, false);
		addlisteners(proceed, true);

		return container;
	}

	private void addlisteners(Button b, boolean g) {
		
		b.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				bdd.setAccept(g);
				StareModeSelector.this.close();
			}
	
		});
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Error Warning");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;

		return new Point((int) Math.round(0.4*w), (int) Math.round(0.2*h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}


}
