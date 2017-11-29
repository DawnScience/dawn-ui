package org.dawnsci.surfacescatter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class InputTileGenerator {

	private Label label;
	private Label label1;
	private Group tile;
	private Text text;
	private Combo comboDropDown;
	private Spinner spinner;
	private Combo comboDropDownTheta;
	private Button radio;
	private Button push;
	boolean stateOfText = false;

	public InputTileGenerator(String l, String t, Composite parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		text = new Text(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		text.setText(t);

	}

	public InputTileGenerator(String l, Composite parent, int p) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(p, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		push = new Button(tile, SWT.PUSH | SWT.FILL);
		push.setText(l);
		push.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		text = new Text(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public InputTileGenerator(String l, Composite parent, boolean s) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.NULL);
		label.setText(l);

		radio = new Button(tile, SWT.CHECK);
		radio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public InputTileGenerator(String l, String d, Composite parent, boolean in) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		radio = new Button(tile, SWT.PUSH);
		radio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radio.setText(d);

		text = new Text(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		text.setText(l);
		if (in) {
			text.setEnabled(stateOfText);
		}

	}

	public Button getRadio() {
		return radio;
	}

	public void setRadio(Button radio) {
		this.radio = radio;
	}

	public InputTileGenerator(String l, String t, Group parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		text = new Text(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		text.setText(t);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public InputTileGenerator(String l, Group parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.NULL);
		label.setText(l);

		comboDropDown = new Combo(tile, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public InputTileGenerator(String l, String t, Composite parent, int n) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(GridData.FILL_HORIZONTAL);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.NULL | SWT.BORDER | SWT.FILL);
		GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(labelData);
		label.setText(l);

		text = new Text(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(textData);
		text.setText(t);

	}

	public InputTileGenerator(String l, Group parent, int min, int max, int inc) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);

		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		spinner = new Spinner(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setIncrement(inc);
		spinner.setSelection(10);
		spinner.setPageIncrement(10);
		spinner.setData(new GridData(SWT.FILL, SWT.FILL, true, false));
		spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	public InputTileGenerator(String l, String k, String[] options, Group parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);
		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		Group left = new Group(tile, SWT.NONE);
		GridLayout leftLayout = new GridLayout(2, true);
		left.setLayout(leftLayout);
		GridData leftData = new GridData(SWT.FILL, SWT.NULL, true, false);
		left.setLayoutData(leftData);

		label = new Label(left, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		comboDropDownTheta = new Combo(left, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDownTheta.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboDropDownTheta.add("Theta");
		comboDropDownTheta.add("2 * Theta");

		Group right = new Group(tile, SWT.NONE);
		GridLayout rightLayout = new GridLayout(2, true);
		right.setLayout(rightLayout);
		GridData rightData = new GridData(SWT.FILL, SWT.NULL, true, false);
		right.setLayoutData(rightData);

		label1 = new Label(right, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label1.setText(k);

		comboDropDown = new Combo(right, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			for (String t : options) {
				comboDropDown.add(t);
			}
		} catch (Exception n) {

		}
	}

	public InputTileGenerator(String l, String k, String[] options, Composite parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);
		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		Group left = new Group(tile, SWT.NONE);
		GridLayout leftLayout = new GridLayout(2, true);
		left.setLayout(leftLayout);
		GridData leftData = new GridData(SWT.FILL, SWT.NULL, true, false);
		left.setLayoutData(leftData);

		label = new Label(left, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		comboDropDownTheta = new Combo(left, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDownTheta.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboDropDownTheta.add("Theta");
		comboDropDownTheta.add("2 * Theta");

		Group right = new Group(tile, SWT.NONE);
		GridLayout rightLayout = new GridLayout(2, true);
		right.setLayout(rightLayout);
		GridData rightData = new GridData(SWT.FILL, SWT.NULL, true, false);
		right.setLayoutData(rightData);

		label1 = new Label(right, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label1.setText(k);

		comboDropDown = new Combo(right, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			for (String t : options) {
				comboDropDown.add(t);
			}
		} catch (Exception n) {

		}
	}

	public InputTileGenerator(String l, String[] options, Group parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);
		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		comboDropDown = new Combo(tile, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			for (String t : options) {
				comboDropDown.add(t);
			}
		} catch (Exception n) {

		}
	}

	public InputTileGenerator(String l, String[] options, Composite parent) {

		tile = new Group(parent, SWT.NONE);
		GridLayout tileLayout = new GridLayout(2, true);
		tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);

		label = new Label(tile, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		label.setText(l);

		comboDropDown = new Combo(tile, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			for (String t : options) {
				comboDropDown.add(t);
			}
		} catch (Exception n) {

		}
	}

	public Combo getComboDropDownTheta() {
		return comboDropDownTheta;
	}

	public void setComboDropDownTheta(Combo comboDropDownTheta) {
		this.comboDropDownTheta = comboDropDownTheta;
	}

	public Group getGroup() {
		return tile;
	}

	public Spinner getSpinner() {
		return spinner;
	}

	public Combo getCombo() {
		return comboDropDown;
	}

	public Label getLabel() {
		return label;
	}

	public Button getPush() {
		return push;
	}

	public Text getText() {
		return text;
	}

	public void setEnabled(boolean in, boolean g) {
		for (Control cl : tile.getChildren()) {
			cl.setEnabled(in);
		}

		if (g) {
			text.setEnabled(stateOfText);
		}
	}

	public void setEnabled(boolean in) {

		for (Control cl : tile.getChildren()) {
			cl.setEnabled(in);
		}

	}

	public boolean isStateOfText() {
		return stateOfText;
	}

	public void setStateOfText(boolean stateOfText) {
		this.stateOfText = stateOfText;
	}

}
