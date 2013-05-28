package org.dawnsci.algorithm.ui.views.runner;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISourceProvider;

/**
 * Please ensure that your implementation has a no argument constructor.
 * @author fcp94556
 *
 */
public interface IAlgorithmProcessPage {
		
    /**
     * 	
     * @return the title of your custom page. The user will not know what
     * a algorithm is so this should use the language of the custom technique they
     * would like to run.
     */
	public Map<String, String> getTitles();

	/**
	 * Creates the custom UI which will configure algorithm configuration.
	 * 
	 * @param parent
	 * @return the composite you added which will be used for focus. Not the parent.
	 */
	public Composite createPartControl(Composite parent);

	
	/**
	 * Run with the current values.  The context provides a method for running
	 * the algorithm so there is no need to copy algorithm running around.
	 */
	public void run(IAlgorithmProcessContext context) throws Exception;

	
	/**
	 * Optionally implement getSourceProviders() if data binding has been
	 * used in your custom UI. This will be passed back into the run
	 * method to retrieve values. If null, null will be sent to the run
	 * method.
	 * 
	 * @return all the source providers in the custom UI created
	 * in create createPartControl, may be null.
	 * 
	 */
	public ISourceProvider[] getSourceProviders();

	
	/**
	 * Access to the underlying view; so that toolbars and menubars
	 * can be configured for instance.
	 * 
	 * This method will be called after the zero argument constructor at the
	 * start of page creation.
	 * 
	 * @param view
	 */
	public void setAlgorithmView(AlgorithmView view);

	/**
	 * Called when containing view is disposed.
	 */
	public void dispose();

}
