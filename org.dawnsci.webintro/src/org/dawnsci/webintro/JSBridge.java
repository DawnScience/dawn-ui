package org.dawnsci.webintro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;

public class JSBridge {
	private final static Logger logger = LoggerFactory.getLogger(JSBridge.class);

	private String getResourceURL(IContributor contributer, String resourceLocation){
    	String url = "platform:/plugin/";
    	url += contributer.getName();
    	url += "/";
    	url += resourceLocation;
    	return url;
    }
    
	private IExtension[] getRegisteredExtensions(){
    	IExtension[] extensions = org.eclipse.core.runtime.Platform
				.getExtensionRegistry()
				.getExtensionPoint("org.dawnsci.introRegister").getExtensions(); //$NON-NLS-1$
    	return extensions;
	}
	
	private IConfigurationElement[] getRegisteredConfigs(){
    	IConfigurationElement[] configs = org.eclipse.core.runtime.Platform
				.getExtensionRegistry()
				.getExtensionPoint("org.dawnsci.introRegister").getConfigurationElements(); //$NON-NLS-1$
    	return configs;
	}
	
	private IConfigurationElement getConfigWithAttribute(String att, String val){
		IConfigurationElement[] configs = getRegisteredConfigs();
		for (IConfigurationElement thisConfigElement : configs){
    		if(thisConfigElement.getAttribute(att).equals(val)){
    			return thisConfigElement;
    		}
		}
		logger.error("Unable to find a config element with "+att+" = "+val);
		return null;
	}
	
	public String getActionsJSON(){
    	IConfigurationElement[] configs = getRegisteredConfigs();
    
    	JsonArrayBuilder configList = Json.createArrayBuilder();

    	for (IConfigurationElement thisConfigElement : configs){
    		String imageURL = getResourceURL(thisConfigElement.getContributor(),thisConfigElement.getAttribute("icon"));
    		configList.add(Json.createObjectBuilder()
    				.add("id", thisConfigElement.getAttribute("id"))
    				.add("name", thisConfigElement.getAttribute("name"))
    				.add("description", thisConfigElement.getAttribute("description"))
    				.add("image", imageURL)
    				.build());
    	}
    	
    	JsonObject rootObject = Json.createObjectBuilder()
    			.add("actions", configList.build())
    			.build();
    	
    	StringWriter stWriter = new StringWriter();
    	try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
    	   jsonWriter.writeObject(rootObject);
    	}
    	return stWriter.toString();
	}
	  
    public boolean runAction(String configId){
    	logger.debug("JSBridge runAction Called");
    	IConfigurationElement config = getConfigWithAttribute("id", configId);
    	
    	try {
    		IActionDelegate delegate = null;
    		delegate = (IActionDelegate) config.createExecutableExtension("class");
    		delegate.run(null);
    		IIntroPart part = PlatformUI.getWorkbench().getIntroManager().getIntro();
    		PlatformUI.getWorkbench().getIntroManager().closeIntro(part);
    		return true;
    	} catch (CoreException e) {
    		logger.error("Error launching action from ID = "+configId);
    		e.printStackTrace();
    		return false;
    	}
    }
}