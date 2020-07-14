package org.dawnsci.plotting.tools.imagecuts;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dawnsci.plotting.tools.imagecuts.CutData.CutType;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slice an image (either blocking or asynchronous) using two axis ROIs, return
 * the sum of the cuts as an event
 */
public class PerpendicularCutsRunner {

	private static final Logger logger = LoggerFactory.getLogger(PerpendicularCutsRunner.class);

	private Executor executor;
	private Set<PerpendicularCutsListener> listeners = new LinkedHashSet<>();

	public PerpendicularCutsRunner() {
		executor = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1),
				new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public void runAsync(IDataset data, IDataset xaxis, IDataset yaxis, RectangularROI xROI, RectangularROI yROI) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				runBlocking(data, xaxis, yaxis, xROI, yROI);
			}
		};

		executor.execute(r);
	}

	public void runBlocking(IDataset data, IDataset xAxis, IDataset yAxis, RectangularROI xROI, RectangularROI yROI) {
		double pointX = xROI.getPointX() > 0 ? xROI.getPointX() : 0;
		double lenX = xROI.getLengths()[0];

		Slice xSlice = new Slice((int) pointX, (int) (pointX + lenX));

		double pointY = yROI.getPointY() > 0 ? yROI.getPointY() : 0;
		double lenY = yROI.getLengths()[1];

		Slice ySlice = new Slice((int) pointY, (int) (pointY + lenY));

		IDataset slicex = DatasetUtils.convertToDataset(data.getSliceView((Slice) null, xSlice)).sum(1).squeeze();
		IDataset slicey = DatasetUtils.convertToDataset(data.getSliceView(ySlice, (Slice) null)).sum(0).squeeze();

		// opposite axis is correct for cut
		passMetadata(yAxis, slicex);
		passMetadata(xAxis, slicey);

		double sum = ((Number) DatasetUtils.convertToDataset(data.getSliceView(ySlice, xSlice)).sum()).doubleValue();

		listeners.stream().forEach(l -> l.cutProcessed(slicex, slicey, sum, buildCuts(xROI, yROI, yAxis, xAxis)));

	}

	private void passMetadata(IDataset original, IDataset cut) {
		if (original == null)
			return;

		AxesMetadata xm;
		try {
			xm = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			xm.setAxis(0, original.getSliceView());
			cut.setMetadata(xm);
		} catch (Exception e) {
			logger.debug("Could not set axis on cut");
		}

	}

	private CutData[] buildCuts(RectangularROI roix, RectangularROI roiy, IDataset yAxis, IDataset xAxis) {

		CutData x = buildCut(roix, xAxis, CutType.X);
		CutData y = buildCut(roiy, yAxis, CutType.Y);

		return new CutData[] { x, y };

	}

	private CutData buildCut(RectangularROI roi, IDataset axis, CutType type) {

		boolean isX = type == CutType.X;

		int i = isX ? 0 : 1;

		double point = isX ? roi.getPointX() : roi.getPointY();

		double len = roi.getLengths()[i];

		int pos = (int) (point + len / 2);
		double val = pos;

		double step = 1;
		String name = isX ? "X-Axis" : "Y-Axis";

		if (axis != null) {
			step = Math.abs(axis.getDouble(axis.getSize() - 1) - axis.getDouble(0)) / (axis.getSize() - 1);
			val = axis.getDouble(pos);
			if (axis.getName() != null && !axis.getName().isEmpty()) {
				name = axis.getName();
			}
		}

		return new CutData(name, pos, len * step, val, type);

	}

	public void addListener(PerpendicularCutsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PerpendicularCutsListener listener) {
		listeners.remove(listener);
	}

}
