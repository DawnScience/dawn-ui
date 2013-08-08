package org.dawnsci.spectrum.ui.preferences;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class SpectrumNameListEditor extends ListEditor {

	
	public SpectrumNameListEditor(String list, String name, Composite composite) {
		super(list,name,composite);
	}
	
	@Override
	protected String createList(String[] items) {
		StringBuilder sb = new StringBuilder();
		
		for (String item : items) {
			sb.append(item);
			sb.append(";");
		}
		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}

	@Override
	protected String getNewInputObject() {
		InputDialog entryDialog = new InputDialog(
		         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
		         "Edit entry", "Edit entry:", "dataset_name", null);
		     if (entryDialog.open() == InputDialog.OK) {
		         return entryDialog.getValue();
		     }
		     return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return stringList.split(";");
	}

}
