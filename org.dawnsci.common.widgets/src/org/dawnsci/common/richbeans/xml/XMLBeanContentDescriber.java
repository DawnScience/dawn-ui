/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

/**
 * Tests XML Content for starting with a tag.
 * 
 * Highly linked to the castor XML at the moment as it 
 * reads the second line in the file to know the base tag.
 */
public abstract class XMLBeanContentDescriber implements IContentDescriber, ITextContentDescriber {

	/**
	 * Registers the describer with the factory.
	 */
	public XMLBeanContentDescriber() {
		XMLBeanContentDescriberFactory.getInstance().addDescriber(this);
	}
	
	protected abstract String getBeanName();
	
	/**
	 * ID must match editor class name and id of editor in extension registry
	 * 
	 * @return editor id
	 */
	protected abstract String getEditorId();
	
	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException {
		
		final BufferedReader reader = new BufferedReader(contents);
		try {
			// TODO Use castor to read the file and use instanceof
			// on the bean type returned.
			@SuppressWarnings("unused")
			final String      titleLine = reader.readLine(); // unused.
			final String      tagLine   = reader.readLine();
			final String      tagName   = getTagName();
			if (tagLine.trim().equalsIgnoreCase("<"+tagName+">")||tagLine.trim().equalsIgnoreCase("<"+tagName+"/>")) {
				return IContentDescriber.VALID;
			}
			return IContentDescriber.INVALID;
		} finally {
			reader.close();
		}
	}
	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		return describe(new InputStreamReader(contents, "UTF-8"), description);
	}

	private String getTagName() {
		String beanName = getBeanName();
		return beanName.substring(beanName.lastIndexOf(".")+1);
	}

	@Override
	public QualifiedName[] getSupportedOptions() {
		return IContentDescription.ALL;
	}

}

	