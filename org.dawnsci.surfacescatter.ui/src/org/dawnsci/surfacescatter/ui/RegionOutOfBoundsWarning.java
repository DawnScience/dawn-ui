package org.dawnsci.surfacescatter.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;



public class RegionOutOfBoundsWarning extends Dialog {
	
	
	private int selector;
	private String note;
	
	
	public RegionOutOfBoundsWarning(Shell parentShell, int selector, String note) {
		
		super(parentShell);
		this.selector = selector;
		this.note = note;		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
//		createDialogArea(parentShell.getParent());

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label warning = new Label(container, SWT.FILL);
		if(selector == 0 ){
			warning.setText("Bounday Box has over run image boundaries");	
		}
		if (selector == 1){
			warning.setText("Enter number as int or double");	
		}
		if (selector == 2){
			warning.setText("Error in geometeric corrections. Check experimental setup");	
		}
		
		
		if(note != null){
			Label noteLabel = new Label(container, SWT.FILL);
			noteLabel.setText(note);
		}
		
		
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Boundaries Warning");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(0.2*w), (int) Math.round(0.2*h));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	
}