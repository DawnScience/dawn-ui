package org.dawnsci.plotting.api.region;

import java.util.EventListener;

public interface IRegionListener extends EventListener {

	public class Stub implements IRegionListener {

		@Override
		public void regionCreated(RegionEvent evt) {
			update(evt);
		}
		@Override
		public void regionCancelled(RegionEvent evt) {

		}

		@Override
		public void regionAdded(RegionEvent evt) {
			update(evt);
		}

		@Override
		public void regionRemoved(RegionEvent evt) {
			update(evt);
		}

		@Override
		public void regionsRemoved(RegionEvent evt) {
			update(evt);
		}

		protected void update(RegionEvent evt) {
			// Nothing
		}
	}

	/**
	 * Called when region created.
	 * @param evt
	 */
	void regionCreated(final RegionEvent evt);
	
	/**
	 * Called when a region add that was happening (with a cursor change)
	 * has been called (because there was a zoom for instance).
	 * @param evt
	 */
	void regionCancelled(final RegionEvent evt);
	
	/**
	 * Called when region added to graph.
	 * @param evt
	 */
	void regionAdded(final RegionEvent evt);

	/**
	 * Called when region removed from graph.
	 * @param evt
	 */
	void regionRemoved(final RegionEvent evt);

    /**
     * Fired when all the regions are removed in one go.
     * @param evt
     */
	void regionsRemoved(final RegionEvent evt);

}
