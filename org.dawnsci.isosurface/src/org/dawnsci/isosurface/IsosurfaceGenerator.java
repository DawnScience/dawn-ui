package org.dawnsci.isosurface;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;



/**
 * This interface represents an algorithm to define an isosurface from data.
 * @author nnb55016
 *
 */
public interface IsosurfaceGenerator {

	/**
	 * Performs the whole algorithm *expensive*
	 * @return a Surface object defined by the three arrays that JavaFX uses to create the triangular mesh
	 */
	public Surface execute() throws Exception;

	/**
	 * Initialises the data in the algorithm
	 */
	public void setData(ILazyDataset lazyData);
	
	/**
	 * Initialises the data in the algorithm
	 */
	public ILazyDataset getData();
			
	/**
	 * Minimum allowed isoValue (calculated from data)
	 * @return
	 */
	public double getIsovalueMin();
	
	/**
	 * Maximum allowed isoValue (calculated from data)
	 * @return
	 */
	public double getIsovalueMax();
	
	/**
	 * The current isovalue
	 * @param isovalue
	 */
	public void setIsovalue(double isovalue);

	/**
	 * Iso value
	 * @return
	 */
	public double getIsovalue();

	/**
	 * Box size
	 * @return
	 */
	public int[] getBoxSize();

	/**
	 * Box size
	 * @param boxSize
	 */
	public void setBoxSize(int[] boxSize);
	
}
