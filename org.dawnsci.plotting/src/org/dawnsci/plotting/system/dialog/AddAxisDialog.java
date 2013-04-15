package org.dawnsci.plotting.system.dialog;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.AxisUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddAxisDialog extends Dialog {

	private IPlottingSystem system;
	private String          title;
	private boolean         y;
	private int             side;

	public AddAxisDialog(Shell parentShell, IPlottingSystem system) {
		super(parentShell);
		this.system = system;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Axis");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		content.setLayout(new GridLayout(2, false));
		
		Label label = new Label(content, SWT.NONE);
		label.setText("Title");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		final Text title = new Text(content, SWT.BORDER);
		final String defaultAxisName = AxisUtils.getUniqueAxisTitle("Axis", system);
		title.setText(defaultAxisName);
		setTitle(defaultAxisName);
		
		title.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				setTitle(title.getText());
			}
		});
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		label = new Label(content, SWT.NONE);
		label.setText("Axis Type");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		final CCombo axisType = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
		axisType.setItems(new String[]{"X-Axis", "Y-Axis"});
		axisType.select(1);
		setY(true);
		axisType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		label = new Label(content, SWT.NONE);
		label.setText("Axis Side");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		final CCombo axisSide = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
		axisSide.setItems(new String[]{"Left", "Right"});
		axisSide.select(0);
		setSide(SWT.LEFT);
		axisSide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		axisType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				setY(axisType.getSelectionIndex()==1);
				if (isY()) {
					axisSide.setItems(new String[]{"Left", "Right"});
				} else {
					axisSide.setItems(new String[]{"Bottom", "Top"});
				}
				axisSide.select(0);
			}
		});
		
		axisSide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int sel = axisSide.getSelectionIndex();
				if (isY()) {
					setSide(sel==0?SWT.LEFT:SWT.RIGHT);
				} else {
					setSide(sel==0?SWT.BOTTOM:SWT.TOP);
				}
			}
		});


		return content;
	}
	
	@Override
	protected void okPressed() {
		// Check axis name unique
		if (!AxisUtils.isAxisUnique(getTitle(), system)) {
			MessageDialog.openWarning(getShell(), "Name Exists", "The axis '"+getTitle()+"' already exists.\n\nPlease choose another one.");
			return;
		}
		super.okPressed();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public boolean isY() {
		return y;
	}

	public void setY(boolean y) {
		this.y = y;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

}
