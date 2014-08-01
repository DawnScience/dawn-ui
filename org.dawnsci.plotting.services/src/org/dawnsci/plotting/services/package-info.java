
/**
 * How to migrate a service to OSGI declarative:
 * 1. Add the OSGI-INF/myfile.xml and edit it.
 * 2. Edit MANIFEST.MF to declare xml file.
 * 3. Edit build.properties to include the xml file.
 * 4. Edit Activator to no longer hard code adding of the service.
 * 5. In service add static block to print that it is started.
 * 6. Ensure that no work is done in the constructor.
 */
package org.dawnsci.plotting.services;

