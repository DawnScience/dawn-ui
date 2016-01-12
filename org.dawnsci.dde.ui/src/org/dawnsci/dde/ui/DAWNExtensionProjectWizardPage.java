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
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaDescriptor;
import org.eclipse.pde.internal.core.schema.SchemaDescriptor;
import org.eclipse.pde.internal.core.schema.SchemaRegistry;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.elements.ElementList;
import org.eclipse.pde.internal.ui.wizards.WizardCollectionElement;
import org.eclipse.pde.internal.ui.wizards.WizardElement;
import org.eclipse.pde.internal.ui.wizards.extension.NewExtensionRegistryReader;
import org.eclipse.pde.ui.templates.ITemplateSection;
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
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Version;

/**
 * This type implements the main page of the <i>New DAWN Plug-in Project</i>
 * wizard. It allows the user to specify some basic information required for all
 * new plug-ins.
 * 
 * @author Torkild U. Resheim
 */
@SuppressWarnings("restriction")
public class DAWNExtensionProjectWizardPage extends WizardNewProjectCreationPage {
	
	private String identifier;
	private String name;
	private String version;
	private String vendor;
	private String extension;

	/** A list of all applicable templates */
	private ArrayList<ITemplateSection> templates;

	private WizardCollectionElement fWizardCollection;

	public static final String PLUGIN_POINT = "newExtension"; //$NON-NLS-1$

	public DAWNExtensionProjectWizardPage(String pageName) {
		super(pageName);
		setTitle("DAWN Plug-in Project");
		setDescription("Define the location of the plug-in project");
		if (!loadTemplateCollection()){
			MessageDialog.open(WARNING, this.getShell(), "Could not load all templates", "Some templates were not loaded. This could be related to bundle loading issues. See the error log for details.", SWT.NONE);
		}
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
		
		for (ITemplateSection template : templates) {
			extensionCombo.add(template.getUsedExtensionPoint());
			// makes this go faster when the user selects items in the combo box
			PDECore.getDefault().getExtensionsRegistry()
					.findExtensionPoint(template.getUsedExtensionPoint());
		}

		final StyledText st = new StyledText(g, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.minimumHeight = 100;
		st.setLayoutData(layoutData);

		extensionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String text = extensionCombo.getText();
				IPluginExtensionPoint point = PDECore.getDefault().getExtensionsRegistry()
						.findExtensionPoint(text);
				URL url = null;
				if (point != null) {
					extension = text;								
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
		g.setText("Plug-in Properties:");

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
		
		versionText.setText("1.0.0.qualifier");

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

	/**
	 * Locate all wizards extending "org.eclipse.pde.ui.newExtension" and place
	 * instances of the associated templates into the templates array.
	 */
	private boolean loadTemplateCollection() {
		boolean ok = true;
		NewExtensionRegistryReader reader = new NewExtensionRegistryReader();
		fWizardCollection = (WizardCollectionElement) reader.readRegistry(PDEPlugin.getPluginId(), PLUGIN_POINT, false);
		WizardCollectionElement templateCollection = new WizardCollectionElement("", "", null);
		collectTemplates(fWizardCollection.getChildren(), templateCollection);
		templates = new ArrayList<>();
		ElementList wizards = templateCollection.getWizards();
		Object[] children = wizards.getChildren();
		for (Object object : children) {
			if (object instanceof WizardElement) {
				String contributingId = ((WizardElement) object).getContributingId();
				try {
					if (DAWNDDEPlugin.isSupportedDAWNExtension(contributingId)) {
						ITemplateSection extension = (ITemplateSection) ((WizardElement) object).getTemplateElement()
								.createExecutableExtension("class");
						templates.add(extension);
					}
				} catch (CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, "org.eclipse.dde.ui",
							"Could not instantiate template " + contributingId, e), StatusManager.LOG);
					ok = false;
				}
			}
		}		
		return ok;
	}

	private void collectTemplates(Object[] children, WizardCollectionElement list) {
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof WizardCollectionElement) {
				WizardCollectionElement element = (WizardCollectionElement) children[i];
				collectTemplates(element.getChildren(), list);
				collectTemplates(element.getWizards().getChildren(), list);
			} else if (children[i] instanceof WizardElement) {
				WizardElement wizard = (WizardElement) children[i];
				if (wizard.isTemplate())
					list.getWizards().add(wizard);
			}
		}
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
	
	public ITemplateSection getSelectedTemplate(){
		for (ITemplateSection template : templates) {
			if (template.getUsedExtensionPoint().equals(extension)){
				return template;
			}				
		}
		return null;
	}

}
