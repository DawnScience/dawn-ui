package org.dawnsci.common.richbeans.examples.example3;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.examples.ExampleJSONWritingValueListener;
import org.dawnsci.common.richbeans.examples.example3.data.ExampleBean;
import org.dawnsci.common.richbeans.examples.example3.data.ExampleItem;
import org.dawnsci.common.richbeans.examples.example3.data.ExampleItem.ItemChoice;
import org.dawnsci.common.richbeans.examples.example3.ui.ExampleComposite;
import org.dawnsci.common.richbeans.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		final ExampleComposite ui = new ExampleComposite(shell, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Something to show value.
		final Label value = new Label(shell, SWT.WRAP);
		value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		shell.pack();
		shell.setSize(420, 500);

		// Set some initial values
		final ExampleBean bean = new ExampleBean();
		bean.addItem(new ExampleItem(1, 2));
		bean.addItem(new ExampleItem(2, 3, ItemChoice.POLAR));

		// Connect the UI and bean
		final BeanController controller = new BeanController(ui, bean);
		controller.addValueListener(new ExampleJSONWritingValueListener(controller, value));
		controller.beanToUI();
		controller.switchUIOn();

		SWTUtils.showCenteredShell(shell);
	}
}
