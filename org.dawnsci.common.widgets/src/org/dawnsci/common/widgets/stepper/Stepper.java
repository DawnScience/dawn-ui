/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.stepper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;


/**
 * Composite that works like a stepper. This stepper displays a slider which displays label underneath it. Also displays
 * a stepper text box which can used along with the slider. For usage, please refer to
 * "uk.ac.gda.ui.components.StepperTest"
 * 
 * @author rsr31645
 */
public class Stepper extends Canvas {
	private static final int LABEL_WIDTH = 60;

	private org.eclipse.swt.widgets.Label actualValue;

	private ArrayList<IStepperSelectionListener> listeners;

	private static final String TEXT_SMALL_7 = "TEXT_SMALL_6";
	private int steps = 1;
	private boolean moved;
	private boolean showActualValueLabel;

	private FigureCanvas figCanvas;

	private Polyline lineAcross;

	private RectangleFigure btnContainer;

	private boolean refreshMarkers;
	private List<MarkerFigure> markers = Collections.emptyList();

	private FontRegistry fontRegistry;
	private IFigure rootFigure;

	private Spinner spinner;

	private int markerCurrentPosition = 0;

	private org.eclipse.swt.widgets.Label stepperLabel;

	private double[] indexValues;

	private boolean notifyWhenDragged = false;

	/**
	 * Switch to request Stepper to notify caller when the stepper is dragged.
	 * 
	 * @param notifyWhenDragged
	 */
	public void setNotifyWhenDragged(boolean notifyWhenDragged) {
		this.notifyWhenDragged = notifyWhenDragged;
	}

	/**
	 * Set the max number of steps
	 * 
	 * @param steps
	 */
	public void setSteps(int steps) {
		setSteps(steps, null);
	}

	/**
	 * @param steps
	 *            - the number of steps
	 * @param indexValues
	 *            - the labels for each of the steps
	 */
	public void setSteps(int steps, double[] indexValues) {
		this.indexValues = indexValues;
		this.steps = steps;
		spinner.setMinimum(0);
		spinner.setMaximum(steps - 1);

		clearMarkerFigures();
		this.layout(true, true);

	}

	private void clearMarkerFigures() {
		if (rootFigure != null) {
			for (MarkerFigure mf : markers) {
				rootFigure.remove(mf);
			}
		}
		markers.clear();
		rootFigure.getLayoutManager().layout(rootFigure);
	}

	private SelectionListener spinnerSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() instanceof Spinner) {
				Spinner spinner = (Spinner) e.getSource();
				int selection = spinner.getSelection();
				//
				moveToStep(selection);
				if (showActualValueLabel) {
					actualValue.setText(getDisplayVal(selection));
				}
				fireNotifyChanged();
			}

		}

	};

	private void moveToStep(int selection) {
		//
		if (markers.size() == steps) {
			if (steps > 1) {
				moveToMarkerClosestTo(markers.get(selection).getLocation());
			}
		} else {
			for (int i = 0; i < markers.size(); i++) {
				MarkerFigure mf = markers.get(i);
				if (mf.getMarkerIndex() == selection) {
					moveToMarkerClosestTo(mf.getLocation());
					break;
				}
				if (i == markers.size() - 1) {
					// if reached the end of the slider.
					moveToStep(steps - 1);
				} else if (mf.getMarkerIndex() < selection && markers.get(i + 1).getMarkerIndex() > selection) {
					// if it is in between any of the two markers.
					int locToMove = 0;
					int numStepsBetweenMarkers = markers.get(i + 1).getMarkerIndex() - mf.getMarkerIndex() - 1;
					int numPixelsBetweenMarkers = markers.get(i + 1).getLocation().x - mf.getLocation().x;
					if (numPixelsBetweenMarkers >= numStepsBetweenMarkers) {
						// there are more pixels than steps between markers.
						double pixelsForStep = numPixelsBetweenMarkers / (numStepsBetweenMarkers + 1);
						int index = selection - mf.getMarkerIndex();
						locToMove = mf.getLocation().x + (int) (index * pixelsForStep);
					} else {
						double stepsPerPixel = numStepsBetweenMarkers / ((double)numPixelsBetweenMarkers - 8);
						int stepsFromMarker = selection - mf.getMarkerIndex();
						int pixelsToMove = (int) (stepsFromMarker / stepsPerPixel);
						locToMove = mf.getLocation().x + pixelsToMove;
					}

					Rectangle b = btnContainer.getBounds();
					btnContainer.setLocation(new Point(locToMove - b.width / 2, b.y));
					moved = true;
					break;
				}
			}
		}
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		refreshMarkers = true;
		super.setBounds(x, y, width, height);
	}

	public Stepper(Composite parent, int style) {
		this(parent, style, true);
	}

	public Stepper(Composite parent, int style, boolean showActualValueLabel) {
		this(parent, style, showActualValueLabel, null);
	}

	public Stepper(Composite parent, int style, Image sliderImage) {
		this(parent, style, true, sliderImage);
	}

	public Stepper(Composite parent, int style, boolean showActualValueLabel, Image sliderImage) {
		super(parent, style);
		this.setBackground(ColorConstants.white);
		this.showActualValueLabel = showActualValueLabel;

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.horizontalSpacing = 1;
		layout.verticalSpacing = 1;
		this.setLayout(layout);
		fontRegistry = new FontRegistry(getDisplay());
		if (sliderImage == null) {
			setSliderImage(Activator.getImage("icons/sliderIcon.gif"));
		} else {
			setSliderImage(sliderImage);
		}
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(TEXT_SMALL_7, new FontData[] { new FontData(fontName, 7, SWT.BOLD) });
		}
		stepperLabel = new org.eclipse.swt.widgets.Label(this, SWT.None);
		stepperLabel.setBackground(ColorConstants.white);
		stepperLabel.setLayoutData(new org.eclipse.swt.layout.GridData());
		figCanvas = new FigureCanvas(this);
		figCanvas.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
		figCanvas.setLayout(new FillLayout());
		figCanvas.setBackground(ColorConstants.white);
		figCanvas.setHorizontalScrollBarVisibility(FigureCanvas.NEVER);
		figCanvas.setVerticalScrollBarVisibility(FigureCanvas.NEVER);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				rootFigure.setSize(figCanvas.getSize().x, figCanvas.getSize().y);
				refreshMarkers = true;
			}
		});

		Composite spinnerGroup = new Composite(this, SWT.None);
		layout = new GridLayout();
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.horizontalSpacing = 1;
		layout.verticalSpacing = 1;
		spinnerGroup.setLayout(layout);
		spinnerGroup.setLayoutData(new org.eclipse.swt.layout.GridData());

		spinner = new Spinner(spinnerGroup, SWT.BORDER);
		spinner.setLayoutData(new org.eclipse.swt.layout.GridData());
		spinner.addSelectionListener(spinnerSelectionListener);
		spinner.setMaximum(0);

		if (showActualValueLabel) {
			actualValue = new org.eclipse.swt.widgets.Label(spinnerGroup, SWT.None);
			actualValue.setBackground(ColorConstants.white);
			actualValue.setLayoutData(new org.eclipse.swt.layout.GridData(GridData.FILL_BOTH));
		}

		listeners = new ArrayList<IStepperSelectionListener>();

	}

	public void addStepperSelectionListener(IStepperSelectionListener listener) {
		listeners.add(listener);
	}

	public void removeStepperSelectionListener(IStepperSelectionListener listener) {
		listeners.remove(listener);
	}

	private MouseListener panelListener = new MouseListener() {

		private boolean mousePressed = false;

		@Override
		public void mousePressed(MouseEvent me) {
			mousePressed = true;
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (mousePressed) {
				mousePressed = false;
			}
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			if (mousePressed) {
				Point location = me.getLocation();

				int oldPosition = markerCurrentPosition;

				moveToMarkerClosestTo(location);
				int newPosition = markerCurrentPosition;
				if (newPosition != oldPosition) {
					MarkerFigure markerFigure = markers.get(markerCurrentPosition);

					int markerIndex = markerFigure.getMarkerIndex();
					spinner.setSelection(markerIndex);
					if (showActualValueLabel) {
						actualValue.setText(getDisplayVal(markerIndex));
					}
					fireNotifyChanged();
				}
				mousePressed = false;
			}
		}

	};

	private Image sliderImage;

	@SuppressWarnings("unused")
	protected IFigure getContents() {
		rootFigure = new Panel();
		rootFigure.setLayoutManager(new StepperLayout());
		lineAcross = new Polyline();
		lineAcross.setLineWidth(2);
		lineAcross.setPoints(new PointList(new int[] { 0, 20, 10, 20 }));
		rootFigure.add(lineAcross);

		ImageFigure imgFigure = new ImageFigure(getSliderImage());
		imgFigure.setOpaque(true);
		btnContainer = new RectangleFigure();
		btnContainer.setLayoutManager(new XYLayout() {
			@SuppressWarnings("rawtypes")
			@Override
			public void layout(IFigure parent) {
				super.layout(parent);
				List children = parent.getChildren();
				int maxWidth = 0;
				for (Object child : children) {
					int width = ((IFigure) child).getSize().width;
					if (width > maxWidth) {
						maxWidth = width;
					}
				}
				parent.setSize(maxWidth + 10, parent.getSize().height);
			}
		});

		rootFigure.addMouseListener(panelListener);
		btnContainer.setBackgroundColor(ColorConstants.lightGray);

		btnContainer.add(imgFigure, new Rectangle(5, 0, -1, -1));

		rootFigure.add(btnContainer, new Rectangle(0, 0, -1, -1));
		new Dragger(btnContainer);
		return rootFigure;
	}

	private Image getSliderImage() {
		return sliderImage;
	}

	public void setSliderImage(Image sliderImage) {
		this.sliderImage = sliderImage;
	}

	protected void moveToMarkerClosestTo(Point relPoint) {
		int xLoc = relPoint.x;
		int minDist = Integer.MAX_VALUE;
		int xDestination = -1;
		int index = -1;
		int controlIndex = index;
		for (index = 0; index < markers.size(); index++) {
			MarkerFigure m = markers.get(index);
			int px = m.getLocation().x;
			int dist = Math.abs(px - xLoc);
			if (dist < minDist) {
				minDist = dist;
				xDestination = px;
				controlIndex = index;
			}
		}

		markerCurrentPosition = controlIndex;
		Rectangle b = btnContainer.getBounds();
		btnContainer.setLocation(new Point(xDestination - b.width / 2, b.y));
		moved = true;

	}

	public void setSelection(final int index) {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				spinner.setSelection(index);
				if (showActualValueLabel) {
					actualValue.setText(getDisplayVal(index));
				}
				if (spinner.getMaximum() > 2 && markers.size() < 1) {
					rootFigure.invalidate();
				}
				moveToStep(index);
			}
		});

	}

	private String getDisplayVal(int index) {
		String displayVal = null;
		if (indexValues != null) {
			if (index == indexValues.length) {
				displayVal = String.format("%.02f", indexValues[indexValues.length - 1]);
			} else {
				displayVal = String.format("%.02f", indexValues[index]);
			}
		} else {
			displayVal = Integer.toString(index);
		}
		return displayVal;
	}

	private class MarkerFigure extends Figure {
		private final int index;

		public MarkerFigure(int index) {
			this.index = index;
			polyline = new Polyline();
			polyline.setForegroundColor(ColorConstants.blue);

			label = new Label(getDisplayVal(index));
			label.setFont(fontRegistry.get(TEXT_SMALL_7));
			polyline.setLineWidth(2);
			setLayoutManager(new XYLayout() {
				@Override
				public void layout(IFigure parent) {
					int px = parent.getBounds().x;
					int py = parent.getBounds().y;
					polyline.setPoints(new PointList(new int[] { px + 1, py, px + 1, py + 10 }));
					label.setBounds(new Rectangle(px - 10, py + 2, LABEL_WIDTH, 10));
				}
			});
			add(polyline);
			add(label);
		}

		protected int getMarkerIndex() {
			return index;
		}

		@Override
		public void setSize(int w, int h) {
			super.setSize(w, h);
		}

		private Polyline polyline;

		private Label label;

		public void setCoordinates(int px, int yStart) {
			this.setBounds(new Rectangle(px, yStart, LABEL_WIDTH, 20));
			this.layout();
		}
	}

	private class StepperLayout extends XYLayout {

		@Override
		public void layout(IFigure parent) {
			super.layout(parent);
			Dimension parentSize = parent.getSize();
			lineAcross.setPoints(new PointList(new int[] { 0, parentSize.height / 2, parentSize.width - 1,
					parentSize.height / 2 }));
			Dimension btnContainerSize = btnContainer.getSize();

			/*
			 * Reducing the button container width to account for the front and end part of the button.
			 */
			if (steps > 0) {

				if (lineAcross.getSize().width > 0) {
					int numMarkers = calculateNumberOfMarkers(lineAcross.getSize().width, steps);
					if (numMarkers > 0) {

						double stepWidth = 0;

						if (!markers.isEmpty()) {
							// in case the number of steps is the same as the
							// number of markers
							if (steps == numMarkers) {
								if (markers.size() != numMarkers) {
									clearMarkerFigures();
								}
								stepWidth = lineAcross.getSize().width / numMarkers;
							} else {
								// in case the number of steps is greater than
								// the number of markers.
								if (markers.size() != numMarkers + 1) {
									clearMarkerFigures();
								}
								stepWidth = lineAcross.getSize().width / (numMarkers + 1);
							}
						}

						if (markers.equals(Collections.emptyList())) {
							markers = new ArrayList<MarkerFigure>();
							int yStart = parentSize.height / 2;
							Rectangle bounds = btnContainer.getBounds();
							// need to remove the btn container and then add it
							// so that it appears above the markers. Markers
							// added dynamically.
							parent.remove(btnContainer);

							int stepSkipped = steps / numMarkers;

							for (int i = 0; i < numMarkers; i++) {
								MarkerFigure m = new MarkerFigure(i * stepSkipped);
								setMarkerFigureCoordinates(stepWidth, yStart, i, m);
								parent.add(m);
								markers.add(m);
							}
							// // need to add the last marker if it isn't there
							// already.
							if (numMarkers != steps) {
								// add an additional marker to state the end of
								// the steps which would essentially be (n-1)
								MarkerFigure m = new MarkerFigure(steps - 1);
								setMarkerFigureCoordinates(stepWidth, yStart, numMarkers + 1, m);
								parent.add(m);
								markers.add(m);
							}

							parent.add(btnContainer, bounds);
							refreshMarkers = true;
						} else if (refreshMarkers) {
							int yStart = parentSize.height / 2;
							for (int i = 0; i < numMarkers; i++) {
								MarkerFigure m = markers.get(i);
								setMarkerFigureCoordinates(stepWidth, yStart, i, m);
							}
							// // need to add the last marker if it isn't there
							// already.
							if (numMarkers != steps) {
								// add an additional marker to state the end of
								// the steps which would essentially be (n-1)
								MarkerFigure m = markers.get(numMarkers);
								setMarkerFigureCoordinates(stepWidth, yStart, numMarkers, m);
							}
							refreshMarkers = false;
						}
					}
				}
				btnContainer.setLocation(new Point(btnContainer.getLocation().x, parentSize.height / 2
						- btnContainerSize.height / 2));
				if (!moved) {
					btnContainer.setLocation(new Point(btnContainer.getLocation().x, parentSize.height / 2
							- btnContainerSize.height / 2));
				} else {
					setSelection(spinner.getSelection());
				}
			}
		}

		private int calculateNumberOfMarkers(int numberOfPixels, int steps) {
			if (numberOfPixels / LABEL_WIDTH < steps) {
				int maxNumMarkers = numberOfPixels / LABEL_WIDTH;
				if (maxNumMarkers > 2) {
					int numMarkers = getClosestRoundedMarker(maxNumMarkers, steps);
					if (numMarkers < 1) {
						// which means steps is prime
						numMarkers = getClosestRoundedMarker(maxNumMarkers, steps - 1);
					}
					return numMarkers;
				}
				return maxNumMarkers;
			}
			return steps;
		}

		private int getClosestRoundedMarker(int maxNumMarkers, int steps) {
			int num = maxNumMarkers;
			while (num != 0 && steps % num != 0) {
				num = num - 1;
			}

			while (num < 2 && num > 0) {
				return getClosestRoundedMarker(maxNumMarkers - 1, steps);
			}
			return num;
		}

		private void setMarkerFigureCoordinates(double stepWidth, int yStart, int i, MarkerFigure m) {
			int px = (int) (i * stepWidth) + btnContainer.getSize().width / 2;
			m.setCoordinates(px, yStart);
		}
	}

	class Dragger extends org.eclipse.draw2d.MouseMotionListener.Stub implements MouseListener {
		private Point movedPoint;
		private final IFigure figure;

		private int spinnerValNotified = -1;
		private long lastTime;

		public Dragger(IFigure figure) {
			this.figure = figure;
			figure.addMouseMotionListener(this);
			figure.addMouseListener(this);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource().equals(figure)) {
				btnContainer.setBackgroundColor(ColorConstants.lightGray);
				int marker0Loc = markers.get(0).getLocation().x;
				int finalMarkerXLoc = markers.get(markers.size() - 1).getLocation().x;
				int wby2 = btnContainer.getSize().width / 2;
				if (btnContainer.getLocation().x > finalMarkerXLoc) {
					btnContainer.setLocation(new Point(finalMarkerXLoc - wby2, btnContainer.getLocation().y));
				}
				if (btnContainer.getLocation().x < marker0Loc) {
					btnContainer.setLocation(new Point(marker0Loc - wby2, btnContainer.getLocation().y));
				}
				moveToStep(spinner.getSelection());

				if (spinner.getSelection() != spinnerValNotified) {
					fireNotifyChanged();
				}
			}
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			movedPoint = e.getLocation();
			btnContainer.setBackgroundColor(ColorConstants.gray);
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (movedPoint != null) {
				Point p = e.getLocation();
				Dimension delta = p.getDifference(movedPoint);
				Figure f = ((Figure) e.getSource());
				// Restricted drag movement for the triangle

				int parentWidth = btnContainer.getParent().getSize().width;
				Rectangle translated = f.getBounds().getTranslated(delta.width, 0);
				if (translated.x < 0) {
					translated.x = 0;
				} else if (translated.x + translated.width > parentWidth) {
					translated.x = parentWidth - btnContainer.getSize().width;
				}

				f.setBounds(translated);
				moveToLocation(f.getBounds().x);

				if (notifyWhenDragged) {
					long currentTimeMillis = System.currentTimeMillis();
					// fires an update only if the previous update hadn't been fired within a timespan of 500 mill
					// seconds.
					// This was added to avoid the UI thread being held up by listeners.
					if (currentTimeMillis - lastTime > 500) {
						spinnerValNotified = spinner.getSelection();
						fireNotifyChanged();
						lastTime = currentTimeMillis;
					}
				}

				movedPoint = p;
			}
		}

	}

	public void moveToLocation(int x) {
		if (steps > 1) {
			int x0Loc = markers.get(0).getLocation().x;
			int xEndLoc = markers.get(markers.size() - 1).getLocation().x;
			int totalPixels = xEndLoc - x0Loc;
			int pixelStep = (x * steps) / totalPixels;
			if (pixelStep > steps - 1) {
				pixelStep = steps - 1;
			}
			markerCurrentPosition = pixelStep;
			if (pixelStep < 0) {
				pixelStep = 0;
			}
			spinner.setSelection(pixelStep);
			if (showActualValueLabel) {
				actualValue.setText(getDisplayVal(pixelStep));
			}
			moved = true;
		}
	}

	public void fireNotifyChanged() {
		if (!markers.isEmpty()) {

			StepperChangedEvent event = new StepperChangedEvent(this, spinner.getSelection());
			for (IStepperSelectionListener l : listeners) {
				l.stepperChanged(event);
			}
		}
	}

	public int getSelection() {
		return spinner.getSelection();
	}

	@Override
	public void dispose() {
		listeners.clear();
	}

	public void setText(String lblText) {
		stepperLabel.setText(lblText);
		stepperLabel.setToolTipText(lblText);
		stepperLabel.pack(true);
		this.layout();
	}

	public String getText() {
		return stepperLabel.getText();
	}

	public int getSteps() {
		return steps;
	}

	public double getSelectedValue() {
		if (indexValues != null) {
			return indexValues[getSelection()];
		}

		return getSelection();
	}

	public double[] getIndexValues() {
		return indexValues;
	}

}
