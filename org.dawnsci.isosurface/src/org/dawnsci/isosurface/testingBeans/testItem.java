package org.dawnsci.isosurface.testingBeans;

public class testItem {

	private String name;
	
	
	public testItem()
	{
		this("testName");
	}
	public testItem(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
		
	
}
