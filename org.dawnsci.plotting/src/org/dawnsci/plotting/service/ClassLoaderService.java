package org.dawnsci.plotting.service;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.vecmath.Vector3d;

import org.dawb.common.services.IClassLoaderService;
import org.dawb.workbench.jmx.UserPlotBean;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

import com.thoughtworks.xstream.core.util.CompositeClassLoader;

/**
 * Defines the class loader that should be used for RMI communications.
 * The Loader will be able to load analysis classes which are communicated via RMI.
 * 
 * @author fcp94556
 *
 */
public class ClassLoaderService extends AbstractServiceFactory implements IClassLoaderService {

	private ClassLoader originalLoader;
	
	@Override
	public void setDataAnalysisClassLoaderActive(boolean active) {
		
	    ClassLoader classLoader;
		if (active) {
			originalLoader = Thread.currentThread().getContextClassLoader();
			final CompositeClassLoader customLoader = new CompositeClassLoader();
			customLoader.add(UserPlotBean.class.getClassLoader());
			customLoader.add(AbstractDataset.class.getClassLoader());
			customLoader.add(Vector3d.class.getClassLoader());
			classLoader = customLoader;
		} else {
			classLoader    = originalLoader;
			originalLoader = null;
		}
		
		if (classLoader==null) return;
		
		final ClassLoader finalCL = classLoader;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				Thread.currentThread().setContextClassLoader(finalCL);
				return null;
			}
		});

	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==IClassLoaderService.class) {
			return new ClassLoaderService(); // Important always new as has member data.
		} 
		return null;
	}

}
