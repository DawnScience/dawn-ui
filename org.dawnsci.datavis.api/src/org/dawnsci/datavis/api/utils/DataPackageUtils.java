package org.dawnsci.datavis.api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.datavis.api.IDataPackage;
import org.dawnsci.datavis.api.IXYData;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.SliceNDIterator;
import org.eclipse.january.metadata.AxesMetadata;

public class DataPackageUtils {

	
	public static List<IXYData> getXYData(List<IDataFilePackage> list, boolean anyRank, boolean selectedOnly) {
		
		
		Stream<IDataFilePackage> stream = list.stream();
		
		if (selectedOnly) {
			stream = stream.filter(IDataFilePackage::isSelected);
		}
		
		return stream.flatMap(f -> Arrays.stream(f.getDataPackages()))
				.filter(IDataPackage::isSelected)
				.filter(d -> anyRank || (d.getLazyDataset().getRank() == 1))
				.flatMap(f -> getData(f).stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public static List<IXYData> getXYData(List<? extends IDataPackage> list) {
		return getXYData(list, true);
	}

	public static List<IXYData> getXYData(List<? extends IDataPackage> list, boolean useDerived) {
		
		Stream<? extends IDataPackage> stream = list.stream();
		
		return stream.flatMap(f -> getData(f, useDerived).stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private static List<IXYData> getData(IDataPackage pack) {
		return getData(pack, true);
	}

	private static List<IXYData> getData(IDataPackage pack, boolean useDerived) {
		if (useDerived) {
			List<IXYData> xy = pack.getDerivedData(IXYData.class);
			if (xy != null) {
				return xy;
			}
		}

		ILazyDataset l = pack.getLazyDataset();
		SliceND s = pack.getSlice();
		
		if (s == null && l.getRank() != 1) return null;
		List<IXYData> list = new ArrayList<>();

		try {
			Dataset y = DatasetUtils.convertToDataset(l.getSlice(s));
			int r = y.getRank();
			if (r > 1) {
				int[] omit = pack.getOmitDimensions();
				if (omit == null) { // maybe ok for single item dataset
					throw new IllegalArgumentException("Data package should have at least one dimension to slice");
				}
				if (omit.length > 1) {
					throw new IllegalArgumentException("Data package has too many dimension to slice");
				}
				SliceNDIterator it = new SliceNDIterator(new SliceND(y.getShapeRef()), omit);
				s = it.getCurrentSlice();
				Dataset x = unpackX(y, omit[0]); // should this squeeze?

				if (ShapeUtils.squeezeShape(x.getShapeRef(), false).length == 1) {
					x.squeeze();
				}
				if (x.getRank() == 1) {
					if (!Arrays.equals(x.getShapeRef(), ShapeUtils.squeezeShape(s.getShape(), false))) {
						throw new IllegalArgumentException("x when 1D must match shape of slice");
					}
					while (it.hasNext()) {
						list.add(createXYData(x, y.getSliceView(s).squeeze(), pack));
					}
				} else if (x.getRank() == r) {
					while (it.hasNext()) {
						list.add(createXYData(x.getSliceView(s).squeeze(), y.getSliceView(s).squeeze(), pack));
					}
				} else {
					throw new IllegalArgumentException("Should not happen that x has intermediate rank");
				}
			} else {
				Dataset x = unpackX(y, 0);
				list.add(createXYData(x, y, pack));
			}
			
			return list;
		} catch (DatasetException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static Dataset unpackX(Dataset y, int d) {
		AxesMetadata md = y.getFirstMetadata(AxesMetadata.class);
		if (md != null) {
			ILazyDataset[] mda = md.getAxes();
			if (mda != null && d < mda.length) {
				try {
					return DatasetUtils.sliceAndConvertLazyDataset(mda[d]);
				} catch (DatasetException e) {
				}
			}
		}

		return DatasetFactory.createRange(y.getShapeRef()[d]);
	}

	/**
	 * Create xy data
	 * @param x
	 * @param y
	 * @param dp data package
	 * @return xy data
	 */
	public static IXYData createXYData(IDataset x, IDataset y, IDataPackage dp) {
		Dataset lv = DatasetUtils.convertToDataset(dp.getLabelValue());
		return new XYDataImpl(x, y, lv == null ? "NaN" : lv.getString(), dp.getFilePath(), dp.getName(), lv == null ? null : lv.getName(), null);
	}
}
