package org.dawnsci.datavis.manipulation.aggregate;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * List dialog that allows resetting
 */
public class ResettableListDialog extends ElementListSelectionDialog {
	public static final int RESET = 2;
	private boolean canReset = true;

	public ResettableListDialog(Shell shell) {
		super(shell, new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LabelOption) element).getLabel();
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});
	}

	@Override
	protected Text createFilterText(Composite parent) {
		Text filterText = super.createFilterText(parent);
		filterText.setToolTipText("Show only datasets whose name contains the entered text");
		filterText.setMessage("Filter label datasets");
		return filterText;
	}

	void setResettable(boolean canReset) {
		this.canReset = canReset;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button b = createButton(parent, IDialogConstants.ABORT_ID, "Reset", false);
		b.setEnabled(canReset);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.ABORT_ID) {
			setReturnCode(RESET);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
	}
}
