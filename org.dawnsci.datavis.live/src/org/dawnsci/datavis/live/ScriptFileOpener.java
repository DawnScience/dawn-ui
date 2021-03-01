package org.dawnsci.datavis.live;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.datavis.api.IScriptFileOpener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptFileOpener implements IScriptFileOpener {

	private static final Logger logger = LoggerFactory.getLogger(ScriptFileOpener.class);
	
	@Override
	public boolean canOpen(String path) {
		
		if (!Boolean.parseBoolean(System.getProperty("datavis.open.script"))) {
			return false;
		}
		
		return (path.endsWith(".py") || path.endsWith(".jy"));

	}

	@Override
	public void open(String path) {
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(() -> open(path));
			return;
		}
		try {
			EclipseUtils.openExternalEditor(path);
		} catch (PartInitException e) {
			logger.error("Could not open editor",e);
		}

	}

}
