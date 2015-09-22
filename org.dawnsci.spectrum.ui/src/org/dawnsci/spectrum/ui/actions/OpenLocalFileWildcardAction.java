package org.dawnsci.spectrum.ui.actions;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class OpenLocalFileWildcardAction extends OpenLocalFileAction {

	public OpenLocalFileWildcardAction() {
		wildcard = true;
	}
	
	@Override
	protected String[] matchNames(String[] names, String filterPath) {
		String match = names[0];
		File folder = new File(filterPath);
		File[] files = folder.listFiles((FilenameFilter)new WildcardFileFilter(match));
		String[] out = new String[files.length];
		for (int i = 0; i <files.length ; i++) out[i] = files[i].getName();
		return out;
	}
	
}
