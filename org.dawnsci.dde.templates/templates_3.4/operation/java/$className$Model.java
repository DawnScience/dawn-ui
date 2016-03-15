package $packageName$;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;

import uk.ac.diamond.scisoft.analysis.processing.operations.EmptyModel;

public class $className$Model extends AbstractOperationModel {

	@OperationModelField(hint="Enter value for operation", label = "Value")
	private double myVar = 1;
	
	public double getMyVar() {
		return myVar;
	}

	public void setMyVar(double myVar) {
		firePropertyChange("myVar", this.myVar, this.myVar = myVar);
	}

}