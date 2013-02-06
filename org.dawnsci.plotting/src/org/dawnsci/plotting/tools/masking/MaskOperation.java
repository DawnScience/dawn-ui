package org.dawnsci.plotting.tools.masking;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;

public class MaskOperation extends AbstractOperation {
	
	public static final IUndoContext MASK_CONTEXT = new IUndoContext() {
		@Override
		public String getLabel() {
			return "Masking operation";
		}

		@Override
		public boolean matches(IUndoContext context) {
			return context == MASK_CONTEXT;
		}
	};
	
	/**
	 * Can be large!
	 */
	private List<MaskPoint> vertexList;
	private BooleanDataset  maskDataset;

	public MaskOperation(BooleanDataset maskDataset, int maxSize) {
		super("Mask operation");
		this.maskDataset = maskDataset;
		this.vertexList  = new ArrayList<MaskPoint>(maxSize);
		addContext(MASK_CONTEXT);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (MaskPoint mp : vertexList)  maskDataset.set(mp.is(), mp.getY(), mp.getX());
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (MaskPoint mp : vertexList)  maskDataset.set(!mp.is(), mp.getY(), mp.getX());
		return Status.OK_STATUS;
	}

	private final static class MaskPoint {
		final int x, y;
		final boolean val;
		MaskPoint(boolean val, int x, int y){
			this.x = x;
			this.y = y;
			this.val = val;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public boolean is() {
			return val;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (val ? 1231 : 1237);
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MaskPoint other = (MaskPoint) obj;
			if (val != other.val)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		
	}

	public void addVertex(boolean mv, int y, int x) {
		vertexList.add(new MaskPoint(mv, x, y));
	}

	public int getSize() {
		return vertexList.size();
	}
	
	/**
	 * Cannot use operation again after been disposed.
	 */
	public void dispose() {
		super.dispose();
		vertexList.clear();
		vertexList  = null;
		maskDataset = null;
	}
}
