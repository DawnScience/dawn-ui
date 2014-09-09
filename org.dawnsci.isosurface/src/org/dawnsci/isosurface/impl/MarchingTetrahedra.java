package org.dawnsci.isosurface.impl;

import org.dawnsci.isosurface.IsosurfaceGenerator;
import org.dawnsci.isosurface.Surface;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
/**
 * 
 * @author nnb55016
 * Possibly in the future, the Marching Tetrahedra algorithm might be implemented as well.
 */
public class MarchingTetrahedra implements IsosurfaceGenerator {

	@Override
	public void setData(ILazyDataset lazyData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ILazyDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Surface execute() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getIsovalueMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getIsovalueMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setIsovalue(double isovalue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getIsovalue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getBoxSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBoxSize(int[] boxSize) {
		// TODO Auto-generated method stub
		
	}


}
