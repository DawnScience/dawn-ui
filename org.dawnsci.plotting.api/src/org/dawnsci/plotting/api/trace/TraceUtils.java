package org.dawnsci.plotting.api.trace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Class containing utility methods for regions to avoid duplication 
 * @author fcp94556
 *
 */
public class TraceUtils {
	
	

	/**
	 * Call to get a unique region name 
	 * @param nameStub
	 * @param system
	 * @return
	 */
	public static String getUniqueTrace(final String nameStub, final IPlottingSystem system, final String... usedNames) {
		int i = 1;
		@SuppressWarnings("unchecked")
		final List<String> used = (List<String>) (usedNames!=null ? Arrays.asList(usedNames) : Collections.emptyList());
		while(system.getTrace(nameStub+" "+i)!=null || used.contains(nameStub+" "+i)) {
			++i;
			if (i>10000) break; // something went wrong!
		}
		return nameStub+" "+i;
	}

	/**
	 * Removes a trace of this name if it is already there.
	 * @param plottingSystem
	 * @param string
	 * @return
	 */
	public static final ILineTrace replaceCreateLineTrace(IPlottingSystem system, String name) {
		if (system.getTrace(name)!=null) {
			system.removeTrace(system.getTrace(name));
		}
		return system.createLineTrace(name);
	}
	
	/**
	 * Determine if IImageTrace has custom axes or not.
	 */
	public static boolean isCustomAxes(IImageTrace trace) {
		
		if (trace==null) return false;
		List<IDataset> axes = trace.getAxes();
		int[]         shape = trace.getData().getShape();

		if (axes==null)     return false;
		if (axes.isEmpty()) return false;
		
		final Class<?> xClazz = axes.get(0).elementClass();
		final Class<?> yClazz = axes.get(1).elementClass();
		if (!xClazz.isInstance(int.class) || !yClazz.isInstance(int.class)) {
			return true;
		}
		
		if (axes.get(0).getSize() == shape[1] &&
		    axes.get(1).getSize() == shape[0]) {
			boolean startZero = axes.get(0).getDouble(0)==0d  &&
				                axes.get(1).getDouble(0)==0d;
			
			if (!startZero) return true;
			
			double xEnd = axes.get(0).getDouble(shape[1]-1);
			double yEnd = axes.get(1).getDouble(shape[0]-1);
			
			boolean maxSame =	xEnd==shape[1]-1 &&
				                yEnd==shape[0]-1;
			
			if (maxSame) return false;
		}
		
		return true;
	}

}
