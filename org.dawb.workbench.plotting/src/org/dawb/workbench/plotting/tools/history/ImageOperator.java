package org.dawb.workbench.plotting.tools.history;

import java.lang.reflect.Array;

import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public enum ImageOperator {

	//NO_OPERATOR("None"),
	ADD("     +     "),
	SUBTRACT("     -     "),
	MULTIPLY("     x     "),
	DIVIDE("     ÷     ");
	//MEDIAN("Median");
	
	private String name;

	ImageOperator(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		final ImageOperator[] ops = ImageOperator.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getOperators() {
		final ImageOperator[] ops = ImageOperator.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public static ImageOperator getOperator(int index) {
		final ImageOperator[] ops = ImageOperator.values();
		return ops[index];
	}
	
	private static OperatorKernel operatorKernel;
	/**
	 * Does an in-place operation on 'a'. Hence ensure that start has been copied
	 * if you don't want to change it.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static synchronized AbstractDataset process(AbstractDataset a, AbstractDataset b, ImageOperator operation) {

		final double[]  da = ((DoubleDataset)DatasetUtils.cast(a, AbstractDataset.FLOAT)).getData();
		final double[]  db = ((DoubleDataset)DatasetUtils.cast(b, AbstractDataset.FLOAT)).getData();
		final int opIndex     = operation.getIndex();
        
        if (operatorKernel==null) {
        	operatorKernel = new OperatorKernel();
        	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        		public void run() {
        			operatorKernel.dispose();
        		}
        	}));
        }

        operatorKernel.setDa(da);
        operatorKernel.setDb(db);
        operatorKernel.setOperation(opIndex);
        
		Range range = Range.create(a.getSize()); 
		operatorKernel.execute(range);

        return new DoubleDataset(operatorKernel.getResult(), a.getShape());
	}
	
	static class OperatorKernel extends Kernel {
		private double[]  da, db;
		private double[]  result;
		private int operation;
		public double[] getResult() {
			return result;
		}
		public void setDa(double[] da) {
			this.da = da;
		}
		public void setDb(double[] db) {
			this.db = db;
		}
		public void setOperation(int operation) {
			this.operation = operation;
		}
		
		public Kernel execute(Range range) {
			result = new double[da.length];
			return super.execute(range);
		}
		
		@Override 
		public void run(){

			int i       = getGlobalId();
			double aVal = da[i];
			double bVal = db[i];
			
			if (operation==0) {
				result[i] = aVal + bVal;

			} else if (operation==1) {
				result[i] = aVal - bVal;

			} else if (operation==2) {
				result[i] = aVal * bVal;

			} else if (operation==3) {
				result[i] = aVal / bVal;
				return;
			}
		}
	}

	private static MultiplyKernel multiplyKernel;

	public static synchronized AbstractDataset multiply(AbstractDataset data, final double b) {
		
		final double[]  da = ((DoubleDataset)DatasetUtils.cast(data, AbstractDataset.FLOAT)).getData();
			
		
        if (multiplyKernel==null) {
        	multiplyKernel = new MultiplyKernel();
        	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        		public void run() {
        			multiplyKernel.dispose();
        		}
        	}));
        }

        multiplyKernel.setDa(da);
        multiplyKernel.setB(b);
        
		Range range = Range.create(data.getSize()); 
		multiplyKernel.execute(range);

        return new DoubleDataset(multiplyKernel.getResult(), data.getShape());
	}
	
	static class MultiplyKernel extends Kernel {
		private double[]  da;
		private double    b;
		private double[]  result;
		public double[] getResult() {
			return result;
		}
		public void setDa(double[] da) {
			this.da = da;
		}
		public void setB(double b) {
			this.b = b;
		}
		
		public Kernel execute(Range range) {
			result = new double[da.length];
			return super.execute(range);
		}
		
		@Override 
		public void run(){
			int i     = getGlobalId();
			result[i] = da[i]*b;
		}
	}}
