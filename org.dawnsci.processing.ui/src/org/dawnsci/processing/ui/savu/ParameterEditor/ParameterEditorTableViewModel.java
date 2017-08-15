package org.dawnsci.processing.ui.savu.ParameterEditor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterEditorTableViewModel {
	private List<ParameterEditorRowDataModel> rows = new ArrayList<ParameterEditorRowDataModel>();
	private final static Logger logger = LoggerFactory.getLogger(ParameterEditorTableViewModel.class);

	private Map<String, Object> pluginDict;

	private static String pluginName=null;

	public static String getPluginName() {
		return pluginName;
	}

	public void updateModel(String pluginName, Map<String, Object> pluginDict) {
		logger.debug(pluginName + " " + this.pluginName);
		try {
			if (pluginDict == null || this.pluginName !=pluginName){
				if (this.pluginDict != null){
					this.pluginDict.clear();// this is the case at the start of the model build
				}
				
				this.pluginName = pluginName;
				this.pluginDict = getMapFromFile();
			}
			
			else if (this.pluginName == pluginName) {
			}
			else {
				this.pluginDict.clear();
				this.pluginDict = pluginDict;
			}
		} catch (IOException e) {
			logger.error("Couldn't select a plugin",e);
		}
		rows.clear();
		rebuildTable(this.pluginDict);
		
	}

	private static String wspacePath;

	public ParameterEditorTableViewModel() {
		wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		this.pluginName = null;
		try {
			if (pluginDict == null) {
				
				try {
					pluginDict = getMapFromFile();
					this.pluginDict = pluginDict;
				} catch (FileNotFoundException e) {
					logger.warn("Haven't found a file associated with plugin name " + this.pluginName.toString() + ". This should only happen on startup.");
					this.pluginDict = null;
				}}

		} catch (IOException e) {
			logger.error("Could not find a map file!",e);
		}
		rebuildTable(pluginDict);

	}

	public void rebuildTable(Map<String, Object> pluginDict) {
		if (pluginDict == null) {
			rows.add(new ParameterEditorRowDataModel("", "", ""));
		} else {
			for (Map.Entry<String, Object> entry : pluginDict.entrySet()) {
				Map<String, Object> info = (Map<String, Object>) entry.getValue();
				rows.add(new ParameterEditorRowDataModel(entry.getKey(), info.get("value"), (String) info.get("hint")));
				
		}	
		}

	}

	public void addEntry(ParameterEditorRowDataModel model) {
		rows.add(model);
	}
	public void clearEntries() {
		rows.clear();
	}
	public Map<String, Object> getPluginDict() {
		return pluginDict;
	}

	public void setPluginDict(Map<String, Object> pluginDict) {
		this.pluginDict = pluginDict;
	}

	public static String getWspacePath() {
		return wspacePath;
	}

	public static void setWspacePath(String wspacePath) {
		ParameterEditorTableViewModel.wspacePath = wspacePath;
	}

	public List<ParameterEditorRowDataModel> getValues() {
		return rows;
	}

	public Map<String, Object> getMapFromFile() throws IOException {
		Map<String, Object> pluginDict = null;
		ObjectInputStream in;
		FileInputStream fileIn;

		try {
			fileIn = new FileInputStream(wspacePath + pluginName+".ser");// just																								// testing
			in = new ObjectInputStream(fileIn);
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e) {
			logger.warn("Error finding plugin info",e);
		}
		return pluginDict;
	}
}