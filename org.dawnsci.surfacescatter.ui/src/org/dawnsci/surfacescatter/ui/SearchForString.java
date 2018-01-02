package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.TableItem;

public class SearchForString {

	public static TableItem[] search(String in, TableItem[] tis) {
		
		ArrayList<TableItem> tiList = new ArrayList<>();
		
		for(TableItem t : tis) {
			if(StringUtils.contains(t.getText(1), in)) {
				tiList.add(t);
			}
		}
		
		TableItem[] p =  new TableItem[tiList.size()];
		
		tiList.toArray(p);
		
		return p;
	}
	
}
