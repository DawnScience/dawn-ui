package org.dawnsci.mapping.ui.wizards;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportMappedDataWizard extends Wizard {

	private Map<String,int[]> datasetNames;
	private Map<String,int[]> nexusDatasetNames = new LinkedHashMap<String, int[]>();
	private MappedDataFileBean mdfbean = new MappedDataFileBean();
	private MappedDataFileBean[] persistedList;
	
	private final static Logger logger = LoggerFactory.getLogger(ImportMappedDataWizard.class);
	
	public ImportMappedDataWizard(String filePath, Map<String,int[]> datasetNames, IMetadata meta) {
		this.datasetNames = datasetNames;
		mdfbean.setScanRank(2);
		try {
			populateNexusMaps(meta);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addPage(new ImportDataCubeWizardPage("Import Full Data Blocks"));
		addPage(new ImportMapWizardPage("Import Maps"));
	}
	
	public MappedDataFileBean getMappedDataFileBean() {
		return mdfbean;
	}
	
	public void createPageControls(Composite pageContainer) {

		IWizardPage[] pa = getPages();

		super.createPageControls(pageContainer);

		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		String jsonArray = ps.getString("TestDescriptionList");
		if (jsonArray != null) {
			BundleContext bundleContext =
	                FrameworkUtil.
	                getBundle(this.getClass()).
	                getBundleContext();
			
			IPersistenceService p = bundleContext.getService(bundleContext.getServiceReference(IPersistenceService.class));
			
			try {
				persistedList = p.unmarshal(jsonArray,MappedDataFileBean[].class);
				for (MappedDataFileBean d : persistedList) {
					if (d != null && datasetNames.containsKey(d.getBlocks().get(0).getName())){
						int scanRank = d.getBlocks().get(0).getxAxisForRemapping() == null ? 2 :1;
						d.setScanRank(scanRank);
						mdfbean = d;
						break;
					}
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (IWizardPage p : pa) {
			if (p instanceof IDatasetWizard) {
				IDatasetWizard pd = (IDatasetWizard) p;
				pd.setDatasetMaps(datasetNames, nexusDatasetNames);
				pd.setMapBean(mdfbean);
			}
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
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IPersistenceService ps = bundleContext.getService(bundleContext.getServiceReference(IPersistenceService.class));
		try {
			IPreferenceStore p = Activator.getDefault().getPreferenceStore();
			updatePersistanceList();
			String json = ps.marshal(persistedList);
			p.setValue("TestDescriptionList", json);
			
		} catch (Exception e) {
			logger.error("Could not set persisted file description list", e);
		}
		
		return true;
	}

	private void updatePersistanceList() {
		
		if (persistedList == null || persistedList.length == 0) {
			persistedList = new MappedDataFileBean[]{mdfbean};
			return;
		}
		
		LinkedList<MappedDataFileBean> ll = new LinkedList<MappedDataFileBean>();
		for (MappedDataFileBean d : persistedList) {
			ll.add(d);
		}
		
		if (ll.contains(mdfbean)) ll.remove(mdfbean);
		
		ll.push(mdfbean);

		if (ll.size() > 10) ll.removeLast();
		
		persistedList = new MappedDataFileBean[ll.size()];
		
		for (int i = 0; i < ll.size(); i++) {
			persistedList[i] = ll.removeFirst();
		}
		
	}
}
