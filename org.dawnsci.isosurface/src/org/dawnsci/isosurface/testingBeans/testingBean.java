package org.dawnsci.isosurface.testingBeans;

import java.util.ArrayList;
import java.util.List;

public class testingBean {
	
	private List<testItem> items;
	
	public testingBean()
	{
		items = new ArrayList<testItem>();
	}
	
	public void clear()
	{
		items.clear();
	}
	
	public List<testItem> getItems()
	{
		return this.items;
	}
	
	public void setItems(List<testItem> newItems)
	{
		this.items = newItems;
	}
	
	public void addItem(testItem newItem)
	{
		items.add(newItem);
	}
}
