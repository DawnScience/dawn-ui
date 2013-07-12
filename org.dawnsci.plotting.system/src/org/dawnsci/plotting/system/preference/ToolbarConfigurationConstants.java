package org.dawnsci.plotting.system.preference;


public enum ToolbarConfigurationConstants {
	
	CONFIG    ("org.dawnsci.plotting.system.preference.config",           "Configuration"),
	ANNOTATION("org.dawnsci.plotting.system.preference.annotation",       "Annotation"),
	TOOLS     ("org.dawnsci.plotting.system.preference.tools",            "Tools"),
	AXIS      ("org.dawnsci.plotting.system.preference.axis",             "Axes"),
	REGION    ("org.dawnsci.plotting.system.preference.region",           "Regions"),
	ZOOM      ("org.dawnsci.plotting.system.preference.zoom",             "Zoom"),
	UNDO      ("org.dawnsci.plotting.system.preference.undo",             "Undo/Redo"),
	EXPORT    ("org.dawnsci.plotting.system.preference.export",           "Export"),
	HISTO     ("org.dawnsci.plotting.system.preference.histo",            "Histogram"),
	PALETTE   ("org.dawnsci.plotting.system.preference.palette",          "Palette"),
	ORIGIN    ("org.dawnsci.plotting.system.preference.origin",           "Origin"),
	MISCELLANEOUS("org.dawnsci.plotting.system.preference.miscellaneous", "Miscellaneous");
	
	private String id;
	private String label;
	
	ToolbarConfigurationConstants(String id, String label) {
		this.id    = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
}
