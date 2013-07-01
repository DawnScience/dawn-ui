package org.dawnsci.plotting.system.dialog;

import java.util.Arrays;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.decorator.BoundsDecorator;
import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IValueChangeListener;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.dawnsci.common.widgets.decorator.ValueChangeEvent;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.histogram.HistogramBound;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
/**
 * TODO Replace with alternate widget library.
 */

public class ImageTraceComposite extends Composite {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageTraceComposite.class);

	private IImageTrace     imageTrace;
	private BoundsDecorator maximum, minimum, minCut, maxCut, lo, hi;
	private CCombo          downsampleChoice, histoChoice;
    private ColorSelector   minCutColor, maxCutColor, nanColor;
	/**
	 * 
	 * @param dialog
	 * @param plottingSystem - may be null!
	 * @param imageTrace
	 */
	public ImageTraceComposite(final Composite       parent,
			                   final Dialog          dialog, 
			                   final IPlottingSystem plottingSystem, 
			                   final IImageTrace     imageTrace) {
		
		super(parent, SWT.NONE);
		this.imageTrace      = imageTrace;

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(1, false));
		
		Label label;
		
		final Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		top.setLayout(new GridLayout(2, false));
		
//		label = new Label(top, SWT.NONE);
//		label.setText("Name");
//		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
//		nameText = new Text(top, SWT.BORDER | SWT.SINGLE);
//		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));		
//        nameText.setText(imageTrace.getName());
		
		final Group group = new Group(this, SWT.NONE);
		group.setText("Histogramming");
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		group.setLayout(new GridLayout(2, false));
		
		final int dType = ((AbstractDataset)imageTrace.getData()).getDtype();
		boolean isInt = dType==AbstractDataset.INT16 ||
				        dType==AbstractDataset.INT32 ||
				        dType==AbstractDataset.INT64;

		label = new Label(group, SWT.NONE);
		label.setText("Minimum Intensity");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		Text minimumBox = new Text(group, SWT.BORDER);
		minimumBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.minimum = isInt ? new IntegerDecorator(minimumBox) : new FloatDecorator(minimumBox);
		if (imageTrace.getMin()!=null) minimum.setValue(imageTrace.getMin().doubleValue());

		label = new Label(group, SWT.NONE);
		label.setText("Maximum Intensity");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		Text maximumBox = new Text(group, SWT.BORDER);
		maximumBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.maximum = isInt ? new IntegerDecorator(maximumBox) : new FloatDecorator(maximumBox);
		if (imageTrace.getMax()!=null) maximum.setValue(imageTrace.getMax().doubleValue());
      		
		maximum.setMaximum(imageTrace.getData().max().doubleValue());
		maximum.setMinimum(minimum);
	    minimum.setMaximum(maximum);
		minimum.setMinimum(imageTrace.getData().min().doubleValue());
	
		
		label = new Label(group, SWT.NONE);
		label.setText("Downsampling Type");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		this.downsampleChoice = new CCombo(group, SWT.READ_ONLY|SWT.BORDER);
		downsampleChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (DownsampleType ds : DownsampleType.values()) {
			downsampleChoice.add(ds.getLabel());
		}
		downsampleChoice.setToolTipText("The algorithm used when downsampling the full image for display.");
		downsampleChoice.select(imageTrace.getDownsampleType().getIndex());
		
		Label histolabel = new Label(group, SWT.NONE);
		histolabel.setText("Histogram Type");
		histolabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		this.histoChoice = new CCombo(group, SWT.READ_ONLY|SWT.BORDER);
		histoChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (HistoType ht : HistoType.values()) {
            histoChoice.add(ht.getLabel());
		}
		histoChoice.setToolTipText("The algorithm used when histogramming the downsampled image.\nNOTE: median is much slower. If you change this, max and min will be recalculated.");
		histoChoice.select(imageTrace.getHistoType().getIndex());
		
		final Composite outlierComp = new Composite(group, SWT.NONE);
		outlierComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		outlierComp.setLayout(new GridLayout(2, false));

		histoChoice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
				if (imageTrace.getHistoType()!=type) {
					imageTrace.setHistoType(type);
					maximum.setValue(imageTrace.getMax().doubleValue());
					minimum.setValue(imageTrace.getMin().doubleValue());
				}
				GridUtils.setVisible(outlierComp, type==HistoType.OUTLIER_VALUES);
				outlierComp.getParent().layout(new Control[]{outlierComp});
				layout();
				getParent().layout(new Control[]{ImageTraceComposite.this});
				getShell().layout();
			}
		});

		if (imageTrace.getData() instanceof RGBDataset) {
			histoChoice.setEnabled(false);
		}
		
		label = new Label(outlierComp, SWT.NONE);
		label.setText("Outlier low");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		
		Text lowBox = new Text(outlierComp, SWT.BORDER);
		lowBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.lo = new FloatDecorator(lowBox);
		if (imageTrace.getImageServiceBean()!=null) lo.setValue(imageTrace.getImageServiceBean().getLo());
		lo.addValueChangeListener(new IValueChangeListener() {		
			@Override
			public void valueValidating(ValueChangeEvent evt) {
				if (lo.isError()) return;
				final double orig = imageTrace.getImageServiceBean().getLo();
				if (orig==evt.getValue().doubleValue()) return;
				try {
					HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
					imageTrace.getImageServiceBean().setLo(evt.getValue().doubleValue());
					boolean ok = imageTrace.setHistoType(type);
					if (!ok) throw new Exception("Histo not working!");
					getPreferenceStore().setValue(BasePlottingConstants.HISTO_LO, evt.getValue().doubleValue());
					maximum.setValue(imageTrace.getMax().doubleValue());
					minimum.setValue(imageTrace.getMin().doubleValue());
				} catch (Throwable ne) {
					imageTrace.getImageServiceBean().setLo(orig);
				}
			}
		});	

		label = new Label(outlierComp, SWT.NONE);
		label.setText("Outlier high");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		
		Text hiBox = new Text(outlierComp, SWT.BORDER);
		hiBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.hi = new FloatDecorator(hiBox);
		if (imageTrace.getImageServiceBean()!=null) hi.setValue(imageTrace.getImageServiceBean().getHi());
		hi.addValueChangeListener(new IValueChangeListener() {		
			@Override
			public void valueValidating(ValueChangeEvent evt) {
				if (hi.isError()) return;
				final double orig = imageTrace.getImageServiceBean().getHi();
				if (orig==evt.getValue().doubleValue()) return;
				try {
					HistoType type = HistoType.values()[histoChoice.getSelectionIndex()];
					imageTrace.getImageServiceBean().setHi(evt.getValue().doubleValue());
					boolean ok = imageTrace.setHistoType(type);
					if (!ok) throw new Exception("Histo not working!");
					getPreferenceStore().setValue(BasePlottingConstants.HISTO_HI, evt.getValue().doubleValue());
					maximum.setValue(imageTrace.getMax().doubleValue());
					minimum.setValue(imageTrace.getMin().doubleValue());
				} catch (Throwable ne) {
					imageTrace.getImageServiceBean().setHi(orig);
				}
			}
		});
      		
		hi.setMaximum(99.999);
		hi.setMinimum(lo);
		lo.setMaximum(hi);
		lo.setMinimum(0.001);
		
		if (imageTrace.getData() instanceof RGBDataset) {
			histolabel.setEnabled(false);
			histoChoice.setEnabled(false);
			hiBox.setEnabled(false);
			lowBox.setEnabled(false);
		}

		GridUtils.setVisible(outlierComp, imageTrace.getHistoType()==HistoType.OUTLIER_VALUES);
		
		if (plottingSystem!=null) {
			label = new Label(group, SWT.NONE);
			label.setText("Open histogram tool");
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	
			final Button openHisto = new Button(group, SWT.NONE);
			openHisto.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
			openHisto.setImage(PlottingSystemActivator.getImage("icons/brightness_contrast.gif"));
			openHisto.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialog.close();
					try {
						final IToolPageSystem system = (IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class);
						system.setToolVisible("org.dawnsci.rcp.histogram.histogram_tool_page", 
								              ToolPageRole.ROLE_2D, 
								              "org.dawb.workbench.plotting.views.toolPageView.2D");
					} catch (Exception e1) {
						logger.error("Cannot show histogram tool programatically!", e1);
					}
					
				}
			});
		}
		
		final Group cuts = new Group(this, SWT.NONE);
		cuts.setText("Invalid Bounds");
		cuts.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		cuts.setLayout(new GridLayout(3, false));

		label = new Label(cuts, SWT.NONE);
		label.setText("Lower cut");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		Text minCutBox = new Text(cuts, SWT.BORDER);
		minCutBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.minCut = isInt ? new IntegerDecorator(minCutBox) : new FloatDecorator(minCutBox);
		if (imageTrace.getMinCut()!=null) minCut.setValue(imageTrace.getMinCut().getBound().doubleValue());
		minCut.setMaximum(minimum);
		minCut.setMinimum(Double.NEGATIVE_INFINITY);
		
		minCutColor = new ColorSelector(cuts);
		minCutColor.getButton().setLayoutData(new GridData());		
		if (imageTrace.getMinCut()!=null) minCutColor.setColorValue(ColorUtility.getRGB(imageTrace.getMinCut().getColor()));

		label = new Label(cuts, SWT.NONE);
		label.setText("Upper cut");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		final Text maxCutBox = new Text(cuts, SWT.BORDER);
		maxCutBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.maxCut = isInt ? new IntegerDecorator(maxCutBox) : new FloatDecorator(maxCutBox);
		if (imageTrace.getMaxCut()!=null) maxCut.setValue(imageTrace.getMaxCut().getBound().doubleValue());

		maxCut.setMinimum(maximum);
		maxCut.setMaximum(Double.POSITIVE_INFINITY);
		
		maxCutColor = new ColorSelector(cuts);
		maxCutColor.getButton().setLayoutData(new GridData());		
		if (imageTrace.getMaxCut()!=null) maxCutColor.setColorValue(ColorUtility.getRGB(imageTrace.getMaxCut().getColor()));
	
		label = new Label(cuts, SWT.NONE);
		label.setText("Invalid number color");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		nanColor = new ColorSelector(cuts);
		nanColor.getButton().setLayoutData(new GridData());		
		if (imageTrace.getNanBound()!=null) nanColor.setColorValue(ColorUtility.getRGB(imageTrace.getNanBound().getColor()));
		
		final Button reset = new Button(cuts, SWT.NONE);
		reset.setLayoutData(new GridData());		
		reset.setImage(PlottingSystemActivator.getImage("icons/reset.gif"));
		reset.setText("Reset");
		reset.setToolTipText("Reset cut bounds");
		reset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				imageTrace.setMinCut(HistogramBound.DEFAULT_MINIMUM);
				imageTrace.setMaxCut(HistogramBound.DEFAULT_MAXIMUM);
				imageTrace.setNanBound(HistogramBound.DEFAULT_NAN);
				minCut.setValue(Double.NEGATIVE_INFINITY);
				minCutColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_MINIMUM.getColor()));
				maxCut.setValue(Double.POSITIVE_INFINITY);
				maxCutColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_MAXIMUM.getColor()));
				maxCutBox.setText(String.valueOf(Double.POSITIVE_INFINITY));
				nanColor.setColorValue(ColorUtility.getRGB(HistogramBound.DEFAULT_NAN.getColor()));
			}
		});
		
		final Group info = new Group(this, SWT.NONE);
		info.setText("Current downsample");
		info.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		info.setLayout(new GridLayout(2, false));

		label = new Label(info, SWT.NONE);
		label.setText("Bin size");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		Label value = new Label(info, SWT.NONE);
		value.setText(imageTrace.getDownsampleBin()+"x"+imageTrace.getDownsampleBin()+" pixels");
		value.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		label = new Label(info, SWT.NONE);
		label.setText("Shape");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		value = new Label(info, SWT.NONE);
		value.setText(Arrays.toString(imageTrace.getDownsampled().getShape()));
		value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	}
	
	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return store;
	}


	public void applyChanges() {

		try {
			imageTrace.setImageUpdateActive(false);
			imageTrace.setHistoType(HistoType.values()[histoChoice.getSelectionIndex()]); // Do first because overrides max and min
			
			if (!Double.isNaN(minimum.getValue().doubleValue())) imageTrace.setMin(minimum.getValue());
			if (!Double.isNaN(maximum.getValue().doubleValue())) imageTrace.setMax(maximum.getValue());
			
			final double min = !Double.isNaN(minCut.getValue().doubleValue()) ? minCut.getValue().doubleValue() : imageTrace.getMinCut().getBound().doubleValue();
			imageTrace.setMinCut(new HistogramBound(min, ColorUtility.getIntArray(minCutColor.getColorValue())));
			
			final double max = !Double.isNaN(maxCut.getValue().doubleValue()) ? maxCut.getValue().doubleValue() : imageTrace.getMaxCut().getBound().doubleValue();
			imageTrace.setMaxCut(new HistogramBound(max, ColorUtility.getIntArray(maxCutColor.getColorValue())));
			
			imageTrace.setNanBound(new HistogramBound(Double.NaN, ColorUtility.getIntArray(nanColor.getColorValue())));
			imageTrace.setDownsampleType(DownsampleType.values()[downsampleChoice.getSelectionIndex()]);
		} finally {
			imageTrace.setImageUpdateActive(true);
		}
	}

}
