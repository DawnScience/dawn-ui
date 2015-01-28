package org.dawnsci.common.richbeans.examples.example5;

import java.util.List;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.examples.example5.data.SimpleBean;
import org.dawnsci.common.richbeans.examples.example5.ui.SimpleComposite;
import org.dawnsci.common.richbeans.util.SWTUtils;
import org.eclipse.dawnsci.doe.DOEUtils;
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
		final SimpleComposite ui = new SimpleComposite(shell, SWT.NONE);
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Something to show value.
		final Label value= new Label(shell, SWT.WRAP);
		
		shell.pack();
		shell.setSize(420,400);
		
		// Wang some the values over
		final SimpleBean bean = new SimpleBean();
		bean.setX("10.0, 50, 1");
		bean.setY("5");
		
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
					List<?>       beans = DOEUtils.expand(bean); // expand the beans
					ObjectMapper mapper = new ObjectMapper();
					StringBuilder   buf = new StringBuilder();
					for (Object b : beans) {
						String json = mapper.writeValueAsString(b);
						buf.append(json);
					}
					value.setText(buf.toString());
					
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
