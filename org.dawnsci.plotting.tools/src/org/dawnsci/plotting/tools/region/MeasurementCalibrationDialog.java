package org.dawnsci.plotting.tools.region;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class MeasurementCalibrationDialog extends Dialog {

	private MeasurementTool tool;
	private Label           currentMeasurementInfo;
	private double          length=0d;
	private double          size=0d;
	private Text measurementUnit;

	protected MeasurementCalibrationDialog(MeasurementTool tool) {
		super(tool.getPlottingSystem().getPlotComposite().getShell());
		setShellStyle(SWT.MODELESS | SWT.SHELL_TRIM | SWT.BORDER);
		this.tool = tool;
	}

	protected Control createDialogArea(Composite parent) {
		
		
		getShell().setText("Axis calibration from measurement");
		
	    Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout(3, false));
	    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    
	    final Label info = new Label(composite, SWT.WRAP);
	    info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	    info.setText("Please choose a measurement, used to calibrate the axes.\nYou can adjust the measurement by clicking and dragging it (even when this form is showing).\nYou can optionally set a unit for your measured axes, this is used in the axis title.\n");
	    
	    final Button isSquare = new Button(composite, SWT.CHECK);
	    isSquare.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	    isSquare.setText("Square calibration");
	    isSquare.setToolTipText("Treat the data as having the same calibration for the x and y axis.");
	    isSquare.setSelection(true);
	    isSquare.setEnabled(false);
	    
	    Label label = new Label(composite, SWT.NONE);
	    label.setText("Measurement");
	    label.setLayoutData(new GridData());
	    
	    final CCombo measurements = new CCombo(composite, SWT.READ_ONLY | SWT.BORDER);
	    final String[] names      = RegionUtils.getRegionNames(tool.getPlottingSystem(), IRegion.RegionType.LINE);
	    measurements.setItems(names);
	    measurements.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	    measurements.select(0);
	    measurements.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		updateMeasurementSelection(measurements.getItem(measurements.getSelectionIndex()));
	    	}
	    });
	    
	    currentMeasurementInfo = new Label(composite, SWT.NONE);
	    currentMeasurementInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	    
	    label = new Label(composite, SWT.NONE);
	    label.setText("Real size");
	    label.setLayoutData(new GridData());
	    
	    final Text measurementSize = new Text(composite, SWT.BORDER|SWT.RIGHT);
	    measurementSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    measurementSize.setText("0");
	    measurementSize.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
				if (e.keyCode==127||e.character=='\b') return;
				if (!String.valueOf(e.character).matches("\\d|\\.")) {
					e.doit = false;
					return;
				}
 			}
		});
	    measurementSize.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					size = Double.parseDouble(measurementSize.getText());
				} catch (Exception ignored) {
					// nowt
				}
			}
		});
	    
	    this.measurementUnit = new Text(composite, SWT.BORDER);
	    measurementUnit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    measurementUnit.setText("µm");
	    measurementUnit.setToolTipText("The unit for the measurement. (Delete to have no unit)");
	    
		if (names!=null && names.length>0) {
			updateMeasurementSelection(names[0]);
		}

	    final Label hint = new Label(composite, SWT.WRAP);
	    hint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	    hint.setText("\n(Hint: to apply calibrated axes to other images, open the measurement tool in a dedicated view.)");

	    return composite;
	}
	
	private void updateMeasurementSelection(final String regionName) {
		updateMeasurementSelection(regionName, null);
	}

	private NumberFormat lengthFormat;
	private IRegion      attachedRegion;
	private IROIListener roiListener;
	private void updateMeasurementSelection(final String regionName, IROI roi) {
		
		if (roi!=null && !(roi instanceof LinearROI)) return;
		if (roi==null) {
		    final IRegion region = tool.getPlottingSystem().getRegion(regionName);
		    if (region.getRegionType()!=RegionType.LINE) return;
		    length  = ((LinearROI)region.getROI()).getLength();
		} else {
			length  = ((LinearROI)roi).getLength();
		}
		if (lengthFormat==null)  lengthFormat = new DecimalFormat("###0");
		currentMeasurementInfo.setText(regionName+":      "+lengthFormat.format(length)+" pixels");
		
		if (roi==null) {
		    final IRegion region = tool.getPlottingSystem().getRegion(regionName);
			if (attachedRegion!=null && roiListener!=null) attachedRegion.removeROIListener(roiListener);
			attachedRegion = region;
			roiListener = new IROIListener.Stub() {
				@Override
				public void update(ROIEvent evt) {
					updateMeasurementSelection(attachedRegion.getName(), evt.getROI());
					currentMeasurementInfo.redraw();
				}
			};
			attachedRegion.addROIListener(roiListener);
		}
	}
	public boolean close() {
        if (attachedRegion!=null) {
        	attachedRegion.removeROIListener(roiListener);
        }
        return super.close();
	}
	protected void okPressed() {
        applyCalibration();
		super.okPressed();
	}
	
	private final static int APPLYID = 7233;
	private final static int RESETID = 7234;

	protected void createButtonsForButtonBar(Composite parent) {
		final Button apply = createButton(parent, APPLYID, "Apply", true);
		apply.setToolTipText("Applies any calibration to axes on the plot, without closing.");
		
		final Button reset = createButton(parent, RESETID, "Reset", false);
		reset.setToolTipText("Resets any custom axes set on the plot.");
		
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if(buttonId==APPLYID) {
        	applyCalibration();
        } else if (buttonId==RESETID) {
        	resetCalibration();
        } 
	}

	private void resetCalibration() {
		final IImageTrace   image = tool.getImageTrace();
		if (image!=null) image.setAxes(null, true);
		tool.setCalibratedAxes(null);
		tool.getPlottingSystem().repaint();
	}

	private void applyCalibration() {
		
		final IImageTrace   image = tool.getImageTrace();
		if (image!=null) {
			final IDataset        set = image.getData();
			
			final double ratio = size/length; // e.g. mm/pixel
			
			// TODO FIXME only for standard orientation
			// TODO FIXME only for square
			final int[] shape = set.getShape();
			Dataset y = DatasetFactory.createRange(shape[0], Dataset.FLOAT64);
			y.imultiply(ratio);
			String unit = measurementUnit.getText();
			y.setName(unit);
			
			Dataset x = DatasetFactory.createRange(shape[1], Dataset.FLOAT64);
			x.imultiply(ratio);
			x.setName(measurementUnit.getText());
		
			image.setAxes(Arrays.asList(x,y), true);
			tool.setCalibratedAxes(unit, ratio, ratio);
			tool.getPlottingSystem().repaint();
		}
	}
}
