/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.jreality.print;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jreality.softviewer.PSRenderer;
import de.jreality.softviewer.SVGRenderer;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.ui.viewerapp.actions.file.ExportImage;

/**
 * Utility class for exporting any active plotting area either as Image file or hardcopy on a printer device
 */
public class PlotExportUtil {

	public static final String[] FILE_TYPES = new String[] { "PNG/JPEG File", "Postscript File", "SVG File" };
	private static final Logger logger = LoggerFactory.getLogger(PlotExportUtil.class);
	private static final String tempDirectory = System.getProperty("java.io.tmpdir");

	private static void savePostScript(File imageFile, AbstractViewerApp viewerApp) throws FileNotFoundException {
		PSRenderer rv;
		Dimension d = viewerApp.getCurrentViewer().getViewingComponentSize();
		rv = new PSRenderer(new PrintWriter(imageFile), d.width, d.height);
		rv.setCameraPath(viewerApp.getCurrentViewer().getCameraPath());
		rv.setSceneRoot(viewerApp.getCurrentViewer().getSceneRoot());
		rv.setAuxiliaryRoot(viewerApp.getCurrentViewer().getAuxiliaryRoot());
		// rv.initializeFrom(viewer);
		rv.render();
	}

	private static void saveSVG(File imageFile, AbstractViewerApp viewerApp) throws FileNotFoundException {
		Dimension d = viewerApp.getCurrentViewer().getViewingComponentSize();
		SVGRenderer rv;
		rv = new SVGRenderer(new PrintWriter(imageFile), d.width, d.height);
		rv.setCameraPath(viewerApp.getCurrentViewer().getCameraPath());
		rv.setSceneRoot(viewerApp.getCurrentViewer().getSceneRoot());
		rv.setAuxiliaryRoot(viewerApp.getCurrentViewer().getAuxiliaryRoot());
		rv.render();
	}

	private static void copytoClipboard(AbstractViewerApp viewerApp) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart active = page.getActivePart();
		File imageFile = new File(tempDirectory + "/" + active.getTitle() + ".png");

		ExportImage.exportImage(viewerApp.getCurrentViewer(), imageFile, 1);

		// copy temp file created to clipboard
		Display display = Display.getCurrent();
		Clipboard clipboard = new Clipboard(display);
		String[] data = { imageFile.getAbsolutePath() };
		clipboard.setContents(new Object[] { data }, new Transfer[] { FileTransfer.getInstance() });
		clipboard.dispose();
	}

	/**
	 * Save the graph with the given filename. If the file name ends with a known extension, this is used as the file
	 * type otherwise it is the string passed in which is read from the save as dialog form normally.
	 * 
	 * @param filename
	 *            the name under which the graph should be saved
	 * @param fileType
	 *            type of the file
	 * @param viewerApp
	 *            ViewerApp containing the current display
	 * @throws FileNotFoundException
	 */
	public static synchronized void saveGraph(String filename, String fileType, AbstractViewerApp viewerApp)
			throws Exception {

		if (!Arrays.asList(FILE_TYPES).contains(fileType))
			throw new RuntimeException("Cannot deal with file type " + fileType);
		// If they have specified the file type in the file name, use that.
		String lname = filename.toLowerCase();
		if (lname.endsWith(".png") || lname.endsWith(".jpg") || lname.endsWith(".jpeg"))
			fileType = FILE_TYPES[0];
		if (lname.endsWith(".ps") || lname.endsWith(".eps"))
			fileType = FILE_TYPES[1];
		if (lname.endsWith(".svg"))
			fileType = FILE_TYPES[2];

		if (fileType.equals(FILE_TYPES[0])) {
			if (!lname.endsWith(".png") && !lname.endsWith(".jpg") && !lname.endsWith(".jpeg"))
				filename = filename + ".png";
			ExportImage.exportImage(viewerApp.getCurrentViewer(), new File(filename), 1);

		} else if (fileType.equals(FILE_TYPES[1])) {
			if (!lname.endsWith(".ps") && !lname.endsWith(".eps"))
				filename = filename + ".ps";
			savePostScript(new File(filename), viewerApp);

		} else if (fileType.equals(FILE_TYPES[2])) {
			if (!lname.endsWith(".svg"))
				filename = filename + ".svg";
			saveSVG(new File(filename), viewerApp);
		} else {
			throw new RuntimeException("Cannot process " + fileType);
		}
	}

	/**
	 * Copy the graph to the Clipboard.
	 * 
	 * @param viewerApp
	 *            ViewerApp containing the current display
	 */
	public static synchronized void copyGraph(AbstractViewerApp viewerApp) {

		copytoClipboard(viewerApp);

	}

	/**
	 * Print the graph to a printer. This will temporary create an image with 4 times super sampling for good quality
	 * and then sends the image to the printer, afterwards deleting the image again
	 * 
	 * @param printerData
	 *            SWT specific printer object
	 * @param viewerApp
	 *            ViewerApp containing the current display
	 * @param display
	 *            SWT display // * @param scaling factor
	 */
	public static synchronized void printGraph(PrinterData printerData, AbstractViewerApp viewerApp, Display display,
			Plot1DGraphTable legendTable, float scaling) {
		if (printerData != null) {
			File imageFile = null;
			// create a temporary image file
			try {
				imageFile = File.createTempFile("test", ".png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (imageFile != null) {
				Printer printer = new Printer(printerData);
				Rectangle area = printer.getClientArea();
				final Point dpi = printer.getDPI();
				final int oversampling = dpi.x < 100 ? 1 : 4;
				if (oversampling == 1) {
					logger.info("Printer dpi was lower than 100 ({}) so oversampling not used", dpi.x);
				}

				final Dimension dims = viewerApp.getCurrentViewer().getViewingComponentSize();
				float imageAspect = (float) dims.width / (float) dims.height;

				float printAspect = (float) area.width / (float) area.height;
				int printScaleFactor = 1;

				// check if we run on gtk don't trust gtk when it comes down to
				// the printer resolution so increase it by a major factor

				if (SWT.getPlatform().toLowerCase().equals("gtk"))
					printScaleFactor = 4;

				ImageData imageData;
				if (imageAspect > printAspect) {
					ExportImage.exportImage(viewerApp.getCurrentViewer(), imageFile, area.height * printScaleFactor,
							area.width * printScaleFactor, oversampling);

					// make new image
					ImageLoader loader = new ImageLoader();
					imageData = loader.load(imageFile.getAbsolutePath())[0];

					Image pseudo = new Image(display, area.width * printScaleFactor, area.height * printScaleFactor);
					GC pgc = new GC(pseudo);
					pgc.setAdvanced(true);
					if (!pgc.getAdvanced()) {
						logger.warn("Warning: no advanced graphics!");
					}

					Image reloadedImage = new Image(display, imageData);

					Transform t = new Transform(display);
					t.translate(area.width * printScaleFactor, 0);
					t.rotate(90);
					t.scale(scaling, scaling);
					pgc.setTransform(t);
					pgc.drawImage(reloadedImage, 0, 0);
					t.dispose();
					pgc.dispose();
					reloadedImage.dispose();
					imageData = pseudo.getImageData();
				} else {
					ExportImage.exportImage(viewerApp.getCurrentViewer(), imageFile, area.width, area.height,
							oversampling);
					ImageLoader loader = new ImageLoader();
					imageData = loader.load(imageFile.getAbsolutePath())[0];
				}

				Rectangle trim = printer.computeTrim(0, 0, 0, 0);
				if (printer.startJob("Graph print")) {
					GC gc = new GC(printer);
					if (printer.startPage()) {
						Image reloadedImage;
						reloadedImage = new Image(printer, imageData);
						int legendGap = 0;
						int numActiveEntries = 0;

						if (legendTable != null) {
							for (int i = 0; i < legendTable.getLegendSize(); i++)
								if (legendTable.getLegendEntry(i).isVisible())
									numActiveEntries++;

							legendGap = 10 + Math.max(1, (numActiveEntries / 4)) * 32;
						}

						float legendRepositioning = ((-500 * scaling) + (500));
						if (imageAspect > printAspect) {
							gc.drawImage(reloadedImage, 0, 0, imageData.width, imageData.height, -trim.x + legendGap,
									-trim.y, area.width - (trim.x + trim.width), area.height - (trim.y + trim.height));
							if (legendTable != null) {
								int currentEntry = 0;
								for (int i = 0; i < legendTable.getLegendSize(); i++) {
									Plot1DAppearance app = legendTable.getLegendEntry(i);
									if (app.isVisible()) {
										int xpos = -10 + legendGap - (currentEntry / 4) * 25
												+ (int) legendRepositioning;
										int ypos = -trim.y + 60 + (currentEntry % 4) * 200;
										app.drawApp(xpos, ypos, gc, display, (imageAspect > printAspect));
										currentEntry++;
									}
								}
							}
						} else {
							gc.drawImage(reloadedImage, 0, 0, imageData.width, imageData.height, -trim.x, -trim.y,
									area.width - (trim.x + trim.width), area.height - (trim.y + trim.height)
											- legendGap);
							if (legendTable != null) {
								int currentEntry = 0;
								for (int i = 0; i < legendTable.getLegendSize(); i++) {
									Plot1DAppearance app = legendTable.getLegendEntry(i);
									if (app.isVisible()) {
										int xpos = trim.x + 60 + (currentEntry % 4) * 200 + (int) legendRepositioning;
										int ypos = area.height - legendGap + (currentEntry / 4) * 25;
										app.drawApp(xpos, ypos, gc, display, (imageAspect > printAspect));
										currentEntry++;
									}
								}
							}
						}
						reloadedImage.dispose();
						printer.endPage();
					}
					printer.endJob();
					gc.dispose();
				}
				printer.dispose();

				// now remove temporary file
				imageFile.delete();
			}
		}
	}
	
	public static synchronized Image createImage(AbstractViewerApp viewerApp, 
									            Display           display,
									            Plot1DGraphTable  legendTable, 
									            PrinterData       printerData, 
									            int resolutionMultiplier) {

		File imageFile = null;
		Image image = null;
		try {
			// create a temporary image file
			imageFile = File.createTempFile("test", ".png");

			ImageData imageData;
			Printer printer = new Printer(printerData);
		
			final Point dpi = printer.getDPI();
			final int oversampling = dpi.x < 100 ? 1 : 3;
			if (oversampling == 1) {
				logger.info("Printer dpi was lower than 100 ({}) so oversampling not used", dpi.x);
			}

			int printScaleFactor = 1;

			// check if we run on gtk don't trust gtk when it comes down to
			// the printer resolution so increase it by a major factor

			if (SWT.getPlatform().toLowerCase().equals("gtk"))
				printScaleFactor = resolutionMultiplier;

			final Dimension size = viewerApp.getCurrentViewer().getViewingComponentSize();
			ExportImage.exportImage(viewerApp.getCurrentViewer(), imageFile, 
					                size.width*printScaleFactor, 
					                size.height*printScaleFactor,
					                oversampling);

			ImageLoader loader = new ImageLoader();
			imageData = loader.load(imageFile.getAbsolutePath())[0];
			Image reloadedImage = new Image(display, imageData);

			Image bigImage = new Image(display, reloadedImage.getBounds().width+100,
				reloadedImage.getBounds().height+100);
			GC gc = new GC(bigImage);

			@SuppressWarnings("unused")
			int legendGap = 0;
			int numActiveEntries = 0;
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);

			if (legendTable != null) {
				for (int i = 0; i < legendTable.getLegendSize(); i++)
					if (legendTable.getLegendEntry(i).isVisible())
						numActiveEntries++;

				legendGap = 10 + Math.max(1, (numActiveEntries / 4)) * 32;
			}

			gc.drawImage(reloadedImage, 0, 0, reloadedImage.getBounds().width, reloadedImage.getBounds().height,
				-trim.x, -trim.y, reloadedImage.getBounds().width, reloadedImage.getBounds().height);
			if (legendTable != null) {
				int currentEntry = 0;
				for (int i = 0; i < legendTable.getLegendSize(); i++) {
					Plot1DAppearance app = legendTable.getLegendEntry(i);
					if (app.isVisible()) {
						int xpos = 60 - (currentEntry / 4);
						int ypos = reloadedImage.getBounds().height + 20 + (currentEntry % 4);
						app.drawApp(xpos, ypos, gc, display, false, printScaleFactor);
						currentEntry++;
					}
				}
			}

			reloadedImage.dispose();
			image = gc.getGCData().image;
			gc.dispose();
			imageFile.delete();
		
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

}
