package org.dawnsci.plotting.pde.templates;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.ui.templates.ITemplateSection;

public class NewFittingFunctionTemplate implements ITemplateSection {

	@Override
	public URL getTemplateLocation() {
		return null;
	}

	@Override
	public String getLabel() {
		return "New Fitting Function Label";
	}

	@Override
	public String getDescription() {
		return "Creates a new function fitting example";
	}

	@Override
	public String getReplacementString(String fileName, String key) {
		return null;
	}

	@Override
	public void addPages(Wizard wizard) {

	}

	@Override
	public WizardPage getPage(int pageIndex) {
		return null;
	}

	@Override
	public int getPageCount() {
		return 0;
	}

	@Override
	public boolean getPagesAdded() {
		return false;
	}

	@Override
	public int getNumberOfWorkUnits() {
		return 0;
	}

	@Override
	public IPluginReference[] getDependencies(String schemaVersion) {
		return null;
	}

	@Override
	public String getUsedExtensionPoint() {
		return "org.dawnsci.common.functions";
	}

	@Override
	public String[] getNewFiles() {
		return null;
	}

	@Override
	public void execute(IProject project, IPluginModelBase model,
			IProgressMonitor monitor) throws CoreException {
		
	}

}
