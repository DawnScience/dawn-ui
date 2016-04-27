package org.dawnsci.fileviewer.table;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TableViewer to demonstrate the usage of a standard content provider
 * with a virtual table
 *
 */
public class Snippet029VirtualTableViewer {


	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet029VirtualTableViewer(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.VIRTUAL);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setUseHashlookup(true);
		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<>();

		for (int i = 0; i < 10000; i++) {
			elements.add(new MyModel(i));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet029VirtualTableViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}