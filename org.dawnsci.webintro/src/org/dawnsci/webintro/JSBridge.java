/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.webintro;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
/**
 * 
 * Class which provides extension point information to the JavaScript running inside the JavaFX WebView.
 * Any public methods can be called by JavaScript, and the result returned to JavaScript
 * @author David Taylor
 *
 */
public class JSBridge {
	private final static Logger logger = LoggerFactory.getLogger(JSBridge.class);

	/** 
	 * Internal class used to build json
	 */
	@SuppressWarnings("unused")	
	private class Item{
		public String id;
		public String name;
		public String image;
		public String description;
		public boolean isContent;
		public boolean isAction;
		public boolean isLink;
		public boolean isCategory;
		public String content;
		public String href;
		public ArrayList<Item> items;
	}
	/** 
	 * Internal class used to build json
	 */
	@SuppressWarnings("unused")
	private class Page{
		public String id;
		public String name;
		public String page_id;
		public String content;
		public ArrayList<Item> items;
	}
	/** 
	 * Internal class used to build json
	 */
	@SuppressWarnings("unused")
	private class Root{
		public ArrayList<Page> pages;
	}
	
	/**
	 * A method to construct the Platform URL of a resource contributed through an extension point
	 * 
	 * @param contributer The IContributer representation of the contributing plugin
	 * @param resourceLocation The location relative to the plugin root of the required resource
	 * @return The Eclipse "Platform URL" of the resource
	 */
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
	
	public String getVersion(){
		String version = BundleUtils.getDawnVersion();
		if(version == null){
			return "(unknown version)";
		}
		return BundleUtils.getDawnVersion();
	}

	/**
	 * Loads text from a file specified with an Eclipse Platform URL
	 * 
	 * @param resourceUrl The full URL of the resource to load
	 * @return A String with the contents of the file. Returns empty string if file does not exist
	 */
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

	/**
	 * Get an array of all contributions to a specific extension point
	 * 
	 * @param point The extension point to load
	 * @return Array of extension point contributions
	 */
	private IConfigurationElement[] getRegisteredConfigs(String point){
		IConfigurationElement[] configs = org.eclipse.core.runtime.Platform
				.getExtensionRegistry()
				.getExtensionPoint(point).getConfigurationElements(); //$NON-NLS-1$
		return configs;
	}

	/**
	 * Gets an array of all contributions to a specific extension point with att = val
	 * 
	 * @param point The extension point to load
	 * @param att The attribute to filter by
	 * @param val The value of att
	 * @return
	 */
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

	/**
	 * Gets an array of items with the a specific parent, and sorts them by the "ordering" field
	 * 
	 * @param parentId The category_id or page_id of the parent item/page
	 * @return An array of item contributions
	 */
	private IConfigurationElement[] getOrderedChildren(String parentId){
		IConfigurationElement[] parentItems	= getConfigsWithAttribute("org.dawnsci.webintro.item", "parent_id", parentId);

		Arrays.sort(parentItems, new ConfigElementComparator());
		return parentItems;
	}

	/**
	 * Gets an array of all pages contributed to the org.dawnsci.webintro.page extension point
	 * and sorts them by the "ordering" field
	 * @return An array of page contributions
	 */
	private IConfigurationElement[] getOrderedPages(){
		IConfigurationElement[] pages = getRegisteredConfigs("org.dawnsci.webintro.page");
		Arrays.sort(pages, new ConfigElementComparator());
		return pages;
	}

	/**
	 * Gets the specified attribute from the IConfigurationElement. 
	 * If it has not been specified, returns an empty string. 
	 * 
	 * @param configElement
	 * @param attributeName
	 * @return The attribute value, or an empty string if not specified
	 */
	private String getOptionalString(IConfigurationElement configElement, String attributeName){
		String val = configElement.getAttribute(attributeName);
		if(val == null){
			return "";
		}else{
			return val;
		}
	}
	
	/**
	 * Builds an Item object for the item described by the provided IConfigurationElement
	 * @param thisItem The configuration element
	 * @return An Item instance with the item information
	 */
	private Item getJsonForItem(IConfigurationElement thisItem){
		String itemImageURL = getResourceURL(thisItem.getContributor(),thisItem.getAttribute("icon"));

		boolean isContent = thisItem.getName().equals("introContent");
		boolean isAction = thisItem.getName().equals("introAction");
		boolean isLink = thisItem.getName().equals("introLink");
		boolean isCategory = thisItem.getName().equals("introCategory");

		Item i = new Item();
		i.id = thisItem.getAttribute("id");
		i.name = thisItem.getAttribute("name");
		i.image = itemImageURL;
		i.description = getOptionalString(thisItem,"description");
		i.isContent = isContent;
		i.isAction = isAction;
		i.isLink = isLink;
		i.isCategory = isCategory;

		if(isContent){
			String itemContentURL = getResourceURL(thisItem.getContributor(), thisItem.getAttribute("content_file"));
			i.content = getTextResource(itemContentURL);
		}else if(isLink){
			i.href = thisItem.getAttribute("href");
		}

		return i;
	}

	/**
	 * Creates an Item ArrayList instance with the information for the items provided and their children.
	 * 
	 * @param items the items to get the JSON for
	 * @param orphanedItems an ArrayList containing all the items. As the item is processed, its entry in this ArrayList will be removed
	 * @param allowCategories a boolean specifying whether to allow categories. 
	 * 		  Should be true for processing pages, and false for processing categories (no nested categories) 
	 * @return An ArrayList containing Items with all information relating to the items & their children
	 */
	private ArrayList<Item> getJsonForItems(IConfigurationElement[] items, ArrayList<IConfigurationElement> orphanedItems, boolean allowCategories) {
		return getJsonForItems(items, orphanedItems, allowCategories, new ArrayList<Item>());
	}

	/**
	 * Creates an ArrayList instance containing Items with the information for the items provided and their children. 
	 * Allows a starting ArrayList to be passed in. The new items will be appended to this array.
	 * 
	 * @param items the items to get the JSON for
	 * @param orphanedItems an ArrayList containing all the items. As the item is processed, its entry in this ArrayList will be removed
	 * @param allowCategories a boolean specifying whether to allow categories. 
	 * 		  Should be true for processing pages, and false for processing categories (no nested categories)
	 * @param startItems a JsonArrayBuilder instance to append new items to 
	 * @return An Item ArrayList containing all information relating to the items & their children
	 */
	private ArrayList<Item> getJsonForItems(IConfigurationElement[] items, ArrayList<IConfigurationElement> orphanedItems, boolean allowCategories, ArrayList<Item> startItems) {
		ArrayList<Item> allItems = startItems;
		for (IConfigurationElement thisItem : items){
			orphanedItems.remove(thisItem); // Remove this item from the main list
			Item thisItemJson = getJsonForItem(thisItem);

			if(thisItem.getName().equals("introCategory")){
				if(!allowCategories){
					logger.error("Tried to add a category to a category, ignoring contribution.");
				}else{
					IConfigurationElement[] catItems = getOrderedChildren(thisItem.getAttribute("category_id"));

					ArrayList<Item> categoryItemsJson = getJsonForItems(catItems, orphanedItems, false);

					thisItemJson.items = categoryItemsJson;
					allItems.add(thisItemJson);
				}
			}else{
				allItems.add(thisItemJson);
			}

		}
		return allItems;
	}

	/**
	 * The primary method which is called by the JavaScript. 
	 * Serialises all extension point information into JSON and returns this as a String
	 * @return String with extension point information serialised as JSON
	 */
	public String getIntroJSON(){
		getVersion();
		IConfigurationElement[] pages = getOrderedPages();

		// Setup a list with all of the items in it. We will remove them from the list when they're added to the JSON
		ArrayList<IConfigurationElement> orphanedItems = new ArrayList<IConfigurationElement>(Arrays.asList(getRegisteredConfigs("org.dawnsci.webintro.item")));

		ArrayList<Page> pagesList = new ArrayList<Page>();

		for (IConfigurationElement thisPage : pages){

			IConfigurationElement[] items = getOrderedChildren(thisPage.getAttribute("page_id"));
			ArrayList<Item> pageItems = getJsonForItems(items, orphanedItems, true);

			String pageContentURL = getResourceURL(thisPage.getContributor(), thisPage.getAttribute("content_file"));
			
			Page thisPageJson = new Page();
			thisPageJson.id = thisPage.getAttribute("id");
			thisPageJson.page_id = thisPage.getAttribute("page_id");
			thisPageJson.name = thisPage.getAttribute("name");
			thisPageJson.content = getTextResource(pageContentURL);
			thisPageJson.items = pageItems;
			
			pagesList.add(thisPageJson);
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

			ArrayList<Item> pageItems = new ArrayList<Item>();
			if(orphanedCategories.size()>0){
				pageItems = getJsonForItems(orphanedCategories.toArray(new IConfigurationElement[0]), orphanedItems, true, pageItems);
			}
			if(orphanedItems.size()>0){
				pageItems = getJsonForItems(orphanedItems.toArray(new IConfigurationElement[0]), orphanedItems, true, pageItems);
			}

			Page thisPageJson = new Page();
			
			thisPageJson.id = "org.dawnsci.webintro.content.other";
			thisPageJson.page_id = "org.dawnsci.webintro.content.other";
			thisPageJson.name = "Other";
			thisPageJson.content = "These items were not assigned to a page:";
			thisPageJson.items = pageItems;

			pagesList.add(thisPageJson);

		}

		Root rootObject = new Root();
		rootObject.pages = pagesList;
		
		Gson gson = new Gson();
		
		return gson.toJson(rootObject);
		
	}
	
	/**
	 * Public method called by JavaScript to invoke a specific action. 
	 * 
	 * @param configId The config ID for which the action is associated
	 * @return boolean indicating whether the action was successful
	 */
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
	/**
	 * Public method called by JavaScript to launch the system browser at a specific page. 
	 * URL will be validated by java.net.URL and checked for http or https protocol
	 * @param href The web address to open
	 * @throws MalformedURLException
	 */
	public boolean openLink(String href) throws MalformedURLException{
		logger.debug("JSBridge openLink Called for url "+href);

		// Use URL class to validate the string (otherwise a website could open an executable on the local filesystem)
		URL urlObject = new URL(href); 
		String protocol = urlObject.getProtocol();
		if( protocol.equals("http") || protocol.equals("https") ){
			Program.launch(urlObject.toString());
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Utility method which allows JavaScript to log to the Java console
	 * @param text The text to log
	 */
	public void log(String text)
	{
		System.out.println("JavaScript logged: "+text);
	}
}