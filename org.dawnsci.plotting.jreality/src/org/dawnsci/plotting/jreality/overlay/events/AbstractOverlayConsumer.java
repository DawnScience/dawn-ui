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

package org.dawnsci.plotting.jreality.overlay.events;

import java.util.Collection;
import java.util.HashSet;

import org.dawnsci.plotting.jreality.overlay.Overlay1DConsumer;
import org.dawnsci.plotting.jreality.overlay.Overlay2DConsumer;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;
import org.eclipse.swt.widgets.Display;


/**
 * An OverlayConsumer with the ability to add listeners which are notified when the user clicks
 * in the diagram. Convenience class for implementing drawing of overlay and listening to
 * different click events.
 * 
 * How to use:
 * 1. Extend class
 * 2. Implement createDrawingParts returning the drawing codes created (currently LINE or BOX)
 * 3. Implement drawOverlay which is called before the user has made a selection and 
 *    after. If the user has not made a selection some implementations will draw nothing.
 * 4. Use addOverlaySelectionListener(...) to listen to selected vertices changing.
 * 
 */
public abstract class AbstractOverlayConsumer implements Overlay1DConsumer, Overlay2DConsumer {


	protected OverlayProvider provider;
	protected int[]           parts;
	private AreaSelectEvent   start;
	private Display           display;
	private boolean mouseListenerEnabled = true;
	
	/**
	 * @param display
	 */
	public AbstractOverlayConsumer(final Display display) {
		this.display = display;
	}
	
	/**
	 * Implement to create the parts required for drawing.
	 */
	protected abstract int[] createDrawingParts(OverlayProvider provider);

	/**
	 * Implement this method to draw the default view. If it does nothing,
	 * nothing is drawn until the user clicks on the diagram and drawOverlay(AreaSelectEvent) is called.
	 */
	protected abstract void drawOverlay(OverlayDrawingEvent evt);
		
	
	@Override
	public void registerProvider(final OverlayProvider provider) {
		this.provider = provider;
		this.parts    = createDrawingParts(provider);
		drawOverlay(new OverlayDrawingEvent(provider, parts));
	}

	@Override
	public void unregisterProvider() {
		provider = null;
		if (selectionListeners != null) selectionListeners.clear();
		selectionListeners = null;
		parts    = null;
		start    = null;
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {
		if (mouseListenerEnabled) {
			if (event.getMode() == 0) {
				start = event;
			}
			if (event.getMode() == 1 || event.getMode() == 2) {
				drawOverlay(new OverlayDrawingEvent(provider, start, event, parts));
				notifyGraphSelectionListeners(event);
			}
		}
	}

	public void enableMouseListener(boolean enabled) {
		mouseListenerEnabled = enabled;
	}

	/**
	 * Does nothing by default
	 */
	@Override
	public void imageDragged(IImagePositionEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Does nothing by default
	 */
	@Override
	public void imageFinished(IImagePositionEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Does nothing by default
	 */
	@Override
	public void imageStart(IImagePositionEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	private Collection<GraphSelectionListener> selectionListeners;
	
	/**
	 * Add listener to events being selected in the graph.
	 * @param l
	 */
	public void addGraphSelectionListener(GraphSelectionListener l) {
		if (selectionListeners == null) selectionListeners = new HashSet<GraphSelectionListener>(5);
		selectionListeners.add(l);
	}

	protected void notifyGraphSelectionListeners(final AreaSelectEvent end) {
		
		if (selectionListeners == null) return;
		
	    if (!display.isDisposed()) {
	    	display.asyncExec(new Runnable()  {
	    		@Override
	    		public void run() {
	    			final GraphSelectionEvent evt = new GraphSelectionEvent(this);
	    			evt.setStart(start);
	    			evt.setEnd(end);
	    			for (GraphSelectionListener l : selectionListeners) {
	    				l.graphSelectionPerformed(evt);
	    			}
	    		}
	    	});
	    }
	}

	@Override
	public void removePrimitives() {
		parts = null;
	}

	@Override
	public void hideOverlays() {
		if (parts != null) {
			for (int p = 0; p < parts.length; p++) {
				provider.setPrimitiveVisible(parts[p], false);
			}
		}
	}

	@Override
	public void showOverlays() {
		if (parts != null) {
			for (int p = 0; p < parts.length; p++) {
				provider.setPrimitiveVisible(parts[p], true);
			}
		}
	}
}
