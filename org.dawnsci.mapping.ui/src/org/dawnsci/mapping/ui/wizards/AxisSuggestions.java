package org.dawnsci.mapping.ui.wizards;

import java.util.ArrayList;
import java.util.List;

public class AxisSuggestions {

	private List<List<String>> suggestions;
	
	public AxisSuggestions(int rank) {
		suggestions = new ArrayList<List<String>>(rank);
		for (int i = 0; i < rank; i++) {
			suggestions.add(new ArrayList<String>());
		}
	}
	
	public void addAxis(int dim, String name) {
		suggestions.get(dim).add(name);
	}
	
	public String[] getSuggestions(int dim) {
		List<String> s = suggestions.get(dim);
		String[] withEmpty = new String[s.size()+1];
		
		for (int i = 0; i < s.size(); i++) {
			withEmpty[i] = s.get(i);
		}
		withEmpty[s.size()] = "";
		
		return withEmpty;
	}
	
}
