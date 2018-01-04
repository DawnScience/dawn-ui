package org.dawnsci.mapping.ui.datamodel;

import java.util.List;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;

public interface LiveStreamMapObject extends PlottableMapObject {
	
	public interface IAxisMoveListener {
		public void axisMoved();
	}
	
	public void addAxisListener(IAxisMoveListener listener);
	
	public void removeAxisListener(IAxisMoveListener listener);
	
	public List<IDataset> getAxes();
	
	public IDynamicShape connect() throws Exception;
	
	public void disconnect() throws Exception;

}
