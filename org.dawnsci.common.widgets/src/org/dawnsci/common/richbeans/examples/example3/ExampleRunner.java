package org.dawnsci.common.richbeans.examples.example3;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.examples.example3.ExampleItem.ItemChoice;
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
		shell.setSize(420,500);
		
		// Wang some the values over
		final ExampleParameters bean = new ExampleParameters();
	    bean.addItem(new ExampleItem(1,2));
	    bean.addItem(new ExampleItem(2,3,ItemChoice.POLAR));
		
		BeanUI.beanToUI(bean, ui);
		BeanUI.switchState(ui, true);
		BeanUI.addValueListener(bean, ui, new ValueAdapter("Example listener") {			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				
				try {
					// Save the values
					BeanUI.uiToBean(ui, bean);
					
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
		SWTUtils.showCenteredShell(shell);

    }
}
