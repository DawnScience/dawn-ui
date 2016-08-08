/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.webintro;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;

import uk.ac.diamond.scisoft.feedback.FeedbackView;
/**
 * 
 * Class which provides allows JavaScript running in the JavaFX webview to open the DAWN Feedback View with some initial information
 * @author David Taylor
 *
 */
public class FeedbackUtil {
	/**
	 * Method to close the introPart and open the DAWN feedback view with the specified initial information.
	 * 
	 * @param email Initial data for the email field
	 * @param subject Initial data for the subject field
	 * @param message Initial data for the message field
	 */
	public void openFeedback(String email, String subject, String message){
		try {
			FeedbackView thePart = (FeedbackView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("uk.ac.diamond.scisoft.feedback.FeedbackView");
			thePart.setEmail(email);
			thePart.setSubject(subject);
			thePart.setMessage(message);
			
			IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
