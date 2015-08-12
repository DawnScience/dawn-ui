package org.dawnsci.mapping.ui.wizards;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;


public class ImportDataCubeWizardPage extends WizardPage implements IDatasetWizard {
	
	protected MappedFileDescription description;
	protected DatasetAndAxesWidget widget;
	
	protected ImportDataCubeWizardPage(String name) {
		super(name);
		this.setTitle("Import Data Blocks");
		this.setDescription("Select all the full data blocks, their axes, and which dimensions correspond to the map X and Y directions");

	}

	@Override
	public void createControl(Composite parent) {
		
		widget = new DatasetAndAxesWidget();
		widget.createControl(parent);
		setControl(widget.getControl());
		
		final IWizardContainer container = this.getContainer();
		if (container instanceof IPageChangeProvider) {
			((IPageChangeProvider)container).addPageChangedListener(new IPageChangedListener() {
				
				@Override
				public void pageChanged(PageChangedEvent event) {
					updateOnPageChange();
					
				}
			});
		}
		
	}
	
	protected void updateOnPageChange() {
		Map<String, String[]> axesMaps = widget.getAxesMaps();
		for (Entry<String, String[]> entry : axesMaps.entrySet()) {
			description.addDataBlock(entry.getKey(), Arrays.asList(entry.getValue()));
		}
		String[] names = widget.getMapXAndYAxesNames();
		description.setxAxisName(names[0]);
		description.setyAxisName(names[1]);
	}
	
	@Override
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames) {
		widget.setDatasetMaps(datasetNames, nexusDatasetNames);
	}
	
	@Override
	public void setMappedDataDescription(MappedFileDescription description) {
		this.description = description;
	}
	
}
