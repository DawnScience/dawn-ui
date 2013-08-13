package org.dawnsci.common.widgets.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Utility class for SWT Radio buttons
 * @author wqk87977
 *
 */
public class RadioUtils {
	/**
	 * Create a set of Radio buttons given a list of Actions<br>
	 * The radio button text is coming from the test defined for each action, so is the ToolTipText.
	 * @param parent
	 * @param actions
	 * @return radioButtonsList
	 * @throws Exception
	 */
	public static List<Button> createRadioControls(Composite parent, List<Action> actions) throws Exception{
		List<Button> radioButtonsList = new ArrayList<Button>();
		if(actions == null) return null;
		int i = 0;
		for (final Action action : actions) {
			final Button radioButton = new Button(parent, SWT.RADIO);
			radioButton.setText(action.getText());
			if (action.getToolTipText() != null)
				radioButton.setToolTipText(action.getToolTipText());
			radioButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if(((Button)e.getSource()).getSelection())
						action.run();
				}
			});
			if(i == 0)
				radioButton.setSelection(true);
			radioButtonsList.add(radioButton);
			i++;
		}
		return radioButtonsList;
	}
}
