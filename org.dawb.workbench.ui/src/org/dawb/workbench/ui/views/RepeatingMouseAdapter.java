/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
