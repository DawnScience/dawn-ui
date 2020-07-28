package org.dawnsci.multidimensional.ui.imagecuts;

import java.util.EventListener;

import org.eclipse.january.dataset.IDataset;

/**
 * Listener to report the results of a cut event
 *
 */
public interface PerpendicularCutsListener extends EventListener {

	public void cutProcessed(IDataset xCut, IDataset yCut, double intersectionSum, CutData[] cuts);

}
