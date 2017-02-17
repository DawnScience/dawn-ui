package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;

import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

public class SurfaceScatterViewStart extends Dialog {

	private String[] filepaths;
	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem3Composite ssps3c;
	private MultipleOutputCurvesTableView outputCurves;
	private DatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private CTabFolder folder;
	private SashForm right;
	private SashForm left;
	private SashForm anaRight;
	private SashForm anaLeft;
	private SashForm setupSash;
	private SashForm analysisSash;
	private int numberOfImages;
	private Dataset nullImage;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private ArrayList<Slider> sliderList;
	private int DEBUG = 1;
	private boolean modify = true;
	private String datFolderPath;
	private Combo correctionsDropDown;
	private Composite container;
	private Button imageFolderSelection;
	private Button datFolderSelection;
	private String imageFolderPath = null;

	public CTabFolder getFolder() {
		return folder;
	}

	public void setFolder(CTabFolder folder) {
		this.folder = folder;
	}

	public String getDatFolderPath() {
		return datFolderPath;
	}

	public void setDatFolderPath(String datFolderPath) {
		this.datFolderPath = datFolderPath;
	}

	public SuperSashPlotSystem3Composite getSsps3c() {
		return ssps3c;
	}

	public void setSsps3c(SuperSashPlotSystem3Composite ssps3c) {
		this.ssps3c = ssps3c;
	}

	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	public SurfaceScatterViewStart(Shell parentShell, String[] filepaths, int numberOfImages, Dataset nullImage,
			SurfaceScatterPresenter ssp, String datFolderPath) {

		super(parentShell);

		this.filepaths = filepaths;
		this.numberOfImages = numberOfImages;
		this.nullImage = nullImage;
		this.ssp = ssp;
		ssp.addStateListener(new IPresenterStateChangeEventListener() {

			@Override
			public void update() {
				// TODO Auto-generated method stub

			}
		});
		this.ssvs = this;
		this.datFolderPath = datFolderPath;

		setShellStyle(getShellStyle() | SWT.RESIZE);

	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);
		return c;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		JFaceResources.getString(IDialogConstants.NO_TO_ALL_LABEL);

		container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		folder = new CTabFolder(container, SWT.BORDER | SWT.CLOSE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		////////////////////////////////////////////////////////
		// Tab 1 Setup
		//////////////////////////////////////////////////////////

		CTabItem setup = new CTabItem(folder, SWT.NONE);
		setup.setText("Setup Parameters");
		setup.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite setupComposite = new Composite(folder, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setup.setControl(setupComposite);

		setupSash = new SashForm(setupComposite, SWT.FILL);
		setupSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		left = new SashForm(setupSash, SWT.VERTICAL);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		right = new SashForm(setupSash, SWT.VERTICAL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setupSash.setWeights(new int[] { 65, 35 });

		//////////////// setupLeft///////////////////////////////////////////////

		/////////////////////////// Window 1 LEFT
		/////////////////////////// SETUP////////////////////////////////////////////////////
		try {

			datDisplayer = new DatDisplayer(left, SWT.FILL, filepaths, ssp, datFolderPath, this);
			datDisplayer.setLayout(new GridLayout());
			datDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		///////////////// setupRight////////////////////////////////////////

		/////////////////////////// Window 2 RIGHT SETUP
		/////////////////////////// ////////////////////////////////////////////////////

		try {
			Group experimentalSetup = new Group(right, SWT.FILL);
			GridLayout experimentalSetupLayout = new GridLayout(1, true);
			GridData experimentalSetupData = new GridData(GridData.FILL_BOTH);
			experimentalSetupData.minimumWidth = 50;
			experimentalSetup.setLayout(experimentalSetupLayout);
			experimentalSetup.setLayoutData(experimentalSetupData);
			experimentalSetup.setText("Experimental Setup");

			Group methodSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout methodSettingLayout = new GridLayout(1, true);
			GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
			methodSettingData.minimumWidth = 50;
			methodSetting.setLayout(methodSettingLayout);
			methodSetting.setLayoutData(methodSettingData);
			methodSetting.setText("SXRD / Reflectivity");

			correctionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
			correctionsDropDown.add("SXRD");
			correctionsDropDown.add("Reflectivity with Flux Correction");
			correctionsDropDown.add("Reflectivity without Flux Correction");
			correctionsDropDown.add("Reflectivity with NO Correction");
			correctionsDropDown.select(0);
			correctionsDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Group parametersSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout parametersSettingLayout = new GridLayout(1, true);
			GridData parametersSettingData = new GridData(GridData.FILL_BOTH);
			parametersSettingData.minimumWidth = 50;
			parametersSetting.setLayout(parametersSettingLayout);
			parametersSetting.setLayoutData(parametersSettingData);
			parametersSetting.setText("Geometric Parameters");

			paramField = new GeometricParametersWindows(parametersSetting, SWT.FILL, ssp);
			paramField.setLayout(new GridLayout());
			paramField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		paramField.getTabFolder().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// ssp.setCorrectionSelection(paramField.getTabFolder().getSelectionIndex());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		datDisplayer.getBuildRod().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<TableItem> checkedList = new ArrayList<>();

				for (TableItem d : datDisplayer.getRodDisplayTable().getItems()) {
					if (d.getChecked()) {
						checkedList.add(d);
					}
				}

				TableItem[] rodComponentDats = new TableItem[checkedList.size()];

				for (int g = 0; g < checkedList.size(); g++) {
					rodComponentDats[g] = checkedList.get(g);
				}

				String[] filepaths = new String[rodComponentDats.length];

				for (int f = 0; f < rodComponentDats.length; f++) {
					String filename = rodComponentDats[f].getText();
					filepaths[f] = datFolderPath + File.separator + filename;
				}

				ssp.surfaceScatterPresenterBuild(ssp.getParentShell(), filepaths, datDisplayer.getSelectedOption(),
						ssp.getImageFolderPath(), datFolderPath, ssp.getCorrectionSelection());

				ssp.resetCorrectionsSelection();

				folder.setSelection(1);

				ssp.setSelection(0);
				//
				 customComposite.generalUpdate();
				 ssps3c.generalUpdate();
				// ssp.backgroundBoxesManager();
				// ssp.regionOfInterestSetter();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// right.setWeights(new int[] {10,90});
		//
		/////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		// Tab 2 Analysis
		////////////////////////////////////////////////////// #

		CTabItem analysis = new CTabItem(folder, SWT.NONE);
		analysis.setText("Analysis");
		analysis.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite analysisComposite = new Composite(folder, SWT.FILL);
		analysisComposite.setLayout(new GridLayout());
		analysisComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		analysis.setControl(analysisComposite);

		analysisSash = new SashForm(analysisComposite, SWT.FILL);
		analysisSash.setLayout(new GridLayout());
		analysisSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		anaLeft = new SashForm(analysisSash, SWT.FILL);
		anaLeft.setLayout(new GridLayout());
		anaLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));

		anaRight = new SashForm(analysisSash, SWT.FILL);
		anaRight.setLayout(new GridLayout());
		anaRight.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));

		analysisSash.setWeights(new int[] { 40, 60 });

		////////////////////// Analysis Left//////////////////////////////
		///////////////// anaLeft Window 3/////////////////////////////////

		customComposite = new PlotSystemCompositeView(anaLeft, SWT.FILL, ssp.getImage(0), 1, numberOfImages, nullImage,
				ssp);

		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		customComposite.getReplay().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ssp.runReplay(customComposite.getPlotSystem(), customComposite.getFolder(),
						customComposite.getSubImageBgPlotSystem());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		customComposite.getSlider().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {

				int sliderPos = customComposite.getSlider().getSelection();
				ssp.sliderMovemementMainImage(sliderPos);
				ssvs.updateIndicators(sliderPos);
				ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), sliderPos);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		customComposite.getGreenRegion().addROIListener(new IROIListener() {

			@Override
			public void roiDragged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				roiStandard(evt);
			}

			@SuppressWarnings("unchecked")
			public void roiStandard(ROIEvent evt) {

				IRegion region = customComposite.getIRegion();

				int[] Len = region.getROI().getBounds().getIntLengths();
				int[] Pt = region.getROI().getBounds().getIntPoint();
				int[][] LenPt = new int[][] { Len, Pt };

				customComposite.setROITexts(LenPt);

			}
		});

		folder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (ssp.getNoImages() != 0) {
					customComposite.getSlider().setMaximum(ssp.getNoImages());
					customComposite.getPlotSystem1CompositeView().generalUpdate();
					paramField.geometricParametersUpdate();
					ssp.regionOfInterestSetter();

					if (ssp.getBackgroundDatArray() == (null)) {
						try {
							getPlotSystemCompositeView().removeBackgroundSubtractedSubImage();
							getSsps3c().isOutputCurvesVisible(false);
							customComposite.getReplay().setEnabled(false);
						} catch (Exception n) {

						}
					}

					ssps3c.resetVerticalAndHorizontalSlices();
					customComposite.getPlotSystem1CompositeView().checkTrackerOnButton();
				} else {
					folder.setSelection(0);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		ssp.addSecondBgRegionListeners();

		customComposite.getGo().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if (modify == true) {
					modify = false;

					double r = 0;
					try {
						r = Double.parseDouble(customComposite.getImageNo().getText());
					} catch (Exception e1) {
						ssp.numberFormatWarning();
					}

					int k = ssp.closestImageIntegerInStack(r);
					ssp.sliderMovemementMainImage(k);
					ssp.sliderZoomedArea(k, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());
					ssp.updateSliders(ssvs.getSliderList(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), k);

					ssvs.updateIndicators(k);

					modify = true;

					int pt0 = (int) Math.round(Double.valueOf(customComposite.getXCoord().getText()));
					int pt1 = (int) Math.round(Double.valueOf(customComposite.getYCoord().getText()));

					int len0 = (int) Math.round(Double.valueOf(customComposite.getXLen().getText()));
					int len1 = (int) Math.round(Double.valueOf(customComposite.getYLen().getText()));

					int[][] newLenPt = new int[][] { { len0, len1 }, { pt0, pt1 } };

					ssp.regionOfInterestSetter(newLenPt);
					ssp.backgroundBoxesManager();

//				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		customComposite.getXValue().addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (modify == true) {
					modify = false;

					double in = 0;
					try {
						in = Double.parseDouble(customComposite.getXValue().getText());
					} catch (Exception e1) {
						ssp.numberFormatWarning();
					}

					int k = ssp.closestImageNo(in);

					ssp.sliderMovemementMainImage(k);
					ssp.sliderZoomedArea(k, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());

					ssp.updateSliders(ssvs.getSliderList(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), k);

					ssvs.updateIndicators(k);
					modify = true;
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		customComposite.getRun().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ssp.setStartFrame(ssvs.getSliderList().get(0).getSelection());
				ssp.resetDataModels();
				ssp.triggerBoxOffsetTransfer();

				if (getPlotSystemCompositeView().getBackgroundSubtractedSubImage() == null) {
					getPlotSystemCompositeView().appendBackgroundSubtractedSubImage();
					getPlotSystemCompositeView().getSash().setWeights(new int[] { 23, 45, 25, 7 });

				}

				if (getSsps3c().getOutputCurves().isVisible() != true) {
					getSsps3c().getOutputCurves().setVisible(true);
					getSsps3c().getSashForm().setWeights(new int[] { 50, 50 });
					getSsps3c().getLeft().setWeights(new int[] { 50, 50 });
					getSsps3c().getRight().setWeights(new int[] { 50, 50 });
				}

				analysisSash.setWeights(new int[] { 40, 60 });
				analysisSash.redraw();

				ssp.runTrackingJob(customComposite.getSubImagePlotSystem(),
						getSsps3c().getOutputCurves().getPlotSystem(), customComposite.getPlotSystem(),
						customComposite.getFolder(), customComposite.getSubImageBgPlotSystem());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		//////////////////////// Analysis Right//////////////////////////////
		///////////////// anaRight Window 4/////////////////////////////////
		try {
			ssps3c = new SuperSashPlotSystem3Composite(anaRight, SWT.FILL, ssvs, ssp);

			ssps3c.setLayout(new GridLayout());
			ssps3c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			ssps3c.setSsp(ssp);
			// outputCurves = ssps3c.getOutputCurves();

		} catch (Exception d) {

		}

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				// if (event.item.equals(specialItem)) {
				event.doit = false;
				// }
			}
		});

		customComposite.getPlotSystem1CompositeView().getSaveButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;

				ssp.saveParameters(title);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		customComposite.getPlotSystem1CompositeView().getSaveButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;

				ssp.saveParameters(title);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		customComposite.getPlotSystem1CompositeView().getLoadButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN);

				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;

				ssp.loadParameters(title, customComposite, customComposite.getPlotSystem1CompositeView());
				ssps3c.generalUpdate();
				customComposite.getPlotSystem1CompositeView().generalUpdate();
				ssp.trackingRegionOfInterestSetter(ssp.getLenPt());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		////////////////////////////////////////////////////////////////////
		customComposite.getSecondBgRegion().setVisible(false);
		customComposite.getSecondBgRegion().repaint();
		appendListenersToOutputCurves();

		datDisplayer.redrawDatDisplayerFolderView();

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SurfaceScatterDialog");

	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;

		return new Point((int) Math.round(0.8 * w), (int) Math.round(0.9 * h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public PlotSystemCompositeView getPlotSystemCompositeView() {
		return customComposite;
	}

	public PlotSystemCompositeView getCustomComposite() {
		return customComposite;
	}

	public MultipleOutputCurvesTableView getOutputCurves() {
		return outputCurves;
	}

	public void updateIndicators(int k) {
		modify = false;
		if (customComposite.getXValue().equals(ssp.getXValue(k)) == false) {
			customComposite.getXValue().setText(String.valueOf(ssp.getXValue(k)));
		}
		if (customComposite.getImageNo().equals(String.valueOf(k)) == false) {
			customComposite.getImageNo().setText(String.valueOf(k));
		}

		RectangularROI r = new RectangularROI(ssp.getXValue(k), 0.1, 0, 0.1, 0);
		try {
			outputCurves.getRegionNo().setROI(r);
		} catch (NullPointerException f) {

		}

		modify = true;
	}

	public ArrayList<Slider> getSliderList() {

		sliderList = null;

		sliderList = new ArrayList<Slider>();

		sliderList.add(customComposite.getSlider());

		return sliderList;
	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

	public Combo getCorrectionSelection() {
		return correctionsDropDown;
	}

	public DatDisplayer getDatDisplayer() {
		return datDisplayer;
	}

	public void appendListenersToOutputCurves() {

		outputCurves = ssps3c.getOutputCurves();

		outputCurves.getOverlapZoom().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<ArrayList<IDataset>> xyArrays = ssp.xyArrayPreparer();

				GeneralOverlapHandlerView goh = new GeneralOverlapHandlerView(getParentShell(), SWT.OPEN,
						xyArrays.get(0), xyArrays.get(1), xyArrays.get(2), xyArrays.get(3), xyArrays.get(4),
						outputCurves.getPlotSystem(), ssp);

				goh.open();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		outputCurves.getRegionNo().addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
			}

			@Override
			public void roiDragged(ROIEvent evt) {
			}

			@Override
			public void roiChanged(ROIEvent evt) {

				if (customComposite.getOutputControl().getSelection() && modify == true) {

					int xPos = ssp.xPositionFinder(outputCurves.getRegionNo().getROI().getPointX());

					ssp.updateSliders(ssvs.getSliderList(), xPos);

					ssp.sliderMovemementMainImage(xPos);
					ssp.sliderZoomedArea(xPos, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());

					ssvs.updateIndicators(xPos);

					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), xPos);

					ssp.trackingRegionOfInterestSetter(xPos);

				}
			}

		});

		outputCurves.getIntensity().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
				Combo selector = outputCurves.getIntensity();
				ssp.switchFhklIntensity(pS, selector);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		outputCurves.getErrorsButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ssp.switchErrorDisplay();

				IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
				Combo selector = outputCurves.getIntensity();
				ssp.switchFhklIntensity(pS, selector);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		outputCurves.getSave().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;

				String[] fr = CurveStateIdentifier.CurveStateIdentifier1(outputCurves.getPlotSystem());

				if (outputCurves.getOutputFormatSelection().getSelectionIndex() == 0) {
					ssp.genXSave(title, fr);
				}
				if (outputCurves.getOutputFormatSelection().getSelectionIndex() == 1) {
					ssp.anarodSave(title, fr);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}
}