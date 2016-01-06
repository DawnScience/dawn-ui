package org.dawnsci.isosurface.test.marchingCubes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.junit.Test;


public class Regression {

	
	static int SEED = 123456789;
	
	// benchmarking
	int[] dataSize = {100,100,100};
	int[] boxSize = {4,4,4};
	
	// general
	MarchingCubes algorithm;
	ILazyDataset lz;
	MarchingCubesModel model;
	Surface testResult;
	
	/**
	 * used to initialise the required information
	 * might have to copy and paste to increase coupling
	 */
	private void start(int[] dataSetSizeXYZ, int[] boxSizeXYZ)
	{
		algorithm = new MarchingCubes();
		
		lz = Random.lazyRand(dataSetSizeXYZ);
		
		Random.seed(SEED);
		
		model = new MarchingCubesModel();
		model.setLazyData(lz);
		model.setBoxSize(boxSizeXYZ);
		model.setIsovalue(0.5);
		model.setVertexLimit(Integer.MAX_VALUE);
		
		algorithm.setModel(model);
		
		// execute the algorithmA
		testResult = algorithm.execute(null, null);
	}
	
	@Test
	public void regressionTest() throws Exception
	{
		start(new int[]{100,100,100},new int[] {3,3,3});
		
		// check the algorithm gave the same results as before

		int knownFaceLength = 606870;       // hard coded if seed changes these will also need to be changed
		int knownPointLength = 245754;      // hard coded if seed changes these will also need to be changed
		int knownTexLength = 6;             // hard coded if seed changes these will also need to be changed
		
		// check lengths first
		assertFalse("Points length wrong.\n" 
					+ "result: " + testResult.getPoints().length + "\n" 
					+ "Should be " + knownPointLength,
				testResult.getPoints().length != knownPointLength);		
		
		assertFalse("Faces length wrong.\n"
				+ "Result: " + Integer.toString(testResult.getFaces().length) + "\n"
				+ "Should be " + knownFaceLength, 
			testResult.getFaces().length != knownFaceLength);
	
		assertFalse("Text length wrong.\n" 
				+ "result: " + testResult.getTexCoords().length + "\n" 
				+ "Should be " + knownTexLength,
			testResult.getTexCoords().length != knownTexLength);
						
		// check data is the same
		Path pointsFile = Paths.get("pointsResults");
		float [] readPointsFile = (float[]) deserialize(Files.readAllBytes(pointsFile));
		assertTrue("Points results do not equal saved file"
				+ "\nIndicates vertex triangle creation could be wrong",
			Arrays.equals(readPointsFile, testResult.getPoints()));
		
		
		Path facesFile = Paths.get("facesResults");
		int [] readFacesFile = (int[]) deserialize(Files.readAllBytes(facesFile));
		assertTrue("Faces Results do not equal saved file"
					+ "\nIndicates vertex indexing could be wrong",
			Arrays.equals(readFacesFile, testResult.getFaces()));
		
				
		Path texFile = Paths.get("texResults");
		float [] readTexFile = (float[]) deserialize(Files.readAllBytes(texFile));
		assertTrue("Tex results do not equal saved file",
			Arrays.equals(readTexFile, testResult.getTexCoords()));
		
		
	}

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }
		
	private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
	}
	
	/**
	 * call if you want to test a new seed
	 */
	public void writeNewFiles(Surface Result) throws Exception
	{
		byte facesData[] = serialize(Result.getFaces());
		Path facesFile = Paths.get("facesResults");
		
		Files.createFile(facesFile);
		Files.write(facesFile, facesData);
		
		byte pointsData[] = serialize(Result.getPoints());
		Path pointsFile = Paths.get("pointsResults");
		Files.createFile(pointsFile);
		Files.write(pointsFile, pointsData);
		
		
		byte texData[] = serialize(Result.getTexCoords());
		Path texFile = Paths.get("texResults");
		Files.createFile(texFile);
		Files.write(texFile, texData);
		
	}
    
    
    
    
    
}
