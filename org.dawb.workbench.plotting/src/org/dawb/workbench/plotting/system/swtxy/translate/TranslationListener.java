package org.dawb.workbench.plotting.system.swtxy.translate;

import java.util.EventListener;

public interface TranslationListener extends EventListener {

	void translateBefore(TranslationEvent evt);
	
	void translationAfter(TranslationEvent evt);
		
	void translationCompleted(TranslationEvent evt);

}
