package org.dawnsci.plotting.jreality.impl;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;

import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;

public class Plot1DAppearanceUpdater {
	static public void updateGraph(Plot1DAppearance pa, DefaultPointShader point)
	{
		point.setDiffuseColor(pa.getColour());
	}	

	static public void updateGraph(Plot1DAppearance pa, DefaultLineShader line, DefaultGeometryShader geom)
	{
		line.setDiffuseColor(pa.getColour());
		double lineWidth = (double)pa.getLineWidth();
		line.setLineWidth(lineWidth);
		switch(pa.getStyle())
		{
			case SOLID:
				line.setLineStipple(false);
				geom.setShowLines(true);
				geom.setShowPoints(false);
			break;
			case DASHED:
				geom.setShowLines(true);
				geom.setShowPoints(false);
				line.setLineStipple(true);
			break;
			case POINT:
			{
				line.setLineStipple(false);
				geom.setShowLines(false);
				geom.setShowPoints(true);
				DefaultPointShader dps =
					(DefaultPointShader)
					geom.createPointShader("default");
				dps.setSpheresDraw(false);
				dps.setDiffuseColor(line.getDiffuseColor());
				dps.setPointSize((double)lineWidth);
			}
			break;
			case SOLID_POINT:
			{
				line.setLineStipple(false);
				geom.setShowLines(true);
				geom.setShowPoints(true);
				DefaultPointShader dps =
					(DefaultPointShader)
					geom.createPointShader("default");
				dps.setSpheresDraw(false);
				dps.setDiffuseColor(line.getDiffuseColor());
				double pointSize = lineWidth * 4.0;
				if (pointSize < 4.0) pointSize = 4.0;
				dps.setPointSize(pointSize);
			}
			break;
			case DASHED_POINT:
			{
				line.setLineStipple(true);
				geom.setShowLines(true);
				geom.setShowPoints(true);
				DefaultPointShader dps =
					(DefaultPointShader)
					geom.createPointShader("default");
				dps.setSpheresDraw(false);
				dps.setDiffuseColor(line.getDiffuseColor());
				double pointSize = lineWidth * 4.0;
				if (pointSize < 4.0) pointSize = 4.0;
				dps.setPointSize(pointSize);
			}
			break;			
		}
	}

}
