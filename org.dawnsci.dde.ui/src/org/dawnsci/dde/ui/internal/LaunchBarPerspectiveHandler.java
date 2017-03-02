/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package org.dawnsci.dde.ui.internal;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarControl;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;

/**
 * This type is used to show or hide the Launch Bar depending on the perspective
 * the user has currently selected.
 * 
 * @author Torkild U. Resheim
 */
@SuppressWarnings("restriction")
public class LaunchBarPerspectiveHandler {

	@Inject
	MApplication application;
	
	@Inject
	EModelService modelService;

	@Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		Object newValue = event.getProperty(EventTags.NEW_VALUE);
	
		if (!(newValue instanceof MPerspective)) {
			return;
		}

		MPerspective perspective = (MPerspective) newValue;
		String id  = perspective.getElementId();
		
		updateLaunchBarState(id);
	}
	
	/**
	 * Adds or removes the launch bar depending on the initial perspective.
	 */
	@Execute
	public void execute(){
		updateLaunchBarState(getActivePerspective());
	}
	
	private String getActivePerspective(){
		List<MWindow> children = application.getChildren();
		for (MWindow mWindow : children) {
			MPerspective activePerspective = modelService.getActivePerspective(mWindow);
			return activePerspective != null ? activePerspective.getElementId() : null;
		}
		return null;
	}

	private void updateLaunchBarState(String id) {
		MToolControl launchBar = getLaunchBar();
		if (id != null) {
			if (id.equals("org.eclipse.pde.ui.PDEPerspective")) {
				if (launchBar == null) {
					MTrimBar topTrimBar = getTopTrimBar();
					launchBar = MMenuFactory.INSTANCE.createToolControl();
					launchBar.setElementId(LaunchBarControl.ID);
					launchBar.setContributionURI(LaunchBarControl.CLASS_URI);
					topTrimBar.getChildren().add(0, launchBar);
				}
			} else if (launchBar != null) {
				MTrimBar topTrimBar = getTopTrimBar();
				topTrimBar.getChildren().remove(launchBar);
				Widget widget = (Widget) launchBar.getWidget();
				if (widget != null) {
					widget.dispose();
				}
			}
		}
	}

	private MTrimBar getTopTrimBar() {
		for (MWindow window : application.getChildren()) {
			if (window instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) window).getTrimBars()) {
					if (trimBar.getSide() == SideValue.TOP) {
						return trimBar;
					}
				}
			}
		}
		return null;
	}

	private MToolControl getLaunchBar() {
		MTrimBar topTrimBar = getTopTrimBar();
		if (topTrimBar == null) {
			return null;
		}
		for (MTrimElement trimElement : topTrimBar.getChildren()) {
			if (LaunchBarControl.ID.equals(trimElement.getElementId())) {
				return (MToolControl) trimElement;
			}
		}
		return null;
	}
}
