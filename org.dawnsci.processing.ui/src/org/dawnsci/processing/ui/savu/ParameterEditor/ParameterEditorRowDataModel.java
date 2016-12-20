package org.dawnsci.processing.ui.savu.ParameterEditor;

/**
 * Model object for a Region Of Interest row used in an AxisPixel Table
 * 
 * @author wqk87977
 *
 */
class ParameterEditorRowDataModel {
	private String key;
	private Object value;
	private String description;

	public ParameterEditorRowDataModel(String key, Object value, String description) {
		this.key = key;
		this.value = value;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}