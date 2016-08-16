package org.dawnsci.spectrum.ui.ReflectivityUI;

import java.util.ArrayList;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class DatasetSplicer {

	
	public static IDataset[] datasetSplice(ArrayList<ILazyDataset> arrayILDy,
			ArrayList<ILazyDataset> arrayILDx){
				
		IDataset[] output = new IDataset[2];
		int probe = 0;
		ArrayList<int[]> usedPoints = new ArrayList<>();
		int[] nextPoint = new int[2];
		
		
		for(int k=0; k<arrayILDx.size(); k++){
			for(int l=0; l<arrayILDx.get(k).getSize(); l++){
				probe++;
			}
		}
		
		
		IDataset xOutput = DatasetFactory.ones(probe);
		IDataset yOutput = DatasetFactory.ones(probe);
		
		int[] testPointPos = new int[2];
		
		
		
		for(int k=0; k<arrayILDx.size(); k++){
			
			SliceND slice =new SliceND(arrayILDx.get(k).getShape());
			
			IDataset testPointIDataset = null;
			
			
			try {
				testPointIDataset = arrayILDx.get(k).getSlice(slice);
			} catch (DatasetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int l=0; l<arrayILDx.get(k).getSize(); l++){
				
				double testPoint = testPointIDataset.getDouble(l);
				testPointPos[0] = k;
				testPointPos[1] = l;
				

				if (arrayCheck(usedPoints, testPointPos) == false){
					nextPoint = recursivePointTest( arrayILDx, usedPoints,
							 testPoint, testPointPos, probe);		
					}
				
				try {
					xOutput.set(arrayILDx.get(nextPoint[0]).getSlice(new SliceND(arrayILDx.get(nextPoint[0]).getShape())).getDouble(nextPoint[1]),k);
					yOutput.set(arrayILDy.get(nextPoint[0]).getSlice(new SliceND(arrayILDy.get(nextPoint[0]).getShape())).getDouble(nextPoint[1]),k);
				} catch (DatasetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				usedPoints.add(nextPoint);
			
			}
		}
			
		if (usedPoints.size() != probe){
			System.out.println("Didn't add up..");
		}
		output[0]=xOutput;
		output[1]=yOutput;
		
		
		return output;
			
			
	}
	
	
//////////////////////////////////////	
	
	public static IDataset[] datasetSplice1(ArrayList<IDataset> arrayILDy,
			ArrayList<IDataset> arrayILDx){
				
		IDataset[] output = new IDataset[2];
		int probe = 0;
		
		
		
		for(int k=0; k<arrayILDx.size(); k++){
			for(int l=0; l<arrayILDx.get(k).getSize(); l++){
				probe++;
			}
		}
		
		int[][] nextPoint = new int[probe][2];
		int[][] usedPointsArr = new int[probe][2];
		
		
		int kset = 0;
		
		IDataset xOutput = DatasetFactory.ones(probe);
		IDataset yOutput = DatasetFactory.ones(probe);
		
		int[] testPointPos = new int[2];
		
		for(int k=0; k<arrayILDx.size(); k++){
			
			IDataset testPointIDataset = null;
			
			testPointIDataset = arrayILDx.get(k);
			
			for(int l=0; l<arrayILDx.get(k).getSize(); l++){
				
				double testPoint = testPointIDataset.getDouble(l);
				testPointPos[0] = k;
				testPointPos[1] = l;
				
				if (k==0 && l==20){
					System.out.println("k=0, l=20, interestng");
				}
				if (arrayCheck1(usedPointsArr, testPointPos) == false){
					
					nextPoint[kset] = (recursivePointTest1( arrayILDx, usedPointsArr,
							 testPoint, testPointPos, probe)).clone();		
					
					xOutput.set(arrayILDx.get(nextPoint[kset][0]).getDouble(nextPoint[kset][1]),kset);
					yOutput.set(arrayILDy.get(nextPoint[kset][0]).getDouble(nextPoint[kset][1]),kset);
					usedPointsArr[kset]=(nextPoint[kset].clone());
					kset++;
				}
				
				else{
					
					xOutput.set(arrayILDx.get(testPointPos[0]).getDouble(testPointPos[1]),kset);
					yOutput.set(arrayILDy.get(testPointPos[0]).getDouble(testPointPos[1]),kset);
					usedPointsArr[kset]=(testPointPos.clone());
					kset++;
				}
				

			}
		}
			
		if (usedPointsArr.length != probe){
			System.out.println("Didn't add up..");
		}
		output[1]=xOutput;
		output[0]=yOutput;
		
		
		return output;
			
			
	}
	
	
	
	
	public static IDataset[] datasetSplice(IDataset[][] input){
				
		
		IDataset[] xArrayCorrected = input[1]; 
		IDataset[] yArrayCorrected = input[0];
		
		
		ArrayList<IDataset> arrayIDx = new ArrayList<IDataset>();
		ArrayList<IDataset> arrayIDy = new ArrayList<IDataset>();
		
		for (int k = 0; k<input[0].length;k++){
			arrayIDx.add(xArrayCorrected[k]);
			arrayIDy.add(yArrayCorrected[k]);
		}
		
		IDataset[] output =datasetSplice1(arrayIDy, arrayIDx);
		return output;
	}
		
		
	public static int[] recursivePointTest(ArrayList<ILazyDataset> arrayILDx, ArrayList<int[]> usedPoints,
			double testPoint, int[] testPointPos, int probe){
		
		IDataset testPointIDataset = null;
		int[] newTestPointPos = new int[2];
		double newTestPoint = 0;
		//int[] currentPointPos = new int[2];
		
		int loopCounter = 0;
		
		for(int k=0; k<arrayILDx.size(); k++){
			SliceND slice =new SliceND(arrayILDx.get(k).getShape());
			try {
				testPointIDataset = arrayILDx.get(k).getSlice(slice);
			} catch (DatasetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int l=0; l<arrayILDx.get(k).getSize(); l++){
				
				if (testPointPos[0] !=k && testPointPos[1] !=l 
						&& arrayCheck(usedPoints, testPointPos) == false){
				
					if (testPointIDataset.getDouble(l) < testPoint){
						newTestPointPos[0] = k;
						newTestPointPos[1] = l;
						newTestPoint = testPointIDataset.getDouble(l);
						testPointPos = recursivePointTest(arrayILDx, usedPoints, newTestPoint, newTestPointPos, probe);
						loopCounter++;
						if (loopCounter >= probe){
							return null;
						}
					}
					
					else{
						return testPointPos;
					}
				}
			}
		}
		return testPointPos;
	}
	
	public static int[] recursivePointTest1(ArrayList<IDataset> arrayILDx, int[][] usedPointsArr,
			double testPoint, int[] testPointPos, int probe){
		
		IDataset testPointIDataset = null;
		int[] newTestPointPos = new int[2];
		double newTestPoint = 0;
		//int[] currentPointPos = new int[2];
		
		
		//if (arrayCheck1(usedPointsArr, testPointPos) == false){
			int loopCounter = 0;
			
			for(int k=0; k<arrayILDx.size(); k++){
				
					testPointIDataset = arrayILDx.get(k);
					if (k==1){
						System.out.println("K=1, pay attention");
					}
				for(int l=0; l<arrayILDx.get(k).getSize(); l++){
					
					if (k==1 && l== 0){
						System.out.println("K=1, l=0, pay attention");
					}
					
					
					if ((testPointPos[0] !=k | testPointPos[1] !=l) 
							&& arrayCheck1(usedPointsArr, testPointPos) == false){
					
						if (testPointIDataset.getDouble(l) < testPoint){
							newTestPointPos[0] = k;
							newTestPointPos[1] = l;
							newTestPoint = testPointIDataset.getDouble(l);
							if (arrayCheck1(usedPointsArr, newTestPointPos) == false){
								int[] testPointPos1 = recursivePointTest1(arrayILDx, usedPointsArr, newTestPoint, newTestPointPos, probe);
								return testPointPos1;
							}
							loopCounter++;
							if (loopCounter >= probe){
								return null;
							}
						}
						
						else{
							return testPointPos;
						}
					}
				}
			}
		//}
		return testPointPos;
	}
	
	
			
			
	public static boolean arrayCheck(ArrayList<int[]> usedPoints, int[] testpoint){
		
		boolean output = false;
		
		for(int k=0; k< usedPoints.size(); k++){
			int[] checkEntry = usedPoints.get(k);
			if (checkEntry[0] == testpoint[0] && checkEntry[1] == testpoint[1]){
				output = true;
				return output;
			}
		}
		
		return output;
	}
	
	public static boolean arrayCheck1(int[][] usedPointsArr, int[] testpoint){
		
		boolean output = false;
		
		for(int k=0; k< usedPointsArr.length; k++){
			int[] checkEntry = usedPointsArr[k];
			if (checkEntry[0] == testpoint[0] && checkEntry[1] == testpoint[1]){
				output = true;
				return output;
			}
		}
		
		return output;
	}
}
