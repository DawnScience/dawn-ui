package org.dawnsci.plotting.tools.diffraction;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class ConfigurableMessageDialog extends MessageDialog {

	public ConfigurableMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}

	public void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle);
	}
}
