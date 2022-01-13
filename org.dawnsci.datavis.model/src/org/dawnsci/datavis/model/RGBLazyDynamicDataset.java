package org.dawnsci.datavis.model;

import java.io.IOException;

import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LazyDynamicDataset;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyDynamicLoader;
import org.eclipse.january.io.ILazyLoader;

public class RGBLazyDynamicDataset extends LazyDynamicDataset {

	private static final long serialVersionUID = 1L;

	public RGBLazyDynamicDataset(ILazyLoader loader, String name, int elements, Class<? extends Dataset> clazz, int[] shape, int[] maxShape) {
		this(loader, name, elements, clazz, shape, maxShape, null);
	}

	public RGBLazyDynamicDataset(ILazyLoader loader, String name, int elements, Class<? extends Dataset> clazz, int[] shape, int[] maxShape, int[] chunks) {
		super(loader, name, elements, clazz, shape, maxShape, chunks);
	}

	public static RGBLazyDynamicDataset buildFromLazyDataset(LazyDynamicDataset lazy) {
		
		int[] shape = lazy.getShape();
		
		boolean interleaved = shape[shape.length-1] == 3;
		
		if (!interleaved && shape[shape.length - 3] != 3) {
			throw new IllegalArgumentException("Last (interleaved) or 3rd last (planar) dimension not equal to 3");
		}
		
		LazyDynamicDataset view = (LazyDynamicDataset)lazy.getSliceView();
		view.clearMetadata(null);
		
		ILazyLoader loader = new RGBLazyLoader(view, interleaved);
		return new RGBLazyDynamicDataset(loader, lazy.getName() + "RGB",
				lazy.getElementsPerItem(), lazy.getInterface(), getRGBShape(shape, interleaved),
				getRGBShape(lazy.getMaxShape(), interleaved));
		
	}


	private static int[] getRGBShape(int[] shape, boolean interleaved) {
		
		int[] out = new int[shape.length-1];
		
		if (interleaved) {
			System.arraycopy(shape, 0, out, 0, out.length);
		} else {
			
			int channelRGB = shape.length-3;
			
			for (int i = 0; i < shape.length; i++) {
				//do nothing when i == channelRGB
				if (i < channelRGB) {
					out[i] = shape[i];
				} else if (i > channelRGB) {
					out[i-1] = shape[i];
				}
			}
		}
		
		return out;
	}
	
	private static class RGBLazyLoader implements ILazyLoader, ILazyDynamicLoader {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private LazyDynamicDataset dataset;
		private boolean interleaved = true;
		
		public RGBLazyLoader(LazyDynamicDataset dataset, boolean interleaved) {
			this.dataset = dataset;
			this.interleaved = interleaved;
		}


		@Override
		public boolean isFileReadable() {
			return true;
		}

		@Override
		public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {

			int rgbDim = interleaved ? dataset.getRank() - 1 : dataset.getRank() - 3;

			SliceND full = new SliceND(dataset.getShape());

			Slice[] sa = slice.convertToSlice();
			for (int i = 0; i < sa.length; i++) {
				if (i < rgbDim) {
					full.setSlice(i, sa[i]);
				} else {
					full.setSlice(i+1, sa[i]);
				}
			}

			full.setSlice(rgbDim, 0, 3, 1);

			Dataset all;
			try {
				all = dataset.getSlice(full);
			} catch (DatasetException e) {
				throw new IOException(e);
			}

			CompoundDataset comp;
			if (interleaved) {
				comp = DatasetUtils.createCompoundDatasetFromLastAxis(all, true);
			} else {

				SliceND sR = new SliceND(all.getShape());
				SliceND sG = full.clone();
				SliceND sB = full.clone();
				sR.setSlice(rgbDim, 0, 1, 1);
				sG.setSlice(rgbDim, 1, 2, 1);
				sB.setSlice(rgbDim, 2, 3, 1);

				Dataset dR = all.getSliceView(sR);
				Dataset dG = all.getSliceView(sG);
				Dataset dB = all.getSliceView(sB);

				int[] ss = slice.getShape();

				dR.setShape(ss);
				dG.setShape(ss);
				dB.setShape(ss);

				comp = DatasetUtils.createCompoundDataset(dR,dG,dB);
			}

			return comp.getElementClass().equals(Byte.class) ? RGBByteDataset.createFromCompoundDataset(comp) :
				RGBDataset.createFromCompoundDataset(comp);
		}

		@Override
		public int[] refreshShape() {
			dataset.refreshShape();
			return getRGBShape(dataset.getShape(),interleaved);
		}
	}
}
