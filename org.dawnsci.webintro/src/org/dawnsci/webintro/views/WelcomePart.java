/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.webintro.views;

import org.dawnsci.webintro.Activator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IntroPart;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import org.dawnsci.webintro.JSBridge;

/**
 * Class to replace the welcome screen with a web browser 
 * 
 * @author David Taylor
 * 
 */
public class WelcomePart extends IntroPart {

	private static String homeLocation = "platform:/plugin/org.dawnsci.webintro/web/index.html";
	
	//Any page urls loaded that do not start with this will be opened in the system browser
	//(Note that this does not apply to JS, CSS, images etc.
	private static String locationLock = "platform:/plugin/org.dawnsci.webintro/web";
	
	private WebEngine webEngine;
	
	private IToolBarManager toolBar;
	
	private Text urlTextBox;
	private Action backButton, forwardButton;

	@Override
	public void createPartControl(Composite container) {
		
		Platform.setImplicitExit(false); // Keep the JavaFX thread running even when window closed (there's probably a better way to do this)
		
		//Creating a JavaFX Browser, so create an FXCanvas to contain it
		FXCanvas canvas = new FXCanvas(container, SWT.NONE);
		BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        
        WebView browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.load(homeLocation);
        
        borderPane.setCenter(browser);

        canvas.setScene(scene);
        
        IActionBars actionBars = getIntroSite().getActionBars();
//		IMenuManager dropDownMenu = actionBars.getMenuManager();
		toolBar = actionBars.getToolBarManager();
		
        setupToolbarButtons();
        setupURLBar(); //Useful for debug
        lockUrl();
        addJSBridges();
	}

	private void setupToolbarButtons(){
		Action homeButton = new Action() {
			@Override
			public void run() {
		        webEngine.load(homeLocation);
			}	
		};
		homeButton.setText("Home");
		homeButton.setImageDescriptor(Activator.getImageDescriptor("icons/home.png"));
		toolBar.add(homeButton);
		
		backButton = new Action() {
			@Override
			public void run() {
				webEngine.getHistory().go(-1);
			}	
		};
		backButton.setText("Back");
		backButton.setImageDescriptor(Activator.getImageDescriptor("icons/back-arrow.png"));
		toolBar.add(backButton);
		
		forwardButton = new Action() {
			@Override
			public void run() {
				webEngine.getHistory().go(1);
			}
		};
		forwardButton.setText("Forward");
		forwardButton.setImageDescriptor(Activator.getImageDescriptor("icons/forward-arrow.png"));
		toolBar.add(forwardButton);
		
		Action reloadButton = new Action() {
			@Override
			public void run() {
				webEngine.reload();
			}
		};
		reloadButton.setText("Reload");
		reloadButton.setImageDescriptor(Activator.getImageDescriptor("icons/reload-arrow.png"));
		toolBar.add(reloadButton);
		
		checkHistory();
		webEngine.getLoadWorker().stateProperty().addListener(
				(ov, oldState, newState) -> {
					if (newState == State.RUNNING | newState == State.SUCCEEDED) this.checkHistory();
				});
	}

	private void setupURLBar(){
		IContributionItem urlBar = new ControlContribution("test") {
			protected Control createControl(Composite parent) {

				urlTextBox = new Text(parent, SWT.BORDER);

				//Detect when enter is pressed
				urlTextBox.addListener(SWT.Traverse, new Listener()
				{
					@Override
					public void handleEvent(Event event)
					{
						if(event.detail == SWT.TRAVERSE_RETURN)
						{
							//Load the url
							webEngine.load(urlTextBox.getText());
						}
					}
				});

				return urlTextBox;
			}
		};        
		toolBar.add(urlBar);

		webEngine.getLoadWorker().stateProperty().addListener(
				(ov, oldState, newState) -> {
					if (newState == State.RUNNING | newState == State.SUCCEEDED) urlTextBox.setText(webEngine.getLocation());
				});
	}
	
	private void checkHistory(){
		int index = webEngine.getHistory().currentIndexProperty().get();
		int size = webEngine.getHistory().getEntries().size();
		
		forwardButton.setEnabled(size-index>1);
		backButton.setEnabled(index>0);
	}
	
	private void lockUrl(){
		webEngine.locationProperty().addListener(new ChangeListener<String>() {
			@Override 
			public void changed(ObservableValue<? extends String> ov, final String oldLoc, final String loc) {
				if (!loc.startsWith(locationLock)) {
					Platform.runLater(new Runnable() { //Workaround for bug with calling engine.load within a ChangeListener event
						@Override public void run() {
							webEngine.load(oldLoc);
							Program.launch(loc);
						}
					});
				}
			}
		});
	}
	
	private void addJSBridges(){
		webEngine.getLoadWorker().stateProperty().addListener(
				(ov, oldState, newState) -> {
					if (newState == State.SUCCEEDED){
						JSObject jsobj = (JSObject) webEngine.executeScript("window");
						jsobj.setMember("java", new JSBridge());
						webEngine.executeScript("javaReady();");
					}
				});
	}
	
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "DAWN Welcome";
	}

	@Override
	public void standbyStateChanged(boolean standby) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}

