/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.services;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.IPlotImageService;
import org.dawb.common.services.PlotImageData;
import org.dawb.common.services.PlotImageData.PlotImageType;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.util.io.FileUtils;
import org.dawnsci.io.h5.H5Loader;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.dawnsci.plotting.api.histogram.functions.AbstractMapFunction;
import org.dawnsci.plotting.api.histogram.functions.GlobalColourMaps;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.services.util.SWTImageUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;
import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * A service to provide SWT Image objects for 2D data.
 * @author fcp94556
 *
 */
public class PlotImageService extends AbstractServiceFactory implements IPlotImageService {
	
	public PlotImageService() {
		
	}
	static {
		// We just use file extensions
		LoaderFactory.setLoaderSearching(false); 
		// This now applies for the whole workbench
	}
	
	private static float minimumThreshold = 0.98f;
	private static int colourMapChoice    = 1;
    private static ImageRegistry imageRegistry;
    
    @Override
	public Image createImage(final File f, final int width, int height) {
		
		if (f.isDirectory()) {
			final Image image = Activator.getImageDescriptor("icons/folder.gif").createImage();
			final Image blank = new Image(Display.getDefault(), width, height);
			GC gc = new GC(blank);
	        gc.drawImage(image, (width/2)-image.getImageData().width/2, height/2-image.getImageData().height/2);
	        gc.dispose();
	        
	        return blank;
		}
		
		try {
			final AbstractDataset thumb = getThumbnail(f, width, height);
		    return createImageSWT(thumb, null);
		    
		} catch (Throwable ne) {
			
			if (imageRegistry == null) imageRegistry = new ImageRegistry(Display.getDefault());
			final String extension = FileUtils.getFileExtension(f);
			Image image = imageRegistry.get(extension);
			if (image != null) return image;

			Program program = Program.findProgram(extension);
			ImageData imageData = (program == null ? null : program.getImageData());
			if (imageData != null) {
				image = new Image(Display.getDefault(), imageData);
				imageRegistry.put(extension, image);
			    return image;
			}

		}
		
		final Image image = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(f.getAbsolutePath()).createImage();
		final Image blank = new Image(Display.getDefault(), width, height);
		GC gc = new GC(blank);
        gc.drawImage(image, (width/2)-image.getImageData().width/2, height/2-image.getImageData().height/2);
        gc.dispose();
        
        return blank;
	}
	
	private AbstractDataset getThumbnail(final File f, final int wdith, final int height) throws Throwable {
		
	    if (H5Loader.isH5(f.getAbsolutePath())) return null; // Cannot risk loading large datasets!
		final ILoaderService loader = (ILoaderService)ServiceManager.getService(ILoaderService.class);
		final AbstractDataset set   = (AbstractDataset)loader.getDataset(f.getAbsolutePath(), null);
		final AbstractDataset thumb = getThumbnail(set, wdith, height);
		return thumb;
	}

	public AbstractDataset getThumbnail(final IDataset ds,  final int w, final int h) {

		if (ds!=null && ds.getRank() == 2) { // 2D datasets only!!!
			int width = ds.getShape()[1];
			int height = ds.getShape()[0];

			int[] stepping = new int[2];
			stepping[1] = Math.max(1, width / w);
			stepping[0] = Math.max(1, height / h);
			Downsample down = new Downsample(DownsampleMode.POINT, stepping);
			AbstractDataset ds_downsampled = down.value(ds).get(0);
			ds_downsampled.setName(ds.getName());
			return ds_downsampled;
		}

		return null;
		
	}

	/**
	 * Modified from fable
	 * @param thumbnail
	 * @return
	 * @throws Exception 
	 */
	public Image createImageSWT(final IDataset thumbnail, ImageServiceBean bean) throws Exception {
        
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		
		if (bean==null) {
			bean = new ImageServiceBean();
			final IPaletteService pservice = (IPaletteService)ServiceManager.getService(IPaletteService.class);
			bean.setPalette(pservice.getDirectPaletteData(store.getString("org.dawb.plotting.system.colourSchemeName")));	
			bean.setOrigin(ImageOrigin.forLabel(store.getString("org.dawb.plotting.system.originChoice")));
		}
		bean.setImage(thumbnail);
		
		final IImageService service = (IImageService)ServiceManager.getService(IImageService.class);
		return  service.getImage(bean);
	}
	/**
	 * Modified from GDA
	 * @param thumbnail
	 * @return
	 */
	public Image createImageDiamond(final AbstractDataset thumbail) {
		
		GlobalColourMaps.InitializeColourMaps();
		
		final int[] shape = thumbail.getShape();
		if (shape.length == 2) {
			double max;
			if (thumbail instanceof RGBDataset) {
				double temp;
				max = Stats.quantile(((RGBDataset) thumbail).createRedDataset(AbstractDataset.INT16),
						minimumThreshold);
				temp = Stats.quantile(((RGBDataset) thumbail).createGreenDataset(AbstractDataset.INT16),
						minimumThreshold);
				if (max < temp)
					max = temp;
				temp = Stats.quantile(((RGBDataset) thumbail).createBlueDataset(AbstractDataset.INT16),
						minimumThreshold);
				if (max < temp)
					max = temp;
			} else {
				max = Stats.quantile(thumbail, minimumThreshold);
			}
			int redSelect   = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4);
			int greenSelect = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 1);
			int blueSelect  = GlobalColourMaps.colourSelectList.get(colourMapChoice * 4 + 2);
			
			AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(redSelect));
			AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(greenSelect));
			AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(Math.abs(blueSelect));
			ImageData imgD = SWTImageUtils.createImageData(thumbail, max, redFunc, greenFunc, blueFunc,
					(redSelect < 0), (greenSelect < 0), (blueSelect < 0));
			
			return new Image(Display.getDefault(), imgD);
		}
		
		return null;
	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		
		if (serviceInterface==IPlotImageService.class) {
			return new PlotImageService();
		} else if (serviceInterface==IFileIconService.class) {
			return new PlotImageService();
		}
		return null;
	}

	private Image createImage(IDataset thumb, ImageServiceBean bean) throws Exception {
		return createImageSWT(thumb, bean);
	}

	@Override
	public Image getImage(final PlotImageData data) throws Exception {
		
		final IDataset set  = data.getData();
		final int     width = data.getWidth();
		final int    height = data.getHeight();
		
		if (set.getShape().length==2 && data.getType()==PlotImageType.IMAGE_ONLY) {
			final AbstractDataset thumb = getThumbnail(set, width, height);
			if (thumb==null) return null;
			return createImage(thumb, (ImageServiceBean)data.getImageServiceBean());
			
		} else {

			PlotDisposable pd  = (PlotDisposable)data.getDisposable();
			if (pd == null) pd = (PlotDisposable)createPlotDisposable(null);
			final PlotDisposable plotDisposable = pd;
			
			final IPlottingSystem system = pd.getSystem();
		
			
			final Image[] scaled = new Image[1];
			
			final Shell   shell   = plotDisposable.getShell();
			final Display display = shell!=null ? shell.getDisplay() : Display.getDefault();
			
			display.syncExec(new Runnable() {
				public void run() {
					
					if (shell!=null) shell.setSize(width+20, height+20);
					
					if (set.getShape().length==1) {
						system.updatePlot1D(null, Arrays.asList(set), new NullProgressMonitor());
						
					} else if (data.getType()==PlotImageType.IMAGE_PLOT) {
						system.setPlotType(PlotType.IMAGE);
						system.updatePlot2D(set, null, new NullProgressMonitor());
						
					} else if (data.getType()==PlotImageType.SURFACE_PLOT) {
						final ISurfaceTrace trace = (ISurfaceTrace)system.getTraces(ISurfaceTrace.class).iterator().next();
						
						// Keep z constant
						List<IDataset> oaxes = trace.getAxes();
						List<IDataset> axes  = new ArrayList<IDataset>(3);
						if (oaxes==null) {
							axes.add(AbstractDataset.arange(set.getShape()[1], AbstractDataset.INT));
							axes.add(AbstractDataset.arange(set.getShape()[0], AbstractDataset.INT));
						} else {
							axes.add(oaxes.get(0));
							axes.add(oaxes.get(1));
						}

						// z only gets larger
						double zLow = Math.min(data.getzLower(), set.min().doubleValue());
						double zUp  = Math.max(data.getzUpper(), set.max().doubleValue());
						IDataset z  = AbstractDataset.arange(zLow, zUp, (zUp-zLow)/1000, AbstractDataset.FLOAT);
						axes.add(z);
						
						trace.setData(data.getData(), axes);
					}

					if (data.getPlotTitle()!=null) system.setTitle(data.getPlotTitle());
					system.repaint(true);
					
					// We try to make the axes only grow if they are caching plotting because
					// it stops the video being shakey.
					if (data.getDisposable()!=null && data.isConstantRange() && data.getType()!=PlotImageType.SURFACE_PLOT){
						double yLow = Math.min(data.getyLower(), system.getSelectedYAxis().getLower());
						double yUp  = Math.max(data.getyUpper(), system.getSelectedYAxis().getUpper());
						data.setyLower(yLow);
						data.setyUpper(yUp);

						double xLow = Math.min(data.getxLower(), system.getSelectedXAxis().getLower());
						double xUp  = Math.max(data.getxUpper(), system.getSelectedXAxis().getUpper());
						data.setxLower(xLow);
						data.setxUpper(xUp);
						
						system.getSelectedYAxis().setRange(yLow, yUp);
						system.getSelectedXAxis().setRange(xLow, xUp);
					}
					
					if (width>=300) {
						scaled[0]   = ((AbstractPlottingSystem)system).getImage(new Rectangle(0, 0, width, height));
					
					} else { // They wanted an icon
			            final Image unscaled = ((AbstractPlottingSystem)system).getImage(new Rectangle(0, 0, 300, 300));
			            scaled[0]   = new Image(display, unscaled.getImageData().scaledTo(width, height));
					}
					
					// They are inefficiently make a new plot part each time.
					if (data.getDisposable()==null) plotDisposable.dispose();
				}
			});
            return scaled[0];
		}
	}
	
	@Override
	public IDisposable createPlotDisposable(String plotName) throws Exception {
		
		// We plot to an offscreen plotting system, then take a screen shot of this.
		final PlotDisposable ret = new PlotDisposable();
		IPlottingSystem system = plotName!=null 
		                       ? PlottingFactory.getPlottingSystem(plotName)
		                       : PlottingFactory.getLightWeightPlottingSystem();
		                       
		if (system==null) system = PlottingFactory.getLightWeightPlottingSystem();
		ret.setSystem(system);
				
		if (system.getPlotComposite()==null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					final Shell   shell   = new Shell(Display.getDefault(), SWT.RESIZE|SWT.NO_TRIM);
					ret.setShell(shell);
					shell.setSize(600, 600);
					
					shell.setLayout(new FillLayout());
					final Composite main = new Composite(shell, SWT.NONE);
					main.setLayout(new GridLayout(1, false));
					
					final Composite plotter = new Composite(main, SWT.NONE);
					plotter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					
					ret.getSystem().createPlotPart(plotter, "Thumbnail", null, PlotType.XY, null);		
				}
			});
		}
		
		return ret;
	}
	
	protected static class PlotDisposable implements IDisposable {
		
		private IPlottingSystem system;
		private Shell           shell;

		@Override
		public void dispose() {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (system!=null) system.dispose();
					if (shell!=null)  shell.dispose();
				}
			});
		}

		public IPlottingSystem getSystem() {
			return system;
		}

		public void setSystem(IPlottingSystem system) {
			this.system = system;
		}

		public Shell getShell() {
			return shell;
		}

		public void setShell(Shell shell) {
			this.shell = shell;
		}
	}

	    
    public Image getIconForFile(File file) {
    	
    	if (file.isDirectory()) {
    		return getFolderImage(file);
    	}

    	final String ext = FileUtils.getFileExtension(file);
    	if (imageRegistry == null) imageRegistry = new ImageRegistry();
    	
    	Image returnImage = imageRegistry.get(ext);
    	if (returnImage != null) return returnImage;   	
    	    	
    	// Eclipse icon
    	ECLISPE_BLOCK: if (returnImage==null) {
    		final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getAbsolutePath());
    		if (desc==null) break ECLISPE_BLOCK;
    		final ImageDescriptor imageDescriptor = desc.getImageDescriptor();
    		if (imageDescriptor==null) break ECLISPE_BLOCK;
	    	returnImage = imageDescriptor.createImage();
    	}

    	
    	// Program icon from system
    	if (returnImage==null) {
	    	final Program program = Program.findProgram(ext);
	    	
	    	if (program!=null) {
		    	ImageData iconData=Program.findProgram(ext).getImageData();
		    	returnImage = new Image(Display.getCurrent(), iconData);
	    	}
    	}
    	    	
    	if (returnImage==null)	returnImage = getImageSWT(file);
    	
    	imageRegistry.put(ext, returnImage);
    	
    	return returnImage;
    }
    
    static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                    for (int x = 0; x < data.width; x++) {
                            int rgb = bufferedImage.getRGB(x, y);
                            int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)); 
                            data.setPixel(x, y, pixel);
                            if (colorModel.hasAlpha()) {
                                    data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                            }
                    }
            }
            return data;            
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                    rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                    for (int x = 0; x < data.width; x++) {
                            raster.getPixel(x, y, pixelArray);
                            data.setPixel(x, y, pixelArray[0]);
                    }
            }
            return data;
        }
        return null;
    }
    
    static Image getImageSWT(File file) {
        ImageIcon systemIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
        java.awt.Image image = systemIcon.getImage();
        if (image instanceof BufferedImage) {
            return new Image(Display.getDefault(), convertToSWT((BufferedImage)image));
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return new Image(Display.getDefault(), convertToSWT(bufferedImage));
    }
    
    private static Image folderImage;
    
	private Image getFolderImage(File file) {
		
		if (folderImage==null) {
			
			if (file==null) file = isWindowsOS() ? new File("C:/Windows/") : new File("/");
			/**
			 * On windows, use windows icon for folder,
			 * on unix folder icon can be not very nice looking, use folder.png
			 */
	        if (isWindowsOS()) {
	        	folderImage = getImageSWT(file);
	        } else {
	        	folderImage = Activator.getImageDescriptor("icons/folder.gif").createImage();

	        }
 			
		}
		return folderImage;
	}

	/**
	 * @return true if windows
	 */
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

}
