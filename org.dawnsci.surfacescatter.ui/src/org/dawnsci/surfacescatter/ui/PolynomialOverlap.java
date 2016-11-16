package org.dawnsci.surfacescatter.ui;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

public class PolynomialOverlap {

	
	public static double correctionRatio(Dataset[] xLowerDataset, Dataset yLowerDataset,
			Dataset[] xHigherDataset, Dataset yHigherDataset, double attenuationFactor) {
	
	
		Polynomial polyFitLower = Fitter.polyFit(xLowerDataset, yLowerDataset, 1e-5,4);
		Polynomial polyFitHigher = Fitter.polyFit(xHigherDataset, yHigherDataset, 1e-5,4);
		
		Dataset calculatedValuesHigher = polyFitHigher.calculateValues(xLowerDataset);
		Dataset calculatedValuesLower = polyFitLower.calculateValues(xLowerDataset);
		
		Dataset correctionsRatioDataset = Maths.divide(calculatedValuesLower.sum(), 
				calculatedValuesHigher.sum());
		
		
		double correction = ((double) correctionsRatioDataset.sum())/((double) correctionsRatioDataset.getSize())*attenuationFactor;
		
		
		return correction;
	}
	
}
