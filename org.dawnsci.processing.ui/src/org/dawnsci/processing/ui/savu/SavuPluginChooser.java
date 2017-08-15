package org.dawnsci.processing.ui.savu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavuPluginChooser extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(SavuPluginChooser.class);
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
	
	public SavuPluginChooser(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, true));
	}


	public void initialiseCombo(Integer selectedItem) {
		this.c = new Combo(this, SWT.READ_ONLY);
		this.c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Map<String, Object> pluginInfo = null;
	    try {
			pluginInfo = getMapFromFile();
		} catch (IOException e) {
			logger.warn("Couldn't get the map from file.",e);
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
			logger.warn("Couldn't open the file for "+wspacePath + "savu_plugin_info.ser",e);
		}
		return pluginDict;
	
	}


	public void addSelectionListener(SelectionListener selectionListener) {
		
		this.c.addSelectionListener(selectionListener);
	}
}
