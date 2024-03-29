/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
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

import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.function.Downsample;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.image.IFileIconService;
import org.eclipse.dawnsci.plotting.api.image.IPlotImageService;
import org.eclipse.dawnsci.plotting.api.image.PlotImageData;
import org.eclipse.dawnsci.plotting.api.image.PlotImageData.PlotImageType;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
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

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.io.AWTImageUtils;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

/**
 * A service to provide SWT Image objects for 2D data.
 * @author Matthew Gerring
 *
 */
public class PlotImageService extends AbstractServiceFactory implements IPlotImageService {
	
	static {
		System.out.println("Starting PlotImageService");
	}

	public PlotImageService() {
	}

	private static ImageRegistry imageRegistry;

	@Override
	public Image createImage(final File f, final int width, int height) {
		
		if (f.isDirectory()) {
			final Image image = Activator.getImageDescriptor("icons/folder.gif").createImage();
			final Image blank = new Image(Display.getDefault(), width, height);
			GC gc = new GC(blank);
			gc.drawImage(image, (width/2)-image.getImageData().width/2, height/2-image.getImageData().height/2);
			gc.dispose();
			image.dispose();
			return blank;
		}
		
		try {
			final Dataset thumb = getThumbnail(f, width, height);
			return createImageSWT(thumb, null);
		} catch (Throwable ne) {
			
			if (imageRegistry == null) imageRegistry = JFaceResources.getImageRegistry();
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
		image.dispose();

		return blank;
	}

	private Dataset getThumbnail(final File f, final int wdith, final int height) throws Throwable {
		final ILoaderService loader = ServiceProvider.getService(ILoaderService.class);
		IDataHolder dh = loader.getData(f.getAbsolutePath(),null);
		ILazyDataset lz = dh.getLazyDataset(0);
		if (lz.getRank() == 2) {
			final Dataset set   = DatasetUtils.convertToDataset(lz.getSlice());
			final Dataset thumb = getThumbnail(set, wdith, height);
			return thumb;
		}
		return null;
	}

	public Dataset getThumbnail(final IDataset ds,  final int w, final int h) {

		if (ds!=null && ds.getRank() == 2) { // 2D datasets only!!!
			int width = ds.getShape()[1];
			int height = ds.getShape()[0];

			int[] stepping = new int[2];
			stepping[1] = Math.max(1, width / w);
			stepping[0] = Math.max(1, height / h);
			Downsample down = new Downsample(DownsampleMode.POINT, stepping);
			Dataset ds_downsampled = DatasetUtils.convertToDataset(down.value(ds).get(0));
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

		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
		
		if (bean==null) {
			bean = new ImageServiceBean();
			final IPaletteService pservice = ServiceProvider.getService(IPaletteService.class);
			bean.setPalette(pservice.getDirectPaletteData(store.getString(PlottingConstants.COLOUR_SCHEME)));
			bean.setOrigin(ImageOrigin.forLabel(store.getString(PlottingConstants.ORIGIN_PREF)));
		}
		bean.setImage(thumbnail);
		
		final IImageService service = new ImageService();
		return  service.getImage(bean);
	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
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
			final Dataset thumb = getThumbnail(set, width, height);
			if (thumb==null) return null;
			return createImage(thumb, (ImageServiceBean)data.getImageServiceBean());
		} else {
			PlotDisposable pd  = (PlotDisposable)data.getDisposable();
			if (pd == null) pd = (PlotDisposable)createPlotDisposable(null);
			final PlotDisposable plotDisposable = pd;
			
			final IPlottingSystem<?> system = pd.getSystem();
			
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
						List<? extends IDataset> oaxes = trace.getAxes();
						List<IDataset> axes  = new ArrayList<IDataset>(3);
						if (oaxes==null) {
							axes.add(DatasetFactory.createRange(IntegerDataset.class, set.getShape()[1]));
							axes.add(DatasetFactory.createRange(IntegerDataset.class, set.getShape()[0]));
						} else {
							axes.add(oaxes.get(0));
							axes.add(oaxes.get(1));
						}

						// z only gets larger
						double zLow = Math.min(data.getzLower(), set.min().doubleValue());
						double zUp  = Math.max(data.getzUpper(), set.max().doubleValue());
						IDataset z  = DatasetFactory.createRange(zLow, zUp, (zUp-zLow)/1000);
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
						scaled[0]   = ((AbstractPlottingSystem<?>)system).getImage(new Rectangle(0, 0, width, height));
					
					} else { // They wanted an icon
						final Image unscaled = ((AbstractPlottingSystem<?>) system).getImage(new Rectangle(0, 0, 300, 300));
						scaled[0] = new Image(display, unscaled.getImageData().scaledTo(width, height));
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
		IPlottingSystem<?> system = plotName!=null 
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
		private IPlottingSystem<?> system;
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

		public <T> IPlottingSystem<T> getSystem() {
			return (IPlottingSystem<T>)system;
		}

		public <T> void setSystem(IPlottingSystem<T> system) {
			this.system = system;
		}

		public Shell getShell() {
			return shell;
		}

		public void setShell(Shell shell) {
			this.shell = shell;
		}
	}

	public Image getIconForFile(final File file) {
		if (file.isDirectory()) {
			return getFolderImage(file);
		}

		final String ext = FileUtils.getFileExtension(file);
		if (imageRegistry == null)
			imageRegistry = JFaceResources.getImageRegistry();

		Image returnImage = imageRegistry.get(ext);
		if (returnImage != null)
			return returnImage;

		// Eclipse icon
		ECLISPE_BLOCK: if (returnImage == null) {
			final IEditorDescriptor desc = PlatformUI.getWorkbench()
					.getEditorRegistry()
					.getDefaultEditor(file.getAbsolutePath());
			if (desc == null)
				break ECLISPE_BLOCK;
			final ImageDescriptor imageDescriptor = desc.getImageDescriptor();
			if (imageDescriptor == null)
				break ECLISPE_BLOCK;
			returnImage = imageDescriptor.createImage();
		}

		// Program icon from system
		if (returnImage == null) {
			final Program program = Program.findProgram(ext);
			if (program != null) {
				final ImageData iconData = Program.findProgram(ext).getImageData();
				if (iconData != null) //Might happen it does not exist
					returnImage = new Image(Display.getCurrent(), iconData);
			}
		}
		if (returnImage == null)
			returnImage = getImageSWT(file);

		/* Not storing null image for ext, because the reason of null is the
		 * broken file. Even if a broken file icon would be gotten in the
		 * future, the image still should not be attached to ext.
		 */
		if (returnImage != null)
			imageRegistry.put(ext, returnImage);

		return returnImage;
	}

	@Override
	public Image createImage(final BufferedImage image) {
		return new Image(null,convertToSWT(image));
	}

	@Override
	public IDataset createDataset(final BufferedImage bufferedImage) {
		return AWTImageUtils.makeDatasets(bufferedImage, true)[0];
	}

	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
					colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
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
			IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
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
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
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
		if (systemIcon == null) // Happens when file does not exist
			return null;
		java.awt.Image image = systemIcon.getImage();
		if (image instanceof BufferedImage) {
			return new Image(Display.getDefault(), convertToSWT((BufferedImage) image));
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
	private static Image rootFolderImage;

	private Image getFolderImage(File file) {
		//file should not be null, but if it is, then try a folder name (next line should be deleted)
//		if (file==null) file = isWindowsOS() ? new File("C:/Windows/") : new File("/etc");
		//if next line causes NPE, then uncommenting previous line is quick fix, but not optimal
		if (imageRegistry == null) {
			imageRegistry = JFaceResources.getImageRegistry();
		}
		if( file.getName().isEmpty() ) { //It is a root folder
			if (rootFolderImage==null) {
				/**
				 * On windows, use windows icon for folder,
				 * on unix folder icon can be not very nice looking, use folder.png
				 */
				if (isWindowsOS()) {
					rootFolderImage = getImageSWT(file);
				} else {
					rootFolderImage = Activator.getImageDescriptor("icons/folder.gif").createImage();
				}
				imageRegistry.put(Activator.PLUGIN_ID + ":root_folder", rootFolderImage);
			}
			return rootFolderImage;
		} else {
			if (folderImage==null) {
				/**
				 * On windows, use windows icon for folder,
				 * on unix folder icon can be not very nice looking, use folder.png
				 */
				if (isWindowsOS()) {
					folderImage = getImageSWT(file);
				} else {
					folderImage = Activator.getImageDescriptor("icons/folder.gif").createImage();
				}
				imageRegistry.put(Activator.PLUGIN_ID + ":folder", folderImage);
			}
			return folderImage;
		}
	}

	/**
	 * @return true if windows
	 */
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

	@Override
	public Image getIconForFile(String path) {
		return getIconForFile(new File(path));
	}

}
