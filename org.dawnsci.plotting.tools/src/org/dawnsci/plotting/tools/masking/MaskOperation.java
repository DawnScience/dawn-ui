package org.dawnsci.plotting.tools.masking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
	private Set<MaskPoint>  vertexList;
	private byte[]          compressedVertices; // Used to reduce memory, works really well and fast.
	private BooleanDataset  maskDataset;

	public MaskOperation(BooleanDataset maskDataset, int maxExpectedSize) {
		super("Mask operation");
		this.maskDataset = maskDataset;
		this.vertexList  = new HashSet<MaskPoint>(maxExpectedSize);
		addContext(MASK_CONTEXT);
	}
	

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		try {
			Collection<MaskPoint> vl = getVertexList();
			for (MaskPoint mp : vl)  maskDataset.set(mp.is(), mp.getY(), mp.getX());
			compressVertexList();
			return Status.OK_STATUS;
		} catch (Throwable ne) {
			throw new ExecutionException(ne.getMessage(), ne);
		}
	}

	private void compressVertexList() throws IOException {
		if (vertexList!=null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
			ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
			objectOut.writeObject(vertexList);
			objectOut.close();
			compressedVertices = baos.toByteArray();
			
			vertexList.clear();
			vertexList = null;
		}
	}
	
	private Collection<MaskPoint> getVertexList() throws IOException, ClassNotFoundException {
		if (vertexList!=null) return vertexList;
		if (compressedVertices!=null) {
			ByteArrayInputStream bais  = new ByteArrayInputStream(compressedVertices);
			GZIPInputStream gzipIn     = new GZIPInputStream(bais);
			ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
			Collection<MaskPoint> vl = (Collection<MaskPoint>) objectIn.readObject();
			objectIn.close();
			return vl;
		}
		return null;
	}


	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		try {
			Collection<MaskPoint> vl = getVertexList();
			for (MaskPoint mp : vl)  maskDataset.set(!mp.is(), mp.getY(), mp.getX());
			return Status.OK_STATUS;
		} catch (Throwable ne) {
			throw new ExecutionException(ne.getMessage(), ne);
		}
	}
	

	public final static class MaskPoint implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6580683756160098489L;
		
		private final int x, y;
		private final boolean val;
		public MaskPoint(boolean val, int x, int y){
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

	/**
	 * Fast, uses hashing algorithm
	 * @param mv
	 * @param y
	 * @param x
	 * @return
	 */
	public boolean isVertex(boolean mv, int y, int x) {
		return vertexList.contains(new MaskPoint(mv, x, y));
	}

	public int getSize() {
		return vertexList.size();
	}
	
	/**
	 * Cannot use operation again after been disposed.
	 */
	public void dispose() {
		super.dispose();
		if (vertexList!=null) vertexList.clear();
		vertexList  = null;
		maskDataset = null;
	}
}
