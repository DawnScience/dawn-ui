package org.dawnsci.spectrum.ui.wizard;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.spectrum.ui.file.IContain1DData;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class SaveFileWizardPage extends ResourceChoosePage {
	
	List<IContain1DData> dataList;
	
	public SaveFileWizardPage(List<IContain1DData> dataList) {
		super("Save file wizard", "Save processed data to file", null);
		this.dataList = dataList;
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
	}
	
	public void finish() {
		
		File                file=null;
		IPersistentFile     pf=null;
		
		try {
    		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
    		file = new File(getAbsoluteFilePath());
    		pf = service.createPersistentFile(file.getAbsolutePath());
		    
    		for (IDataset ds : dataList.get(0).getyDatasets()) {
		    	pf.setData(ds);
		    }
    		
		    pf.setAxes(Arrays.asList(new IDataset[] {dataList.get(0).getxDataset(), null}));
		        		    
		} catch (Throwable ne) {
			ne.printStackTrace();
		} finally {
			if (pf!=null) pf.close();
		}
	}

}
