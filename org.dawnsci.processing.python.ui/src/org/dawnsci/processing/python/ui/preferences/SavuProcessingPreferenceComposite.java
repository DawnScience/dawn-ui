package org.dawnsci.processing.python.ui.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dawnsci.processing.python.ui.SavuPluginFinder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavuProcessingPreferenceComposite extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	
	private static final Logger logger = LoggerFactory.getLogger(SavuProcessingPreferenceComposite.class);
	
	private final List list;

	private Label descriptionLabel;

	private Map<String, Object> pluginDict;

	public SavuProcessingPreferenceComposite(Composite parent, int style) {
		super(parent, style);

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(2, false));
		
		Label lblAvailableSavuPlugin = new Label(this, SWT.NONE);
		lblAvailableSavuPlugin.setText("List of Available Savu Plugins");
		FontDescriptor descriptor = FontDescriptor.createFrom(lblAvailableSavuPlugin.getFont()).setStyle(SWT.BOLD);
		Font bigFont = descriptor.createFont(lblAvailableSavuPlugin.getDisplay());
		lblAvailableSavuPlugin.setFont(bigFont);
		lblAvailableSavuPlugin.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		
		list = new List(this, SWT.BORDER | SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		list.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selection = list.getSelection();
				if (selection.length == 0)
					return;
				@SuppressWarnings("unchecked")
				HashMap<String, String> dict = (HashMap<String, String>) pluginDict.get(selection[0]);
				String description = dict.get("description");
				if (description == null)
					descriptionLabel.setText("Not found");
				else
					descriptionLabel.setText(description.trim());
			}
		});
		
		Button btnNewButton = new Button(this, SWT.PUSH);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(Display.getCurrent(), () -> {
					SavuPluginFinder findme = null;
					try {
						findme = new SavuPluginFinder();
						// run the finder. We should save this to file and update the input
						findme.populateSavuPlugins();
						Map<String, Object> pd = findme.getSavuPluginInfo();

						try (
								FileOutputStream fileOut = new FileOutputStream(getSavuPluginPath());
								ObjectOutputStream out = new ObjectOutputStream(fileOut);
							) {
							// this writes out the names and description file.
							out.writeObject(pd);
							// I should here write out the parameter lists in files with a title of the plugin name.
						}
						Set<String> myset = pd.keySet();			
						String[] stuff = myset.toArray(new String[myset.size()]);
						pluginDict = pd;
						list.setItems(stuff);
						for (Map.Entry<String, Object> entry : pd.entrySet()) {	
							Map<String, Object> ppd = findme.getPluginParameters(entry.getKey());
							logger.debug(entry.getKey());
							String pluginFilePath = getWorkspacePath()+entry.getKey()+".ser";
							logger.debug("The plugin filepath is"+pluginFilePath);
							try (
								FileOutputStream fileOut = new FileOutputStream(pluginFilePath);
								ObjectOutputStream out = new ObjectOutputStream(fileOut);
							) {
								out.writeObject(ppd);
							}
						}
					} catch (Exception exc) {
						Display.getCurrent().syncExec(() -> 
							ErrorDialog.openError(SavuProcessingPreferenceComposite.this.getShell(), "Could not get Savu Plugins!", null, new Status(IStatus.WARNING, "org.dawnsci.processing.python.ui", "Click on Details to see the exception messages", exc))
						);
					} 
					if (findme != null)
						findme.stopPythonService();
				});
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("Check for Plugins");
		Group descriptionGroup = new Group(this, SWT.NONE);
		GridData descriptionGroupGridData = new GridData(SWT.FILL, SWT.END, true, false, 2, 1);
		descriptionGroup.setLayoutData(descriptionGroupGridData);
		descriptionGroup.setLayout(new GridLayout(1, false));
		descriptionGroup.setText("Plugin Description");
		descriptionLabel = new Label(descriptionGroup, SWT.WRAP);
		GridData descriptionLabelGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		descriptionLabelGridData.minimumHeight = 100;
		descriptionLabel.setLayoutData(descriptionLabelGridData);
	}
	
	private String getWorkspacePath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separator;
	}
	
	private String getSavuPluginPath() {
		String wspacePath = getWorkspacePath();
		return wspacePath + "savu_plugin_info.ser";
	}
	
	@SuppressWarnings("unchecked")
	public void populateList() {
		// if the plugin dict already exist then populate the list from that automatically
		try (	FileInputStream fileIn = new FileInputStream(getSavuPluginPath());
				ObjectInputStream in = new ObjectInputStream(fileIn);
				) {
			pluginDict = (Map<String, Object>) in.readObject();
			Set<String> myset = pluginDict.keySet();			
			String[] stuff = myset.toArray(new String[myset.size()]);
			list.setItems(stuff);
		} catch (Exception e) {
			logger.debug("Savu plugin file could not be opened", e);
		}  
		
	}
}
