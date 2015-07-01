package org.dawnsci.common.richbeans.examples.example7;

import java.lang.reflect.Method;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.examples.ExampleJSONWritingValueListener;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleBean;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleItem;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleItem.ItemChoice;
import org.dawnsci.common.richbeans.examples.example7.ui.ExampleComposite;
import org.dawnsci.common.richbeans.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An example of a larger bean of over 100 values.
 * 
 * @author Matthew Gerring
 */
public class ExampleRunner {

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Change a value to see bean as JSON");
		
		final Composite content = new Composite(shell, SWT.BORDER);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Composite
		final ExampleComposite ui = new ExampleComposite(content, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ui.getItems().setListHeight(180);

		// Something to show value.
		final Text value = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL| SWT.WRAP);
		value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.pack();
		shell.setSize(420, 750);

		// Set some initial values
		final ExampleBean bean = createSimpleBean();

		// Connect the UI and bean
		final BeanController controller = new BeanController(ui, bean);
		ExampleJSONWritingValueListener listener = new ExampleJSONWritingValueListener(controller, value);
		listener.setTextLimit(300);
		controller.addValueListener(listener);
		controller.beanToUI();
		controller.switchUIOn();

		SWTUtils.showCenteredShell(shell);
	}

	private static ExampleBean createSimpleBean() throws Exception {
		
		ExampleBean ret = new ExampleBean();
		
		for(int j = 1; j<=1000; ++j) {
			ExampleItem item1 = new ExampleItem(j, 2);
			ExampleItem item2 = new ExampleItem(Double.parseDouble(j+"."+j), 4, ItemChoice.POLAR);
			ret.addItem(item1);
			ret.addItem(item2);
	
			for(int i=1;i<100;i++) {
				Method m = ExampleItem.class.getMethod("setD"+i, double.class);
				m.invoke(item1, i);
				m.invoke(item2, Double.parseDouble(i+"."+i));
			}
		}
		return ret;
	}
}
