package org.dawnsci.plotting.draw2d.swtxy.translate;

import java.util.EventListener;

public interface TranslationListener extends EventListener {

	/**
	 * Callback when mouse pressed to activate translation
	 * @param evt
	 */
	void onActivate(TranslationEvent evt);

	/**
	 * Callback during dragging but before figure is translated
	 * @param evt
	 */
	void translateBefore(TranslationEvent evt);
	
	/**
	 * Callback during dragging after figure is translated
	 * @param evt
	 */
	void translationAfter(TranslationEvent evt);
		
	/**
	 * Callback when mouse released
	 * @param evt
	 */
	void translationCompleted(TranslationEvent evt);

}
