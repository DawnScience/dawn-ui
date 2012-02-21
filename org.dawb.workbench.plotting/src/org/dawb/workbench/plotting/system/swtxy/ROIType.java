package org.dawb.workbench.plotting.system.swtxy;

import org.dawb.workbench.plotting.Activator;
import org.eclipse.swt.graphics.Image;

public enum ROIType {

	LINE("Selection of lines over the plotted data", "icons/ProfileLine.png"), 
	BOX("Selection of boxes over the plotted data", "icons/ProfileBox.png"), 
	NONE("No selection", "icons/MouseArrow.png");
	
	
	private String description;
	private Image iconImage;

	private ROIType(final String description, 
			        final String iconImagePath) {
		this.description = description;
		this.iconImage   = Activator.getImage(iconImagePath);
	}

	public String getDescription() {
		return description;
	}

	public Image getIconImage() {
		return iconImage;
	}
}
