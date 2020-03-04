package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.junit.Test;

public class MappingUtilsTest {

	@Test
	public void guessBestShapeLine() {
		
		int[] expected = {1,10};
		int[] expectedreversed = {10,1};
		
		Dataset x = DatasetFactory.createRange(10);
		Dataset y = DatasetFactory.createRange(10);
		
		int[] shape = MappingUtils.guessBestShape(x, y);
		System.out.println("10-point diagonal line");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		//should be square
		assertEquals(shape[0], shape[1]);
		//should be a reasonable size
		assertTrue(shape[0] >= 4 && shape[0] <= 20);
		
		x = DatasetFactory.createRange(10);
		y = DatasetFactory.ones(10);
		
		shape = MappingUtils.guessBestShape(x, y);
		System.out.println("10-point straight line x inc");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expected, shape);
		
		
		x = DatasetFactory.ones(10);
		y = DatasetFactory.createRange(10);
		
		shape = MappingUtils.guessBestShape(x, y);
		System.out.println("10-point straight line y inc");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expectedreversed, shape);
		
	}
	
	@Test
	public void guessBestShapeSpiral() {
		Dataset[] coor = buildSpiral(10, 1, 100);
		int[] shape = MappingUtils.guessBestShape(coor[0], coor[1]);
		System.out.println("spiral");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		//should be a reasonable size
		assertTrue(shape[0] >= 9 && shape[0] <= 20);
		assertTrue(shape[1] >= 9 && shape[1] <= 20);
		
	}
	
	@Test
	public void guessBestShapeGrid() {
		
		int[] expected = {3,5};
		int[] expectedreversed = {5,3};
		
		Dataset[] grid = buildGrid(0, 1, 5, 0, 1, 3, false);
		int[] shape = MappingUtils.guessBestShape(grid[0], grid[1]);
		System.out.println("Grid, 3,5");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expected, shape);
		
		shape = MappingUtils.guessBestShape(grid[1], grid[0]);
		System.out.println("Grid, 5,3");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expectedreversed, shape);
		
		Dataset x = grid[0].getSlice(new Slice(0,12));
		Dataset y = grid[1].getSlice(new Slice(0,12));
		
		shape = MappingUtils.guessBestShape(x, y);
		System.out.println("Partial Grid, 3,5");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expected, shape);
	}
	
	@Test
	public void guessBestShapeSnakeGrid() {
		
		int[] expected = {3,5};
		int[] expectedreversed = {5,3};
		
		Dataset[] grid = buildGrid(0, 1, 5, 0, 1, 3, true);
		int[] shape = MappingUtils.guessBestShape(grid[0], grid[1]);
		System.out.println("snake Grid, 3,5");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expected, shape);
		
		shape = MappingUtils.guessBestShape(grid[1], grid[0]);
		System.out.println("snake Grid, 5,3");
		System.out.println(Arrays.toString(shape));
		assertNotNull(shape);
		assertArrayEquals(expectedreversed, shape);
	}
	
	/**
	 * Archimedean spiral from scanpointgenerator
	 * @param radius
	 * @param scale
	 * @param nPoints
	 * @return
	 */
	private Dataset[] buildSpiral(double radius, double scale, int nPoints) {
		
		double alpha = Math.sqrt(4 * Math.PI);
		double beta = scale / (2 * Math.PI);
		
		Dataset phi = Maths.sqrt(DatasetFactory.createRange(nPoints).iadd(0.5)).imultiply(alpha);
		
		//add center coord here if needed
		Dataset x = Maths.sin(phi).imultiply(phi).imultiply(beta);
		Dataset y = Maths.cos(phi).imultiply(phi).imultiply(beta);
		
		return new Dataset[] {x,y};
		
	}
	
	private Dataset[] buildGrid(double xStart, double xStep, int xNpoints, double yStart, double yStep, int yNpoints, boolean snake) {
		
		int size = xNpoints*yNpoints;
		
		Dataset x = DatasetFactory.zeros(DoubleDataset.class, new int[] {size});
		Dataset y = DatasetFactory.zeros(DoubleDataset.class, new int[] {size});
		
		//assume x is fast
		for (int i = 0 ; i < size ; i++) {
			
			int yLine = (i/xNpoints);
			
			double yp = yLine*yStep + yStart;
			int xFrac = (snake && (yLine+1)%2 == 0) ? (xNpoints - i%xNpoints) : i%xNpoints;
			
			double xp = xStart + xStep*xFrac;
			
			x.setObjectAbs(i, xp);
			y.setObjectAbs(i, yp);
			
		}
		
 		return new Dataset[] {x,y};
		
	}
	
	
}
