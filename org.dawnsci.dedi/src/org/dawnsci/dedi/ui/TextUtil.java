package org.dawnsci.dedi.ui;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for handling text that represents numerical values.
 * Allows to format numerical values in a consistent manner across the interface of DEDI.
 */
public class TextUtil {
	
	private TextUtil() {
		throw new IllegalStateException("This class is not meant to be instantiated.");
	}
	
	
	/**
	 * @param value       - the number to format
	 * @param precision   - the number of significant figures
	 * 
	 * @return - The number formatted to the given number of significant figures. 
	 *           If the magnitude of the number is greater than 10^(precision) or less than 0.1, 
	 *           exponential (scientific) notation is used.
	 *           Returns the String 'NaN' if the given value isNaN or Infinity.
	 * 
	 * @throws IllegalArgumentException if the precision argument is less than zero.
	 */
	public static String format(double value, int precision){
		if(value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY || Double.isNaN(value)) return "NaN";
		
		String result;
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(precision));
		BigDecimal absValue = bd.abs();
		
		if ((absValue.compareTo(BigDecimal.valueOf(Math.pow(10, precision))) <= 0 && 
			 absValue.compareTo(BigDecimal.valueOf(0.1)) >= 0) || absValue.compareTo(BigDecimal.valueOf(0)) == 0) { 
			
			result = bd.toString();
		}
		else 
			result = new DecimalFormat("0." + Stream.generate(() -> "0").limit(--precision).collect(Collectors.joining("")) + "E0").format(value);
		
		return result;
	}
	
	
	/**
	 *  @return - The given value formatted to 3 significant figures. 
	 *             If the magnitude of the number is greater than 1000 or less than 0.1, 
			       exponential (scientific) notation is used.
	 */
	public static String format(double value) {
		return format(value, 3);
	}
	
	
	/**
	 * @return Returns true if the two strings represent the same numerical value (although maybe formatted differently).
	 *         Returns false otherwise (including when the Strings cannot be parsed as doubles).
	 */
	public static boolean equalAsDoubles(String s1, String s2){
		boolean result;
		try{
			double d1 = Double.parseDouble(s1);
			double d2 = Double.parseDouble(s2);
			result = (d1 == d2);
		} catch(NumberFormatException | NullPointerException e){
			result = false;
		}
		return result;
	}
	
	
	/**
	 * "Fail safe" method for parsing a String - returns null in case the String is null or cannot be parsed as a double
	 * rather than throwing an exception like Double.parseDouble() would do.
	 * 
	 * @param s - the string to parse.
	 */
	public static Double parseDouble(String s) {
		Double result;
		try {
			result = Double.parseDouble(s);
		} catch(NumberFormatException | NullPointerException e) {
			result = null;
		}
		
		return result;
	}
}
