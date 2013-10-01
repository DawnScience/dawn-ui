package org.dawb.workbench.ui.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.filter.IPlottingFilter;
import org.dawnsci.plotting.api.filter.NamedPlottingFilter;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Class to manage filters defined by the user.
 * 
 * These filters have a pydev interpreter which runs the user defined filter.
 * 
 * @author fcp94556
 *
 */
final class PlotDataFilterProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(PlotDataFilterProvider.class);
	
	private IFilterDecorator             decorator;
	private Map<String, IPlottingFilter> filters;

	public PlotDataFilterProvider(IPlottingSystem system) {
		this.decorator = PlottingFactory.createFilterDecorator(system);
		decorator.setActive(false);
		filters = new HashMap<String, IPlottingFilter>();
	}

	/**
	 * 
	 * @param ob
	 * @throws Exception with user readable message describing why filter is not valid.
	 */
	public void createFilter(ITransferableDataObject ob, int filterRank) throws Exception {
		
		if (ob.getFilterPath()==null) throw new Exception("There must be a filter path to a python script in order for a filter to be applied!");
		
		final IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(ob.getFilterPath());
		if (res==null) throw new Exception("The script '"+ob.getFilterPath()+"' cannot be found!");
				
        // TODO use constructor with true which offers to configure a python interpreter if none exists.
		final AnalysisRpcPythonPyDevService service = new AnalysisRpcPythonPyDevService(res.getProject());
		service.addHandlers("execfile('''" + res.getLocation().toPortableString() + "''')", new String[]{"filter1D", "filter2D"});
		
		// TODO FIXME Service must be stopped when data is unplotting
		
		IPythonFilter newProxyInstance = service.getClient().newProxyInstance(IPythonFilter.class);
		
		PythonPlottingFilter filter = new PythonPlottingFilter(ob.getName(), res, newProxyInstance, filterRank);
		filters.put(ob.getName(), filter);
		decorator.addFilter(filter);
		decorator.setActive(true);
	}
	

	public void deleteFilter(ITransferableDataObject ob) {
		IPlottingFilter filter = filters.get(ob.getName());
		if (filter==null) return;
		filter.setActive(false);
		decorator.removeFilter(filter);
	}

	
	/**
	 * A named filter able to deal with 1D and 2D data.
	 * 
	 * @author fcp94556
	 *
	 */
	public class PythonPlottingFilter extends NamedPlottingFilter {

		private IResource               file;
		private IPythonFilter  pythonProxy;
		private int                     filterRank;

		public PythonPlottingFilter(String name, IResource file, IPythonFilter pythonProxy, int filterRank) {
			super(name);
			this.file       = file;
			this.pythonProxy    = pythonProxy;
			this.filterRank = filterRank;
		}

		@Override
		public int getRank() {
			return filterRank; 
		}
		
		@Override
		protected IDataset[] filter(IDataset x,    IDataset y) {
			try {
				return pythonProxy.filter1D(x, y);
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 1D filter, wrong implementation of method.");
				return new IDataset[]{x,y};
			}
		}
		
		@Override
		protected Object[] filter(IDataset image,    List<IDataset> axes) {
			try {
				IDataset xaxis =  axes!=null&&axes.size()==2 ? axes.get(0): null;
				IDataset yaxis =  axes!=null&&axes.size()==2 ? axes.get(1): null;
				IDataset[] data = pythonProxy.filter2D(image, xaxis, yaxis);
				
				return new Object[]{data[0], Arrays.asList(data[0], data[1])};
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 1D filter, wrong implementation of method.");
				return new Object[]{image,axes};
			}
		}

	}

}
