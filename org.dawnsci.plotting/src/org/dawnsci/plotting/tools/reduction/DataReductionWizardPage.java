package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.slicing.DimensionalEvent;
import org.dawb.common.ui.slicing.DimensionalListener;
import org.dawb.common.ui.slicing.DimsData;
import org.dawb.common.ui.slicing.DimsDataList;
import org.dawb.common.ui.slicing.SliceUtils;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class DataReductionWizardPage extends AbstractSliceConversionPage {

	public DataReductionWizardPage(String pageName, String description, ImageDescriptor icon) {
		super(pageName, description, icon);
		setFileLabel("Output File");
	}

	@Override
	protected void createAdvanced(Composite parent) {
		

	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
        super.createContentAfterFileChoose(container);
		sliceComponent.setAxesVisible(true);
        sliceComponent.addDimensionalListener(new DimensionalListener() {			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				isPageValid();
			}
		});
	}

	@Override
	public void setContext(IConversionContext context) {
		
		super.setContext(context);
		if (context.getOutputPath()!=null) {
			setPath(context.getOutputPath());
		}
	}
	
	public boolean isPageValid() {
        if (!super.isPageValid()) return false;
        
        // Check dimensionality of slice again that which the tool
        // normally acts on.
        int rank=0;
        final DimsDataList dl = sliceComponent.getDimsDataList();
        for (DimsData dd : dl.getDimsData()) {
			if (dd.isRange()||dd.isSlice()) continue;
			rank++;
		}
        
        final IToolPage page = ((ToolConversionVisitor)context.getConversionVisitor()).getTool();
        if (rank!=page.getToolPageRole().getPreferredRank()) {
        	setErrorMessage("The tool '"+page.getTitle()+"' should be used with slices which give data with "+page.getToolPageRole().getPreferredRank()+" dimensions.");
            return false;
        }
        
		final File output = new File(getAbsoluteFilePath());
		if (output.exists() && (!output.canRead() || !output.canWrite())) {
			setErrorMessage("Cannot write to file '"+output.getName()+"'. Please choose a different file.");
			return false;
		}

        
		setErrorMessage(null);

        return true;
	}
	
	/**
	 * May be null
	 * @return
	 */
	protected List<IDataset> getNexusAxes() throws Exception {
		final Map<Integer, String> names = sliceComponent.getNexusAxes();
		final DimsDataList ddl = sliceComponent.getDimsDataList();
		
		IDataset x=null; IDataset y=null;
		for (DimsData dd : ddl.getDimsData()) {
			
			if (dd.getAxis()==0) {
				final String name = names.get(dd.getDimension()+1);
				x = SliceUtils.getNexusAxis(sliceComponent.getCurrentSlice(), name, false, null);
			}
			if (dd.getAxis()==1) {
				final String name = names.get(dd.getDimension()+1);
				y = SliceUtils.getNexusAxis(sliceComponent.getCurrentSlice(), name, false, null);
			}

		}
		if (x==null && y==null) return null;
		if (x!=null && y==null) return Arrays.asList(x);
		if (x!=null && y!=null) return Arrays.asList(x,y);
		return null;
	}

}
