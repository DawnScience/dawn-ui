package org.dawnsci.plotting.roi;

public abstract class IRegionRow {
	protected String name;
	protected boolean enabled=true;
	public String getName() {
		return name;
	}
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}