package org.dawnsci.processing.python.ui;

import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.python.rpc.PythonRunSavuService;


public class SavuPluginFinder {
	AnalysisRpcPythonPyDevService s = null;
	PythonRunSavuService pythonRunSavuService;
	
	public SavuPluginFinder() throws Exception {
//		Does this on startup
	try {
		this.s = AnalysisRpcPythonPyDevService.create();
		pythonRunSavuService = new PythonRunSavuService(this.s);
	} catch (Exception e) {
		throw new Exception("Could not start the savu python interpreter");
	}
	}
	
	public void stopPythonService() {
		this.s.stop();
	}
	
	public Map<String, Object> getSavuPluginInfo() throws Exception {

		try {
			Map<String, Object> out = pythonRunSavuService.get_plugin_info();
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not get savu plugins");
		}
	}
	
	public void populateSavuPlugins() throws Exception {

		try {
			pythonRunSavuService.populate_plugins();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not populate savu plugins");
		}
	}

	public Map<String, Object> getPluginParameters(String pluginName) throws Exception {

		try {
			Map<String, Object> out = pythonRunSavuService.get_plugin_params(pluginName);
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not get plugin parameters");
		}
	}
	
}
