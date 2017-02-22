package org.dawnsci.processing.ui.model;

import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationSetupWizardPage extends WizardPage implements IOperationSetupWizardPage {

	protected OperationData od = null;
	protected OperationData id = null;
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractOperationSetupWizardPage.class);
	
	protected AbstractOperationSetupWizardPage(String pageName) {
		super(pageName);
	}

	protected AbstractOperationSetupWizardPage(String name, String description, ImageDescriptor image) {
		super(name, description, image);
	}

	@Override
	public OperationData getOutputData() {
		//ensure that the returned data has SliceFromSeriesMetadata
		SliceFromSeriesMetadata meta;
		try {
			meta = od.getData().getFirstMetadata(SliceFromSeriesMetadata.class);
			if (meta == null) {
				// get metadata from id
				meta = id.getData().getFirstMetadata(SliceFromSeriesMetadata.class);
				od.getData().setMetadata(meta);
			}
		} catch (Exception e) {
			logger.warn("Could not set SliceFromSeriesMetadata", e);
		}
		return od;
	}

	@Override
	public void setInputData(OperationData id) {
		this.id = id;
		update();
	}

	protected abstract void update();
	
	@Override
	public void finishPage() {
		// default is to do nothing
	}
}
