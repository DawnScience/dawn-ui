package org.dawnsci.jzy3d;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AxesAspectDialog extends Dialog {
	
	private CustomSquarifier squarifier;
	
	private Text xText;
	private Text yText;
	private Text zText;

	protected AxesAspectDialog(Shell parentShell, CustomSquarifier squarifier) {
		super(parentShell);
		this.squarifier = squarifier;
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		Label x = new Label(container, SWT.NONE);
		x.setText("X size");
		x.setLayoutData(GridDataFactory.fillDefaults().create());
		
		xText = new Text(container, SWT.NONE);
		xText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		xText.setText("1");
		setUpListener(xText);
		
		Label y = new Label(container, SWT.NONE);
		y.setText("Y size");
		y.setLayoutData(GridDataFactory.fillDefaults().create());
		yText = new Text(container, SWT.NONE);
		yText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		yText.setText("1");
		setUpListener(yText);
		
		Label z = new Label(container, SWT.NONE);
		z.setText("Z size");
		z.setLayoutData(GridDataFactory.fillDefaults().create());
		
		zText = new Text(container, SWT.NONE);
		zText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		zText.setText("1");
		setUpListener(zText);
		
		update();
		
		xText.forceFocus();
		
		return container;
	}

	private void update() {
		Double d = parseDouble(xText);
		squarifier.setxScaleFactor(d != null ? d.floatValue() : 1.0f);
		if (d == null) {
			xText.setText("1");
		}
		d = parseDouble(yText);
		squarifier.setyScaleFactor(d != null ? d.floatValue() : 1.0f);
		if (d == null) {
			yText.setText("1");
		}
		d = parseDouble(zText);
		squarifier.setzScaleFactor(d != null ? d.floatValue() : 1.0f);
		if (d == null) {
			zText.setText("1");
		}
	}
	
	private Double parseDouble(Text widget) {
		String text = widget.getText();
		
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	private void setUpListener(Text text) {
		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				update();
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				//do nothing
				
			}
		});
		
		//force focus loss on enter
		text.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (SWT.TRAVERSE_RETURN == e.detail) {
					update();
				}
			}
		});
	}
	
}
