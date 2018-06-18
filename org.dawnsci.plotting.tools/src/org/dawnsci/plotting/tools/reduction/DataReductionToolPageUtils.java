package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.util.DialogUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.tools.reduction.DataReductionWizard.AutoOpenMode;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReductionToolPageUtils {

	private static final Logger logger = LoggerFactory.getLogger(DataReductionToolPageUtils.class);
	
	public static WizardDialog getToolPageReductionWizardDialog(SliceFromSeriesMetadata sliceMetadata, IDataReductionToolPage tool) {
		DataReductionWizard wiz=null;
		
		try {
			wiz = (DataReductionWizard)EclipseUtils.openWizard(DataReductionWizard.ID, false);
		} catch (Exception e) {
			logger.error("Cannot open wizard "+DataReductionWizard.ID, e);
		}
		
		wiz.setAutoOpenMode(AutoOpenMode.EVENT);
		
		wiz.setData(new File(sliceMetadata.getFilePath()),
				    sliceMetadata.getDatasetName(),
				    tool,
				    sliceMetadata.getParent());
		
		List<DimsData> dd = new ArrayList<>();
		
		SliceInformation sliceInfo = sliceMetadata.getSliceInfo();
		sliceInfo.getDataDimensions();
		
		for (int i = 0; i < sliceMetadata.getParent().getShape().length; i++) {
			DimsData d = new DimsData(i);
			d.setPlotAxis(AxisType.RANGE);
			dd.add(d);
		}
		
		int[] dataDimensions = sliceInfo.getDataDimensions();
		
		if (dataDimensions.length == 1) {
			DimsData dimsData = dd.get(dataDimensions[0]);
			dimsData.setPlotAxis(AxisType.X);
		} else if (dataDimensions.length == 2) {
			DimsData dimsData = dd.get(dataDimensions[0]);
			dimsData.setPlotAxis(AxisType.Y);
			dimsData = dd.get(dataDimensions[1]);
			dimsData.setPlotAxis(AxisType.X);
		} else {
			//error 
			return null;
		}
		
		wiz.setSlice(sliceMetadata.getParent(), new DimsDataList(dd));
		
		WizardDialog wd = new  WizardDialog(Display.getDefault().getActiveShell(), wiz);
		wd.setTitle(wiz.getWindowTitle());
		wd.create();
		wd.getShell().setSize(650, 800);
		DialogUtils.centerDialog(Display.getDefault().getActiveShell(), wd.getShell());
		
		return wd;
	}
	
}
