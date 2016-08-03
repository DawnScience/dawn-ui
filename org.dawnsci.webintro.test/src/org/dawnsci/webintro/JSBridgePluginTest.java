package org.dawnsci.webintro;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
	
	private int getJsonItemIndex(JsonArray array, String param, String val){
		for(int i=0;;i++){
			JsonObject thisObject = array.getJsonObject(i);
			String testVal = thisObject.getString(param);
			if (testVal.equals(val)){
				return i;
			}
		}
	}
	
	private JsonObject findJsonItem(JsonArray array, String param, String val){
		try{
			int index = getJsonItemIndex(array, param, val);
			return array.getJsonObject(index);
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
	
	@Test
	public void testCategoryContribution(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject contentItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.categorytest");
		
		assertEquals("Test Category Contribution", contentItem.getString("name"));
		assertEquals("A description for the category contribution", contentItem.getString("description"));
		assertEquals("platform:/plugin/org.dawnsci.webintro/icons/test-dawn-logo-icon.png", contentItem.getString("image"));
	}
	
	@Test
	public void testCategoryItemContributions(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject category = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.categorytest");
		
		JsonObject catItem1 = findJsonItem(category.getJsonArray("items"), "id", "org.dawnsci.webintro.test.categorytest.item1");
		JsonObject catItem2 = findJsonItem(category.getJsonArray("items"), "id", "org.dawnsci.webintro.test.categorytest.item2");
		
		assertEquals("Category Item 1", catItem1.getString("name"));
		assertEquals("Category Item 2", catItem2.getString("name"));
	}
	
	@Test
	public void checkPageOrdering(){
		JsonArray pageArray = testJson.getJsonArray("pages");
		int testPageIndex = getJsonItemIndex(pageArray, "page_id", "plugin_test_page");
		int testPageIndex2 = getJsonItemIndex(pageArray, "page_id", "plugin_test_page_2");
		
		assertTrue("plugin_test_page is after plugin_test_page_2",testPageIndex > testPageIndex2);
	}
	
	@Test
	public void checkItemOrdering(){
		JsonObject testPage = getPage("plugin_test_page");
		
		JsonArray itemArray = testPage.getJsonArray("items");
		int[] indexList = {getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.contenttest"),
		                   getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.linktest"),
		                   getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.actiontest"),
		                   getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.categorytest")
		};
		int[] expected = {0, 1, 2, 3};
		assertArrayEquals("Items on page are in order",expected,indexList);
		
	}
	
	@Test
	public void checkCategoryOrdering(){
		JsonObject testPage = getPage("plugin_test_page");
		JsonObject category = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.categorytest");

		JsonArray itemArray = category.getJsonArray("items");
		int[] indexList = {getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.categorytest.item2"),
		                   getJsonItemIndex(itemArray, "id", "org.dawnsci.webintro.test.categorytest.item1")
		};
		int[] expected = {0, 1};
		assertArrayEquals("Items in category are in order",expected,indexList);
	}
	
	@Test
	public void checkOrphanedItems(){
		JsonObject testPage = getPage("org.dawnsci.webintro.content.other");
		
		JsonObject orphanedItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.orphan1");
		assertEquals("Orphaned item is on 'other' page","Orphaned Item", orphanedItem.getString("name"));
		
		JsonObject testCatItem = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.orphan2");
		assertNull("Category item is not a direct child of the page",testCatItem);
		
		JsonObject category = findJsonItem(testPage.getJsonArray("items"), "id", "org.dawnsci.webintro.test.orphancategory");
		assertEquals("Orphaned category is on 'other' page", "Orphaned Category", category.getString("name"));
		
		JsonObject categoryItem = findJsonItem(category.getJsonArray("items"), "id", "org.dawnsci.webintro.test.orphan2");
		assertEquals("Item of orphaned category exists", "Orphaned Category Item", categoryItem.getString("name"));
	}

}
