package org.dawnsci.plotting.javafx.trace.spline;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.SceneDisplayer;
import org.dawnsci.plotting.javafx.ServiceLoader;
import org.dawnsci.plotting.javafx.trace.JavafxTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ILine3DTrace;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

import javafx.scene.Node;

/**
 * !!!!!!!!!<br>
 * THIS CLASS HAS NOT BEEN TESTED AND WAS MADE IN ROUGHLY 1 HOUR<br>
 * DO NOT USE UNTIL IT IS TESTED<br>
 * !!!!!!!!!<br>
 * Remove this message once tested<br>
 * @author uij85458
 *
 */
public class LineTrace extends JavafxTrace implements ILine3DTrace
{
	private ILazyDataset lazyDataset;
	private LineGroup line;
	
	public LineTrace(IPlottingSystemViewer<?> plotter, SceneDisplayer newScene, String name) {
		super(plotter, name, newScene);
	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setData(final IDataset points) {
		final PaletteService pservice = (PaletteService) ServiceLoader.getPaletteService();

		lazyDataset = points;
		line = new LineGroup(points);

	}

	@Override
	public IDataset getData() {
		if (lazyDataset == null) {
			throw new IllegalArgumentException("lazyDataset was null");
		}
		try {
			return lazyDataset.getSlice();
		} catch (DatasetException e) {
			throw new IllegalArgumentException("Could not get data from lazy dataset", e);
		}
	}

	@Override
	public Node getNode() {
		return line;
	}

	@Override
	public boolean isLit() {
		return false;
	}

	@Override
	public void setOpacity(double opacity) {
		// it's a line, does nothing
	}
}
