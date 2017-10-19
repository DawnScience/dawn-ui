package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.BatchSetupYAxes;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;

public class BatchTracking {

	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private String savePath;

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(String savepath1, BatchSavingAdvancedSettings[] bsas,
			BatchSetupMiscellaneousProperties bsmps, String imageFolderPath, String paramFile, String[] datFiles,
			boolean useTrajectory) {
		
		
//		SetupModel stmi = new SetupModel();
//		stmi.setImageFolderPath(imageFolderPath);
//
//		SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
//		sspi.setStm(stmi);
//		sspi.createGm();
//
//		FittingParametersInputReader.anglesAliasReaderFromNexus(paramFile);
//
//		FittingParametersInputReader.geometricalParametersReaderFromNexus(paramFile, sspi.getGm(), sspi.getDrm());
//
//		sspi.surfaceScatterPresenterBuildWithFrames(datFiles, sspi.getGm().getxName(), MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));
//
//		sspi.loadParameters(paramFile, useTrajectory);
//
//		BatchTracking bat = new BatchTracking();
//		bat.setSsp(sspi);
		
		int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(ssp.getFms().get(0).getRoiLocation());
		
		
		this.savePath = savepath1;

		ssp.regionOfInterestSetter(lenpt);

		ssp.regionOfInterestSetter(lenpt);

		boolean[] doneArray = new boolean[ssp.getDrm().getDatFilepaths().length];

		ssp.getDrm().setDoneArray(doneArray);

		new TrackingCore(doneArray, ssp, null, null, false, null, null);

		ssp.writeNexus(savePath+".nxs");

		yAxes[] yA = goodYAxes(bsmps.getBsya());

		for (BatchSavingAdvancedSettings bsa : bsas) {
			if (bsa != null) {
				SaveFormatSetting sfs = bsa.getSfs();
				for (yAxes y : yA) {
					if (bsa.isAllPoints()) {
						ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), false, sfs, ssp.getDrm().getCsdp(), y,
								savePath + "_" + sfs.getDisplayName() + "_" + "ALL_POINTS"+ "_" +y.getYAxisName());
					}
					if (bsa.isGoodPoints()) {
						ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), true, sfs, ssp.getDrm().getCsdp(), y,
								savePath + "_" + sfs.getDisplayName() + "_" + "GOOD_POINTS_ONLY"+ "_" +y.getYAxisName());
					}
				}
			}
		}

		return;

	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	private yAxes[] goodYAxes(BatchSetupYAxes[] bsya) {

		ArrayList<yAxes> a = new ArrayList<>();

		for (BatchSetupYAxes b : bsya) {
			if (b.isUse()) {
				a.add(b.getY());
			}
		}

		return a.toArray(new yAxes[a.size()]);
	}
}
