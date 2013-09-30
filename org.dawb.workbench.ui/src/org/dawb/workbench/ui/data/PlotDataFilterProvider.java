package org.dawb.workbench.ui.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.python.PyDevUtils;
import org.dawb.common.python.PyDevUtils.AvailableInterpreter;
import org.dawb.common.python.rpc.PythonRunScriptService;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.filter.IPlottingFilter;
import org.dawnsci.plotting.api.filter.NamedPlottingFilter;
import org.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.python.pydev.plugin.nature.PythonNature;
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
				
		final String interpreterName = getInterpreterName(ob, res);
		
		AvailableInterpreter info = PyDevUtils.getMatchingChoice(interpreterName, res.getProject());

		// TODO Allow debug of filter script one day
		final PythonRunScriptService pythonRunScriptService = PythonRunScriptService.getService(false/* TODO */, res.getProject(), info.info);
		
		PythonPlottingFilter filter = new PythonPlottingFilter(ob.getName(), res, pythonRunScriptService, filterRank);
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


	@SuppressWarnings("unused")
	private String getInterpreterName(ITransferableDataObject ob, IResource res) throws Exception {
		
		final IProject project = res.getProject();
		final String[] interpreters = PyDevUtils.getChoices(true, project);

		if (project == null) throw new Exception("The script '"+ob.getFilterPath()+"' is not valid, because it has no project!");

		try {
		    PythonNature nature = (PythonNature)project.getNature(PythonNature.PYTHON_NATURE_ID);
		    String name = nature.getProjectInterpreterName();
		    if (name==null) throw new Exception();
		    
		    for (String interpreter : interpreters) {
				if (interpreter.startsWith(name+" - ")) return interpreter;
			}
		    
		    return name;
		} catch (Exception ne) {
			if (interpreters==null || interpreters.length<1) {
				throw new Exception("A Pydev interpreter cannot be found to run '"+ob.getFilterPath()+"'.\nPlease define an intertpreter under 'Window->Preferences->Pydev Interpreter'.");
			}
			if (interpreters!=null) {
				logger.error("Cannot determine interpreter to use for project "+project+". Using '"+interpreters[0]+"'.");
				return interpreters[0];
			}
		}
		throw new Exception("A Pydev interpreter cannot be found to run '"+ob.getFilterPath()+"'.\nPlease define an intertpreter under 'Window->Preferences->Pydev Interpreter'.");
	}
	
	/**
	 * A named filter able to deal with 1D and 2D data.
	 * 
	 * @author fcp94556
	 *
	 */
	public class PythonPlottingFilter extends NamedPlottingFilter {

		private IResource               file;
		private PythonRunScriptService  service;
		private int                     filterRank;

		public PythonPlottingFilter(String name, IResource file, PythonRunScriptService pythonRunScriptService, int filterRank) {
			super(name);
			this.file       = file;
			this.service    = pythonRunScriptService;
			this.filterRank = filterRank;
		}

		@Override
		public int getRank() {
			return filterRank; 
		}
		
		@Override
		protected IDataset[] filter(IDataset x,    IDataset y) {
			
			try {
				final Map<String, IDataset> data = new HashMap<String, IDataset>(2);
				data.put("x", x);
				data.put("y", y);
				
				final Map<String, ? extends Object> result = service.runScript(file.getLocation().toOSString(), data);
				
				return new IDataset[]{(IDataset)result.get("x"), (IDataset)result.get("y")};
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 1D filter, wrong implementation of method.");
				return new IDataset[]{x,y};
			}

		}
		
		@Override
		protected Object[] filter(IDataset image,    List<IDataset> axes) {
			
			try {
				final Map<String, IDataset> data = new HashMap<String, IDataset>(2);
				data.put("image", image);
				data.put("xaxis", axes!=null&&axes.size()==2 ? axes.get(0): null);
				data.put("yaxis", axes!=null&&axes.size()==2 ? axes.get(1): null);
				
				final Map<String, ? extends Object> result = service.runScript(file.getLocation().toOSString(), data);
				
				image = (IDataset)result.get("image");
				IDataset xAxis = (IDataset)result.get("xaxis");
				IDataset yAxis = (IDataset)result.get("yaxis");
				return new Object[]{image,Arrays.asList(new IDataset[]{xAxis, yAxis})};
				
			} catch (Exception ne) {
				logger.debug("Cannot use "+file.getName()+" for 2D filter, wrong implementation of method.");
				return new Object[]{image,axes};
			}

		}

	}

}
