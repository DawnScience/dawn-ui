package org.dawnsci.common.widgets.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
	 * Create a set of Radio buttons given a list of Actions
	 * @param parent
	 * @param actions
	 * @return radioButtonsList
	 * @throws Exception
	 */
	public static List<Button> createRadioControls(Composite parent, List<Entry<String, Action>> actions) throws Exception{
		List<Button> radioButtonsList = new ArrayList<Button>();
		if(actions == null) return null;
		int i = 0;
		for (final Entry<String, Action> action : actions) {
			final Button radioButton = new Button(parent, SWT.RADIO);
			radioButton.setText(action.getKey());
			if (action.getValue().getToolTipText() != null)
				radioButton.setToolTipText(action.getValue().getToolTipText());
			radioButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if(((Button)e.getSource()).getSelection())
						action.getValue().run();
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
