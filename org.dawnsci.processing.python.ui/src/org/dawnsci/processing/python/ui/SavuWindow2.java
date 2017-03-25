package org.dawnsci.processing.python.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SavuWindow2 extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SavuWindow2(Composite parent, int style) throws FileNotFoundException {
		super(parent, style);
		final String wspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		final String savuPluginPath = wspacePath+"savu_plugin_info.ser";
		setLayout(new GridLayout(3, false));
		Label lblAvailableSavuPlugin = new Label(this, SWT.NONE);
		lblAvailableSavuPlugin.setText("Available Savu Plugins");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		final List list = new List(this, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_list = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_list.heightHint = 242;
		gd_list.widthHint = 218;
		list.setLayoutData(gd_list);
		// if the plugin dict already exist then populate the list from that automatically
		try {
			FileInputStream fileIn = new FileInputStream(savuPluginPath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Map<String, Object> pluginDict = null;
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
			Set<String> myset = pluginDict.keySet();			
			String[] stuff = myset.toArray(new String[myset.size()]);
			list.setItems(stuff);
		} catch (IOException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SavuPluginFinder findme = null;
				try {
					findme = new SavuPluginFinder();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try {
					// run the finder. We should save this to file and update the input
					findme.populateSavuPlugins();
					Map<String, Object>pluginDict = findme.getSavuPluginInfo();

					FileOutputStream fileOut = null;
					ObjectOutputStream out = null;
					try {
						// this writes out the names and description file.
						fileOut = new FileOutputStream(savuPluginPath);
						out = new ObjectOutputStream(fileOut);
						out.writeObject(pluginDict);
						out.close();
						fileOut.close();
						// I should here write out the parameter lists in files with a title of the plugin name.
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					Set<String> myset = pluginDict.keySet();			
					String[] stuff = myset.toArray(new String[myset.size()]);
					list.setItems(stuff);
					
					try {
						for (Map.Entry<String, Object> entry : pluginDict.entrySet()) {	
							Map<String, Object> pluginparamDict = findme.getPluginParameters(entry.getKey());
							System.out.println(entry.getKey());
							String pluginFilePath = wspacePath+entry.getKey()+".ser";
							System.out.println(pluginFilePath);
							fileOut = new FileOutputStream(pluginFilePath);
							out = new ObjectOutputStream(fileOut);
							out.writeObject(pluginparamDict);
							out.close();
							fileOut.close();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					findme.stopPythonService();

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
				);
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, true, 2, 1);
		gd_btnNewButton.heightHint = 36;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Check for Plugins");

		
		
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
