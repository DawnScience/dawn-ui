package org.dawnsci.surfacescatter.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class BatchRodNameSetterDialogue extends Dialog {
	
	private Text suggestedRodNameText;
	private BatchDatDisplayer dd;
	private String suggestedRodName;
	
	public BatchRodNameSetterDialogue(Shell parentShell, 
								   String suggestedRodName,
								   BatchDatDisplayer dd) {
		
		super(parentShell);
		this.dd= dd;
		this.suggestedRodName =suggestedRodName;
				
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);	
		
		return c;
	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		Label explanation = new Label(container, SWT.NONE);
		GridData explanationData = new GridData((GridData.FILL_HORIZONTAL));
		explanation.setLayoutData(explanationData);
		
		explanation.setText("Name for this rod?");
		
		if(StringUtils.isEmpty(suggestedRodName)) {
			suggestedRodName = "the_rod_with_no_name";
		}
		
		InputTileGenerator tile1 = new InputTileGenerator("Rod Name:", suggestedRodName, container, 0);
		suggestedRodNameText = tile1.getText();
		
		suggestedRodNameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				suggestedRodName = suggestedRodNameText.getText();
				dd.setRodName(suggestedRodName);
				
			}
		});
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Name The Rod");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(0.6*w), (int) Math.round(0.3*h));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	
}