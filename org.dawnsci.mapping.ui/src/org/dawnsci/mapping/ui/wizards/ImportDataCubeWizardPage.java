package org.dawnsci.mapping.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class ImportDataCubeWizardPage extends WizardPage {

	private String filePath;
	private Map<String,int[]> datasetNames;
	
	protected ImportDataCubeWizardPage(String filePath) {
		super("Import Data Cube");
		this.filePath = filePath;
	}

	@Override
	public void createControl(Composite parent) {

		setControl(parent);
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					try {
						datasetNames = MappingUtils.getDatasetInfo(filePath, null);
						datasetNames.toString();
					} catch (Exception e) {
						//TODO
					}
				}
			});

		} catch (Exception e) {
			//TODO
		}

	}
	
}
