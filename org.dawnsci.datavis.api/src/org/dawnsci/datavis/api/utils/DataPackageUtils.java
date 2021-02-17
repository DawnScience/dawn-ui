package org.dawnsci.datavis.api.utils;

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
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class DataPackageUtils {

	
	public static List<IXYData> getXYData(List<IDataFilePackage> list, boolean selectedOnly) {
		
		
		Stream<IDataFilePackage> stream = list.stream();
		
		if (selectedOnly) {
			stream = stream.filter(IDataFilePackage::isSelected);
		}
		
		return stream.flatMap(f -> Arrays.stream(f.getDataPackages()))
				.filter(IDataPackage::isSelected)
				.filter(d -> (d.getLazyDataset().getShape().length == 1))
				.map(DataPackageUtils::getData)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	
	public static List<IXYData> getXYData(List<? extends IDataPackage> list) {
		
		Stream<? extends IDataPackage> stream = list.stream();
		
		return stream.map(DataPackageUtils::getData)
					 .filter(Objects::nonNull)
                     .collect(Collectors.toList());
	}
	
	private static IXYData getData(IDataPackage pack) {
		
		ILazyDataset l = pack.getLazyDataset();
		SliceND s = pack.getSlice();
		
		if (s == null && l.getRank() != 1) return null;
		
		if (s == null) {
			s = new SliceND(l.getShape());
		}
		
		try {
			
			IDataset y = l.getSlice(s);
			
			IDataset[] unpackXY = unpackXY(y);
			
			Dataset labelValue = DatasetUtils.convertToDataset(pack.getLabelValue());
			return labelValue == null ? new XYDataImpl(unpackXY[0], unpackXY[1], Double.NaN, pack.getFilePath(), pack.getName(), null, pack.getSlice()) :
				new XYDataImpl(unpackXY[0], unpackXY[1], labelValue.getDouble(), pack.getFilePath(), pack.getName(), labelValue.getName(), pack.getSlice());
		} catch (DatasetException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private static IDataset[] unpackXY(IDataset y) {
		y = y.squeeze();
		IDataset x = null;
		AxesMetadata md = y.getFirstMetadata(AxesMetadata.class);
		if (md != null && md.getAxes() != null && md.getAxes()[0] != null) {
			try {
				x = md.getAxes()[0].getSlice();
			} catch (DatasetException e) {
				
			}
		}
		
		if (x == null) {
			x = DatasetFactory.createRange(y.getSize());
		}
		
		return new IDataset[]{x,y};
	}
	
}
