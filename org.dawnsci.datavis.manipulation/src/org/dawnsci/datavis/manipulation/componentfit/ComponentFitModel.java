package org.dawnsci.datavis.manipulation.componentfit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.api.IXYData;
import org.dawnsci.datavis.api.utils.DataPackageUtils;
import org.dawnsci.datavis.manipulation.DataManipulationUtils;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

/**
 * Perform a linear least squares component fit on a stack of data
 * <p>
 * Created using the {@link #buildModel(List)} method on a list of {@link DataOptions}.
 * <p>
 * List must contain 1 2D dataset (plotted as a line - the data to be fitted to) and n 1D datasets,
 * with both the file and the dataset selected.
 *
 */
public class ComponentFitModel {
	
	private IDataset stack;
	private IDataset components;
	
	private ComponentFitModel(IDataset stack, IDataset components) {
		this.stack = stack;
		this.components = components;
	}
	
	
	public static ComponentFitModel buildModel(List<DataOptions> options) throws IllegalArgumentException, DatasetException {
		
		List<IDataPackage> components = new ArrayList<>();
		
		DataOptions stack = null;
		LoadedFile stackFile = null;
		
		//search for n 1D datasets and 1 nD
		for (DataOptions op : options) {
			
			int[] shape = op.getLazyDataset().getShape();
			
			int[] squeezeShape = ShapeUtils.squeezeShape(shape, false);
			
			int rank = squeezeShape.length;
			
			if (rank == 0) throw new IllegalArgumentException("Cannot component fit with a single scalar dataset");
			
			if (rank == 1) {
				components.add(op);
			} else {
				
				if (stack == null) {
					stack = op;
					stackFile = op.getParent();
				} else {
					if (stackFile != op.getParent()) {
						throw new IllegalArgumentException("Cannot component fit with more than 1 stack dataset");
					}
				}
			}
		}
		
		ILazyDataset lz = stack.getLazyDataset();
		
		if (lz.getRank() != 2) {
			throw new IllegalArgumentException("Data stack must have rank 2");
		}
		
		List<IXYData> xyData = DataPackageUtils.getXYData(components);
		
		if (requiresTranspose(stack)) {
			lz = lz.getTransposedView();
		}
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(stack.getLazyDataset());
		int[] rangeIndices = new int[2];
		
		//TODO dont assume rank-1
		IDataset x = ax[lz.getRank()-1];
		if (x == null) {
			int[] shape = lz.getShape();
			x = DatasetFactory.createRange(shape[shape.length-1]);
		}
		List<IXYData> compatibleDatasets = DataManipulationUtils.getCompatibleDatasets(xyData, x, rangeIndices);
		IDataset comps  = DataManipulationUtils.combine(compatibleDatasets);
		
		//TODO not just use fast axes, get axes from NDimensions
		
		
		
		SliceND s = new SliceND(lz.getShape());
		s.setSlice(lz.getRank()-1, rangeIndices[0], rangeIndices[1], 1);
		
		IDataset dataset = lz.getSlice(s);
		
		return new ComponentFitModel(dataset, comps);
	}
	
	private static boolean requiresTranspose(DataOptions o) throws IllegalArgumentException {
		
		Object[] dimensionOptions = o.getPlottableObject().getNDimensions().getOptions();
		
		IPlotMode plotMode = o.getPlottableObject().getPlotMode();		
		
		if (plotMode.getOptions().length == 1) {
			
			String testOption = plotMode.getOptions()[0];
			
			if (dimensionOptions[0].equals(testOption)) {
				return true;
			}
			
			if (dimensionOptions[1].equals(testOption)) {
				return false;
			}
		}
		
		throw new IllegalArgumentException("Could not determine stack orientation!");
	}
	
	public IDataset getComponents() {
		return components.getSliceView();
	}
	
	public ComponentFitStackResult runFit() {
	
		RealMatrix compMat = new Array2DRowRealMatrix((double [][]) DatasetUtils.createJavaArray(DatasetUtils.cast(DoubleDataset.class, components)));
		compMat = compMat.transpose();
		
		RealMatrix seriesMat = new Array2DRowRealMatrix((double [][]) DatasetUtils.createJavaArray(DatasetUtils.cast(DoubleDataset.class, stack)));
		seriesMat = seriesMat.transpose();
		
		DecompositionSolver solver = new SingularValueDecomposition(compMat).getSolver();
		RealMatrix solution = solver.solve(seriesMat);
		
		Dataset result = DatasetFactory.createFromObject(solution.getData());
		
		AxesMetadata samd = stack.getFirstMetadata(AxesMetadata.class);
		AxesMetadata camd = components.getFirstMetadata(AxesMetadata.class);
		
		try {
			AxesMetadata rmd = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			ILazyDataset ax1 = samd.getAxes()[0].getSliceView().squeezeEnds();
			ILazyDataset ax0 = camd.getAxes()[0].getSliceView().squeezeEnds();
			
			rmd.setAxis(0, ax0);
			rmd.setAxis(1, ax1);
			result.setMetadata(rmd);
			
		} catch (Exception e) {
			//do nothing
		}
		
		return new ComponentFitStackResult(stack,components,result);
	}

}
