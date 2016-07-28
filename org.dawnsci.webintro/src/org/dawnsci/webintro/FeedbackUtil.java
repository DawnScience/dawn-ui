package org.dawnsci.webintro;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;

import uk.ac.diamond.scisoft.feedback.FeedbackView;

public class FeedbackUtil {
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
