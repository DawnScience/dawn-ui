package org.dawnsci.processing.ui.model;

public class CComboWithEntryCellEditorData {
	
	String[] items = null;
	
	String active_item = null;
	
	public CComboWithEntryCellEditorData(String[] items, String active_item) {
		if (items == null)
			throw new NullPointerException("items cannot be null");
		if (active_item == null)
			throw new NullPointerException("active_item cannot be null");
		
		this.items = new String[items.length];
		for (int item = 0 ; item < items.length ; item++) {
			this.items[item] = new String(items[item]);
		}
		this.active_item = new String(active_item);
	}

	public CComboWithEntryCellEditorData(CComboWithEntryCellEditorData old, String new_active_item) {
		this.items = new String[old.items.length];
		for (int item = 0 ; item < old.items.length ; item++) {
			this.items[item] = new String(old.items[item]);
		}
		this.active_item = new String(new_active_item);
	}
	
	public CComboWithEntryCellEditorData(CComboWithEntryCellEditorData old) {
		this.items = new String[old.items.length];
		for (int item = 0 ; item < old.items.length ; item++) {
			this.items[item] = new String(old.items[item]);
		}
		this.active_item = new String(old.active_item);
	}
	
	public CComboWithEntryCellEditorData(String[] items) {
		this(items, items[0]);
	}
	
	public String getActiveItem() {
		return active_item;
	}
	
	@Override
	public String toString() {
		return active_item;
	}
}
