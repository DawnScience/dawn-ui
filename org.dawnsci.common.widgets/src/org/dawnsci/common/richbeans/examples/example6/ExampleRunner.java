package org.dawnsci.common.richbeans.examples.example6;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.examples.ExampleJSONWritingValueListener;
import org.dawnsci.common.richbeans.examples.example6.data.DecoratorBean;
import org.dawnsci.common.richbeans.examples.example6.ui.DecoratorComposite;
import org.dawnsci.common.richbeans.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * An example of using the composite and bean together.
 * 
 * @author Matthew Gerring
 */
public class ExampleRunner {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Change a value to see bean as JSON");

		// Composite
		final DecoratorComposite ui = new DecoratorComposite(shell, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Button enable = new Button(shell, SWT.TOGGLE);
		enable.setSelection(true);
		enable.setText("Enable");

		// Something to show value.
		final Label value = new Label(shell, SWT.WRAP);

		shell.pack();
		shell.setSize(420, 400);

		// Set some initial values
		final DecoratorBean bean = new DecoratorBean();
		bean.setX(10.0);
		bean.setY(5);

		// Connect the UI and bean
		final BeanController controller = new BeanController(ui, bean);
		controller.addValueListener(new ExampleJSONWritingValueListener(controller, value));
		controller.beanToUI();
		controller.switchUIOn();

		// Enable/disable by reflection
		enable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				try {
					controller.setUIEnabled(enable.getSelection());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		SWTUtils.showCenteredShell(shell);
	}
}
