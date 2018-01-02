package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class SearchForString {
	
	public static String[][] search(String in, String[] in0, String[] in1) {
		
		ArrayList<String> in0List = new ArrayList<>();
		ArrayList<String> in1List = new ArrayList<>();
		
		for(int y = 0; y<in0.length; y++) {
			if(StringUtils.contains(in1[y], in) || StringUtils.contains(in0[y], in) ) {
				in0List.add(in0[y]);
				in1List.add(in1[y]);
			}
		}
		
		String[] in0out =  new String[in0List.size()];
		String[] in1out =  new String[in1List.size()];
		
		in0List.toArray(in0out);
		in1List.toArray(in1out);
		
		return new String[][] {in0out, in1out};
	}
	
}
