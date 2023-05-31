package org.dawnsci.datavis.view.parts;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyToClipboardAction extends Action {

	private String string;
	
	public CopyToClipboardAction(String s) {
		super("Copy path to clipboard");
		string = s;
	}
	
	@Override
	public void run() {
		Clipboard cb = new Clipboard(Display.getDefault());
		TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new Object[] { string },
				new Transfer[] { textTransfer });
	}
	
	
}
