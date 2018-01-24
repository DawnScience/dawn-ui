package org.dawnsci.datavis.manipulation;

import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class DataVisManipulationServiceManager {
	
	private static INexusFileFactory nexusFactory;

	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}

	public void setNexusFactory(INexusFileFactory nexusFactory) {
		DataVisManipulationServiceManager.nexusFactory = nexusFactory;
	}
}
