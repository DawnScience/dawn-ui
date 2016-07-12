package org.dawnsci.webintro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;

public class JSBridge {
	private final static Logger logger = LoggerFactory.getLogger(JSBridge.class);

	protected static String getResourceURL(IContributor contributer, String resourceLocation){
    	String url = "platform:/plugin/";
    	url += contributer.getName();
    	url += "/";
    	url += resourceLocation;
    	return url;
    }
	
	private String getTextResource(String resourceUrl){
		URL url;
		try {
			url = new URL(resourceUrl);
			InputStream inputStream = url.openConnection().getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String outputString = "";
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				outputString += inputLine + "\n";
			}

			in.close();
			
			return outputString;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private IConfigurationElement[] getRegisteredConfigs(String point){
    	IConfigurationElement[] configs = org.eclipse.core.runtime.Platform
				.getExtensionRegistry()
				.getExtensionPoint(point).getConfigurationElements(); //$NON-NLS-1$
    	return configs;
	}
	
	private IConfigurationElement[] getConfigsWithAttribute(String point, String att, String val){
		IConfigurationElement[] configs = getRegisteredConfigs(point);
		
		List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		
		for (IConfigurationElement thisConfigElement : configs){
    		if(thisConfigElement.getAttribute(att).equals(val)){
    			result.add(thisConfigElement);
    		}
		}
		return result.toArray(new IConfigurationElement[result.size()]);
	}
	
	private IConfigurationElement[] getOrderedPageActions(String pageId){
		IConfigurationElement[] pageActions	= getConfigsWithAttribute("org.dawnsci.webintro.action", "page_id", pageId);
		Arrays.sort(pageActions, new ConfigElementComparator());
		return pageActions;
	}
	
	private IConfigurationElement[] getOrderedPages(){
		IConfigurationElement[] pages = getRegisteredConfigs("org.dawnsci.webintro.page");
		Arrays.sort(pages, new ConfigElementComparator());
		return pages;
	}
	
	public String getIntroJSON(){
		IConfigurationElement[] pages = getOrderedPages();
	    
    	JsonArrayBuilder pagesList = Json.createArrayBuilder();

    	for (IConfigurationElement thisPage : pages){
    		
    		IConfigurationElement[] actions = getOrderedPageActions(thisPage.getAttribute("page_id"));
    		JsonArrayBuilder pageActions = Json.createArrayBuilder();
    		
        	for (IConfigurationElement thisAction : actions){
        		String actionImageURL = getResourceURL(thisAction.getContributor(),thisAction.getAttribute("icon"));
        		pageActions.add(Json.createObjectBuilder()
        				.add("id", thisAction.getAttribute("id"))
        				.add("name", thisAction.getAttribute("name"))
        				.add("description", thisAction.getAttribute("description"))
        				.add("image", actionImageURL)
        				.build());
        	}

    		String pageImageURL = getResourceURL(thisPage.getContributor(),thisPage.getAttribute("icon"));
    		String pageContentURL = getResourceURL(thisPage.getContributor(), thisPage.getAttribute("content_file"));
    		pagesList.add(Json.createObjectBuilder()
    				.add("id", thisPage.getAttribute("id"))
    				.add("page_id", thisPage.getAttribute("page_id"))
    				.add("name", thisPage.getAttribute("name"))
    				.add("content", getTextResource(pageContentURL) )
    				.add("image", pageImageURL)
    				.add("actions", pageActions.build())
    				.build());
    	}
    	
    	JsonObject rootObject = Json.createObjectBuilder()
    			.add("pages", pagesList.build())
    			.build();
    	
    	StringWriter stWriter = new StringWriter();
    	try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
    	   jsonWriter.writeObject(rootObject);
    	}
    	return stWriter.toString();
	}
	  
    public boolean runAction(String configId){
    	logger.debug("JSBridge runAction Called");
    	IConfigurationElement config = getConfigsWithAttribute("org.dawnsci.webintro.action","id", configId)[0];
    	
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
    
    public void log(String text)
    {
        System.out.println(text);
    }
}