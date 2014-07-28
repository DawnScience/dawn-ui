package org.dawnsci.common.widgets.table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * An interface for providing fitting functions by providing the name and
 * the function itself when required
 *
 */
public interface ISeriesItemDescriptor extends IAdaptable {


	/**
	 * Instantiate the function/operation/operator (if it is null)
	 * and return it. Otherwise return the current version.
	 * 
	 * @return the function
	 * @throws InstantiationException
	 */
	Object getSeriesObject() throws InstantiationException;

	/**
	 * Provides the function name
	 * @return String name
	 */
	String getName();

	/**
	 * Provides the description of the function
	 *
	 * @return String description of the function
	 */
	String getDescription();

	/**
	 * Provides the long description of the function, for example may include
	 * parameter information
	 *
	 * @return String description of the function
	 */
	String getLongDescription();

	/**
	 * Function Descriptors can choose to adapt to:
	 * <ul>
	 * <li> {@link IContentProposalProvider} - if the function descriptor is
	 * going to contribute to auto-completion suggestions.
	 * </ul>
	 *
	 * @param clazz
	 * @return
	 */
	@Override
	Object getAdapter(@SuppressWarnings("rawtypes") Class clazz);
	
	
	ISeriesItemDescriptor ADD = new ISeriesItemDescriptor() {

		@Override
		public Object getSeriesObject() throws InstantiationException {
			return null;
		}

		@Override
		public String getName() {
			return "Add...";
		}

		@Override
		public String getDescription() {
			return "Add a choosable item to the list of items.";
		}

		@Override
		public String getLongDescription() {
			return getDescription();
		}

		@Override
		public Object getAdapter(Class clazz) {
			return null;
		}
		
	};

}
