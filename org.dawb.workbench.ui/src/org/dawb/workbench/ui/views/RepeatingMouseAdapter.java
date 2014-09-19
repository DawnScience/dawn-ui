/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.workbench.ui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;

/**
 * Mouse adaptor to repeatedly perform a task when the mouse button is held down
 * and stop once the button is released
 */
public class RepeatingMouseAdapter extends MouseAdapter {
	private final EventRepeater repeater;

	public RepeatingMouseAdapter(Display display, SlowFastRunnable task) {
		repeater = new EventRepeater(display, task);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		repeater.setStarter(e);
		repeater.run();
	}

	@Override
	public void mouseUp(MouseEvent e) {
		repeater.stop();
	}
}

/**
 * Repeater that has three speeds
 */
class EventRepeater implements Runnable {
	boolean stop;
	Display display;
	static int slow = 200; // time in ms between tasks at slow speed 
	static int mid = 40;   // 
	static int fast = 8;
	static int[] threshold = {4, 8}; // count down in number of repetitions between switching speeds
	int[] count;
	private SlowFastRunnable task;

	public EventRepeater(Display display, SlowFastRunnable task) {
		this.display = display;
		this.task = task;
		stop = true;
	}

	MouseEvent first;
	public void setStarter(MouseEvent me) {
		first = me;
		stop = false;
		count = threshold.clone();
		task.setFast(false);
	}

	@Override
	public void run() {
		if (!stop) {
			task.run();
//			System.out.printf(".");
			if (count[0] >= 0) {
				count[0]--;
				display.timerExec(slow, this);
			} else if (count[1] >= 0) {
				count[1]--;
				display.timerExec(mid, this);
			} else {
				task.setFast(true);
				display.timerExec(fast, this);
			}
		}
	}

	public void stop() {
		stop = true;
		task.stop();
	}
}
