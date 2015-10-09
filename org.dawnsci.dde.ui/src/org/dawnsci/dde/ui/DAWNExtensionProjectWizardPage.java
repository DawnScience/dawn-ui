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
package org.dawnsci.dde.ui;

import java.net.URL;

import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaDescriptor;
import org.eclipse.pde.internal.core.schema.SchemaDescriptor;
import org.eclipse.pde.internal.core.schema.SchemaRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.osgi.framework.Version;

/**
 * This type implements the main page of the <i>New DAWN Plug-in Project</i>
 * wizard. It allows the user to specify some basic information required for all
 * new plug-ins.
 */
@SuppressWarnings("restriction")
public class DAWNExtensionProjectWizardPage extends WizardNewProjectCreationPage {
	
	private String identifier;
	private String name;
	private String version;
	private String vendor;
	private String extension;

	public DAWNExtensionProjectWizardPage(String pageName) {
		super(pageName);
		setTitle("DAWN Plug-in Project");
		setDescription("Define the location of the plug-in project");
	}
    
	public void createControl(Composite parent) {
    	super.createControl(parent);
        Composite composite = (Composite) getControl();
        createDetailsControl(composite);
        createExtensionControl(composite);
    }
    
	private void createExtensionControl(Composite composite) {
		Group g = new Group(composite, SWT.BORDER);
		g.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		g.setLayout(new GridLayout(2, false));
		g.setText("Extension:");

		Label idLabel = new Label(g, SWT.NONE);
		idLabel.setText("Extension point identifier:");
		final Combo extensionCombo = new Combo(g, SWT.BORDER | SWT.READ_ONLY);
		extensionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// add viable extension points to the list
		IExtensionPoint[] extensionPoints = Platform.getExtensionRegistry().getExtensionPoints();
		for (IExtensionPoint iExtensionPoint : extensionPoints) {
			String id = iExtensionPoint.getContributor().getName();
			if (DAWNDDEPlugin.isSupportedDAWNExtension(id)) {
				extensionCombo.add(iExtensionPoint.getUniqueIdentifier());
			}
		}

		final StyledText st = new StyledText(g, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.minimumHeight = 100;
		st.setLayoutData(layoutData);

		extensionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				IPluginExtensionPoint point = PDECore.getDefault().getExtensionsRegistry()
						.findExtensionPoint(extensionCombo.getText());
				URL url = null;
				if (point != null) {
					extension = extensionCombo.getText();								
					url = SchemaRegistry.getSchemaURL(point);
					if (url != null) {
						ISchemaDescriptor desc = new SchemaDescriptor(extension, url);
						ISchema fSchema = desc.getSchema(false);
						st.setText(fSchema.getDescription());
					} else {
						st.setText("Error: Cannot find extension point schema.");
					}
				}
	            boolean valid = validatePage();
	            setPageComplete(valid);
			}
		});
		
	}

	private void createDetailsControl(Composite composite) {
		Group g = new Group(composite, SWT.BORDER);
		g.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		g.setLayout(new GridLayout(2, false));
		g.setText("Properties:");

		Label idLabel = new Label(g,SWT.NONE);
        idLabel.setText("Identifier:");
        final Text idText = new Text(g, SWT.BORDER);
        idText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		idText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				identifier = idText.getText();
				boolean valid = validatePage();
				setPageComplete(valid);
			}
		});

        Label versionLabel = new Label(g,SWT.NONE);
        versionLabel.setText("Version:");
        final Text versionText = new Text(g, SWT.BORDER);
        versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				version = versionText.getText();
				boolean valid = validatePage();
				setPageComplete(valid);
			}
		});

        Label nameLabel = new Label(g,SWT.NONE);
        nameLabel.setText("Name:");
        final Text nameText = new Text(g, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				boolean valid = validatePage();
				setPageComplete(valid);
			}
		});
        
        Label vendorLabel = new Label(g,SWT.NONE);
        vendorLabel.setText("Institute:");
        final Text vendorText = new Text(g, SWT.BORDER);
        vendorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vendorText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				vendor = vendorText.getText();
				boolean valid = validatePage();
				setPageComplete(valid);
			}
		});
	}
	
	@Override
	protected boolean validatePage() {
		boolean ok = super.validatePage();
		if (!ok) return false;
		setErrorMessage(null);
		if (identifier == null || identifier.isEmpty()){
			setMessage("A plug-in identifier must be specified.");
			return false;
		}
		if (version == null || version.isEmpty()){
			setMessage("A plug-in version number must be specified.");
			return false;
		}
		try {
			Version.parseVersion(version);
		} catch (Exception e){
			setErrorMessage(e.getMessage());
			return false;
		}
		if (name == null || name.isEmpty()){
			setMessage("A plug-in name must be specified.");
			return false;
		}
		if (vendor == null || vendor.isEmpty()){
			setMessage("A plug-in institue must be specified.");
			return false;
		}
		if (extension == null || extension.isEmpty()){
			setMessage("An extension point must be specified.");
			return false;
		}
		return true;
	}

	public String getBundleIdentifier() {
		return identifier;
	}
	
	public String getBundleName() {
		return name;
	}
	
	public String getBundleVersion() {
		return version;
	}
	
	public String getBundleVendor() {
		return vendor;
	}
	
	public String getExtensionId() {
		return extension;
	}

}
