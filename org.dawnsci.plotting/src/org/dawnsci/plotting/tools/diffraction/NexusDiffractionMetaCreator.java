package org.dawnsci.plotting.tools.diffraction;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5ScalarDS;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.nexus.IFindInNexus;
import org.dawb.hdf5.nexus.NexusFindDatasetByName;
import org.dawb.hdf5.nexus.NexusFindGroupByAttributeText;
import org.dawb.hdf5.nexus.NexusUtils;
import org.dawnsci.plotting.preference.detector.DiffractionDetectorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class NexusDiffractionMetaCreator {
	
	private static final Logger logger = LoggerFactory.getLogger(NexusDiffractionMetaCreator.class);
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter wrapping the argument metadata
	 *  and populated with default values from the preferences store and from the nexus file
	 */
	public static IDiffractionMetadata diffractionMetadataFromNexus(String filePath, int[] imageSize) {
		
		if (!HierarchicalDataFactory.isHDF5(filePath)) return null;
		
		final DetectorProperties detprop = DiffractionDefaultMetadata.getPersistedDetectorProperties(imageSize);
		final DiffractionCrystalEnvironment diffcrys = DiffractionDefaultMetadata.getPersistedDiffractionCrystalEnvironment();
		
		IHierarchicalDataFile hiFile = null;
		
		try {
			hiFile = HierarchicalDataFactory.getReader(filePath);
			
			Group rootGroup = hiFile.getRoot();
			
			//Check only one entry in root - might not act on it at the moment but may be useful to know
			if (rootGroup.getMemberList().size() > 1)
				logger.warn("More than one root node in file, metadata may be incorrect");
			
			if (rootGroup.getMemberList().get(0) instanceof Group) {
				
				//Find NXinstrument (hopefully there is only one!)
				NexusFindGroupByAttributeText finder = new NexusFindGroupByAttributeText("NXinstrument",NexusUtils.NXCLASS);
				List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(finder, (Group)rootGroup.getMemberList().get(0), true);
				//if no instrument best just return null
				if (hOb.isEmpty() || !(hOb.get(0) instanceof Group)) { return null;}
				Group nxInstrument = (Group)hOb.get(0);
				
				//Find nxDetectors in instrument
				// TODO should probably change to find data then locate correct
				// detector from image size
				List<Group> nxDetectors = findNXdetetors(nxInstrument, "pixel");
				
				Group searchNode = null;
				
				// use the image size to make sure we have the correct detector
				//  this will also validate detprop.setPx(px) detprop.setPy(py) 
				if (nxDetectors.size() > 1) {
					
					for (Group detector : nxDetectors) {
						H5ScalarDS dataset = getDataset(detector, "data");
						
						long[] dataShape = dataset.getDims();
						
						boolean matchesX = false;
						boolean matchesY = false;
						
						for (long val : dataShape) {
							if (val == imageSize[0]) matchesX = true;
							else if (val == imageSize[1]) matchesY = true;
						}
						
						if (matchesX & matchesY) {
							searchNode = detector;
						}
						
					}
					
				}
				
				//if no detectors with pixel in search the entire nxInstrument group
				if (nxDetectors.isEmpty() || searchNode == null) {searchNode = nxInstrument;}
				
				//populate the crystal environ
				populateFromNexus(searchNode, diffcrys);
				
				populateFromNexus(searchNode, detprop, imageSize);
				
				//find nx mono
				finder.attributeValue = "NXmonochromator";
				hOb = NexusUtils.nexusBreadthFirstSearch(finder, (Group)rootGroup.getMemberList().get(0), true);
				if (!hOb.isEmpty() && (hOb.get(0) instanceof Group)) {
					Group mxMono = (Group)hOb.get(0);
					updateEnergy(mxMono,diffcrys);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (hiFile!= null)
				try {
					hiFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		return new DiffractionMetadata(filePath,detprop,diffcrys);
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
	
	private static void updateBeamCentre(Group nexusGroup, DetectorProperties detprop) {
		H5ScalarDS dataset = getDataset(nexusGroup, "beam centre");
		if (dataset == null) return;
		double[] values = getDoubleData(dataset);
		if (values == null) return;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return;
		if (units.equals("pixels")) {detprop.setBeamCentreCoords(values);}
	}
	
	private static void updateDetectorDistance(Group nexusGroup, DetectorProperties detprop) {
		H5ScalarDS dataset = getDataset(nexusGroup, "camera length");
		if (dataset == null) return;
		double[] values = getDoubleData(dataset);
		if (values == null) return;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return;
		if (units.equals("mm")) {detprop.setBeamCentreDistance(values[0]);}
	}
	
	
	private static void populateFromNexus(Group nxDetector, DetectorProperties detprop, int[] shape) {
		
		if (!updatePixelSize(nxDetector,detprop)) {
			double[] pixelSize = DiffractionDetectorHelper.getXYPixelSizeMM(shape);
			if (pixelSize != null) {
				detprop.setHPxSize(pixelSize[0]);
				detprop.setVPxSize(pixelSize[0]);
			}
			
		}
		updateBeamCentre(nxDetector,detprop);
		updateDetectorDistance(nxDetector,detprop);
		// TODO detprop.setNormalAnglesInDegrees(yaw, pitch, roll);
	}
	
	private static boolean updatePixelSize(Group nexusGroup, DetectorProperties detprop) {
		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_size");
		if (dataset == null) return false;
		double[] xPx = getDoubleData(dataset);
		if (xPx == null) return false;
		String units = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		if (units == null) return false;
		
		if (units.equals("mm")) {detprop.setVPxSize(xPx[0]);}
		else if (units.equals("m")) {detprop.setVPxSize(xPx[0]*1000);}
		
		dataset = getDataset(nexusGroup, "y_pixel_size");
		if (dataset == null) return false;
		String unitsy = NexusUtils.getNexusGroupAttributeValue(dataset, "units");
		double[] yPx = getDoubleData(dataset);
		if (yPx == null) return false;
		if (unitsy == null) return false;
		if (unitsy.equals("mm")) {
			detprop.setHPxSize(xPx[0]);
			return true;
		} else if (unitsy.equals("m")) {
			detprop.setHPxSize(xPx[0]*1000);
			return true;
		}
		return false;
		
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
