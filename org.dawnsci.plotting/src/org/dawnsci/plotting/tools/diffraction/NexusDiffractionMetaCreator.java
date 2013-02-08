package org.dawnsci.plotting.tools.diffraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.nexus.IFindInNexus;
import org.dawb.hdf5.nexus.NexusFindDatasetByName;
import org.dawb.hdf5.nexus.NexusFindGroupByAttributeText;
import org.dawb.hdf5.nexus.NexusUtils;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetaDataAdapter;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

public class NexusDiffractionMetaCreator {
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter wrapping the argument metadata
	 *  and populated with default values from the preferences store and from the nexus file
	 */
	public static IDiffractionMetadata diffractionMetadataFromNexus(String filePath, final IMetaData metaData, int[] imageSize) {
		
		final DetectorProperties detprop = DiffractionDefaultMetadata.getPersistedDetectorProperties(imageSize);
		final DiffractionCrystalEnvironment diffcrys = DiffractionDefaultMetadata.getPersistedDiffractionCrystalEnvironment();
		
		IHierarchicalDataFile hiFile = null;
		
		try {
			hiFile = HierarchicalDataFactory.getReader(filePath);
			
			Group rootGroup = hiFile.getRoot();
			
			//Check only one entry in root - might not act on it at the moment but may be useful to know
			
			if (rootGroup.getMemberList().get(0) instanceof Group) {
				
				boolean isEntry = NexusUtils.getNexusGroupAttributeValue((Group)rootGroup.getMemberList().get(0),
																		NexusUtils.NXCLASS).equals("NXentry");
				//Find NXinstrument (hopefully there is only one!)
				NexusFindGroupByAttributeText finder = new NexusFindGroupByAttributeText("NXinstrument",NexusUtils.NXCLASS);
				List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(finder, (Group)rootGroup.getMemberList().get(0), true);
				if (hOb.isEmpty() || !(hOb.get(0) instanceof Group)) { return null;}
				Group nxInstrument = (Group)hOb.get(0);
				
				//Find nxDetectors in instrument
				// TODO maybe use the image size to make sure we have the correct detector
				//  this will also validate detprop.setPx(px) detprop.setPy(py) 
				List<Group> nxDetectors = findNXdetetors(nxInstrument, "pixel");
				
				if (nxDetectors.isEmpty()) {return null;}
				// TODO do something better if list is empty or has more than one object
				
				//populate the crystal environ
				populateFromNexus(nxDetectors.get(0), diffcrys);
				
				populateFromNexus(nxDetectors.get(0), detprop);
				
				//find nx mono
				finder.attributeValue = "NXmonochromator";
				hOb = NexusUtils.nexusBreadthFirstSearch(finder, (Group)rootGroup.getMemberList().get(0), true);
				if (hOb.isEmpty() || !(hOb.get(0) instanceof Group)) { return null;}
				Group mxMono = (Group)hOb.get(0);
				updateEnergy(mxMono,diffcrys);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (hiFile!= null)
				try {
					hiFile.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
		final DiffractionCrystalEnvironment diffClone = diffcrys.clone();
		final DetectorProperties detpropClone = detprop.clone(); 
		
		return new DiffractionMetaDataAdapter() {
			
			@Override
			public DiffractionCrystalEnvironment getDiffractionCrystalEnvironment() {
				return diffClone;
			}
			
			@Override
			public DetectorProperties getDetector2DProperties() {
				return detpropClone;
			}
			
			@Override
			public IDiffractionMetadata clone(){
				return null;
			}
			
			@Override
			public DetectorProperties getOriginalDetector2DProperties() {
				return detprop;
			}
			
			@Override
			public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
				return diffcrys;
			}
			
			@Override
			public Collection<String> getMetaNames() throws Exception{
				return metaData.getMetaNames();
			}

			@Override
			public String getMetaValue(String fullAttributeKey) throws Exception{
				return metaData.getMetaValue(fullAttributeKey).toString();
			}
			
			@Override
			public Collection<String> getDataNames() {
				return metaData.getDataNames();
			}

			@Override
			public Map<String, Integer> getDataSizes() {
				return metaData.getDataSizes();
			}

			@Override
			public Map<String, int[]> getDataShapes() {
				return metaData.getDataShapes();
			}
			
		};
	}
	
	private static void populateFromNexus(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
		updateExposureTime(nexusGroup,diffcrys);
		// TODO diffcrys.setPhiRange(phiRange);
		// TODO diffcrys.setPhiStart(phiStart);
	}
	
	private static void updateEnergy(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
		H5ScalarDS dataset = getDataset(nexusGroup, "energy");
		if (dataset == null) return;
		double[] energyValues = getDoubleData(dataset);
		if (energyValues == null) return;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return;
		if (units.equals("keV")) {diffcrys.setWavelengthFromEnergykeV(energyValues[0]);}
	}
	
	private static void updateExposureTime(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
		H5ScalarDS dataset = getDataset(nexusGroup, "count_time");
		if (dataset == null) return;
		float[] values = getFloatData(dataset);
		if (values == null) return;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return;
		if (units.equals("s")) {diffcrys.setExposureTime(values[0]);}
	}
	
	private static void populateFromNexus(Group nxDetector, DetectorProperties detprop) {
		
		updatePixelSize(nxDetector,detprop);
		// TODO detprop.setBeamCentreCoords(coords);
		// TODO detprop.setDetectorDistance(distance);
		// TODO detprop.setNormalAnglesInDegrees(yaw, pitch, roll);
	}
	
	private static void updatePixelSize(Group nexusGroup, DetectorProperties detprop) {
		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_size");
		if (dataset == null) return;
		double[] xPx = getDoubleData(dataset);
		if (xPx == null) return;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return;
		
		if (units.equals("mm")) {detprop.setVPxSize(xPx[0]);}
		else if (units.equals("m")) {detprop.setVPxSize(xPx[0]*1000);}
		
		dataset = getDataset(nexusGroup, "y_pixel_size");
		if (dataset == null) return;
		String unitsy = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		double[] yPx = getDoubleData(dataset);
		if (yPx == null) return;
		if (unitsy == null) return;
		if (unitsy.equals("mm")) {detprop.setHPxSize(xPx[0]);}
		else if (unitsy.equals("m")) {detprop.setHPxSize(xPx[0]*1000);}
		
	}
	
	private static H5ScalarDS getDataset(Group group, String name) {
		NexusFindDatasetByName dataFinder = new NexusFindDatasetByName(name);
		List<HObject>  hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder, group, true);
		hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder,group, false);
		if (hOb.isEmpty() || !(hOb.get(0) instanceof H5ScalarDS)) { return null;}
		H5ScalarDS h5data = (H5ScalarDS)hOb.get(0);
		
		return h5data;
	}
	
	private static double[] getDoubleData(H5ScalarDS dataset) {
		//should be single value or vector
		if (dataset.getRank() > 2) { return null;}
		try {
			return (double[]) dataset.getData();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static float[] getFloatData(H5ScalarDS dataset) {
		//should be single value or vector
		if (dataset.getRank() > 2) { return null;}
		try {
			return (float[]) dataset.getData();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static List<Group> findNXdetetors(Group nxInstrument, String childNameContains) {
		final String groupText = "NXdetector";
		final String childText = childNameContains;;
		
		IFindInNexus findWithChild = new IFindInNexus() {
			
			@Override
			public boolean inNexus(HObject nexusObject) {
				if(nexusObject instanceof Group) {
					if (NexusUtils.getNexusGroupAttributeValue((Group)nexusObject,NexusUtils.NXCLASS).toLowerCase().equals(groupText.toLowerCase())) {
						for (Object ob: ((Group)nexusObject).getMemberList()) {
							if(ob instanceof H5ScalarDS) {
								if (((H5ScalarDS)ob).getName().toLowerCase().contains((childText.toLowerCase()))) {
									return true;
								}
							}
						}
					}
				}
				return false;
			}
		};
		
		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(findWithChild, nxInstrument, false);
		
		List<Group> detectorGroups = new ArrayList<Group>(hOb.size());
		
		for (HObject ob : hOb) {
			if (ob instanceof Group) {
				detectorGroups.add((Group)ob);
			}
		}
		
		return detectorGroups;
	}
}
