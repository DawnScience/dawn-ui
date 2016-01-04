package org.dawnsci.isosurface.test.marchingCubes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.dawnsci.isosurface.alg.MarchingCubes;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.junit.Test;


public class MarchingCubesAlgorithmTest {

	
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
		assertFalse("Faces length wrong.\n"
					+ "Result: " + Integer.toString(testResult.getFaces().length) + "\n"
					+ "Should be " + knownFaceLength, 
				testResult.getFaces().length != knownFaceLength);
		
		assertFalse("Points length wrong.\n" 
					+ "result: " + testResult.getPoints().length + "\n" 
					+ "Should be " + knownPointLength,
				testResult.getPoints().length != knownPointLength);		
		
		assertFalse("Text length wrong.\n" 
				+ "result: " + testResult.getTexCoords().length + "\n" 
				+ "Should be " + knownTexLength,
			testResult.getTexCoords().length != knownTexLength);	
		
		
		// check data is the same
		Path facesFile = Paths.get("facesResults");
		int [] readFacesFile = (int[]) deserialize(Files.readAllBytes(facesFile));
		
		assertTrue("Faces Results do not equal saved file",
			Arrays.equals(readFacesFile, testResult.getFaces()));
		
		Path pointsFile = Paths.get("pointsResults");
		float [] readPointsFile = (float[]) deserialize(Files.readAllBytes(pointsFile));
		
		
		assertTrue("Points results do not equal saved file",
			Arrays.equals(readPointsFile, testResult.getPoints()));
		
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
	private void writeNewFiles(Surface Result) throws Exception
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
    
    
    
    @Test
    public void RandomNoiseBenchMarkTest() throws Exception
    {
    	int BENCHMARK_TEST_COUNT = 100;
    	
    	start(dataSize, boxSize);
    	
    	
    	System.out.println("++++++ STARTING BENCH MARK TEST ++++++");
    	System.out.println("RUNNING " + BENCHMARK_TEST_COUNT + " TESTS");
    	System.out.println("BOX SIZE - " + Arrays.toString(boxSize));
    	System.out.println("DATA SIZE - " + Arrays.toString(dataSize));
    	
    	double average = 0;
    	for (int i = 0; i < BENCHMARK_TEST_COUNT; i ++)
    	{
    		java.util.Random rnd = new java.util.Random();
    		
    		Random.seed(rnd.nextInt());
    		
    		if (i % 10 == 0)
        	{
        		System.out.print("\n");
        	}
    		double startTime = System.currentTimeMillis();
        	testResult = algorithm.execute(null, null);
        	double completionTime = System.currentTimeMillis() - startTime;
        	average += completionTime;
        	System.out.print(completionTime + ",\t");
        	
    	}
    	
    	average /= BENCHMARK_TEST_COUNT;

    	System.out.println("\n\n++++++ ENDING BENCH MARK TEST ++++++");
    	
    	System.out.println("AVERAGE COMPLETION TIME - " + average);
    	
    	System.out.println("++++++++++++++++++++++++++++++++++++");
    	
    	InetAddress addr;
    	
        addr = InetAddress.getLocalHost();
        java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            	
    	String resultPrint = Double.toString(average);
    	resultPrint += "\t" + Arrays.toString(dataSize) + "\t" + Arrays.toString(boxSize);
    	resultPrint += "\t" + localMachine.getHostName();
    	
    	DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    	Date dateobj = new Date();
    	
    	resultPrint += "\t" + df.format(dateobj);
    	
    	Files.write(Paths.get("Results/random_noise_bench_mark_results.txt"), (resultPrint + "\n").getBytes(), StandardOpenOption.APPEND);
    	
    	System.out.println(resultPrint);
    	
    }
    
    @Test
    public void varrying_BoxSize_RandomNoiseBenchMarkTest() throws Exception
    {
    	this.dataSize = new int[]{100,100,100};
    	for (int i = 20; i > 0; i --)
    	{
    		this.boxSize = new int[]{i,i,i};
        	RandomNoiseBenchMarkTest();
    	}
    }
    
    @Test
    public void varrying_DataSize_RandomNoiseBenchMarkTest() throws Exception
    {
    	this.boxSize = new int[]{4,4,4};
    	for (int i = 50; i < 300; i += 25)
    	{
    		this.dataSize = new int[]{i,i,i};
        	RandomNoiseBenchMarkTest();
    	}
    }
    
    
    
    
    
}
