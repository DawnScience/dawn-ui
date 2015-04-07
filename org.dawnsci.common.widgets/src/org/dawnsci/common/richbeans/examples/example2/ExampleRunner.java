package org.dawnsci.common.richbeans.examples.example2;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.examples.example2.data.ExampleItem;
import org.dawnsci.common.richbeans.examples.example2.data.ExampleParameters;
import org.dawnsci.common.richbeans.examples.example2.ui.ExampleComposite;
import org.dawnsci.common.richbeans.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An example of using the composite and bean together.
 * 
 * @author Matthew Gerring
 *
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
		final Label value= new Label(shell, SWT.WRAP);
		value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		shell.pack();
		shell.setSize(420,600);
		
		// Wang some the values over
		final ExampleParameters bean = new ExampleParameters();
	    bean.setElement("Fe");
	    bean.setEdge("K");
	    bean.setStart(100d);
	    bean.setStop(200d);
	    bean.addItem(new ExampleItem(1,2));
		
		final BeanController controller = new BeanController(ui, bean);
		controller.setValueListener(new ValueAdapter("Example listener") {			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				
				try {
					// Save the values
					controller.uiToBean();
					
					// We spit out the bean in JSON since
					// rich bean does not care if bean in XML or
					// whatever at this stage.
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(bean);
					value.setText(json);
					value.getParent().layout();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		controller.start();
		
		SWTUtils.showCenteredShell(shell);

    }
}
