/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.conversion.ui.api;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.jface.resource.ImageDescriptor;

public interface IConversionWizardPageService {

	/** Get the wizardPage for a particular scheme
	 *
	 * @param scheme a IConversionScheme instance that should have been returned by this service
	 * @return the requested page
	 */
	public IConversionWizardPage getPage(IConversionScheme scheme);

	/** Get the wizardPage for a particular UI label
	 *
	 * @param scheme a IConversionScheme instance that should have been returned by this service
	 * @return the requested page
	 */
	public IConversionWizardPage getPage(String label);

	/** Get the UI labels of all available schemes
	 *
	 * @return the requested labels
	 */
	public String[] getLabels();

	/** Get the UI labels of all available schemes
	 *
	 * @param visibleOnly if true only those labels that are marked as visible in their schemes will be returned
	 * @return the requested labels
	 */
	public String[] getLabels(boolean visibleOnly);

	/** Get the wizardPages of all available schemes
	 *
	 * @param visibleOnly if true only those pages that are marked as visible in their schemes will be returned
	 * @return the requested pages
	 */
	public IConversionWizardPage[] getPages(boolean visibleOnly);

	/** Get the wizardPages of all available schemes
	 *
	 * @return the requested pages
	 */
	public IConversionWizardPage[] getPages();

	/** For a given IConversionScheme implementing class, return the corresponding internal instance
	 *
	 * @param scheme The class for which the instance is required
	 * @return the requested instance
	 */
	public <U extends IConversionScheme> U getSchemeForClass(Class<U> scheme);

	/** Get the IConversionScheme that is associated with a given UI label
	 *
	 * @param label the scheme UI label
	 * @return the requested scheme
	 */
	public IConversionScheme fromLabel(String label);

	/** Get the instances of all registered IConversionSchemes
	 *
	 * @param visibleOnly if true, only those schemes that are marked as visible will be returned
	 * @return the requested schemes
	 */
	public List<IConversionScheme> getSchemes(boolean visibleOnly);

	/** Get the instances of all registered IConversionSchemes
	 *
	 * @return the requested schemes
	 */
	public List<IConversionScheme> getSchemes();

	/** Get the ImageDescriptor for the image file that is associated with a given scheme
	 *
	 * @param scheme An instance of the required scheme
	 * @return the requested ImageDescriptor
	 */
	public ImageDescriptor getImage(IConversionScheme scheme);
}
