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
			outputString = outputString.substring(0, outputString.length() - 1); //Don't want the last \n
			
			in.close();

			return outputString;
		} catch (IOException e) {
			return "";
		}
		
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
			String thisAtt = thisConfigElement.getAttribute(att);
			if(thisAtt != null && thisAtt.equals(val)){
				result.add(thisConfigElement);
			}
		}
		return result.toArray(new IConfigurationElement[result.size()]);
	}

	private IConfigurationElement[] getOrderedParentItems(String parentId){
		IConfigurationElement[] parentItems	= getConfigsWithAttribute("org.dawnsci.webintro.item", "parent_id", parentId);

		Arrays.sort(parentItems, new ConfigElementComparator());
		return parentItems;
	}

	private IConfigurationElement[] getOrderedPages(){
		IConfigurationElement[] pages = getRegisteredConfigs("org.dawnsci.webintro.page");
		Arrays.sort(pages, new ConfigElementComparator());
		return pages;
	}

	private String getOptionalString(IConfigurationElement configElement, String attributeName){
		String val = configElement.getAttribute(attributeName);
		if(val == null){
			return "";
		}else{
			return val;
		}
	}

	private JsonObjectBuilder getJsonForItem(IConfigurationElement thisItem){
		String itemImageURL = getResourceURL(thisItem.getContributor(),thisItem.getAttribute("icon"));
		
		boolean isContent = thisItem.getName().equals("introContent");
		boolean isAction = thisItem.getName().equals("introAction");
		boolean isLink = thisItem.getName().equals("introLink");
		boolean isCategory = thisItem.getName().equals("introCategory");

		JsonObjectBuilder thisJSONItem = Json.createObjectBuilder()
				.add("id", thisItem.getAttribute("id"))
				.add("name", thisItem.getAttribute("name"))
				.add("image", itemImageURL)
				.add("description", getOptionalString(thisItem,"description"))
				.add("isContent", isContent)
				.add("isAction", isAction)
				.add("isLink", isLink)
				.add("isCategory", isCategory);

		if(isContent){
			String itemContentURL = getResourceURL(thisItem.getContributor(), thisItem.getAttribute("content_file"));
			thisJSONItem.add("content", getTextResource(itemContentURL));
		}else if(isLink){
			thisJSONItem.add("href", thisItem.getAttribute("href"));
		}

		return thisJSONItem;
	}

	public String getIntroJSON(){
		IConfigurationElement[] pages = getOrderedPages();

		// Setup a list with all of the items in it. We will remove them from the list when they're added to the JSON
		ArrayList<IConfigurationElement> orphanedItems = new ArrayList<IConfigurationElement>(Arrays.asList(getRegisteredConfigs("org.dawnsci.webintro.item")));

		JsonArrayBuilder pagesList = Json.createArrayBuilder();

		for (IConfigurationElement thisPage : pages){

			IConfigurationElement[] items = getOrderedParentItems(thisPage.getAttribute("page_id"));
			JsonArrayBuilder pageItems = getJsonForItems(items, orphanedItems, true);

			String pageContentURL = getResourceURL(thisPage.getContributor(), thisPage.getAttribute("content_file"));
			pagesList.add(Json.createObjectBuilder()
					.add("id", thisPage.getAttribute("id"))
					.add("page_id", thisPage.getAttribute("page_id"))
					.add("name", thisPage.getAttribute("name"))
					.add("content", getTextResource(pageContentURL) )
					.add("items", pageItems.build())
					.build());
		}
		
		if (orphanedItems.size()>0){ // If there are orphaned items, we should make an "other" page
			// separate the categories
			ArrayList<IConfigurationElement> orphanedCategories = new ArrayList<IConfigurationElement>();
			for (IConfigurationElement thisItem : orphanedItems){
				if(thisItem.getName().equals("introCategory")){
					orphanedCategories.add(thisItem);
				}
			}
			for (IConfigurationElement thisItem : orphanedCategories){
				orphanedItems.remove(thisItem);
			}
			
			JsonArrayBuilder pageItems = Json.createArrayBuilder();
			if(orphanedCategories.size()>0){
				getJsonForItems(orphanedCategories.toArray(new IConfigurationElement[0]), orphanedItems, true, pageItems);
			}
			if(orphanedItems.size()>0){
				getJsonForItems(orphanedItems.toArray(new IConfigurationElement[0]), orphanedItems, true, pageItems);
			}
			
			pagesList.add(Json.createObjectBuilder()
					.add("id", "org.dawnsci.webintro.content.other")
					.add("page_id", "org.dawnsci.webintro.content.other")
					.add("name", "Other")
					.add("content", "These items were not assigned to a page:")
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

	private JsonArrayBuilder getJsonForItems(IConfigurationElement[] items, ArrayList<IConfigurationElement> orphanedItems, boolean allowCategories) {
		return getJsonForItems(items, orphanedItems, allowCategories, Json.createArrayBuilder());
	}
	
	private JsonArrayBuilder getJsonForItems(IConfigurationElement[] items, ArrayList<IConfigurationElement> orphanedItems, boolean allowCategories, JsonArrayBuilder startItems) {
		JsonArrayBuilder allItems = startItems;
		for (IConfigurationElement thisItem : items){
			orphanedItems.remove(thisItem); // Remove this item from the main list
			JsonObjectBuilder thisItemJson = getJsonForItem(thisItem);

			if(thisItem.getName().equals("introCategory")){
				if(!allowCategories){
					logger.error("Tried to add a category to a category, ignoring contribution.");
				}else{
					IConfigurationElement[] catItems = getOrderedParentItems(thisItem.getAttribute("category_id"));
					
					JsonArrayBuilder categoryItemsJson = getJsonForItems(catItems, orphanedItems, false);
					
					thisItemJson.add("items", categoryItemsJson.build());
					allItems.add(thisItemJson.build());
				}
			}else{
				allItems.add(thisItemJson.build());
			}
			
		}
		return allItems;
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