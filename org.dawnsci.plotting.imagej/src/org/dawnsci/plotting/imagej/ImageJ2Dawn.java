package org.dawnsci.plotting.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IRectangularROI;

public class ImageJ2Dawn {

	/**
	 * Process an image or 3D stack of images with an ImageJ plugin
	 * @param data - must be set
	 * @param roi  - may be null
	 * @param className   - must be the class name of a filter on the ImageJ class path
	 * @param commandName - command for filter to process
	 * @param args        - arguments to the filter if any.
	 * @return Filtered data
	 * @throws Exception  - Thrown for a range of invlaid conditions, also where the filter is not applicable with an appropriate message.
	 */
	public static IDataset processFilter(IDataset data, IRectangularROI rroi, String className, String commandName, String args) throws Exception {
		
		final int[] shape = data.getShape();
		if (shape.length!=2 && shape.length!=3) throw new Exception("Cannot process data of rank "+data.getRank());
		
		ClassLoader loader  = IJ.getClassLoader();
		final Object object = loader.loadClass(className).newInstance();
		if (!(object instanceof PlugInFilter)) throw new Exception("Class "+className+" is not a PlugInFilter");

        final PlugInFilter filter = (PlugInFilter)object;
		final ImageStack   stack  = createImageStackFromData(data);
		      ImagePlus    imp    = new ImagePlus(data.getName(), stack);
		
		int flags = filter.setup(args, imp);
		
		if ((flags&PlugInFilter.DONE)!=0) throw new Exception("Cannot setup filter "+className);
		if (!checkImagePlus(imp, flags, commandName)) throw new Exception("Filter "+className+" is not applicable for "+data);

		if ((flags&PlugInFilter.NO_IMAGE_REQUIRED)!=0) throw new Exception("Filter "+className+" is a no data filter and is not applicable for "+data);
		
		Roi roi = rroi!=null 
				? new Roi(rroi.getPoint()[0], rroi.getPoint()[1], rroi.getLength(0), rroi.getLength(1))
		        : null;

		
	    
		return null;
	}

	/**
	 * Attempts to create an imageJ ImageStack from an IDataset
	 * @param orig
	 * @return
	 */
	public static ImageStack createImageStackFromData(IDataset orig) {
		
		final int[] shape = orig.getShape();
		ImageStack stack = null;
		if  (orig.getRank()==2) {
			stack = new ImageStack(shape[1], shape[0]);
			for (int y = 0; y < shape[0]; y++) {
				for (int x = 0; x < shape[1]; y++) {
					stack.setVoxel(x, y, 0, orig.getDouble(y,x));
				}
			}
		} else {
			stack = new ImageStack(shape[1], shape[0], shape[2]);
			for (int y = 0; y < shape[0]; y++) {
				for (int x = 0; x < shape[1]; y++) {
					for (int z = 0; z < shape[2]; y++) {
						stack.setVoxel(x, y, z, orig.getDouble(y,x,z));
					}
				}
			}
		}
		return stack;
	}
	
	
	// Copied from ImageJ PlugInFilterRunner
	
	/**
	 * 
	 * @param imp
	 * @param flags
	 * @param cmd
	 * @return
	 */
	private static boolean checkImagePlus(ImagePlus imp, int flags, String cmd) {
		boolean imageRequired = (flags&PlugInFilter.NO_IMAGE_REQUIRED)==0;
		if (imageRequired && imp==null)
			{IJ.noImage(); return false;}
		if (imageRequired) {
			if (imp.getProcessor()==null)
				{wrongType(flags, cmd); return false;}
			int type = imp.getType();
			switch (type) {
				case ImagePlus.GRAY8:
					if ((flags&PlugInFilter.DOES_8G)==0)
					{wrongType(flags, cmd); return false;}
					break;
				case ImagePlus.COLOR_256:
					if ((flags&PlugInFilter.DOES_8C)==0)
					{wrongType(flags, cmd); return false;}
					break;
				case ImagePlus.GRAY16:
					if ((flags&PlugInFilter.DOES_16)==0)
					{wrongType(flags, cmd); return false;}
					break;
				case ImagePlus.GRAY32:
					if ((flags&PlugInFilter.DOES_32)==0)
					{wrongType(flags, cmd); return false;}
					break;
				case ImagePlus.COLOR_RGB:
					if ((flags&PlugInFilter.DOES_RGB)==0)
					{wrongType(flags, cmd); return false;}
					break;
			}
			if ((flags&PlugInFilter.ROI_REQUIRED)!=0 && imp.getRoi()==null)
			{IJ.error(cmd, "This command requires a selection"); return false;}
			if ((flags&PlugInFilter.STACK_REQUIRED)!=0 && imp.getStackSize()==1)
			{IJ.error(cmd, "This command requires a stack"); return false;}
		} // if imageRequired
		return true;
	}
	
	static void wrongType(int flags, String cmd) {
		String s = "\""+cmd+"\" requires an image of type:\n \n";
		if ((flags&PlugInFilter.DOES_8G)!=0) s +=  "	8-bit grayscale\n";
		if ((flags&PlugInFilter.DOES_8C)!=0) s +=  "	8-bit color\n";
		if ((flags&PlugInFilter.DOES_16)!=0) s +=  "	16-bit grayscale\n";
		if ((flags&PlugInFilter.DOES_32)!=0) s +=  "	32-bit (float) grayscale\n";
		if ((flags&PlugInFilter.DOES_RGB)!=0) s += "	RGB color\n";
		IJ.error(s);
	}
}
