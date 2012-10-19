/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.overlay;


import org.dawnsci.plotting.jreality.overlay.objects.ArrowObject;
import org.dawnsci.plotting.jreality.overlay.objects.BoxObject;
import org.dawnsci.plotting.jreality.overlay.objects.CircleObject;
import org.dawnsci.plotting.jreality.overlay.objects.PointListObject;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;
import org.eclipse.swt.SWT;

/**
 * A demonstration overlay that demonstrates the functionality of a Overlay2DConsumer
 */
@SuppressWarnings("unused")
public class DemoOverlay implements Overlay2DConsumer {

	private Overlay2DProvider2 provider;
	private int sx,sy,ex,ey;
	private boolean drawing = false;
	private ArrowObject line;
	private BoxObject box;
	private CircleObject circle;
	private PointListObject pointList;
	private OverlayImage image;
	int primID = -1;
	int primID2 = -1;
	/**
	 * Constructor of the TestOverlay
	 */
	public DemoOverlay()
	{
		provider = null;
		sx = 0;
		sy = 0;
		ex = 0;
		ey = 0;
	}
	
	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider2)provider;
		line = (ArrowObject) this.provider.registerObject(PrimitiveType.ARROW);
		box = (BoxObject)this.provider.registerObject(PrimitiveType.BOX);
		circle = (CircleObject)this.provider.registerObject(PrimitiveType.CIRCLE);
		pointList = (PointListObject)this.provider.registerObject(PrimitiveType.POINTLIST);
		image = this.provider.registerOverlayImage(128, 128);
		if (line != null)
			line.setLinePoints(30,30,60,60);
		if (box != null)
			box.setBoxPoints(80, 80, 120, 110);
		if (circle != null) {
			circle.setCirclePoint(128, 128);
			circle.setRadius(30);
		}
		if (pointList != null) {
			double[] xs = new double[100];
			double[] ys = new double[100];
			for (int i = 0; i < xs.length; i++) {
				xs[i] = 256 * Math.random();
				ys[i] = 256 * Math.random();
			}
			pointList.setPointPositions(xs, ys);
		}
		drawOverlay();
	}

	private void drawOverlay() {
		provider.begin(OverlayType.VECTOR2D);
		line.setColour(java.awt.Color.blue);
		line.draw();
		box.setColour(java.awt.Color.red);
		box.draw();
		circle.setColour(java.awt.Color.DARK_GRAY);
		circle.draw();
		pointList.setColour(java.awt.Color.MAGENTA);
		pointList.draw();
		provider.end(OverlayType.VECTOR2D);
		provider.begin(OverlayType.IMAGE);
		image.clear((short)255, (short)0, (short)0,(short) 128);
		image.putPixel(64, 64, (short)255, (short)255, (short)255, (short)255);
		provider.end(OverlayType.IMAGE);
		
	}
	
	@Override
	public void imageDragged(IImagePositionEvent event) {
		ex = event.getImagePosition()[0];
		ey = event.getImagePosition()[1];
		provider.setPlotAreaCursor(SWT.CURSOR_CROSS);
	}


	@Override
	public void imageFinished(IImagePositionEvent event) {
		provider.restoreDefaultPlotAreaCursor();
	}


	@Override
	public void imageStart(IImagePositionEvent event) {
		sx = event.getImagePosition()[0];
		sy = event.getImagePosition()[1];
		System.out.println(event.getPrimitiveID());
		provider.setPlotAreaCursor(SWT.CURSOR_HELP);
	}

	@Override
	public void unregisterProvider() {
		provider = null;
	}

	@Override
	public void hideOverlays() {
		if (provider != null)
			provider.setPrimitiveVisible(primID, false);
	}

	@Override
	public void showOverlays() {
		if (provider != null)
			provider.setPrimitiveVisible(primID, true);
	}

	@Override
	public void removePrimitives() {
		primID = -1;
	}


}
