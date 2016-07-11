package org.dawnsci.webintro;

import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;

public class ConfigElementComparator implements Comparator<IConfigurationElement> {
	/**
	 * Compares two elements.
	 * 
	 * @param  f1  The first element you want to compare.
	 * @param  f2  The second element you want to compare.
	 * @return  -1,0,1  Whether or not one is greater than, less than,
	 * or equal to one another.
	 */
	public int compare(IConfigurationElement arg0, IConfigurationElement arg1) {
		Double num0, num1;
		try{
			num0 = Double.valueOf(arg0.getAttribute("ordering"));
		}catch (NumberFormatException e){
			// Arg0 is not a valid number, so push it to the bottom of the list
			return -1;
		}

		try{
			num1 = Double.valueOf(arg1.getAttribute("ordering"));
		}catch (NumberFormatException e){
			// Arg1 is not a valid number, so Arg0 should be above it
			return 1;
		}

		if (num0 < num1) {
			return -1;
		}

		if (num0 > num1) {
			return 1;
		}

		return 0; // They're the same!
	}
}