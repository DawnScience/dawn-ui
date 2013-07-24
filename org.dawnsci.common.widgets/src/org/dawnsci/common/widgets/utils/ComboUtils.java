package org.dawnsci.common.widgets.utils;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Utility class for SWT Combo buttons
 * @author wqk87977
 *
 */
public class ComboUtils {

	/**
	 * Create a set of action items in a Combo box given a list of actions
	 * @param parent
	 * @param actions
	 * @throws Exception
	 */
	public static void createComboControls(Composite parent, final List<Entry<String, Action>> actions) throws Exception{
		if(actions == null) return;
		final Combo comboButton = new Combo(parent, SWT.BORDER);
		for (final Entry<String, Action> action : actions) {
			comboButton.add(action.getKey());
		}
		
		comboButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				int index = ((Combo)e.getSource()).getSelectionIndex();
				actions.get(index).getValue().run();
			}
		});
		comboButton.select(0);
	}
}
