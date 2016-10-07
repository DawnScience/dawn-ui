package org.dawnsci.common.widgets.filedataset;

import org.eclipse.january.dataset.ILazyDataset;

@FunctionalInterface
public interface IFileDatasetFilter {
	public static class FileDatasetFilterRank implements IFileDatasetFilter {
		private final int rank;
		public FileDatasetFilterRank(int rank) {
			this.rank = rank;
		}

		@Override
		public boolean accept(ILazyDataset dataset) {
			if (dataset.getRank() != rank)
				return false;
			return true;
		}
	}
	
	public static final IFileDatasetFilter FILTER_0D = new FileDatasetFilterRank(0);
	public static final IFileDatasetFilter FILTER_1D = new FileDatasetFilterRank(1);
	public static final IFileDatasetFilter FILTER_2D = new FileDatasetFilterRank(2);
	public static final IFileDatasetFilter FILTER_3D = new FileDatasetFilterRank(3);
	public static final IFileDatasetFilter FILTER_4D = new FileDatasetFilterRank(4);
	public static final IFileDatasetFilter FILTER_5D = new FileDatasetFilterRank(5);
	
	boolean accept(ILazyDataset dataset);
}
