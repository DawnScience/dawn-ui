/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawb.workbench.ui.diffraction;

import java.lang.reflect.InvocationTargetException;

import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.diffraction.DiffractionCalibrationUtils.ManipulateMode;
import org.dawb.workbench.ui.diffraction.table.DiffractionDataManager;
import org.dawb.workbench.ui.diffraction.table.DiffractionTableData;
import org.dawb.workbench.ui.views.RepeatingMouseAdapter;
import org.dawb.workbench.ui.views.SlowFastRunnable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;

/**
 * 
 * @author wqk87977
 *
 */
public class CalibrantPositioningWidget {

	private DiffractionDataManager manager;
	private Control[] controls;
	private DiffractionTableData currentData;
	private TableViewer tableViewer;
	private IRunnableWithProgress ringFinder;

	/**
	 * Creates a widget group with all the calibrant positioning widgets
	 * used in a diffraction calibration view.
	 * @param parent
	 *         parent composite of the widget
	 * @param model
	 *         List of all diffraction data present in the TableViewer (used to update beam centre)
	 */
	public CalibrantPositioningWidget(Composite parent, final DiffractionDataManager manager) {
		this.manager = manager;
		final Display display = Display.getDefault();

		Group controllerHolder = new Group(parent, SWT.BORDER | SWT.FILL);
		controllerHolder.setText("Calibrant positioning");
		controllerHolder.setLayout(new GridLayout(2, false));

		// Pad composite
		Composite padComp = new Composite(controllerHolder, SWT.BORDER);
		padComp.setLayout(new GridLayout(5, false));
		padComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		padComp.setToolTipText("Move calibrant");

		Label l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);
		Button upButton = new Button(padComp, SWT.ARROW | SWT.UP);
		upButton.setToolTipText("Move rings up");
		upButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.UP, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		upButton.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);

		l = new Label(padComp, SWT.NONE);
		Button leftButton = new Button(padComp, SWT.ARROW | SWT.LEFT);
		leftButton.setToolTipText("Shift rings left");
		leftButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.LEFT, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		leftButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		l = new Label(padComp, SWT.NONE);
		l.setImage(Activator.getImage("icons/centre.png"));
		l.setToolTipText("Move calibrant");
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Button rightButton = new Button(padComp, SWT.ARROW | SWT.RIGHT);
		rightButton.setToolTipText("Shift rings right");
		rightButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.RIGHT, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		rightButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		l = new Label(padComp, SWT.NONE);

		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);
		Button downButton = new Button(padComp, SWT.ARROW | SWT.DOWN);
		downButton.setToolTipText("Move rings down");
		downButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.DOWN, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		downButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);

		// Resize group actions
		Composite actionComp = new Composite(controllerHolder, SWT.NONE);
		actionComp.setLayout(new GridLayout(3, false));
		actionComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

		Composite sizeComp = new Composite(actionComp, SWT.BORDER);
		sizeComp.setLayout(new GridLayout(1, false));
		sizeComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		sizeComp.setToolTipText("Change size");

		Button plusButton = new Button(sizeComp, SWT.PUSH);
		plusButton.setImage(Activator.getImage("icons/arrow_out.png"));
		plusButton.setToolTipText("Make rings larger");
		plusButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ENLARGE, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		plusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Button minusButton = new Button(sizeComp, SWT.PUSH);
		minusButton.setImage(Activator.getImage("icons/arrow_in.png"));
		minusButton.setToolTipText("Make rings smaller");
		minusButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SHRINK, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		minusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Composite shapeComp = new Composite(actionComp, SWT.BORDER);
		shapeComp.setLayout(new GridLayout(1, false));
		shapeComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		shapeComp.setToolTipText("Change shape");

		Button elongateButton = new Button(shapeComp, SWT.PUSH);
		elongateButton.setText("Elongate");
		elongateButton.setToolTipText("Make rings more elliptical");
		elongateButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ELONGATE, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		elongateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Button squashButton = new Button(shapeComp, SWT.PUSH | SWT.FILL);
		squashButton.setText("Squash");
		squashButton.setToolTipText("Make rings more circular");
		squashButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SQUASH, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		squashButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Composite rotateComp = new Composite(actionComp, SWT.BORDER);
		rotateComp.setLayout(new GridLayout(1, false));
		rotateComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		rotateComp.setToolTipText("Change rotation");

		Button clockButton = new Button(rotateComp, SWT.PUSH);
		clockButton.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
		clockButton.setToolTipText("Rotate rings clockwise");
		clockButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.CLOCKWISE, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		clockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		Button antiClockButton = new Button(rotateComp, SWT.PUSH);
		antiClockButton.setImage(Activator.getImage("icons/arrow_rotate_anticlockwise.png"));
		antiClockButton.setToolTipText("Rotate rings anti-clockwise");
		antiClockButton.addMouseListener(new RepeatingMouseAdapter(display,
				new SlowFastRunnable() {
					@Override
					public void run() {
						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ANTICLOCKWISE, isFast());
					}

					@Override
					public void stop() {
						if (tableViewer != null)
							tableViewer.refresh();
					}
				}));
		antiClockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		Button setBeamCentreButton = new Button(controllerHolder, SWT.PUSH);
		setBeamCentreButton.setText("Apply beam centre");
		setBeamCentreButton.setToolTipText("Apply current beam centre to all the images");
		setBeamCentreButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		setBeamCentreButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentData == null) return;
				DetectorProperties properties = currentData.getMetaData().getDetector2DProperties();
				double[] coords = properties.getBeamCentreCoords();
				if (manager.isValidModel())	return;
				for (DiffractionTableData dd : manager.iterable()) {
					dd.getMetaData().getDetector2DProperties().setBeamCentreCoords(coords);
				}
			}
		});

		Button findRingButton = new Button(controllerHolder, SWT.PUSH);
		findRingButton.setText("Match rings to image");
		findRingButton.setToolTipText("Use pixel values to find rings in image near calibration rings");
		findRingButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		findRingButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (CalibrantPositioningWidget.this.ringFinder != null) {
					
					ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
					try {
						dia.run(true, true, CalibrantPositioningWidget.this.ringFinder);
					} catch (InvocationTargetException e1) {
						// TODO Auto-generated catch block
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Calibration Error", "An error occured: " + e1.getTargetException().getMessage());
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					updateAfterRingFinding();
				}
			}
		});
	}

	/**
	 * Update the diffraction data
	 * @param data
	 */
	public void setDiffractionData(DiffractionTableData data) {
		this.currentData = data;
	}

	private void setCalibrateButtons() {

			// enable calibrate button if all images have rings
			for (DiffractionTableData d : manager.iterable()) {
				if (d.getNrois() <= 0) {
					setCalibrateOptionsEnabled(false);
					return;
				}
			}
			setCalibrateOptionsEnabled(true);
		
	}

	private void setCalibrateOptionsEnabled(boolean b) {
		if (controls == null)
			return;
		for (int i = 0; i < controls.length; i++) {
			if (controls[i] != null)
				controls[i].setEnabled(b);
		}
	}

	/**
	 * set the controls to update (enable/disable)
	 * @param controls
	 */
	public void setControlsToUpdate(Control... controls) {
		this.controls = controls;
	}

	/**
	 * Set the Table viewer to update (refresh)
	 * @param tableViewer
	 */
	public void setTableViewerToUpdate (TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}
	
	public void setRingFinder(IRunnableWithProgress ringFinder) {
		this.ringFinder = ringFinder;
	}
	
	private void updateAfterRingFinding(){
		if (currentData != null && currentData.getNrois() > 0) {
			setCalibrateButtons();
		}
		if (tableViewer != null)
			tableViewer.refresh();
	}
}
