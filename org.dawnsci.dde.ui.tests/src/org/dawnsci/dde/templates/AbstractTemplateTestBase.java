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
package org.dawnsci.dde.templates;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.IOUtils;
import org.dawnsci.dde.core.DAWNExtensionNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.Test;

/**
 * Base class for testing "New DAWN Plug-in Project" template wizards. Has
 * shared tests for catching build issues, builder and nature configuration.
 * 
 * @author Torkild U. Resheim
 */
public abstract class AbstractTemplateTestBase {
	
	protected static SWTWorkbenchBot bot;
	
	/** Folder for storing screenshots */
	private static File screenshotsDir;
	
	/** Number of screenshots taken */
	private int screenshotCount;

	/** Screenshot shadow radius */
	private static final int RADIUS = 16;

	protected static final String DIAMOND_LIGHT_SOURCE = "Diamond Light Source";
	

	static {
		screenshotsDir = new File("screenshots");
		screenshotsDir.mkdirs();
	}

	private static void fillRoundRectangleDropShadow(GC gc, Rectangle bounds, int radius) {
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		gc.setAlpha(0x8f / radius);
		for (int i = 0; i < radius; i++) {
			Rectangle shadowBounds = new Rectangle(bounds.x + i, bounds.y + i, bounds.width - (i * 2),
					bounds.height - (i * 2));
			gc.fillRoundRectangle(shadowBounds.x, shadowBounds.y, shadowBounds.width, shadowBounds.height, radius * 2,
					radius * 2);
		}
		gc.setAlpha(0xff);
	}

	protected void buildProject(final IProject project, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable build = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) throws CoreException {
				SubMonitor localmonitor = SubMonitor.convert(pm, "Building", 1);
				try {
					if (localmonitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, localmonitor.newChild(1));
				} finally {
					localmonitor.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(build, monitor);
	}

	protected IMarker getLaunchProblem(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		if (markers.length > 0) {
			for (int i = 0; i < markers.length; i++) {
				if (isLaunchProblem(markers[i])) {
					return markers[i];
				}
			}
		}
		return null;
	}

	/**
	 * Implement to return the name of the project.
	 * 
	 * @return the project name
	 */
	protected abstract String getProjectName();

	/**
	 * Implement to return the expected contents of <i>plugin.xml</i>
	 * 
	 * @return the plugin.xml contents
	 */
	protected abstract String getPluginContents();

	protected boolean hasBuilder(String id) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		IProjectDescription description = project.getDescription();
		for (ICommand iCommand : description.getBuildSpec()) {
			if (iCommand.getBuilderName().equals(id)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isLaunchProblem(IMarker problemMarker) throws CoreException {
		Integer severity = (Integer) problemMarker.getAttribute(IMarker.SEVERITY);
		if (severity != null) {
			return severity.intValue() >= IMarker.SEVERITY_ERROR;
		}
		return false;
	}

	/**
	 * Utility method for capturing a screenshot of a dialog or wizard window
	 * into a file.
	 */
	protected void takeScreenshot(final Shell shell, final String templateName) {
		shell.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Grab a screenshot of the dialog shell
				final Rectangle b = shell.getBounds();
				final Image screenshot = new Image(shell.getDisplay(), b.width, b.height);
				GC gc = new GC(shell.getDisplay());
				gc.copyArea(screenshot, b.x, b.y);

				// Create drop shadow image
				final Image image = new Image(shell.getDisplay(), b.width + RADIUS, b.height + RADIUS);
				GC gc2 = new GC(image);
				fillRoundRectangleDropShadow(gc2, image.getBounds(), RADIUS);
				gc2.drawImage(screenshot, RADIUS / 2, RADIUS / 2);
				screenshotCount++;
				String text = templateName.replace(' ', '_') + "_Page#" + screenshotCount + ".png";
				File file = new File(screenshotsDir, text);
				ImageLoader loader = new ImageLoader();
				if (file.exists()) {
					loader.load(file.getAbsolutePath());
					Image original = new Image(shell.getDisplay(), file.getAbsolutePath());
					if (!original.getImageData().equals(image.getImageData())) {
						loader.data = new ImageData[] { image.getImageData() };
						loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
					}
				} else {
					loader.data = new ImageData[] { image.getImageData() };
					loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
				}
				gc.dispose();
			}
		});
	}

	/**
	 * Tests whether the project builders are set correctly. Override to match a
	 * different set of builders.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testBuilders() throws CoreException {
		assertTrue(hasBuilder("org.eclipse.jdt.core.javabuilder"));
		assertTrue(hasBuilder("org.eclipse.pde.ManifestBuilder"));
		assertTrue(hasBuilder("org.eclipse.pde.SchemaBuilder"));
		assertTrue(hasBuilder("org.eclipse.pde.ds.core.builder"));
	}

	/**
	 * Verifies that the project builds without any errors.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testIncrementalBuild() throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		buildProject(project, new NullProgressMonitor());
		IMarker problem = getLaunchProblem(project);
		if (problem != null) {
			fail(problem.getAttribute(IMarker.MESSAGE).toString());
		}
	}

	/**
	 * Tests whether the project natures are set correctly. Override to match a
	 * different set of natures.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testNatures() throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		assertTrue(project.hasNature(DAWNExtensionNature.IDENTIFIER));
		assertTrue(project.hasNature("org.eclipse.pde.PluginNature"));
		assertTrue(project.hasNature("org.eclipse.jdt.core.javanature"));
	}

	/**
	 * Tests the contents of <i>plugin.xml</i>. The test is a simple string
	 * compare. It may be rewritten to test using internal PDE API.
	 * 
	 * @throws CoreException 
	 * @throws IOException 
	 */
	@Test
	public void testPlugin() throws CoreException, IOException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		IFile file = project.getFile(new Path("plugin.xml"));
		InputStream contents = file.getContents(false);
		InputStream expected = new ByteArrayInputStream(getPluginContents().getBytes());
		if (!isEqual(expected, contents)){
			contents = file.getContents();
			expected = new ByteArrayInputStream(getPluginContents().getBytes());
			StringWriter sw = new StringWriter();
			sw.write("Expected: \n");
			IOUtils.copy(expected, sw);
			sw.write("Found: \n");
			IOUtils.copy(contents, sw);
			fail(sw.toString());
		}
	}

	private static boolean isEqual(InputStream i1, InputStream i2)
	        throws IOException {

	    ReadableByteChannel ch1 = Channels.newChannel(i1);
	    ReadableByteChannel ch2 = Channels.newChannel(i2);

	    ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
	    ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

	    try {
	        while (true) {

	            int n1 = ch1.read(buf1);
	            int n2 = ch2.read(buf2);

	            if (n1 == -1 || n2 == -1) return n1 == n2;

	            buf1.flip();
	            buf2.flip();

	            for (int i = 0; i < Math.min(n1, n2); i++)
	                if (buf1.get() != buf2.get())
	                    return false;

	            buf1.compact();
	            buf2.compact();
	        }

	    } finally {
	        if (i1 != null) i1.close();
	        if (i2 != null) i2.close();
	    }
	}
}
