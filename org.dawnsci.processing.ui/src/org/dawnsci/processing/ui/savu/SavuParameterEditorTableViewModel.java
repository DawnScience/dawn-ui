package org.dawnsci.processing.ui.savu;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.ResourcesPlugin;

public class SavuParameterEditorTableViewModel {
	private List<SavuParameterEditorRowDataModel> rows = new ArrayList<>();
	private static final String wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();


	private final Map<String, Map<String, Map<String, Object>>> pluginParameterDict;

	private String pluginName = null;

	public String getPluginName() {
		return pluginName;
	}

	public void updateModel(String pluginName) {
		this.pluginName = pluginName;
		if (pluginName != null && !pluginParameterDict.containsKey(pluginName)) {
			Map<String, Map<String, Object>> newPluginParameterDict;
			try {
				newPluginParameterDict = getMapFromFile(pluginName);
				pluginParameterDict.put(pluginName, newPluginParameterDict);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		rows.clear();
		rebuildTable();
		
	}

	public SavuParameterEditorTableViewModel(Map<String, Map<String, Map<String, Object>>> pluginParameterDict) {
		this.pluginParameterDict = pluginParameterDict;
		updateModel(null);
	}

	public void rebuildTable() {
		if (pluginParameterDict != null && pluginName != null) {
		//	rows.add(new SavuParameterEditorRowDataModel("", "", ""));
		//} else {
			for (Entry<String, Map<String, Object>> entry : pluginParameterDict.get(pluginName).entrySet()) {
				Map<String, Object> info = entry.getValue();
				rows.add(new SavuParameterEditorRowDataModel(entry.getKey(), info.get("value"), (String) info.get("hint")));
			}	
		}

	}

	public void addEntry(SavuParameterEditorRowDataModel model) {
		rows.add(model);
	}
	public void clearEntries() {
		rows.clear();
	}
	public Map<String, Map<String, Map<String,Object>>> getPluginParameterDict() {
		return pluginParameterDict;
	}

	public List<SavuParameterEditorRowDataModel> getValues() {
		return rows;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Map<String, Object>> getMapFromFile(String pluginName) throws Exception {
		try (
			FileInputStream fileIn = new FileInputStream(wspacePath + File.separator + pluginName+".ser");// just																								// testing
			ObjectInputStream in = new ObjectInputStream(fileIn);
			) {
			return (Map<String, Map<String, Object>>) in.readObject();
		} 
	}
}