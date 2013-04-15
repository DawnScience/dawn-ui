package org.dawnsci.plotting.api.annotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;

public class AnnotationUtils {

	/**
	 * Call to get a unique annotation name 
	 * @param nameStub
	 * @param system
	 * @return
	 */
	public static String getUniqueAnnotation(final String nameStub, final IPlottingSystem system, final String... usedNames) {
		int i = 1;
		@SuppressWarnings("unchecked")
		final List<String> used = usedNames!=null ? (List<String>) Arrays.asList(usedNames) : (List<String>) Collections.EMPTY_LIST;
		while(system.getAnnotation(nameStub+" "+i)!=null || used.contains(nameStub+" "+i)) {
			++i;
			if (i>10000) break; // something went wrong!
		}
		return nameStub+" "+i;
	}

	/**
	 * 
	 * @param system
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static final IAnnotation replaceCreateAnnotation(IPlottingSystem system, String name) throws Exception {
		
        if (system.getAnnotation(name)!=null) {
        	system.removeAnnotation(system.getAnnotation(name));
        }
		return system.createAnnotation(name);
	}

}
