package org.dawnsci.common.widgets.filedataset;

import org.eclipse.january.dataset.ILazyDataset;

public class FileDatasetFilterFactory {
	private FileDatasetFilterFactory() {
		
	}
	
	private static class FileDatasetFilterRank implements IFileDatasetFilter {
		
		private final int rank;
		
		public FileDatasetFilterRank(int rank) {
			this.rank = rank;
		}

		@Override
		public boolean accept(ILazyDataset dataset) {
			return dataset.getRank() == rank;
		}
	}
	
	public static final IFileDatasetFilter FILTER_0D = createFileDatasetFilterForRank(0);
	public static final IFileDatasetFilter FILTER_1D = createFileDatasetFilterForRank(1);
	public static final IFileDatasetFilter FILTER_2D = createFileDatasetFilterForRank(2);
	public static final IFileDatasetFilter FILTER_3D = createFileDatasetFilterForRank(3);
	public static final IFileDatasetFilter FILTER_4D = createFileDatasetFilterForRank(4);
	public static final IFileDatasetFilter FILTER_5D = createFileDatasetFilterForRank(5);
	
	public static final IFileDatasetFilter FILTER_TRUE = dataset -> true;
	
	public static IFileDatasetFilter createFileDatasetFilterForRank(int rank) {
		if (rank < 0)
			throw new IllegalArgumentException("rank must be greater than or equal to zero");
		return new FileDatasetFilterRank(rank);
	}
}
