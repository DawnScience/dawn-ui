package org.dawnsci.plotting.jreality;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JPanel;

import org.dawnsci.plotting.jreality.compositing.CompositeEntry;
import org.dawnsci.plotting.jreality.compositing.CompositingControl;
import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1D;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot1DStack;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2D;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot2DMulti;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot3D;
import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot2D;
import org.dawnsci.plotting.jreality.impl.DataSetScatterPlot3D;
import org.dawnsci.plotting.jreality.impl.HistogramChartPlot1D;
import org.dawnsci.plotting.jreality.impl.Plot1DAppearance;
import org.dawnsci.plotting.jreality.impl.Plot1DGraphTable;
import org.dawnsci.plotting.jreality.legend.LegendChangeEvent;
import org.dawnsci.plotting.jreality.legend.LegendChangeEventListener;
import org.dawnsci.plotting.jreality.legend.LegendComponent;
import org.dawnsci.plotting.jreality.legend.LegendTable;
import org.dawnsci.plotting.jreality.swt.InfoBoxComponent;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.dawnsci.plotting.jreality.tool.CameraRotationTool;
import org.dawnsci.plotting.jreality.tool.ClickWheelZoomTool;
import org.dawnsci.plotting.jreality.tool.ClickWheelZoomToolWithScrollBar;
import org.dawnsci.plotting.jreality.tool.PanActionListener;
import org.dawnsci.plotting.jreality.tool.PanningTool;
import org.dawnsci.plotting.jreality.tool.SceneDragTool;
import org.dawnsci.plotting.jreality.util.JOGLChecker;
import org.dawnsci.plotting.jreality.util.PlotColorUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.ViewerAppSwt;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

/**
 * A class allowing plotting to be done to a hardware accelerated 
 * component sitting in an SWT Composite.
 * 
 * Code lifted out of DataSetPlotter. Could refactor at some point.
 * 
 * @author fcp94556
 *
 */
public class HardwarePlotting implements SelectionListener, PaintListener, Listener {

	
	private static Logger logger = LoggerFactory.getLogger(HardwarePlotting.class);
	
	protected IDataSet3DCorePlot plotter = null;

	private Plot1DGraphTable graphColourTable;
	private SceneGraphComponent root;
	private SceneGraphComponent graph;
	private SceneGraphComponent coordAxes;
	private SceneGraphComponent coordXLabels;
	private SceneGraphComponent coordYLabels;
	private SceneGraphComponent coordZLabels;
	private SceneGraphComponent coordGrid = null;
	private SceneGraphComponent coordTicks = null;
	private SceneGraphComponent toolNode = null;
	private SceneGraphComponent cameraNode = null;
	private SceneGraphComponent bbox = null;
	private CameraRotationTool cameraRotateTool = null;
	private InfoBoxComponent infoBox = null;
	private SceneDragTool dragTool = null;
	private ClickWheelZoomTool zoomTool = null;
	private boolean hasJOGL;
	private boolean isInExporting = false;
	private boolean showScrollBars = true;
	private LegendComponent legendTable = null;
	private ClickWheelCameraZoomTool cameraZoomTool = null;
	private SashForm container;
	private Composite plotArea;
	private ScrollBar hBar;
	private boolean donotProcessEvent = false;
	private ScrollBar vBar;
	private Cursor defaultCursor;
	private AbstractViewerApp viewerApp;
	private boolean hasJOGLshaders;
	private PanningTool panTool = null;
	private PlottingMode currentMode;
	private boolean useLegend = true;
	private CompositingControl cmpControl = null;

	
	/**
	 * Call to create plotting
	 * @param parent
	 * @param initialMode may be null
	 */
	public void createControl(final Composite parent, PlottingMode initialMode) {
		
		init(parent);
		createUI(parent);
		if (initialMode!=null) setInitMode(initialMode);
	}
	
	/**
	 * 
	 * @param data
	 * @param axes AxesValues for each axis required, for 3D there should be three for instance.
	 * @param mode
	 * @return true if something plotted
	 */
	public boolean plot(final AbstractDataset data, final List<AxisValues> axes, final PlottingMode mode) {
		
		setMode(mode);
		
		switch(mode) {
		
		case SURF2D:
			final AxisValues xAxis = axes.get(0);
			final AxisValues yAxis = axes.get(1);
			final AxisValues zAxis = axes.get(2);
			
			setMode(mode); // Does nothing if mode not supported
			setAxisModes((xAxis == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
	                     (yAxis == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
	                     (zAxis == null ? AxisMode.LINEAR : AxisMode.CUSTOM));

			setXAxisValues(xAxis, 1);
			setYAxisValues(yAxis);
			setZAxisValues(zAxis);
			
			setYTickLabelFormat(TickFormatting.roundAndChopMode);
			setXTickLabelFormat(TickFormatting.roundAndChopMode);
			
			update(data);
			setTitle(data.getName());
			refresh(true);
			
			return true;
		default:
			return false;
		}
		
		
	}
	
	
	private void update(AbstractDataset data) {
		final List<IDataset> sets = Arrays.asList((IDataset)data);
		checkAndAddLegend(sets);
		
		if (graph==null) {
			graph = plotter.buildGraph(sets, graph);
		} else {
			plotter.updateGraph(sets);
		}
	}
	
	private void checkAndAddLegend(Collection<? extends IDataset> dataSets) {
		if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.SCATTER2D) {
			if (dataSets != null && dataSets.size() > graphColourTable.getLegendSize()) {
				logger.info("# graphs > # of entries in the legend will auto add entries");
				for (int i = graphColourTable.getLegendSize(); i < dataSets.size(); i++) {
					graphColourTable.addEntryOnLegend(new Plot1DAppearance(PlotColorUtility.getDefaultColour(i),
							PlotColorUtility.getDefaultStyle(i), ""));
				}
			}
		}
	}

	
	private void setXAxisValues(AxisValues xAxis, int numOfDataSets) {
		if (plotter != null) {
			if (xAxis.getName()!=null) plotter.setXAxisLabel(xAxis.getName());
			plotter.setXAxisValues(xAxis, numOfDataSets);
		}
	}
	private void setYAxisValues(AxisValues yAxis) {
		if (plotter != null) {
			if (yAxis.getName()!=null) plotter.setYAxisLabel(yAxis.getName());
			plotter.setYAxisValues(yAxis);
		}
	}
	private void setZAxisValues(AxisValues zAxis) {
		if (plotter != null) {
			if (zAxis.getName()!=null) plotter.setZAxisLabel(zAxis.getName());
			plotter.setZAxisValues(zAxis);
		}
	}
	private void setXTickLabelFormat(TickFormatting newFormat) {
		if (plotter != null) plotter.setXAxisLabelMode(newFormat);
	}
	private void setYTickLabelFormat(TickFormatting newFormat) {
		if (plotter != null) plotter.setYAxisLabelMode(newFormat);
	}
	private void setZTickLabelFormat(TickFormatting newFormat) {
		if (plotter != null) plotter.setZAxisLabelMode(newFormat);
	}

	
	private void setInitMode(PlottingMode mode) {
		switch (currentMode) {
		case ONED:
			plotter = new DataSet3DPlot1D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			toolNode.removeTool(panTool);
			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
			break;
		case ONED_THREED:
			plotter = new DataSet3DPlot1DStack(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			break;
		case SCATTER2D:
			plotter = new DataSetScatterPlot2D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			toolNode.removeTool(panTool);
			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
			break;
		case TWOD:
			plotter = new DataSet3DPlot2D(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			break;
		case MULTI2D:
			plotter = new DataSet3DPlot2DMulti(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			break;
		case SURF2D:
			toolNode.removeTool(panTool);
			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, false);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			toolNode.addTool(dragTool);
			cameraNode.addTool(cameraRotateTool);
			viewerApp.getSceneRoot().addTool(cameraZoomTool);		
			break;
		case SCATTER3D:
			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, false);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			toolNode.addTool(dragTool);
			cameraNode.addTool(cameraRotateTool);
			break;
		case BARCHART:
			toolNode.removeTool(panTool);
			toolNode.removeTool(dragTool);
			cameraNode.removeTool(cameraRotateTool);
			viewerApp.getSceneRoot().removeTool(zoomTool);
			viewerApp.getSceneRoot().removeTool(cameraZoomTool);
			plotter = new HistogramChartPlot1D(viewerApp, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			break;
		case EMPTY:
			break;
		}
	}

	private void init(Composite parent) {
		graphColourTable = new Plot1DGraphTable();
		Secure.setProperty(SystemProperties.AUTO_RENDER, "false");
		root = SceneGraphUtility.createFullSceneGraphComponent("world");
		graph = SceneGraphUtility.createFullSceneGraphComponent("graph");
		graph.setOwner(root);
		coordAxes = SceneGraphUtility.createFullSceneGraphComponent("axis");
		coordXLabels = SceneGraphUtility.createFullSceneGraphComponent("xLabels");
		coordYLabels = SceneGraphUtility.createFullSceneGraphComponent("yLabels");
		coordZLabels = SceneGraphUtility.createFullSceneGraphComponent("zLabels");
		root.addChild(coordAxes);
		root.addChild(coordXLabels);
		root.addChild(coordYLabels);
		root.addChild(coordZLabels);
		root.addChild(graph);
		// check if JOGL is available
		hasJOGL = JOGLChecker.isHardwareEnabled(parent);
		
	}
	

	private static final String ERROR_MESG_NO_SHADERS = "System does not support OpenGL shaders falling back to compatibily mode. Some advanced features might not work";

	private Composite createUI(Composite parent) {
		
		container = new SashForm(parent, SWT.NONE|SWT.VERTICAL);
		container.addPaintListener(this);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		// Margins make the graph look bad when put as the
		// main item in a view which is the most common usage.
		gridLayout.marginBottom = 0;
		gridLayout.marginTop = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		container.setLayout(gridLayout);
		if (hasJOGL)
			plotArea = new Composite(container, SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL);
		else
			plotArea = new Composite(container, SWT.EMBEDDED | SWT.V_SCROLL | SWT.H_SCROLL);
		hBar = plotArea.getHorizontalBar();
		hBar.addSelectionListener(this);
		vBar = plotArea.getVerticalBar();
		vBar.addSelectionListener(this);

		// Linux GTK hack to keep scroll wheel events from changing
		// the vertical scrollbar position this interferes with
		// the MouseWheel zooming
		if (SWT.getPlatform().equals("gtk")) {
			plotArea.addListener(SWT.MouseWheel, new Listener() {

				@Override
				public void handleEvent(Event event) {
					donotProcessEvent = true;
				}
			});
		}

		hBar.setVisible(false);
		vBar.setVisible(false);
		defaultCursor = plotArea.getCursor();
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		plotArea.setLayoutData(gridData);
		if (hasJOGL) {
			plotArea.setLayout(new FillLayout());
			plotArea.addListener(SWT.Resize, this);
			viewerApp = new ViewerAppSwt(root, plotArea);
			hasJOGLshaders = JOGLChecker.isShadingSupported(viewerApp);
			if (!hasJOGLshaders)
				logger.warn(ERROR_MESG_NO_SHADERS);
		} else {
			final JPanel viewerPanel = new JPanel();
			viewerApp = new ViewerApp(root, true);
			BoxLayout layout = new BoxLayout(viewerPanel, BoxLayout.Y_AXIS);
			viewerPanel.setLayout(layout);
			java.awt.Component comp = ((ViewerApp) viewerApp).getContent();
			viewerPanel.add(comp);
			comp.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent arg0) {
					if (plotter != null) {
						Component comp = ((ViewerApp) viewerApp).getViewingComponent();
						plotter.notifyComponentResize(comp.getWidth(), comp.getHeight());
					}
				}
			});
			java.awt.Frame frame = SWT_AWT.new_Frame(plotArea);
			JApplet applet = new JApplet();
			frame.add(applet);
			applet.add(viewerPanel);
		}
		viewerApp.setBackgroundColor(java.awt.Color.white);
		if (useLegend) buildLegendTable();
		removeInitialTools();
		return container;
	}
	
	private void buildLegendTable() {
		if (legendTable == null || legendTable.isDisposed()) {
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.heightHint = 75;
			legendTable = new LegendTable(container, SWT.DOUBLE_BUFFERED);
			legendTable.setLayoutData(gridData);
		}
		container.setWeights(new int[] {90, 10});		
		legendTable.addLegendChangeEventListener(new LegendChangeEventListener() {
			@Override
			public void legendDeleted(LegendChangeEvent evt) {
				if (currentMode == PlottingMode.ONED) {
					int index = evt.getEntryNr();
					// TODO... We do not usually use HardwarePlotting for 1D
					// use DataSetPlotter if this is needed.
					
				}
			}

			@Override
			public void legendUpdated(LegendChangeEvent evt) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * Force the render to refresh. Thread safe.
	 * 
	 * @param async
	 */
	public void refresh(final boolean async) {
		
		if (Thread.currentThread()==Display.getDefault().getThread()) {
			refreshInternal(async);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					refreshInternal(async);
				}
			});
		}
		
	}

	private void refreshInternal(boolean async) {
		if (!isInExporting) {
			if (viewerApp != null) {
				if (!async)
					viewerApp.getCurrentViewer().render();
				else
					viewerApp.getCurrentViewer().renderAsync();
			}
		}
		if ((currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED || currentMode == PlottingMode.SCATTER2D)
				&& legendTable != null)
			legendTable.updateTable(graphColourTable);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void widgetSelected(SelectionEvent e) {
		if (!donotProcessEvent) {
			if (e.getSource().equals(hBar)) {
				if (currentMode == PlottingMode.TWOD) {
					double[] matrix = toolNode.getTransformation().getMatrix();
					double tTransX = matrix[3];
					double tTransY = matrix[7];
					double tTransZ = matrix[11];
					double minValue = -hBar.getMaximum() / 20.0;
					tTransX = -minValue - hBar.getSelection() / 10.0f;
					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
					refresh(false);
				}
			} else	if (e.getSource().equals(vBar) && (e.detail != 0 || SWT.getPlatform().equals("gtk"))) {
				if (currentMode == PlottingMode.TWOD) {
					double[] matrix = toolNode.getTransformation().getMatrix();
					double tTransX = matrix[3];
					double tTransY = matrix[7];
					double tTransZ = matrix[11];
					double minValue = -vBar.getMaximum() / 20.0;
					tTransY = (minValue + vBar.getSelection() / 10.0f)*1.15f;
					MatrixBuilder.euclidean().translate(tTransX, tTransY, tTransZ).assignTo(toolNode);
					refresh(false);
				}
			} else if (e.data != null) {
				if (e.data instanceof List<?> && currentMode == PlottingMode.MULTI2D) {
					List<CompositeEntry> list = (List<CompositeEntry>)e.data;
					((DataSet3DPlot2DMulti)plotter).updateCompositingSettings(list);
					refresh(false);
				}
			}
		}
		donotProcessEvent = false;
	}

	
	@Override
	public void paintControl(PaintEvent e) {
		viewerApp.getCurrentViewer().render();
	}

	@Override
	public void handleEvent(Event event) {
		if (plotter != null) {
			Rectangle bounds = plotArea.getBounds();
			plotter.notifyComponentResize(bounds.width, bounds.height);
		}
	}

	private void removeInitialTools() {
		List<SceneGraphComponent> children = viewerApp.getSceneRoot().getChildComponents();
		List<Tool> rootTools = viewerApp.getSceneRoot().getTools();
		for (Tool t : rootTools) {
			if (t instanceof de.jreality.tools.ClickWheelCameraZoomTool)
				cameraZoomTool = (ClickWheelCameraZoomTool) t;
		}
		// remove the automated added rotation tool

		Tool rotateTool = null;
		Tool dragingTool = null;
		for (SceneGraphComponent child : children) {
			List<Tool> tools = child.getTools();
			for (Tool t : tools) {
				if (t instanceof de.jreality.tools.RotateTool) {
					cameraNode = child;
					rotateTool = t;
				}
				if (t instanceof de.jreality.tools.DraggingTool) {
					toolNode = child;
					dragingTool = t;
				}
			}
		}
		if (toolNode != null && dragingTool != null) {
			toolNode.removeTool(dragingTool);
			cameraNode.removeTool(rotateTool);
			panTool = new PanningTool(toolNode);
			panTool.addPanActionListener(new PanActionListener() {
				
				@Override
				public synchronized void panPerformed(final double xTrans, final double yTrans) {
					hBar.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							double xtrans = xTrans;
							double ytrans = yTrans;
							if (hBar.isVisible()) {
								double minValue = -hBar.getMaximum() / 20.0;
								xtrans = Math.max(xtrans, minValue - 0.25);
								xtrans = Math.min(xtrans, -minValue + 0.25);
								hBar.setSelection(hBar.getMaximum() - (int) ((xtrans - minValue) * 10));
							}
							if (vBar.isVisible()) {
								double minValue = -vBar.getMaximum() / 20.0;
								ytrans = Math.max(ytrans, minValue - 1.5);
								ytrans = Math.min(ytrans, -minValue + 1.5);
								vBar.setSelection((int) ((ytrans - minValue) * 10));
							}
							MatrixBuilder.euclidean().translate(xtrans, ytrans, 0.0).assignTo(toolNode);
							refresh(false);
						}
					});
				}
			});
			cameraRotateTool = new CameraRotationTool();
			dragTool = new SceneDragTool();
			toolNode.addTool(panTool);
		}
		zoomTool = new ClickWheelZoomToolWithScrollBar(root, 
													   toolNode, 
													   (showScrollBars ? hBar : null), 
													   (showScrollBars ? vBar : null));

	}

	
	public void setUseLegend(final boolean useLeg) {
		this.useLegend = useLeg;
		if (useLegend) {
			if (legendTable == null) buildLegendTable();
			legendTable.setVisible(true);
			legendTable.updateTable(graphColourTable);
		} else {
			if (legendTable != null) legendTable.setVisible(false);
		}
		legendTable.getParent().layout();
	}

	
	private void setEnableImageScrollBars(boolean enable) {
		if (!enable) {
			if (hBar != null && hBar.isVisible())
				hBar.setVisible(enable);
			if (vBar != null && vBar.isVisible())
				vBar.setVisible(enable);
		}
		if (zoomTool != null) {
			if (zoomTool instanceof ClickWheelZoomToolWithScrollBar)
				((ClickWheelZoomToolWithScrollBar)zoomTool).setScrollBars((enable?vBar:null), 
																		  (enable?hBar:null));
		}
		showScrollBars = enable;
	}
	
	private void setAxisModes(AxisMode xAxis, AxisMode yAxis, AxisMode zAxis) {
		plotter.setAxisModes(xAxis, yAxis, zAxis);
	}

	public void setTitle(final String titleStr) {
		if(plotter != null) {
			plotter.setTitle(titleStr);
			if (infoBox != null) {
				infoBox.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						infoBox.setName(titleStr);
					}
				});
			}
		}
	}
	
	/**
	 * Set the plotter to a new plotting mode
	 * 
	 * @param newPlotMode
	 *            the new plotting mode
	 */
	private void setMode(PlottingMode newPlotMode) {
		
		if (newPlotMode==currentMode) return;
		clearPlot();
		currentMode = newPlotMode;
		if (hasJOGL) plotArea.setFocus();

		// this might be a bit strange but to make sure
		// the tool doesn't get added twice first remove
		// it if it isn't attached it will simply do nothing

		toolNode.removeTool(panTool);
		toolNode.removeTool(dragTool);
		cameraNode.removeTool(cameraRotateTool);
		viewerApp.getSceneRoot().removeTool(zoomTool);
		viewerApp.getSceneRoot().removeTool(cameraZoomTool);

		switch (currentMode) {
		case ONED:
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			plotter = new DataSet3DPlot1D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			buildLegendTable();
			container.layout();
			setPerspectiveCamera(true,false);
			hBar.setVisible(false);
			vBar.setVisible(false);			
			break;
		case ONED_THREED:
			// this might be a bit strange but to make sure
			// the tool doesn't get added twice first remove
			// it if it isn't attached it will simply do nothing
			plotter = new DataSet3DPlot1DStack(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			buildLegendTable();
			container.layout();
			toolNode.addTool(dragTool);
			cameraNode.addTool(cameraRotateTool);
			viewerApp.getSceneRoot().addTool(cameraZoomTool);
			hBar.setVisible(false);
			vBar.setVisible(false);			
			break;
		case SCATTER2D:
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			plotter = new DataSetScatterPlot2D(viewerApp, plotArea, defaultCursor, graphColourTable, hasJOGL);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			buildLegendTable();
			container.layout();
			setPerspectiveCamera(true,false);
			hBar.setVisible(false);
			vBar.setVisible(false);						
			break;
		case TWOD:
		{	
			root.removeChild(coordTicks);
			plotter = new DataSet3DPlot2D(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			toolNode.addTool(panTool);
			viewerApp.getSceneRoot().addTool(zoomTool);
			if (useLegend)
				buildInfoBox();
			container.layout();
			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
			setPerspectiveCamera(true,false);			
			break;
		}
		case MULTI2D:
			root.removeChild(coordTicks);
			plotter = new DataSet3DPlot2DMulti(viewerApp, plotArea, defaultCursor, panTool, hasJOGL, hasJOGLshaders);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			toolNode.addTool(panTool);
			viewerApp.getSceneRoot().addTool(zoomTool);
			buildCompositingControl();
			container.layout();
			root.getTransformation().addTransformationListener((DataSet3DPlot2D) plotter);
			setPerspectiveCamera(true,false);			
			break;
		case SURF2D:
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			plotter = new DataSet3DPlot3D(viewerApp, hasJOGL, false);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildCoordAxis(coordAxes);
			container.layout();
			toolNode.addTool(dragTool);
			cameraNode.addTool(cameraRotateTool);
			viewerApp.getSceneRoot().addTool(cameraZoomTool);
			setPerspectiveCamera(true,false);
			hBar.setVisible(false);
			vBar.setVisible(false);						
			break;
		case SCATTER3D:
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			plotter = new DataSetScatterPlot3D(viewerApp, hasJOGL, false);
			plotter.buildXCoordLabeling(coordXLabels);
			plotter.buildYCoordLabeling(coordYLabels);
			plotter.buildZCoordLabeling(coordZLabels);
			coordTicks = plotter.buildCoordAxesTicks();
			root.addChild(coordTicks);
			plotter.buildCoordAxis(coordAxes);
			container.layout();
			toolNode.addTool(dragTool);
			cameraNode.addTool(cameraRotateTool);
			viewerApp.getSceneRoot().addTool(cameraZoomTool);
			setPerspectiveCamera(true,false);
			hBar.setVisible(false);
			vBar.setVisible(false);						
			break;
		case BARCHART:
			plotter = new HistogramChartPlot1D(viewerApp, graphColourTable, hasJOGL);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(toolNode);
			MatrixBuilder.euclidean().translate(0.0, 0.0, 0.0).assignTo(root);
			setPerspectiveCamera(true,false);
			hBar.setVisible(false);
			vBar.setVisible(false);						
			break;
		case EMPTY:
			Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
			sceneCamera.setPerspective(true);			
			break;
		}
		coordXLabels.setVisible(true);
		coordYLabels.setVisible(true);	
		coordZLabels.setVisible(true);
		coordAxes.setVisible(true);
	}

	private void buildInfoBox() {
		if (infoBox == null || infoBox.isDisposed()) {
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.heightHint = 55;
			infoBox = new InfoBoxComponent(container, SWT.DOUBLE_BUFFERED);
			infoBox.setLayoutData(gridData);
			container.setWeights(new int[] {90, 10});		
		}
	}
	private void buildCompositingControl() {
		if (cmpControl == null || cmpControl.isDisposed()) {
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.heightHint = 95;
			cmpControl = new CompositingControl(container, SWT.DOUBLE_BUFFERED);
			cmpControl.setLayoutData(gridData);		
			cmpControl.addSelectionListener(this);
			container.setWeights(new int[] {90, 10});					
		}
	}

	private double perspFOV = 56.5;
	private double orthoFOV = 140.0;
	/**
	 * Switch between perspective and orthographic camera
	 * 
	 * @param persp
	 *            should this be a perspective camera (true) otherwise false
	 */
	private void setPerspectiveCamera(boolean persp, boolean needToRender) {
		Camera sceneCamera = CameraUtility.getCamera(viewerApp.getCurrentViewer());
		if (sceneCamera.isPerspective())
			perspFOV = sceneCamera.getFieldOfView();
		else
			orthoFOV = sceneCamera.getFieldOfView();

		sceneCamera.setPerspective(persp);
		if (persp)
			sceneCamera.setFieldOfView(perspFOV);
		else
			sceneCamera.setFieldOfView(orthoFOV);
		if (needToRender) viewerApp.getCurrentViewer().render();
	}

	private void clearPlot() {
		removeOldSceneNodes();
		if (legendTable != null) {
			legendTable.removeAllLegendChangeEventListener();
			legendTable.dispose();
			legendTable = null;
		}
		if (infoBox != null) {
			infoBox.dispose();
			infoBox = null;
		}
		if (cmpControl != null) {
			cmpControl.removeSelectionListener(this);
			cmpControl.dispose();
			cmpControl = null;
		}

	}
	
	private void removeOldSceneNodes() {
		if (bbox != null) {
			root.removeChild(bbox);
			bbox = null;
		}
		if (coordTicks != null) {
			root.removeChild(coordTicks);
			coordTicks = null;
		}
		if (coordGrid != null) {
			root.removeChild(coordGrid);
			coordGrid = null;
		}
		if (currentMode == PlottingMode.TWOD) {
			if (root != null && root.getTransformation() != null)
				root.getTransformation().removeTransformationListener((DataSet3DPlot2D) plotter);
		}
		if (graph != null) {
			graph.setGeometry(null);
		}
		// remove all hanged on children on the graph node
		if (plotter != null)
			plotter.cleanUpGraphNode();

		// since we removed all the previous
		// scene nodes now might be a good time
		// to call garbage collector to make sure

		// System.gc();
	}


}
