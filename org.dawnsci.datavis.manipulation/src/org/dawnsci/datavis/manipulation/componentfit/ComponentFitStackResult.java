package org.dawnsci.datavis.manipulation.componentfit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.datavis.manipulation.DataManipulationUtils;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LinearAlgebra;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentFitStackResult {
	
	private static final Logger logger = LoggerFactory.getLogger(ComponentFitStackResult.class);
	
	private IDataset stack;
	private IDataset components;
	private IDataset concentrations;
	private IDataset residual;
	private IDataset rSquared;
	
	private int sampleDims = 0;
	private int signalDims = 1;
	
	public ComponentFitStackResult(IDataset stack, IDataset components, IDataset concentrations) {
		this.stack = stack;
		this.components = components;
		this.concentrations = concentrations;
		Dataset model = LinearAlgebra.dotProduct(DatasetUtils.convertToDataset(concentrations.getTransposedView()), DatasetUtils.convertToDataset(components));
		this.residual = Maths.subtract(stack, model);
		
		this.rSquared = calculateRSq();
		
		AxesMetadata xm = stack.getFirstMetadata(AxesMetadata.class);
		
		try {
			AxesMetadata rmsax = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			if (xm != null) {
				ILazyDataset[] axes = xm.getAxes();
				if (axes[0] != null) {
					rmsax.setAxis(0, axes[0].getSliceView().squeezeEnds());
				}
			}
			rSquared.setMetadata(rmsax);
			
		} catch (MetadataException e) {
			logger.debug("Problem making axes metdata", e);
		}
	}
	
	public int getSizeOfStack() {
		return stack.getShape()[sampleDims];
	}
	
	public IDataset getConcentrations() {
		return concentrations.getSliceView();
	}
	
	public IDataset getSeriesRSquared() {
		return rSquared;
	}
	
	private IDataset calculateRSq() {
		
		Dataset mean = DatasetUtils.convertToDataset(stack).mean(signalDims,true);
		mean.setShape(new int[] {mean.getSize(),1});
		Dataset ssr = Maths.square(residual).sum(signalDims, true);
		Dataset sst = Maths.square(Maths.subtract(stack, mean)).sum(signalDims, true);
		
		Dataset rsq = Maths.subtract(1,Maths.divide(ssr,sst));
		rsq.setName("R-Sq");
		return rsq;
	}
	
	public ComponentFitResult getData(int number) {
		
		double axisPosition = number;
		
		IDataset data = stack.getSlice(new Slice(number, number+1));
		
		AxesMetadata axmd = data.getFirstMetadata(AxesMetadata.class);
		
		if (axmd != null && axmd.getAxes() != null && axmd.getAxes()[0] != null) {
			try {
				axisPosition = axmd.getAxes()[0].getSlice().getDouble(0,0);
			} catch (Exception e) {
				//ignore, just use index value
			}
		}
				
		data = data.squeeze();
		data.setName("data");
		
		IDataset concs = concentrations.getSlice((Slice)null, new Slice(number, number+1));
		
		IDataset fitted = Maths.multiply(concs, components);
		
		IDataset sum = DatasetUtils.convertToDataset(fitted).sum(sampleDims, true);
		sum.setName("Sum");
		
		IDataset residual = this.residual.getSlice(new Slice(number, number+1)).squeeze();
		residual.setName("Residual");
		
		AxesMetadata axm = data.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] ax = axm.getAxes();
		
		AxesMetadata cm = components.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] cx = cm.getAxes();
		
		List<IDataset> componentResults = new ArrayList<>();
		
		try {
			AxesMetadata mf = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			
			mf.setAxis(0, cx[0].getSliceView());
			mf.setAxis(1, cx[1].getSliceView());
			
			if (ax != null && ax[0] != null) {
				AxesMetadata mr = MetadataFactory.createMetadata(AxesMetadata.class, 1);
				AxesMetadata ms = MetadataFactory.createMetadata(AxesMetadata.class, 1);
				mr.setAxis(0, ax[0].getSliceView());
				ms.setAxis(0, ax[0].getSliceView());
				residual.setMetadata(mr);
				sum.setMetadata(ms);
			}
			
			
			fitted.setMetadata(mf);
			
		} catch (Exception e) {
			logger.debug("Problem making axes metdata", e);
		}
		
		return new ComponentFitResult(data, fitted, sum, residual, componentResults, axisPosition);
		
	}
	
	public Iterator<IDataset> getConcentrationIterator() {
		
		return DataManipulationUtils.getXYIterator(concentrations);
	}

}
