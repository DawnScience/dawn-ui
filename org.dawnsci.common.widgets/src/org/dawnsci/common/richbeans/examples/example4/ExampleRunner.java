package org.dawnsci.common.richbeans.examples.example4;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.examples.ExampleJSONWritingValueListener;
import org.dawnsci.common.richbeans.examples.example4.data.ExampleBean;
import org.dawnsci.common.richbeans.examples.example4.data.ExampleItem;
import org.dawnsci.common.richbeans.examples.example4.data.ExampleItem.ItemChoice;
import org.dawnsci.common.richbeans.examples.example4.data.OptionItem;
import org.dawnsci.common.richbeans.examples.example4.ui.ExampleComposite;
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
		final ExampleComposite ui = new ExampleComposite(shell, SWT.BORDER);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Something to show value.
		final Label value = new Label(shell, SWT.WRAP);
		value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		shell.pack();
		shell.setSize(700, 700);

		// Set some initial values
		final ExampleBean bean = new ExampleBean();
		ExampleItem item = new ExampleItem(1, 2);
		item.addOption(new OptionItem(true, true, true, true));
		bean.addItem(item);

		item = new ExampleItem(2, 3, ItemChoice.POLAR);
		item.addOption(new OptionItem(true, false, true, false));
		item.addOption(new OptionItem(false, false, false, false));
		bean.addItem(item);

		bean.addItem(new ExampleItem(3, 4, ItemChoice.XY));

		// Connect the UI and bean
		final BeanController controller = new BeanController(ui, bean);
		controller.addValueListener(new ExampleJSONWritingValueListener(controller, value));
		controller.beanToUI();
		controller.switchUIOn();

		SWTUtils.showCenteredShell(shell);
	}
}
