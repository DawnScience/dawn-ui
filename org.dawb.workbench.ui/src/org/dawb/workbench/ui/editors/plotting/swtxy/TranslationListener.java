package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.EventListener;

public interface TranslationListener extends EventListener {

	void translateBefore(TranslationEvent evt);
	
	void translationAfter(TranslationEvent evt);
		
	void translationCompleted(TranslationEvent evt);

}
