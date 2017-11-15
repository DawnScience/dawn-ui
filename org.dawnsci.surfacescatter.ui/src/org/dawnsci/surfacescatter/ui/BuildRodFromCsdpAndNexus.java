package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.OverviewNexusObjectBuilderEnum;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;

public class BuildRodFromCsdpAndNexus {

	public void BuildRodFromCsdpAndNexus(String inFile) {
		
		SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
		sspi.createGm();

		
		NexusFile file = new NexusFileFactoryHDF5().newNexusFile(inFile);
		
		FittingParametersInputReader.anglesAliasReaderFromNexus(file);

		FittingParametersInputReader.geometricalParametersReaderFromNexus(file, sspi.getGm(), sspi.getDrm());
//
//		sspi.surfaceScatterPresenterBuildWithFrames(datFiles, sspi.getGm().getxName(),
//				MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));

		
	}
	
	private FrameModel frameModelBuilder(GroupNode g) {
		
		FrameModel fm = new FrameModel();
		

		for (OverviewNexusObjectBuilderEnum oe : OverviewNexusObjectBuilderEnum.values()) {
			try {
				oe.frameGroupNodePopulateFromFrameModelMethod(oe, nxData, fm);
			} catch (Exception j) {
				System.out.println(j.getMessage());
			}
			try {
				oe.frameExtractionMethod(oe, m, fm);
			} catch (Exception ji) {
				System.out.println(ji.getMessage());
			}
		}
		
		
		
		return fm;
		
	}
	
	
}
