/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.util;

/**
 *
 */

import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.ui.viewerapp.ViewerAppSwt;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

public class JOGLChecker {

	private static final String RENDER_SOFTWARE_PROPERTY_STRING = "uk.ac.diamond.analysis.rcp.plotting.useSoftware";
	private static final String RENDER_HYBRID_PROPERTY_STRING = "uk.ac.diamond.analysis.rcp.plotting.useGL13";

	private static final Logger logger = LoggerFactory.getLogger(JOGLChecker.class);

	private static int maxXdim = 8192;
	private static int maxYdim = 8192;
	private static String glVendorStr = "";
	
	/**
	 * 
	 * @param viewer, may be null
	 * @param parent
	 * @return
	 */
	@SuppressWarnings("static-access")
	static public boolean canUseJOGL_OpenGL(String viewer, Composite parent) {
		
		if (viewer==null) viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);

		GLCanvas dummyCanvas = null;		
		boolean hasJOGL = true;
		try {
			Class.forName(viewer).newInstance();
			GLData data = new GLData();
			data.doubleBuffer = true;
			data.depthSize = 16;
			data.redSize = 8;
			data.greenSize = 8;
			data.blueSize = 8;
			data.alphaSize = 8;
			dummyCanvas = new GLCanvas(parent,SWT.NO_BACKGROUND,data);
			dummyCanvas.setCurrent();
			GLContext context = GLDrawableFactory.getFactory().createExternalGLContext();
			context.makeCurrent();
			context.getGL().glMultTransposeMatrixd(new double[]{0.0,0.0,0.0,0.0,
																0.0,0.0,0.0,0.0,
																0.0,0.0,0.0,0.0,
																0.0,0.0,0.0,0.0,
																0.0,0.0,0.0,0.0},0);
			glVendorStr = context.getGL().glGetString(GL.GL_VENDOR);
			IntBuffer intB = IntBuffer.allocate(1);
			context.getGL().glGetIntegerv(context.getGL().GL_MAX_TEXTURE_SIZE, intB);
			
			maxXdim = maxYdim = intB.get(0);
			boolean isOkay = false;
			while (!isOkay) {
				context.getGL().glTexImage2D(context.getGL().GL_PROXY_TEXTURE_2D, 0, context.getGL().GL_RGBA, maxXdim, 
	                     maxYdim, 0, context.getGL().GL_RGBA, context.getGL().GL_BYTE, null);
				context.getGL().glGetTexLevelParameteriv(context.getGL().GL_PROXY_TEXTURE_2D,0,context.getGL().GL_TEXTURE_WIDTH,intB);
				if (intB.get(0) == maxXdim) {
					isOkay = true;
				} else {
					maxXdim = (maxXdim >> 1);
					maxYdim = (maxYdim >> 1);
				}
			}
			context.release();
			context.destroy();
		} catch (NoClassDefFoundError ndfe) {
			logger.warn("No class found for JOGL hardware acceleration");
			hasJOGL = false;
		} catch (UnsatisfiedLinkError le) {
			logger.warn("JOGL linking error");
			hasJOGL = false;
		} catch (Throwable e) { //
			logger.warn("No JOGL using software render",e);
			hasJOGL = false;
		}
		if (dummyCanvas != null) dummyCanvas.dispose();
		return hasJOGL;
	}
	
	static public int getMaxTextureWidth() {
		return maxXdim;
	}
	
	static public int getMaxTextureHeight() {
		return maxYdim;
	}
	
	static public String getVendorName() {
		return glVendorStr;
	}
	
	/**
	 * 
	 * @return true if hardware enabled
	 */
	public static boolean isHardwareEnabled(final Composite parent) {
		boolean hasJOGL = true;
		String propString = System.getProperty(RENDER_SOFTWARE_PROPERTY_STRING);
		if (propString != null && propString.toLowerCase().equals("true")) {
			logger.warn("Force software render");
			hasJOGL = false;
		} else {
			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);
			hasJOGL = JOGLChecker.canUseJOGL_OpenGL(viewer, parent);
		}
		return hasJOGL;
	}

	/**
	 * 
	 * @param viewerApp
	 * @return
	 * @throws ClassCastException if viewerApp is not a ViewerAppSwt
	 */
	public static boolean isShadingSupported(AbstractViewerApp viewerApp) {
		boolean hasJOGLshaders = ((ViewerAppSwt) viewerApp).supportsShaders();
		if (Boolean.getBoolean(RENDER_HYBRID_PROPERTY_STRING)) {
			hasJOGLshaders = false;
		}
		return hasJOGLshaders;
	}
	
}
