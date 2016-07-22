package org.dawnsci.webintro;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.swt.program.Program;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class JSBridge {
	private final static Logger logger = LoggerFactory.getLogger(JSBridge.class);

	protected static String getResourceURL(IContributor contributer, String resourceLocation){
		if(resourceLocation == null || resourceLocation.isEmpty()){
			return "";
		}
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
	
	private IConfigurationElement[] getOrderedPageItems(String pageId){
		IConfigurationElement[] pageItems	= getConfigsWithAttribute("org.dawnsci.webintro.item", "page_id", pageId);

		Arrays.sort(pageItems, new ConfigElementComparator());
		return pageItems;
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
    		
    		IConfigurationElement[] items = getOrderedPageItems(thisPage.getAttribute("page_id"));
    		JsonArrayBuilder pageItems = Json.createArrayBuilder();
    		
        	for (IConfigurationElement thisItem : items){
        		String itemImageURL = getResourceURL(thisItem.getContributor(),thisItem.getAttribute("icon"));
        		
        		boolean isContent = thisItem.getName().equals("introContent");
        		boolean isAction = thisItem.getName().equals("introAction");
        		boolean isLink = thisItem.getName().equals("introLink");
        		
        		JsonObjectBuilder thisJSONItem = Json.createObjectBuilder()
        				.add("id", thisItem.getAttribute("id"))
        				.add("name", thisItem.getAttribute("name"))
        				.add("image", itemImageURL)
        				.add("description", thisItem.getAttribute("description"))
        				.add("isContent", isContent)
        				.add("isAction", isAction)
        				.add("isLink", isLink);
        		
        		if(isContent){
            		String itemContentURL = getResourceURL(thisPage.getContributor(), thisItem.getAttribute("content_file"));
        			thisJSONItem.add("content", getTextResource(itemContentURL));
        		}else if(isLink){
        			thisJSONItem.add("href", thisItem.getAttribute("href"));
        		}
        				
        		pageItems.add(thisJSONItem.build());
        	}

    		String pageImageURL = getResourceURL(thisPage.getContributor(),thisPage.getAttribute("icon"));
    		String pageContentURL = getResourceURL(thisPage.getContributor(), thisPage.getAttribute("content_file"));
    		pagesList.add(Json.createObjectBuilder()
    				.add("id", thisPage.getAttribute("id"))
    				.add("page_id", thisPage.getAttribute("page_id"))
    				.add("name", thisPage.getAttribute("name"))
    				.add("content", getTextResource(pageContentURL) )
    				.add("image", pageImageURL)
    				.add("items", pageItems.build())
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
    	logger.debug("JSBridge runAction Called for id "+configId);
    	IConfigurationElement config = getConfigsWithAttribute("org.dawnsci.webintro.item","id", configId)[0];
    	
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
    
    public void openLink(String href) throws MalformedURLException{
    	logger.debug("JSBridge openLink Called for url "+href);

    	// Use URL class to validate the string (otherwise a website could open an executable on the local filesystem)
    	URL urlObject = new URL(href); 
    	
    	Program.launch(urlObject.toString());
    }
    
    public void log(String text)
    {
        System.out.println(text);
    }
}