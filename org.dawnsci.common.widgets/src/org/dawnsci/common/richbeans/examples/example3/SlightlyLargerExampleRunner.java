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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An example of using the composite and bean together.
 * 
 * @author Matthew Gerring
 */
public class SlightlyLargerExampleRunner {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Change a value to see bean as JSON");

		// Composite
		final ExampleComposite ui = new ExampleComposite(shell, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ui.getItems().setListHeight(300);

		// Something to show value.
		final Text value = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL| SWT.WRAP);
		value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.pack();
		shell.setSize(420, 750);

		// Set some initial values
		final ExampleBean bean = createLargeBean();

		// Connect the UI and bean
		final BeanController controller = new BeanController(ui, bean);
		ExampleJSONWritingValueListener listener = new ExampleJSONWritingValueListener(controller, value);
		listener.setTextLimit(300);
		controller.addValueListener(listener);
		controller.beanToUI();
		controller.switchUIOn();

		SWTUtils.showCenteredShell(shell);
	}

	private static ExampleBean createLargeBean() {
		ExampleBean ret = new ExampleBean();
		
		for(int i=1;i<1000;i++) {
			ret.addItem(new ExampleItem(i, i+1));
			ret.addItem(new ExampleItem(2, 3, ItemChoice.POLAR));
		}
		return ret;
	}
}
