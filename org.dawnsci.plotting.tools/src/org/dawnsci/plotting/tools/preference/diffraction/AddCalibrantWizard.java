package org.dawnsci.plotting.tools.preference.diffraction;

import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.plotting.tools.preference.diffraction.AddCalibrantWizard.NewCalibrantModel.NewMode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantGenerator;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantGenerator.Cubic;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;

public class AddCalibrantWizard extends Wizard implements IWorkbenchWizard{

	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.addCalibrantWizard";
		
	private CalibrationStandards calibrationStandards;
	
	public AddCalibrantWizard() {
		super();
		addPage(new AddCalibrantPage("Add Calibrant Spacing"));
		setWindowTitle("Add Calibrant");
	}

	@Override
	public boolean performFinish() {
		
		final AddCalibrantPage page = (AddCalibrantPage)getPages()[0];
		NewCalibrantModel model = page.getModel();
		
		CalibrantSpacing cs;
		
		if (model.mode == NewMode.COPY) {
			cs = calibrationStandards.getCalibrant().clone();
		} else if (model.mode == NewMode.CUBIC) {
				cs = CalibrantGenerator.createCubicStandard(model.name, model.cubicA, model.numberOfReflections, model.cubicType);
		} else if (model.mode == NewMode.HEX) {
			cs = CalibrantGenerator.createRhombohedralStandard(model.name, model.hexA, model.hexC, model.numberOfReflections);
		}else {
			cs = createEmpty();
		}
		
		cs.setName(model.name);
		
		calibrationStandards.addCalibrant(cs);
		calibrationStandards.setSelectedCalibrant(cs.getName(), true);
		
	    return true;
	}

	private CalibrantSpacing createEmpty() {
		CalibrantSpacing cs = new CalibrantSpacing();
		cs.addHKL(new HKL());
		return cs;
	}

	private final class AddCalibrantPage extends WizardPage {

		private Text    nameTxt;
		private NewCalibrantModel model;
		
		protected AddCalibrantPage(String pageName) {
			super(pageName);
		}

		@Override
		public void createControl(Composite parent) {
			
			model = new NewCalibrantModel();
			
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 2;
			layout.verticalSpacing = 9;

			Label label = new Label(container, SWT.NONE);
			label.setText("Calibrant Name");
			this.nameTxt = new Text(container, SWT.BORDER);
			nameTxt.setTextLimit(64);
			nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			nameTxt.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					nameChanged();
				}
			});
			
			Button copyButton = new Button(container, SWT.RADIO);
			copyButton.setSelection(true);
			copyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			copyButton.setText("Copy '"+calibrationStandards.getSelectedCalibrant()+"' ");
			
			Button emptyButton = new Button(container, SWT.RADIO);
			emptyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			emptyButton.setText("Empty List");
			

			Button cubicButton = new Button(container, SWT.RADIO);
			cubicButton.setSelection(false);
			cubicButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			cubicButton.setText("New Cubic");
			
			final Group cubicComposite = new Group(container, SWT.None);
			cubicComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			cubicComposite.setLayout(new GridLayout(2, false));
			
			Button hexButton = new Button(container, SWT.RADIO);
			hexButton.setSelection(false);
			hexButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			hexButton.setText("New Rhombohedral - R3\u0305c [hexagonal] (e.g. Al2O3, Cr2O3) a");
			
			final Group hexComposite = new Group(container, SWT.None);
			hexComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			hexComposite.setLayout(new GridLayout(3, false));
			
			cubicButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.mode = NewMode.CUBIC;
					cubicComposite.setEnabled(true);
					for (Control child : cubicComposite.getChildren())
						  child.setEnabled(true);
					
					hexComposite.setEnabled(false);
					for (Control child : hexComposite.getChildren())
						  child.setEnabled(false);
					
					
				}			
			});
			copyButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.mode = NewMode.COPY;
					cubicComposite.setEnabled(false);
					for (Control child : cubicComposite.getChildren())
						  child.setEnabled(false);
					
					hexComposite.setEnabled(false);
					for (Control child : hexComposite.getChildren())
						  child.setEnabled(false);
				}			
			});
			emptyButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.mode = NewMode.EMPTY;
					cubicComposite.setEnabled(false);
					for (Control child : cubicComposite.getChildren())
						  child.setEnabled(false);
					
					hexComposite.setEnabled(false);
					for (Control child : hexComposite.getChildren())
						  child.setEnabled(false);
				}			
			});
			
			Label cubicLabel = new Label(cubicComposite, SWT.None);
			cubicLabel.setText("Lattice Parameter a (nm):");
			final Text cubicText = new Text(cubicComposite, SWT.BORDER);
			FloatDecorator fd = new FloatDecorator(cubicText);
			cubicText.setText(String.valueOf(model.cubicA));
			cubicText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					
					int caret = cubicText.getCaretPosition();
					
					try {
						model.cubicA = Double.parseDouble(cubicText.getText());
					} catch (Exception e2) {
						cubicText.setText(String.valueOf(model.cubicA));
						cubicText.setSelection(caret-1);
						
					}
					
				}
			});
			
			Button simple = new Button(cubicComposite, SWT.RADIO);
			simple.setText("Pm3\u0305m (e.g. LaB6)");
			simple.setSelection(true);
			simple.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.cubicType = Cubic.SIMPLE;
				}			
			});
			Button bcc = new Button(cubicComposite, SWT.RADIO);
			bcc.setText("Im3\u0305m (e.g. Fe, V, W)");
			bcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.cubicType = Cubic.BCC;
				}			
			});
			Button fcc = new Button(cubicComposite, SWT.RADIO);
			fcc.setText("Fm3\u0305m (e.g. CeO2, Ni)");
			fcc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.cubicType = Cubic.FCC;
				}			
			});
			Button diamond = new Button(cubicComposite, SWT.RADIO);
			diamond.setText("Fd3\u0305m (e.g. Si, Ge)");
			diamond.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.cubicType = Cubic.DIAMOND;
				}			
			});
			
			Label numberLabel = new Label(cubicComposite, SWT.None);
			numberLabel.setText("Number of reflections:");
			final Spinner spinner = new Spinner(cubicComposite, SWT.NONE);
			spinner.setMinimum(1);
			spinner.setMaximum(50);
			spinner.setSelection(model.numberOfReflections);
			spinner.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					model.numberOfReflections = spinner.getSelection();
				}
			});
			
			
			cubicComposite.setEnabled(false);
			for (Control child : cubicComposite.getChildren())
				  child.setEnabled(false);
			
			hexButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.mode = NewMode.HEX;
					hexComposite.setEnabled(true);
					for (Control child : hexComposite.getChildren())
						  child.setEnabled(true);
					
					cubicComposite.setEnabled(false);
					for (Control child : cubicComposite.getChildren())
						  child.setEnabled(false);
				}			
			});
			
			Label hexLabel = new Label(hexComposite, SWT.None);
			hexLabel.setText("Lattice Parameters a and c (nm):");
			final Text hexaText = new Text(hexComposite, SWT.BORDER);
			fd = new FloatDecorator(hexaText);
			hexaText.setText(String.valueOf(model.hexA));
			hexaText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					
					int caret = cubicText.getCaretPosition();
					
					try {
						model.hexA = Double.parseDouble(hexaText.getText());
					} catch (Exception e2) {
						cubicText.setText(String.valueOf(model.hexA));
						cubicText.setSelection(caret-1);
						
					}
					
				}
			});
			
			final Text hexcText = new Text(hexComposite, SWT.BORDER);
			fd = new FloatDecorator(hexcText);
			hexcText.setText(String.valueOf(model.hexC));
			hexcText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					
					int caret = hexcText.getCaretPosition();
					
					try {
						model.hexA = Double.parseDouble(hexcText.getText());
					} catch (Exception e2) {
						hexcText.setText(String.valueOf(model.hexC));
						hexcText.setSelection(caret-1);
					}
				}
			});
			
			numberLabel = new Label(hexComposite, SWT.None);
			numberLabel.setText("Number of reflections:");
			final Spinner spinnerhex = new Spinner(hexComposite, SWT.NONE);
			spinnerhex.setMinimum(1);
			spinnerhex.setMaximum(50);
			spinnerhex.setSelection(model.numberOfReflections);
			spinnerhex.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent e) {
					model.numberOfReflections = spinnerhex.getSelection();
				}
			});
			
			hexComposite.setEnabled(false);
			for (Control child : hexComposite.getChildren())
				  child.setEnabled(false);
			
			nameChanged();
			setControl(container);

		}

		/**
		 * Ensures that both text fields are set.
		 */

		private void nameChanged() {

            this.model.name = nameTxt.getText().trim();
            
            if (this.model.name==null||"".equals(this.model.name)) {
            	updateStatus("Please set a name for the new calibrant.");
            	return;
            }
            if (calibrationStandards.getCalibrantList().contains(this.model.name)) {
            	updateStatus("The calibrant '"+this.model.name+"' already exists. Please choose a unique calibrant name.");
            	return;
            }

			updateStatus(null);
		}

		private void updateStatus(String message) {
			setErrorMessage(message);
			setPageComplete(message == null);
		}

		@SuppressWarnings("unused")
		public boolean isCopy() {
			return this.model.mode == NewMode.COPY;
		}

		public String getName() {
			return this.model.name;
		}
		
		public NewCalibrantModel getModel() {
			return model;
		}

	}

	public CalibrationStandards getCalibrationStandards() {
		return calibrationStandards;
	}

	public void setCalibrationStandards(CalibrationStandards calibrationStandards) {
		this.calibrationStandards = calibrationStandards;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	
	public static final class NewCalibrantModel {
		
		public enum NewMode {
			EMPTY, COPY, CUBIC, HEX;
		}
		
		public String name;
		public NewMode mode = NewMode.COPY;
		public CalibrantGenerator.Cubic cubicType = Cubic.SIMPLE;
		public double cubicA = 0.41569162;
		public double hexA = 0.4958979;
		public double hexC = 1.359592;
		public int numberOfReflections = 10;
		
	}

}
