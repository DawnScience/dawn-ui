package org.dawnsci.slicing.api.system;

import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;

/**
 * 
 * A generic interface for slicing data. The inspector must be able 
 * to create a UI for slicing which is added to the SWT part. It is
 * provided from the loader with an ILazyDataset of the whole data
 * and provides a mechanism for slicing the lazy dataset.
 * 
 * The inspector may be extended to have additional tools. When chosen these
 * tools may replace the original plotting system with a custom one for more
 * flexible slicing.
 * 
 * <code>
 * ISliceComponent slicer = SlicerFactory.createSliceComponent();
 * //Optional
 * slicer.setPlottingSystem(...); // The plotting system which you will plot slices on to.
 * sliceComponent.addCustomAction(...);
 * 
 * // Essential
 * slicer.createPartControl(...)
 * 
 * // Essential
 * SliceSource source = new SliceSource(...)
 * slicer.setData(source);
 * 
 * 
 * //UI created and can now slice things.
 * 
 * </code>
 * 
 * @author fcp94556
 *
 */
public interface ISliceSystem {
	
	/**
	 * Main method for creating a part that can slice data.
	 * Once called the UI for slicing is created.
	 * @param parent
	 * @return
	 */
	public Control createPartControl(Composite parent);
	
	/**
	 * Set the data in the form of a source which provides the ILazyDataset
	 * @param source
	 */
	public void setData(SliceSource source);
	
	/**
	 * 
	 * @param system
	 */
	public void setPlottingSystem(IPlottingSystem system);
	
	/**
	 * 
	 * @return system
	 */
	public IPlottingSystem getPlottingSystem();
	
	/**
	 * 
	 * @return the current data which we are slicing.
	 */
	public SliceSource getData();
	
	/**
	 * Set the way that the slicing should be set up in the slice system.
	 */
	public void setDimsDataList(DimsDataList sliceSetup);

	/**
	 * 
	 * @return an object which provides information about how the user
	 * set up the slice in the UI.
	 */
	public DimsDataList getDimsDataList();
	
	/**
	 * A name describing the current slice 
	 * @return
	 */
	public String getSliceName();


	/**
	 * To change the slice value by data index and optionally replot a slice
	 * call this method.
	 * 
	 * @param dimension to slice (if slicing it already)
	 * @param index
	 * @param doReslice - true to replot the slice straight away.
	 * @throws IndexOutOfBoundsException if the dimension is not already being
	 *         sliced or the index is outside the size of the dimension.
	 */
	public void setSliceIndex(int dimension, int index, boolean doReslice);

	/**
	 * The slicer should be disposed when finished with and once this is called it
	 * will no longer be usable. 
	 */
	public void dispose();

	/**
	 * Call to reprocess a slice where the slice system
	 * is slicing using a standard tool like line or image.
	 */
	public void update();

	/**
	 * Refresh the widget showing the slice setup.
	 * Can be used after editing the dimensional data list
	 * to define the slice.
	 */
	public void refresh();

	/**
	 * Set the id of the IViewPart which implements ISliceGallery to be used
	 * as a gallery view of the dataset which is being sliced.
	 * 
	 * @param sliceGalleryId
	 */
	public void setSliceGalleryId(String sliceGalleryId);

	/**
	 * 
	 * @param false cancels any slice and sets this component invisible.
	 */
	public void setVisible(boolean vis);

	/**
	 * Set if the user should be able to enter slice ranges that average or sum several slices.
	 * @param b
	 */
	public void setRangesAllowed(boolean allowed);

	/**
	 * If the slice component has specialist slice actions in a toolbar
	 * or tabbed panes. This will disable or enable the actions.
	 * @param b
	 */
	public void setSliceActionsEnabled(boolean enabled);
	
	/**
	 * Sets if slicing is allowed in the slice widget. If set to false
	 * the slice widget (e.g. a table) will become deactivated.
	 * @param enabled
	 */
	public void setSlicingEnabled(boolean enabled);

	/**
	 * Sets a string to be used as explaination text somewhere in the system depending
	 * on implementation.
	 * 
	 * @param string
	 */
	public void setLabel(String string);

	/**
	 * Sets wether the slice component should show axes or not.
	 * @param b
	 */
	public void setAxesVisible(boolean vis);

	/**
	 * Add extra action(s) which should appear on the toolbar of the slicer. 
	 * @param action
	 */
	public void addCustomAction(IAction action);

	/**
	 * Add a listener to be notified when the dimensions change.
	 * @param l
	 */
	public void addDimensionalListener(DimensionalListener l);
	
	/**
	 * Remove a DimensionalListener
	 * @param l
	 */
	public void removeDimensionalListener(DimensionalListener l);

	/**
	 * The names (nexus path to axis normally) of the axis by dimension
	 * number.
	 * 
	 * @return map of axes.
	 */
	public Map<Integer, String> getAxesNames();
	
	/**
	 * 
	 * @return the slice object used to produce the current slice.
	 */
	public SliceObject getCurrentSlice();

	/**
	 * 
	 * @return
	 */
	public Enum getSliceType();

	/**
	 * Normally one of the PlotType enums
	 * @param plotType
	 */
	public void setSliceType(Enum type);

}
