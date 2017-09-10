package org.dawnsci.dedi.configuration.calculations.results.models;

import org.dawnsci.dedi.configuration.BeamlineConfiguration;
import org.dawnsci.dedi.configuration.calculations.results.controllers.AbstractResultsController;
import org.dawnsci.dedi.configuration.calculations.results.controllers.DefaultResultsController;

/**
 * A service that provides the currently used {@link BeamlineConfiguration} and the associated {@link AbstractResultsController}.
 * It registers the default {@link IResultsModel} model with the controller upon instantiation.
 * This class is a singleton. Access the shared instance via getInstance().
 */
public final class ResultsService {
	private static final ResultsService INSTANCE = new ResultsService();
	
	private AbstractResultsController controller;
	private IResultsModel results;
	private BeamlineConfiguration configuration;
	
	
	private ResultsService(){
		results = new Results();
		configuration = new BeamlineConfiguration();
		controller = new DefaultResultsController(configuration);
		controller.addModel(results);
		controller.update(null, null);
	}
	
	
	public static ResultsService getInstance(){
		return INSTANCE;
	}


	/**
	 * @return The default controller.
	 */
	public AbstractResultsController getController() {
		return controller;
	}


	/**
	 * @return The default {@link IResultsModel}.
	 */
	public IResultsModel getResults() {
		return results;
	}
	
	
	/**
	 * @return The {@link BeamlineConfiguration} instance that this service's {@link IResultsModel} is based on.
	 */
	public BeamlineConfiguration getBeamlineConfiguration(){
		return controller.getBeamlineConfiguration();
	}
}
