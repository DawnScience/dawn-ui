/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.*;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;

public abstract class PluginTestBase {

	private Shell shell;

	protected abstract void createControl(Composite parent);

	public PluginTestBase() {
		super();
	}

	/**
	 * Call this to stop at this point and be able to interact with the UI
	 */
	public void readAndDispatchForever() {
		readAndDispatch(-1);
	}

	public void readAndDispatch() {
		readAndDispatch(0);
	}

	/**
	 *
	 * @param time
	 *            < 0 == forever
	 */
	public void readAndDispatch(long time) {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		long endtime = System.currentTimeMillis() + time;
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				if (System.currentTimeMillis() >= endtime && time >= 0) {
					return;
				}
				display.sleep();
			}
		}
	}

	@Before
	public void createAndOpenShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		shell = new Shell(window.getShell(), SWT.RESIZE);
		shell.setText("Python Information");
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());
		createControl(shell);
		shell.open();
		readAndDispatch();
	}

	@After
	public void closeAndDisposeShell() {
		// TODO remove this read and dispatch, some events
		// related to auto-complete are running async, so
		// just give all them a chance to complete before
		// we dispose.
		readAndDispatch();

		shell.close();
		shell.dispose();
	}

	/**
	 * clear clipboard by putting something "else" in it
	 */
	protected void clearClipboard() {
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			cb.clearContents();
			cb.setContents(new Object[] { "EMPTY" },
					new Transfer[] { TextTransfer.getInstance() });
		} finally {
			cb.dispose();
		}
	}

	/**
	 * Set the clipboard contents to this object.
	 *
	 * <ul>
	 * <li>The TextTransfer is set to object.toString()
	 * <li>The LocalSelectionTransfer is set to object
	 * </ul>
	 *
	 * @param object
	 */
	protected void setClipboard(Object object) {
		StructuredSelection selection = new StructuredSelection(object);
		LocalSelectionTransfer localSelectionTransfer = LocalSelectionTransfer
				.getTransfer();
		localSelectionTransfer.setSelection(selection);
		Object[] data = new Object[] { object.toString(), selection };
		Transfer[] dataTypes = new Transfer[] { TextTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() };
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			cb.setContents(data, dataTypes);
		} finally {
			cb.dispose();
		}
	}

	/**
	 * Gets a single selection transfer item from the clipboard
	 *
	 * @return
	 */
	protected Object getClipboardLocalSelection() {
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			Object contents = cb.getContents(LocalSelectionTransfer
					.getTransfer());
			IStructuredSelection selection = (IStructuredSelection) contents;
			assertEquals(1, selection.size());
			return selection.getFirstElement();

		} finally {
			cb.dispose();
		}
	}

	/**
	 * Gets a text transfer item from the clipboard
	 *
	 * @return
	 */
	protected String getClipboardText() {
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			Object contents = cb.getContents(TextTransfer.getInstance());
			// don't use contents.toString(), contents must be String already
			return (String) contents;

		} finally {
			cb.dispose();
		}
	}

	/**
	 * Simulate typing string.
	 * <p>
	 * Based on <a href=
	 * "http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet146.java"
	 * >Snippet146</a>
	 * <p>
	 * See <a href=
	 * "http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet304.java"
	 * >Snippet304</a> for an example on implementing sending ctrl-sequences
	 *
	 * @param string
	 *            to simulate typing
	 */
	protected void type(String string) {
		Display display = Display.getDefault();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			boolean shift = Character.isUpperCase(ch);
			ch = Character.toLowerCase(ch);
			if (shift) {
				Event event = new Event();
				event.type = SWT.KeyDown;
				event.keyCode = SWT.SHIFT;
				display.post(event);
				readAndDispatch(100);
			}
			Event event = new Event();
			event.type = SWT.KeyDown;
			event.character = ch;
			display.post(event);
			readAndDispatch(100);
			event.type = SWT.KeyUp;
			display.post(event);
			readAndDispatch(100);
			if (shift) {
				event = new Event();
				event.type = SWT.KeyUp;
				event.keyCode = SWT.SHIFT;
				display.post(event);
				readAndDispatch(100);
			}
		}
	}

	/**
	 * Simulate sending a keycode.
	 * <p>
	 * Based on <a href=
	 * "http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet146.java"
	 * >Snippet146</a>
	 * <p>
	 * See <a href=
	 * "http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet304.java"
	 * >Snippet304</a> for an example on implementing sending ctrl-sequences
	 *
	 * @param string
	 *            to simulate typing
	 */
	protected void type(int keyCode) {
		Display display = Display.getDefault();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keyCode;
		display.post(event);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		readAndDispatch(100);
		event = new Event();
		event.type = SWT.KeyUp;
		event.keyCode = keyCode;
		display.post(event);
		readAndDispatch(100);
	}

}