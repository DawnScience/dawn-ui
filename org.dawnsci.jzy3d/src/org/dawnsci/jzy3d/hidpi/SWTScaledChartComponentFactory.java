package org.dawnsci.jzy3d.hidpi;

import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.jzy3d.bridge.IFrame;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.keyboard.camera.ICameraKeyController;
import org.jzy3d.chart.controllers.keyboard.camera.NewtCameraKeyController;
import org.jzy3d.chart.controllers.keyboard.screenshot.IScreenshotKeyController;
import org.jzy3d.chart.controllers.keyboard.screenshot.IScreenshotKeyController.IScreenshotEventListener;
import org.jzy3d.chart.controllers.keyboard.screenshot.NewtScreenshotKeyController;
import org.jzy3d.chart.controllers.mouse.camera.ICameraMouseController;
import org.jzy3d.chart.controllers.mouse.camera.NewtCameraMouseController;
import org.jzy3d.chart.controllers.mouse.picking.IMousePickingController;
import org.jzy3d.chart.controllers.mouse.picking.NewtMousePickingController;
import org.jzy3d.chart.factories.ChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.chart.swt.SWTChart;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Dimension;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.maths.Utils;
import org.jzy3d.plot3d.primitives.axes.AxeBox;
import org.jzy3d.plot3d.primitives.axes.IAxe;
import org.jzy3d.plot3d.rendering.canvas.CanvasNewtAwt;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.OffscreenCanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Scene;
import org.jzy3d.plot3d.rendering.view.AWTRenderer3d;
import org.jzy3d.plot3d.rendering.view.AWTView;
import org.jzy3d.plot3d.rendering.view.Renderer3d;
import org.jzy3d.plot3d.rendering.view.View;
import org.jzy3d.plot3d.rendering.view.layout.ColorbarViewportLayout;
import org.jzy3d.plot3d.rendering.view.layout.IViewportLayout;

import com.jogamp.opengl.GLCapabilities;

public class SWTScaledChartComponentFactory extends ChartComponentFactory {
//    static Logger logger = Logger.getLogger(SWTScaledChartComponentFactory.class);
    
    private Composite canvas;
    
    private SWTScaledChartComponentFactory(Composite canvas) {
    	this.canvas = canvas;
    }

    public static Chart chart(Composite parent) {
    	SWTScaledChartComponentFactory f = new SWTScaledChartComponentFactory(parent);
    	return f.newChart(Quality.Intermediate, Toolkit.swt_newt);
    }
    
    public static Chart chart(Composite parent, Quality quality) {
    	SWTScaledChartComponentFactory f = new SWTScaledChartComponentFactory(parent);
    	return f.newChart(quality, Toolkit.swt_newt);
    }

    /* */

    /**
     * @param toolkit can be used to indicate "offscreen, 800, 600" and thus replace implicit "awt"
     */
    @Override
    public Chart newChart(IChartComponentFactory factory, Quality quality, String toolkit) {
        return new SWTChart(canvas, factory, quality, toolkit);
    }
    
    public Composite getComposite() {
    	return canvas;
    }

    @Override
    public IAxe newAxe(BoundingBox3d box, View view) {
        AxeBox axe = new AxeBox(box);
        axe.setView(view);
        return axe;
    }

    @Override
    public IViewportLayout newViewportLayout() {
        return new ColorbarViewportLayout();
    }

    /**
     * The AWTView support Java2d defined components (tooltips, background
     * images)
     */
    @Override
    public View newView(Scene scene, ICanvas canvas, Quality quality) {
        return new AWTView(getFactory(), scene, canvas, quality);
    }

    /** Provide AWT Texture loading for screenshots */
    @Override
    public Renderer3d newRenderer(View view, boolean traceGL, boolean debugGL) {
        return new AWTRenderer3d(view, traceGL, debugGL);
    }

    /** bypass reflection used in super implementation */
    @Override
    protected IFrame newFrameSwing(Chart chart, Rectangle bounds, String title) {
        return null;
    }

    /** bypass reflection used in super implementation */
    @Override
    protected IFrame newFrameAWT(Chart chart, Rectangle bounds, String title, String message) {
        return null;
    }

    @Override
    public ICanvas newCanvas(IChartComponentFactory factory, Scene scene, Quality quality, String windowingToolkit, GLCapabilities capabilities) {
        boolean traceGL = false;
        boolean debugGL = false;
        Toolkit chartType = getToolkit(windowingToolkit);
        switch (chartType) {
        case awt:
            return newCanvasAWT(factory, scene, quality, capabilities, traceGL, debugGL);
        case swing:
//            Logger.getLogger(ChartComponentFactory.class).warn("Swing canvas is deprecated. Use Newt instead");
            return newCanvasSwing(factory, scene, quality, capabilities, traceGL, debugGL);
        case newt:
            return new CanvasNewtAwt(factory, scene, quality, capabilities, traceGL, debugGL);
        case offscreen:
            Dimension dimension = getCanvasDimension(windowingToolkit);
            return new OffscreenCanvas(factory, scene, quality, capabilities, dimension.width, dimension.height, traceGL, debugGL);
        case swt_newt:
        	return new CanvasNewtScaledSWT(factory, scene, quality, capabilities, traceGL, debugGL);
        default:
            throw new RuntimeException("unknown chart type:" + chartType);
        }
    }

    /** bypass reflection used in super implementation */
    @Override
    protected ICanvas newCanvasAWT(IChartComponentFactory chartComponentFactory, Scene scene, Quality quality, GLCapabilities capabilities, boolean traceGL, boolean debugGL) {
        return null;
    }

    /** bypass reflection used in super implementation */
    @Override
    protected ICanvas newCanvasSwing(IChartComponentFactory chartComponentFactory, Scene scene, Quality quality, GLCapabilities capabilities, boolean traceGL, boolean debugGL) {
        return null;
    }

    @Override
    public IChartComponentFactory getFactory() {
        return this;
    }

    @Override
    public ICameraMouseController newMouseCameraController(Chart chart) {
            return new NewtCameraMouseController(chart);
    }
    
    @Override
    public IMousePickingController newMousePickingController(Chart chart, int clickWidth) {
            return new NewtMousePickingController(chart, clickWidth);
    }

    /**
     * Output file of screenshot can be configured using {@link IScreenshotKeyController#setFilename(String)}.
     */
    @Override
    public IScreenshotKeyController newKeyboardScreenshotController(Chart chart) {
        // trigger screenshot on 's' letter
        String file = SCREENSHOT_FOLDER + "capture-" + Utils.dat2str(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".png";
        IScreenshotKeyController screenshot = new NewtScreenshotKeyController(chart, file);
        
        
        screenshot.addListener(new IScreenshotEventListener() {
            @Override
            public void failedScreenshot(String file, Exception e) {
//                logger.error("Failed to save screenshot to '" + file + "'", e);
            }

            @Override
            public void doneScreenshot(String file) {
//                logger.info("Screenshot save to '" + file + "'");
            }
        });
        return screenshot;
    }

    @Override
    public ICameraKeyController newKeyboardCameraController(Chart chart) {
        ICameraKeyController key = new NewtCameraKeyController(chart);
        return key;
    }

    @Override
    public IFrame newFrame(Chart chart, Rectangle bounds, String title) {
        return null;
    }
}
