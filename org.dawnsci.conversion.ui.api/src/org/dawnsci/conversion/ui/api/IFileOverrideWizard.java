package org.dawnsci.conversion.ui.api;

import java.io.File;
import java.util.List;

/**
 * Wizard in which the file selection from the workbench can be overridden
 *
 */
public interface IFileOverrideWizard {
	
	void setFileSelectionOverride(List<File> files);

}
