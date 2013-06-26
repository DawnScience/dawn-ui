package org.dawnsci.common.widgets.decorator;


import java.util.regex.Pattern;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * This class is a number decorator. Which gives formatting to a Text object including
 * bounds. If the bounds are exceeded the Text object is given a red foreground.
 * 
 * By simply changing the pattern it can become another decorator (e.g. a DateDecorator).
 */
public class RegexDecorator {

	protected Text    text;
	private   Pattern pattern;
	private   VerifyListener verifyListener;
	/**
	 * 
	 * @param text
	 * @param pattern - each character entered must match this pattern NOT the whole string.
	 */
	public RegexDecorator(Text text, final String stringPattern) {
		this.text    = text;
		this.pattern = Pattern.compile(stringPattern);
		attachListeners();
	}

	private void attachListeners() {
		verifyListener = new VerifyListener() {			
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text) && !pattern.matcher(e.text).matches()) {
					e.doit = false;
					return;
				}
				
				final String oldS = text.getText();
	            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

				if (!check(newS, e.text)) {
					e.doit = false;
					return;
				}
			}
		};
		text.addVerifyListener(verifyListener);
	}
	
	/**
	 * Please override this method to provide additional checking when a character is entered.
	 * @return true if ok, false otherwise.
	 */
	protected boolean check(String value, String delta) {
		return true;
	}

	public void dispose() {
		text.removeVerifyListener(verifyListener);
	}



}
