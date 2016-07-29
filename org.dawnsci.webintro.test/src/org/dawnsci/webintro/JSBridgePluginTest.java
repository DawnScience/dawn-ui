package org.dawnsci.webintro;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IContributor;
import org.junit.Before;
import org.junit.Test;

public class JSBridgeTest {
	
	@Before
	public void setup(){
	}
	
	@Test
	public void testGetResourceURL(){
		IContributor test = new IContributor() {
			@Override
			public String getName() {
				return "org.test.package";
			}
		};
		String resourceLocation = "icon/test.png";
		String actual = JSBridge.getResourceURL(test, resourceLocation);
		
		assertEquals("platform:/plugin/org.test.package/icon/test.png", actual);
		
	}
}
