package org.dawnsci.webintro;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.junit.Before;
import org.junit.Test;

public class JSBridgePluginTest {
	
	private JSBridge bridge;
	private JsonObject testJson;
	
	private JsonObject findJsonItem(JsonArray array, String param, String val){
		try{
			for(int i=0;;i++){
				JsonObject thisObject = array.getJsonObject(i);
				String testVal = thisObject.getString(param);
				if (testVal.equals(val)){
					return thisObject;
				}
			}
		}catch(IndexOutOfBoundsException e){
			return null;
		}
	}
	
	private JsonObject getPage(String page_id){
		JsonArray pageArray = testJson.getJsonArray("pages");
		return findJsonItem(pageArray,"page_id",page_id);
	}
	
	@Before
	public void setup(){
		bridge = new JSBridge();
		bridge.getIntroJSON();
		
		String json = bridge.getIntroJSON();
		
		JsonReader reader = Json.createReader(new StringReader(json));
		testJson = reader.readObject();
		reader.close();
	}
	
	@Test
	public void testPage(){
		JsonObject testPage = getPage("plugin_test_page");
		
		assertEquals("Test Page", testPage.getString("name"));
		assertEquals("plugin_test_page",testPage.getString("page_id"));
		assertEquals("org.dawnsci.webintro.test.test",testPage.getString("id"));
		assertEquals("Test content for the test page", testPage.getString("content"));
	}
	
	@Test
	public void testContentContribution(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject contentItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.contenttest");
		
		assertEquals("Test Content Contribution", contentItem.getString("name"));
		assertEquals("A description for the content contribution", contentItem.getString("description"));
		assertEquals("Test content for the content contribution", contentItem.getString("content"));
		assertEquals("platform:/plugin/org.dawnsci.webintro/icons/test-dawn-logo-icon.png", contentItem.getString("image"));
	}
	
	@Test
	public void testLinkContribution(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject contentItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.linktest");
		
		assertEquals("Test Link Contribution", contentItem.getString("name"));
		assertEquals("A description for the link contribution", contentItem.getString("description"));
		assertEquals("http://dawnsci.org", contentItem.getString("href"));
		assertEquals("platform:/plugin/org.dawnsci.webintro/icons/test-dawn-logo-icon.png", contentItem.getString("image"));
	}
	
	@Test
	public void testActionContribution(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject contentItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.actiontest");
		
		assertEquals("Test Action Contribution", contentItem.getString("name"));
		assertEquals("A description for the action contribution", contentItem.getString("description"));
		assertEquals("platform:/plugin/org.dawnsci.webintro/icons/test-dawn-logo-icon.png", contentItem.getString("image"));
		
		// Try running the action, if an exception is thrown the test will fail (no assert needed)
		bridge.runAction("org.dawnsci.webintro.test.actiontest");
	}

}
