package org.dawnsci.processing.ui.savu.ParameterEditor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;

public class ParameterEditorTableViewModel {
	private List<ParameterEditorRowDataModel> rows = new ArrayList<ParameterEditorRowDataModel>();

	private Map<String, Object> pluginDict;

	private static String pluginName="PaganinFilter";

	public static String getPluginName() {
		return pluginName;
	}

	public void updateModel(String pluginName, Map<String, Object> pluginDict) {
//		this.pluginName = pluginName;
		System.out.println(pluginName + " " + this.pluginName);

		try {
			if (pluginDict == null || this.pluginName !=pluginName){
				this.pluginDict.clear();
				System.out.println("Here");
				this.pluginName = pluginName;
				this.pluginDict = getMapFromFile();
				System.out.println(this.pluginName + this.pluginDict);
			}
			
			else if (this.pluginName == pluginName) {
				System.out.println("things");
			}
			else {
				this.pluginDict.clear();
//				System.out.println("I'm in the else"+pluginDict.toString());
				System.out.println("other things");
				this.pluginDict = pluginDict;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rows.clear();
//		System.out.println("plugindict is "+pluginDict.toString());
		rebuildTable(this.pluginDict);
		
	}

	private static String wspacePath;

	public ParameterEditorTableViewModel(String pluginName) {
		this(pluginName, null);
	}

	public ParameterEditorTableViewModel(String pluginName, Map<String, Object> pluginDict) {
		wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		this.pluginName = pluginName;
		try {
			if (pluginDict == null) {
				pluginDict = getMapFromFile();
			this.pluginDict = pluginDict;}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rebuildTable(pluginDict);

	}

	public void rebuildTable(Map<String, Object> pluginDict) {
		for (Map.Entry<String, Object> entry : pluginDict.entrySet()) {
			Map<String, Object> info = (Map<String, Object>) entry.getValue();
			rows.add(new ParameterEditorRowDataModel(entry.getKey(), info.get("value"), (String) info.get("hint")));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pluginDict;
	}
}