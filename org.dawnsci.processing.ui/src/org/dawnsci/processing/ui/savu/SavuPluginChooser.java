package org.dawnsci.processing.ui.savu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.processing.python.ui.SavuPluginFinder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.richbeans.widgets.cell.FieldComponentCellEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.FloatSpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;

public class SavuPluginChooser extends Composite {

	public String pluginName;
	public String pluginPath;
	public Integer pluginRank;
	private Combo c;
	private int selectionIndex;

	public String getPluginName() {
		return pluginName;
	}


	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}


	public String getPluginPath() {
		return pluginPath;
	}


	public void setPluginPath(String pluginPath) {
		this.pluginPath = pluginPath;
	}


	public Integer getPluginRank() {
		return pluginRank;
	}


	public void setPluginRank(Integer pluginRank) {
		this.pluginRank = pluginRank;
	}


	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Tester");
		shell.setLayout(new GridLayout());

		new SavuPluginChooser(shell, SWT.NONE);
		shell.pack();
		shell.setSize(600, 300);
		shell.open();

		shell.layout(true, true);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	
	public SavuPluginChooser(Composite parent, int style) {
		super(parent, style);
	}


	public void initialiseCombo(Integer selectedItem) {
		this.c = new Combo(this.getParent(), SWT.READ_ONLY);
		Map<String, Object> pluginInfo = null;
	    this.c.setBounds(50, 50, 150, 65);
	    try {
			pluginInfo = getMapFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String> myset = pluginInfo.keySet();			
		String[] stuff = myset.toArray(new String[myset.size()]);
		
	    this.c.setItems(stuff);
	    this.c.addSelectionListener(getPluginPath(c, pluginInfo));
	    if (selectedItem!=null) {
	    	this.c.select(selectedItem);
	    }
	}

	public SelectionAdapter getPluginPath(final Combo c, Map<String, Object> pluginInfo) {
		SelectionAdapter ac = new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Map <String, Object> pluginEntry = (Map<String, Object>) pluginInfo.get(c.getText());
				setSelectionIndex(c.getSelectionIndex());
				setPluginName(c.getText());
				setPluginPath((String) pluginEntry.get("path2plugin"));
				setPluginRank((Integer) pluginEntry.get("input rank"));
			}

	    };
		return ac;
	}
	
	public void setSelectionIndex(int selectionIndex) {
		this.selectionIndex = selectionIndex;
		
	}
	public Integer getSelectionIndex() {
		return this.selectionIndex;
		
	}

	private static Map<String, Object> getMapFromFile() throws IOException {
		final String wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

		Map<String, Object> pluginDict = null;
		ObjectInputStream in;
		FileInputStream fileIn;
			try {
			fileIn = new FileInputStream(wspacePath + "savu_plugin_info.ser");// just
			in = new ObjectInputStream(fileIn);
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pluginDict;
	
	}


	public void addSelectionListener(SelectionListener selectionListener) {
		
		this.c.addSelectionListener(selectionListener);
	}
}
