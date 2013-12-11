package org.dawnsci.algorithm.ui.views.runner;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

/**
 * Please ensure that your implementation has a no argument constructor.
 * @author fcp94556
 *
 */
public interface IAlgorithmProcessPage extends IAdaptable {

    /**
     * Initializes this view with the given view site.  A memento is passed to
     * the view which contains a snapshot of the views state from a previous
     * session.  Where possible, the view should try to recreate that state
     * within the part controls.
     * <p>
     * This method is automatically called by the workbench shortly after the part 
     * is instantiated.  It marks the start of the views's lifecycle. Clients must 
     * not call this method.
     * </p>
     *
     * @param site the view site
     * @param memento the IViewPart state or null if there is no previous saved state
     * @exception PartInitException if this view was not initialized successfully
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException;

    /**
     * Saves the object state within a memento.
     *
     * @param memento a memento to receive the object state
     */
    public void saveState(IMemento memento);

    /**
     * 	
     * @return the title of your custom page. The user will not know what
     * a algorithm is so this should use the language of the custom technique they
     * would like to run.
     */
	public String getTitle();

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
	public void setAlgorithmView(IViewPart view);

	/**
	 * Called when containing view is disposed.
	 */
	public void dispose();

}
