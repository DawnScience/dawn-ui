package org.dawnsci.common.richbeans.examples.example6;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
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
		final DecoratorComposite ui = new DecoratorComposite(shell, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final Button enable = new Button(shell, SWT.TOGGLE);
		enable.setSelection(true);
		enable.setText("Enable");
		
		// Something to show value.
		final Label value= new Label(shell, SWT.WRAP);
		
		shell.pack();
		shell.setSize(420,400);
		
		// Create values.
		final DecoratorBean bean = new DecoratorBean();
		bean.setX(10.0);
		bean.setY(5);
		
		// Enable/disable by reflection
		enable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					BeanUI.setEnabled(bean, ui, enable.getSelection());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		// Wang some the values over
		BeanUI.beanToUI(bean, ui);
		BeanUI.switchState(ui, true);
		BeanUI.addValueListener(bean, ui, new ValueAdapter("Exmaple listener") {			
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
