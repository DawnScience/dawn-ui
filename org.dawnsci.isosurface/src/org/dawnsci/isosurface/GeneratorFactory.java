package org.dawnsci.isosurface;

import org.dawnsci.isosurface.impl.MarchingCubes;
import org.dawnsci.isosurface.impl.MarchingTetrahedra;

/**
 * Factory to generate algorithms for isosurface definition.
 * @author nnb55016
 *
 */
public class GeneratorFactory {

	
	public static IsosurfaceGenerator createMarchingCubes() {
		return new MarchingCubes();
	}
	
	public static IsosurfaceGenerator createMarchingTetrahedra() {
		return new MarchingTetrahedra();
	}

}
