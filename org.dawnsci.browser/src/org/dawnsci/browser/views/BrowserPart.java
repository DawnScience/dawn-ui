/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.browser.views;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.dawnsci.browser.Activator;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.scene.web.WebHistory.Entry;

public class BrowserPart {
	
	private WebEngine webEngine;
	private ToolItem backButton;
	private ToolItem forwardButton;
	private Combo urlBar;
	private String homeLocation = "http://dawnsci.org";
	
	private ToolItem refreshButton;
	
	Image stop_image = Activator.getImageDescriptor("icons/nav_stop.gif").createImage();
	Image reload_image = Activator.getImageDescriptor("icons/reload-arrow.png").createImage();
	Image go_image = Activator.getImageDescriptor("icons/nav_go.gif").createImage();
	Image error_image = Activator.getImageDescriptor("icons/exclamation-red.png").createImage();
	
	/**
	 * E4 part
	 */
	public BrowserPart() {
		
	}

	@PostConstruct
	public void init() {
		
	}

	@PersistState
	public void saveState(MPart part) {
		part.getPersistedState().put("CURRENT_PAGE", webEngine.getLocation());
	}

	@PostConstruct
	public void postConstruct(Composite parent, MPart part) {
		Platform.setImplicitExit(false);
		
		GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        
        parent.setLayout(layout);

        Composite toolbarComp = new Composite(parent, SWT.NONE);
        toolbarComp.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false));
        toolbarComp.setLayout(new ToolbarLayout());
        
        createBrowser(parent);
        addEventListeners();
        
        createNavigation(toolbarComp);
        createURLBar(toolbarComp);
        
        webEngine.getLoadWorker().stateProperty().addListener(
        		(ov, oldState, newState) -> {
        			if (newState == State.SUCCEEDED){ 
        				String title = webEngine.getTitle();
        				if(title != null && !title.isEmpty()){
        					part.setLabel(webEngine.getTitle()+" | DAWN Web Browser");
        				}else{
        					part.setLabel("DAWN Web Browser");
        				}
        			}
        		});
        String persistedPageUrl = part.getPersistedState().get("CURRENT_PAGE");
        if(persistedPageUrl != null && !persistedPageUrl.isEmpty()){
        	webEngine.load(persistedPageUrl);
        }else{
        	webEngine.load(homeLocation);
        }
	}

	private WebView createBrowser(Composite parent){
		FXCanvas canvas = new FXCanvas(parent, SWT.NONE);
        canvas.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        
        // Java FX Stuff:
		BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        canvas.setScene(scene);
        
        WebView browser = new WebView();
        webEngine = browser.getEngine();
        borderPane.setCenter(browser);
        
        return browser;
	}
	
	private void addEventListeners(){
		webEngine.setOnAlert(event -> showAlert(event.getData()));
        webEngine.setConfirmHandler(message -> showConfirm(message));
        webEngine.setPromptHandler(event -> showPrompt(event));
}
	
	private void showAlert(String message) {
		MessageBox dialog = 
				new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
		dialog.setText("Javascript Alert");
		dialog.setMessage(message);
		dialog.open();
	}
	
	private String showPrompt(PromptData event) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
				"Javascript Prompt", event.getMessage(), event.getDefaultValue(), null);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}

	private boolean showConfirm(String message) {
		MessageBox dialog = 
				new MessageBox(Display.getCurrent().getActiveShell()
						, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
		dialog.setText("Javascript Confirm");
		dialog.setMessage(message);
		int result = dialog.open();
		return result == SWT.OK;
	}
	
	private void setURL(String url){
		webEngine.load(url);
	}
	
	private void checkHistory(){
		int index = webEngine.getHistory().currentIndexProperty().get();
		int size = webEngine.getHistory().getEntries().size();
		
		forwardButton.setEnabled(size-index>1);
		backButton.setEnabled(index>0);
	}
	
	private ToolBar createURLBar(Composite parent){
        urlBar = new Combo(parent, SWT.DROP_DOWN);

        urlBar.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent we) {
	            if (urlBar.getSelectionIndex() != -1 && !urlBar.getListVisible()) {
	            	setURL(urlBar.getItem(urlBar.getSelectionIndex()));
	            }
               
            }
        });
        urlBar.addListener(SWT.DefaultSelection, new Listener() {
            @Override
			public void handleEvent(Event e) {
            	setURL(urlBar.getText());
            }
        });
        
        WebHistory history = webEngine.getHistory();
        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
			@Override
			public void onChanged(Change<? extends Entry> c) {
				c.next();
				for (Entry e : c.getRemoved()) {
					urlBar.remove(e.getUrl());
				}
				for (Entry e : c.getAddedSubList()) {
					urlBar.add(e.getUrl(),0);
				}
			}
		});
        
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
		
		refreshButton = new ToolItem(toolbar, SWT.NONE);
		checkStatusButton();
		
		refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
            	Image refreshImage = refreshButton.getImage();
            	if (refreshImage.equals(stop_image)){
            		webEngine.getLoadWorker().cancel();
            	}else if (refreshImage.equals(go_image)){
            		setURL(urlBar.getText());
            	}else{
            		webEngine.reload();
            	}
            }
        });
		
		webEngine.getLoadWorker().stateProperty().addListener(
        		(ov, oldState, newState) -> {
        			urlBar.setText(webEngine.getLocation());
        			if (newState == State.FAILED && !urlBar.getText().startsWith("http://")){
        				setURL("http://"+urlBar.getText());
        			}
        			checkStatusButton();
        			
        		});
		
		urlBar.addModifyListener(new ModifyListener() {
        	public void modifyText(ModifyEvent e) {
        		checkStatusButton();
        	}
        });
		
		new ToolItem(toolbar, SWT.SEPARATOR);
		
		ToolItem newTab = new ToolItem(toolbar, SWT.NONE);
		newTab.setToolTipText("Create new browser tab");
		newTab.setImage(Activator.getImageDescriptor("icons/plus.png").createImage());
		
		newTab.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
            	try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().
					getActivePage().showView("org.dawnsci.browser.views.BrowserPart",String.valueOf(new Date().getTime()),IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
            }
        });
		
		ToolItem openInBrowser = new ToolItem(toolbar, SWT.NONE);
		openInBrowser.setToolTipText("Open this page in system browser");
		openInBrowser.setImage(Activator.getImageDescriptor("icons/globe--arrow.png").createImage());
		
		openInBrowser.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
				Program.launch(urlBar.getText());
            }
        });
		
		return toolbar;
	}
	
	private void checkStatusButton(){
		Boolean urlMatchesLocation = urlBar.getText().equals(webEngine.getLocation());
		State workerState = webEngine.getLoadWorker().getState();
		switch (workerState){
			case RUNNING:
			case SCHEDULED:
				refreshButton.setImage(stop_image);
				refreshButton.setToolTipText("Stop Loading");
				break;
			case SUCCEEDED:
			case READY:
				if (urlMatchesLocation){
					refreshButton.setImage(reload_image);
					refreshButton.setToolTipText("Reload Page");
				}else{
					refreshButton.setImage(go_image);
					refreshButton.setToolTipText("Go to URL");
				}
				break;
			case FAILED:
			case CANCELLED:
				refreshButton.setImage(error_image);
				refreshButton.setToolTipText("Error, click to reload");
				break;
		} 
		
	}
	
	private ToolBar createNavigation(Composite parent) {
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
		
        backButton = new ToolItem(toolbar, SWT.NONE);
		backButton.setImage(Activator.getImageDescriptor("icons/back-arrow.png").createImage());
		backButton.setToolTipText("Back");
		backButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
                webEngine.getHistory().go(-1);
            }
        });
		
		forwardButton = new ToolItem(toolbar, SWT.NONE);
		forwardButton.setImage(Activator.getImageDescriptor("icons/forward-arrow.png").createImage());
		forwardButton.setToolTipText("Forward");
		forwardButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
                webEngine.getHistory().go(1);
            }
        });
		
		ToolItem homeButton = new ToolItem(toolbar, SWT.NONE);
		homeButton.setImage(Activator.getImageDescriptor("icons/home.png").createImage());
		homeButton.setToolTipText("Home");
        homeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
                webEngine.load(homeLocation);
            }
        });
		
		webEngine.getLoadWorker().stateProperty().addListener(
				(ov, oldState, newState) -> {
					if (newState == State.RUNNING | newState == State.SUCCEEDED) this.checkHistory();
				});
		
		return toolbar;
  }
	
	@Focus
	public void onFocus() {

	}

	@PreDestroy
	private void partDestroyed() {
	}

	
}