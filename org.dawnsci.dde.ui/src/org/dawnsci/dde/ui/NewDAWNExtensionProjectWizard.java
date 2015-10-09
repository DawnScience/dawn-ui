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

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.dawnsci.dde.core.DAWNExtensionNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.elements.ElementList;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.pde.internal.ui.wizards.WizardCollectionElement;
import org.eclipse.pde.internal.ui.wizards.WizardElement;
import org.eclipse.pde.internal.ui.wizards.extension.NewExtensionRegistryReader;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.pde.internal.ui.wizards.templates.NewExtensionTemplateWizard;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.AbstractNewPluginTemplateWizard;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * <p>
 * This wizard provides means to create a new DAWN extension. It is similar to
 * {@link NewExtensionTemplateWizard} in that it is used to create plug-in
 * content. However this implementation filters out non DAWN specific templates.
 * </p>
 * <p>
 * In order to provide templates for this wizard one must declare extensions
 * using the <b>org.eclipse.pde.ui.newExtension</b> extension point. An
 * associated template must also be declared using the
 * <b>org.eclipse.pde.ui.templates</b> extension point. The template is used to
 * populate wizard pages and product content.
 * </p>
 * 
 * @see NewExtensionTemplateWizard
 */
@SuppressWarnings("restriction")
public class NewDAWNExtensionProjectWizard extends AbstractNewPluginTemplateWizard implements INewWizard {
	
	final String PLUGIN_ID = "org.dawnsci.dde.ui";

	/** Initial content of the .classpath file */
	private final String DOT_CLASSPATH = 
			"<classpath>\n" + 
			" <classpathentry kind=\"src\" path=\"src\"/>\n" + 
			" <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n" + 
			" <classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>\n" + 
			" <classpathentry kind=\"output\" path=\"bin\"/>\n" + 
			"</classpath>";
	
	/** Initial content of the plugin.xml file */
	private final String PLUGIN_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<?eclipse version=\"3.4\"?>\n" + 
			"<plugin>\n" + 
			"</plugin>\n"; 
	
	/** Initial content of the build.properties file */
	private final String BUILD_PROPERTIES = 
			"source.. = src/\n" + 
			"output.. = bin/\n" + 
			"bin.includes = META-INF/,\\\n" + 
			"               .,\\\n" + 
			"               plugin.xml\n";
	
	/** Initial content of the MANIFEST.MF file */
	private final String MANIFEST_MF = 
			"Manifest-Version: 1.0\n" + 
			"Bundle-ManifestVersion: 2\n" + 
			"Bundle-Name: @BundleName\n" + 
			"Bundle-SymbolicName: @SymbolicName\n" + 
			"Bundle-Vendor: @BundleVendor\n" + 
			"Bundle-Version: @Version\n" + 
			"Bundle-RequiredExecutionEnvironment: JavaSE-1.7\n";
	
	private final String JDT_SETTINGS = 
			"eclipse.preferences.version=1\n" + 
			"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled\n" + 
			"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.7\n" + 
			"org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve\n" + 
			"org.eclipse.jdt.core.compiler.compliance=1.7\n" + 
			"org.eclipse.jdt.core.compiler.debug.lineNumber=generate\n" + 
			"org.eclipse.jdt.core.compiler.debug.localVariable=generate\n" + 
			"org.eclipse.jdt.core.compiler.debug.sourceFile=generate\n" + 
			"org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n" + 
			"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n" + 
			"org.eclipse.jdt.core.compiler.source=1.7\n"; 
			
	
	private DAWNExtensionProjectWizardPage p1;

	private WizardCollectionElement fWizardCollection;
	
	public static final String PLUGIN_POINT = "newExtension"; //$NON-NLS-1$

	/** A list of all applicable templates */
	private ArrayList<ITemplateSection> templates;

	/** The selected template section */
	private ITemplateSection fSection;
	
	public NewDAWNExtensionProjectWizard() {
		setDefaultPageImageDescriptor(DAWNDDEPlugin.imageDescriptorFromPlugin("org.dawnsci.dde.ui", "/icons/wizban/project_wiz.gif"));
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void init(IFieldData data) {
		super.init(data);
		loadTemplateCollection();
		setWindowTitle("New DAWN Plug-in Project");	
	}
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		init(new PluginFieldData());
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		if (page.getName().equals("p1")){
			((PluginFieldData)getData()).setId(p1.getBundleIdentifier());
			String selectedExtension = p1.getExtensionId();
			// determine whether or not the selected extension has an associated template.
			for (ITemplateSection template : templates) {
				if (template.getUsedExtensionPoint().equals(selectedExtension)){
					// add the template pages to the wizard
					if (!template.getPagesAdded()){
						template.addPages(this);
					}
					fSection = template;						
					return template.getPage(0);		
				}				
			}
			// no associated template, so no next page
			return null;
		}
		return nextPage;
	}
	
	@Override
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					final IProject project = createNewProject();
					// only execute this bit if a template has been selected
					if (fSection != null) {
						ModelModification modification = new ModelModification(project) {
							@Override
							protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
								if (model instanceof IPluginModelBase) {
									updateDependencies((IPluginModelBase) model);
									fSection.execute(project, (IPluginModelBase)model, monitor);
								}
							}
						};
						PDEModelUtility.modifyModel(modification, monitor);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}

		};
		try {
			getContainer().run(false, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Creates a new project.
	 *  
	 * @throws CoreException
	 */
	protected IProject createNewProject() throws CoreException {

		// get a project handle
		final IProject newProjectHandle = p1.getProjectHandle();

		// get a project descriptor
		URI location = null;
		if (!p1.useDefaults()) {
			location = p1.getLocationURI();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 3];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = DAWNExtensionNature.IDENTIFIER;
		newNatures[natures.length+1] = "org.eclipse.pde.PluginNature";
		newNatures[natures.length+2] = "org.eclipse.jdt.core.javanature";
		description.setNatureIds(newNatures);

		// add relevant project builders
		addBuilder(description, "org.eclipse.jdt.core.javabuilder");
		addBuilder(description, "org.eclipse.pde.ManifestBuilder");
		addBuilder(description, "org.eclipse.pde.SchemaBuilder");
		addBuilder(description, "org.eclipse.pde.ds.core.builder");

		// create the new project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			protected void execute(IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);
				
				// create build.properties
				IFile f1 = newProjectHandle.getFile("build.properties");
				f1.create(new ByteArrayInputStream(BUILD_PROPERTIES.getBytes(StandardCharsets.UTF_8)), true,  monitor);

				// create the classpath specification
				IFile f2 = newProjectHandle.getFile(".classpath");
				f2.create(new ByteArrayInputStream(DOT_CLASSPATH.getBytes(StandardCharsets.UTF_8)), true,  monitor);
								
				// create org.eclipse.jdt.core.prefs
				createFolder(newProjectHandle,".settings",monitor);
				IFile f3 = newProjectHandle.getFile(".settings/org.eclipse.jdt.core.prefs");
				f3.create(new ByteArrayInputStream(JDT_SETTINGS.getBytes(StandardCharsets.UTF_8)), true,  monitor);

				// create manifest.mf
				createFolder(newProjectHandle,"META-INF",monitor);
				IFile f4 = newProjectHandle.getFile("META-INF/MANIFEST.MF");
				String mf = MANIFEST_MF.replace("@Version", p1.getBundleVersion());
				mf = mf.replace("@SymbolicName", p1.getBundleIdentifier());				
				mf = mf.replace("@BundleVendor", p1.getBundleVendor());				
				mf = mf.replace("@BundleName", p1.getBundleName());				
				f4.create(new ByteArrayInputStream(mf.getBytes(StandardCharsets.UTF_8)), true,  monitor);
				
				// create the source folder
				createFolder(newProjectHandle,"src",monitor);
				
				// create plugin.xml
				IFile plugin_xml = newProjectHandle.getFile("plugin.xml");
				plugin_xml.create(new ByteArrayInputStream(PLUGIN_XML.getBytes(StandardCharsets.UTF_8)), true, monitor);
				
				// an empty extension should only be added when there is no
				// template selected
				if (fSection == null){
					String extensionId = p1.getExtensionId();
					addExtension(newProjectHandle, monitor, extensionId);
				}
			}
			
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Could not set up new DAWN project",e));
		}
		return newProjectHandle;
	}
	
	private void createFolder(@NonNull IProject project, @NonNull String name, @NonNull IProgressMonitor monitor) throws CoreException{
		IFolder folder = project.getFolder(name);
		folder.create(true, true, monitor);		
	}
	

	private void createProject(@NonNull IProjectDescription description, @NonNull IProject projectHandle, @NonNull IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			projectHandle.create(description, new SubProgressMonitor(monitor, 1000));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Adds the specified project builder to the project settings.
	 * 
	 * @param project
	 *            the project
	 * @param id
	 *            the identifier of the project builder
	 * @throws CoreException
	 */
	private void addBuilder(@NonNull IProjectDescription project, @NonNull String id) throws CoreException {
		ICommand[] commands = project.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(id))
				return;
		// add builder to project
		ICommand command = project.newCommand();
		command.setBuilderName(id);
		ICommand[] nc = new ICommand[commands.length + 1];
		// Add it before other builders.
		System.arraycopy(commands, 0, nc, 1, commands.length);
		nc[0] = command;
		project.setBuildSpec(nc);
	}	

	/**
	 * Adds an extension point to the project.
	 * 
	 * @param project
	 *            the project handle
	 * @param monitor
	 *            the progress monitor
	 * @param extensionId
	 *            identifier of the extension point
	 */
	private void addExtension(@NonNull final IProject project, @NonNull IProgressMonitor monitor, @NonNull final String extensionId) {
		ModelModification modification = new ModelModification(project) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				if (model instanceof IPluginModelBase) {
					IPluginExtension extension = ((ISharedPluginModel) model).getFactory().createExtension();
					extension.setPoint(extensionId);
					((IPluginModelBase) model).getPluginBase().add(extension);
				}
			}
		};
		PDEModelUtility.modifyModel(modification, monitor);
	}
	
	/**
	 * Locate all wizards extending "org.eclipse.pde.ui.newExtension" and place
	 * instances of the associated templates into the templates array.
	 */
	private void loadTemplateCollection() {
		NewExtensionRegistryReader reader = new NewExtensionRegistryReader();
		fWizardCollection = (WizardCollectionElement) reader.readRegistry(PDEPlugin.getPluginId(), PLUGIN_POINT, false);
		WizardCollectionElement templateCollection = new WizardCollectionElement("", "", null);
		collectTemplates(fWizardCollection.getChildren(), templateCollection);
		templates = new ArrayList<>();
		ElementList wizards = templateCollection.getWizards();
		Object[] children = wizards.getChildren();
		for (Object object : children) {
			if (object instanceof WizardElement){
				try {
					if (DAWNDDEPlugin.isSupportedDAWNExtension(((WizardElement) object).getContributingId())){
						ITemplateSection extension = (ITemplateSection) ((WizardElement) object).getTemplateElement().createExecutableExtension("class");
						templates.add(extension);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
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

	private void updateDependencies(IPluginModelBase model) throws CoreException {
		IPluginReference[] refs = fSection.getDependencies(model.getPluginBase().getSchemaVersion());
		for (int i = 0; i < refs.length; i++) {
			IPluginReference ref = refs[i];
			if (!modelContains(model, ref)) {
				IPluginImport iimport = model.getPluginFactory().createImport();
				iimport.setId(ref.getId());
				iimport.setMatch(ref.getMatch());
				iimport.setVersion(ref.getVersion());
				model.getPluginBase().add(iimport);
			}
		}
	}

	private boolean modelContains(IPluginModelBase model, IPluginReference ref) {
		IPluginBase plugin = model.getPluginBase();
		IPluginImport[] imports = plugin.getImports();
		for (int i = 0; i < imports.length; i++) {
			IPluginImport iimport = imports[i];
			if (iimport.getId().equals(ref.getId())) {
				// good enough
				return true;
			}
		}
		return false;
	}

	@Override
	protected void addAdditionalPages() {
		p1 = new DAWNExtensionProjectWizardPage("p1"); //$NON-NLS-1$
		addPage(p1);
	}

	@Override
	public ITemplateSection[] getTemplateSections() {
		return null;
	}

}
