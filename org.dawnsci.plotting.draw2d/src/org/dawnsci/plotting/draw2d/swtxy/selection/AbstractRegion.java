package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionContainer;
import org.eclipse.dawnsci.plotting.api.region.MouseListener;
import org.eclipse.dawnsci.plotting.api.region.MouseMotionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * This is a Figure, disabled for mouse events. 
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractRegion<T extends IROI> extends Figure implements IRegion, IRegionContainer {

	private Collection<IROIListener> roiListeners;
	protected boolean regionEventsActive = true;
	private boolean maskRegion         = false;
	protected String label = null;
	protected Color labelColour = null;
	protected Font labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.NORMAL);
	protected Dimension labeldim;

	protected T roi;

	@Override
	public boolean addROIListener(final IROIListener l) {
		if (roiListeners==null) roiListeners = new HashSet<IROIListener>(11);
		if (!roiListeners.contains(l)) return roiListeners.add(l);
		return false;
	}
	
	@Override
	public boolean removeROIListener(final IROIListener l) {
		if (roiListeners==null) return false;
		return roiListeners.remove(l);
	}
	
	protected void clearListeners() {
		if (roiListeners!=null)             roiListeners.clear();
		if (mouseListenerRegister!=null && !mouseListenerRegister.isEmpty()) {
			logger.debug("mouseListenerRegister should be empty here");
			mouseListenerRegister.clear();
		}
		if (mouseMotionListenerRegister!=null && !mouseMotionListenerRegister.isEmpty()) {
			logger.debug("mouseMotionListenerRegister should be empty here");
			mouseMotionListenerRegister.clear();
		}
	}
	
	protected void fireROIDragged(T roi, ROIEvent.DRAG_TYPE type) {
		if (roiListeners==null) return;
		if (!regionEventsActive) return;
		
		final ROIEvent evt = new ROIEvent(this, roi);
		evt.setDragType(type);
		for (IROIListener l : roiListeners) {
			try {
			    l.roiDragged(evt);
			} catch (Throwable ne) {
				logger.error("Unexpected exception in drawing!", ne);
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(AbstractRegion.class);
	
	protected void fireROIChanged(T roi) {
		if (roiListeners==null)  return;
		if (!regionEventsActive) return;
		
		final ROIEvent evt = new ROIEvent(this, roi);
		for (IROIListener l : roiListeners.toArray(new IROIListener[0])) {
			try {
			    l.roiChanged(evt);
			} catch (Throwable ne) {
				logger.error("Unexpected exception in drawning!", ne);
			}
		}
	}

	protected void fireROISelected(T roi) {
		if (roiListeners==null)  return;
		if (!regionEventsActive) return;
		
		final ROIEvent evt = new ROIEvent(this, roi);
		for (IROIListener l : roiListeners) {
			try {
			 l.roiSelected(evt);
			} catch (Throwable ne) {
				logger.error("Unexpected exception in drawning!", ne);
			}
		}
	}

	@Override
	public T getROI() {
		return roi;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setROI(IROI roi) {
		// Required fix after someone thought it would be a laugh to send
		// null ROIs over.
		if (roi == null) throw new NullPointerException("Cannot have a null region position!");
		setActive(roi.isPlot()); // set the region isActive flag
		if (this.roi == roi) {
			// return; // do not fire event
			logger.warn("Setting ROI same");
		}

		try {
			this.roi = (T) roi;
		} catch (ClassCastException ex) {
			T troi = convertROI(roi);
			if (troi == null) {
				logger.error("Could not convert {}", roi);
				return;
			}
		}

		String name = roi.getName();
		if (name == null || name.isEmpty() || name.trim().isEmpty()) {
			roi.setName(getName());
		}
		if (isVisible()) {
			try {
				regionEventsActive = false;
				updateRegion();
			} finally {
				regionEventsActive = true;
			}
		}
		fireROIChanged(this.roi);
	}

	/**
	 * Implement to return the region of interest
	 * @param recordResult if true this calculation changes the recorded absolute position
	 */
	protected abstract T createROI(boolean recordResult);  // TODO not required

	/**
	 * Override this to allow other types of ROIs to be set
	 * @param oroi
	 * @return converted ROI
	 */
	protected T convertROI(IROI oroi) {
		return null;
	}

	/**
	 * Implement this method to redraw the figure to the axis coordinates (only).
	 * Updates the region, usually called when items have been created and the position of the
	 * region should be updated. Does not fire events.
	 */
	protected abstract void updateRegion();

	public String toString() {
		if (getName()!=null) return getName();
		return super.toString();
	}
	
	protected boolean trackMouse;

	@Override
	public boolean isTrackMouse() {
		return trackMouse;
	}

	@Override
	public void setTrackMouse(boolean trackMouse) {
		this.trackMouse = trackMouse;
	}
	
	private boolean userRegion = true; // Normally a user region.

	@Override
	public boolean isUserRegion() {
		return userRegion;
	}

	@Override
	public void setUserRegion(boolean userRegion) {
		this.userRegion = userRegion;
	}
	
	public IRegion getRegion() {
		return this;
	}

	public void setRegion(IRegion region) {
		// Does nothing
	}

	public boolean isMaskRegion() {
		return maskRegion;
	}

	public void setMaskRegion(boolean maskRegion) {
		this.maskRegion = maskRegion;
	}
	
	public String getLabel() {
		if (label==null) return getName();
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label ;
		this.labeldim = FigureUtilities.getTextExtents(label, labelFont);
	}
	
	private Object userObject;
	/**
	 * 
	 * @return last object
	 */
	public Object setUserObject(Object object) {
		Object tmp = userObject;
		userObject = object;
		return tmp;
	}
	
	
	/**
	 * Call to remove unused resources. Do not forget to use
	 * super.dispose() in your override.
	 */
	public void dispose() {
		if (labelFont!=null) labelFont.dispose();
		labelFont   = null;
		if (labelColour!=null) labelColour.dispose();
		labelColour = null;
		labeldim    = null;
	}

	
	/**
	 * 
	 * @return object
	 */
	public Object getUserObject() {
		return userObject;
	}

	private boolean isActive;

	/**
	 * Returns whether the region is active or not
	 */
	@Override
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Set whether the region is active or not
	 * @param b
	 */
	@Override
	public void setActive(boolean b) {
		this.isActive = b;
	}

	// Record the listeners in a map so that they can be removed.
	protected Map<MouseListener, MouseListenerAdapter> mouseListenerRegister;

	/**
	 * Registers the given listener so it can be added only once, and also
	 * removed. The same (equal by reference) MouseListener can be registered
	 * only once.
	 * 
	 * @param listener
	 *            The listener to register
	 */
	protected MouseListenerAdapter registerMouseListener(
			final MouseListener listener) {
		if (mouseListenerRegister == null)
			mouseListenerRegister = new IdentityHashMap<MouseListener, MouseListenerAdapter>();
		else if (mouseListenerRegister.containsKey(listener))
			throw new IllegalStateException(
					"Registering an existing (equal by reference) MouseListener more times is not allowed!");
		final MouseListenerAdapter ad = new MouseListenerAdapter(listener);
		mouseListenerRegister.put(listener, ad);
		return ad;
	}

	/**
	 * Registers the given listener as a MouseListener of this AbstractRegion.
	 * The same (equal by reference) MouseListener can be added only once.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	@Override
	public void addMouseListener(final MouseListener listener) {
		try {
			super.addMouseListener(registerMouseListener(listener));
		} catch (final IllegalStateException e) {
			logger.debug(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Unregisters the given listener.
	 * 
	 * @param listener
	 *            The listener to cache
	 */
	protected MouseListenerAdapter unregisterMouseListener(
			final MouseListener listener) {
		final MouseListenerAdapter ad;
		if (mouseListenerRegister == null
				|| (ad = mouseListenerRegister.remove(listener)) == null)
			throw new IllegalStateException(
					"Unregistering a not existing MouseListener is not allowed!");
		return ad;
	}

	/**
	 * Unregisters the given listener, so that it will no longer receive
	 * notification of mouse events.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	@Override
	public void removeMouseListener(final MouseListener listener) {
		try {
			super.removeMouseListener(unregisterMouseListener(listener));
		} catch (final IllegalStateException e) {
			logger.debug(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	// Record the listeners in a map so that they can be removed.
	protected Map<MouseMotionListener, MouseMotionAdapter> mouseMotionListenerRegister;

	/**
	 * Registers the given listener so it can be added only once, and also
	 * removed. The same (equal by reference) MouseMotionListener can be
	 * registered only once.
	 * 
	 * @param listener
	 *            The listener to register
	 */
	protected MouseMotionAdapter registerMouseMotionListener(
			final MouseMotionListener listener) {
		if (mouseMotionListenerRegister == null)
			mouseMotionListenerRegister = new IdentityHashMap<MouseMotionListener, MouseMotionAdapter>();
		else if (mouseMotionListenerRegister.containsKey(listener))
			throw new IllegalStateException(
					"Registering an existing (equal by reference) MouseMotionListener more times is not allowed!");
		final MouseMotionAdapter ad = new MouseMotionAdapter(listener);
		mouseMotionListenerRegister.put(listener, ad);
		return ad;
	}

	/**
	 * Registers the given listener as a MouseMotionListener of this
	 * AbstractRegion. The same (equal by reference) MouseMotionListener can be
	 * added only once.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	@Override
	public void addMouseMotionListener(final MouseMotionListener listener) {
		try {
			super.addMouseMotionListener(registerMouseMotionListener(listener));
		} catch (final IllegalStateException e) {
			logger.debug(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Unregisters the given listener.
	 * 
	 * @param listener
	 *            The listener to cache
	 */
	protected MouseMotionAdapter unregisterMouseMotionListener(
			final MouseMotionListener listener) {
		final MouseMotionAdapter ad;
		if (mouseMotionListenerRegister == null
				|| (ad = mouseMotionListenerRegister.remove(listener)) == null)
			throw new IllegalStateException(
					"Unregistering a not existing MouseMotionListener is not allowed!");
		return ad;
	}

	/**
	 * Unregisters the given listener, so that it will no longer receive
	 * notification of mouse motion events.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	@Override
	public void removeMouseMotionListener(final MouseMotionListener listener) {
		try {
			super.removeMouseMotionListener(unregisterMouseMotionListener(listener));
		} catch (final IllegalStateException e) {
			logger.debug(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return true if the selection region only draws an outline.
	 */
	public boolean isOutlineOnly() {
		return false;
	}
	
	/**
	 * Set if the region should draw in outline only mode. If
	 * outline only is not available for this selection region, this
	 * method will throw a RuntimeException.
	 */
	public void setOutlineOnly(boolean outlineOnly) {
		throw new RuntimeException("setOutlineOnly is not currently implemented by "+getClass().getSimpleName());
	}

	/**
	 * Set to false by default
	 */
	private boolean isFromServer = false;

	public boolean fromServer() {
		return isFromServer;
	}

	public void setFromServer(boolean isFromServer) {
		 this.isFromServer = isFromServer;
	}
}
