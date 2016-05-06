package org.dawnsci.isosurface.isogui;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MaximumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiHidden;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiReadOnly;
import org.eclipse.swt.graphics.RGB;
public interface IIsoItem {
	@UiReadOnly
	public Type getRenderType();
	public double getValue();
	public void setValue(double value);
	public int getResolution();
	@MinimumValue("1")
	public void setResolution(int resolution);
	public int getOpacity();
	@MaximumValue("100")
	@MinimumValue("1")
	public void setOpacity(int opacity);
	public RGB getColour();
	public void setColour(RGB rgb);
	@UiHidden
	public String getTraceKey();
	
}