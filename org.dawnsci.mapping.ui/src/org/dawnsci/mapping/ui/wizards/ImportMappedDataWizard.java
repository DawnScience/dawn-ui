package org.dawnsci.mapping.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.MappingUtils;
import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ImportMappedDataWizard extends Wizard {

	private String filePath;
	private Map<String,int[]> datasetNames;
	private Map<String,int[]> nexusDatasetNames = new LinkedHashMap<String, int[]>();
	private MappedFileDescription description = new MappedFileDescription();
	
	public ImportMappedDataWizard(String filePath) {
		this.filePath = filePath;
		addPage(new ImportDataCubeWizardPage("Import Full Data Blocks"));
		addPage(new ImportMapWizardPage("Import Maps"));
	}
	
	public MappedFileDescription getMappedFileDescription() {
		return description;
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					try {
						datasetNames = MappingUtils.getDatasetInfo(filePath, null);
						
						try {
							IMetadata meta = LocalServiceManager.getLoaderService().getMetadata(filePath, null);
							populateNexusMaps(meta);
							
						} catch (Exception e) {
							
						}
						
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								IWizardPage[] pa = getPages();
								
								for (IWizardPage p : pa) {
									if (p instanceof IDatasetWizard) {
										IDatasetWizard pd = (IDatasetWizard) p;
										pd.setDatasetMaps(datasetNames, nexusDatasetNames);
										pd.setMappedDataDescription(description);
										
									}
								}
								
							}
						});
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void populateNexusMaps(IMetadata meta) throws Exception {
		Collection<String> mnames = meta.getMetaNames();
		
		for (String key : datasetNames.keySet()) {
			
			String k = key.substring(0, key.lastIndexOf("/"));
			String kclass = k + "@NX_class";
			String ksignal = k+"@signal";
			String keysignal = key+"@signal";
			if (mnames.contains(kclass) && meta.getMetaValue(kclass).toString().equals("NXdata")) {
				
				if (mnames.contains(ksignal) && key.equals(k+"/"+meta.getMetaValue(ksignal).toString())) {
					nexusDatasetNames.put(key, datasetNames.get(key));
				} else if (mnames.contains(keysignal)) {
					nexusDatasetNames.put(key, datasetNames.get(key));
				}
			}
		}
	}
	
	
	@Override
	public boolean performFinish() {
		IWizardPage page = getPage("Import Maps");
		if (page instanceof ImportMapWizardPage) {
			((ImportMapWizardPage)page).pushChanges();
		}
		return true;
	}

}
