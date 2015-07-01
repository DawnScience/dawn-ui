package org.dawnsci.common.richbeans.examples.example7.ui;

import java.lang.reflect.Field;

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.BoundsProvider;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.ComboWrapper;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleItem;
import org.dawnsci.common.richbeans.examples.example7.data.ExampleItem.ItemChoice;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ExampleItemComposite extends Composite {

	private TextWrapper  itemName;
	private ScaleBox     x,y;
	private ComboWrapper choice;
	private ScaleBox     r,theta;

	private ScaleBox d0, d1,d2,d3,d4,d5,d6,d7,d8, d9;
	private ScaleBox d10, d11,d12,d13,d14,d15,d16,d17,d18, d19;
	private ScaleBox d20, d21,d22,d23,d24,d25,d26,d27,d28, d29;
	private ScaleBox d30, d31,d32,d33,d34,d35,d36,d37,d38, d39;
	private ScaleBox d40, d41,d42,d43,d44,d45,d46,d47,d48, d49;
	private ScaleBox d50, d51,d52,d53,d54,d55,d56,d57,d58, d59;
	private ScaleBox d60, d61,d62,d63,d64,d65,d66,d67,d68, d69;
	private ScaleBox d70, d71,d72,d73,d74,d75,d76,d77,d78, d79;
	private ScaleBox d80, d81,d82,d83,d84,d85,d86,d87,d88, d89;
	private ScaleBox d90, d91,d92,d93,d94,d95,d96,d97,d98, d99;

	public ExampleItemComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Name");
	
		this.itemName = new TextWrapper(this, SWT.BORDER);
		itemName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		label = new Label(this, SWT.NONE);
		label.setText("Coordinate System");
		
		choice = new ComboWrapper(this, SWT.READ_ONLY);
		choice.setItems(ExampleItem.ItemChoice.names());
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		choice.addValueListener(new ValueAdapter("Conditional Visibility Example") {	
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateVisibility();
			}
		});
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		x = new ScaleBox(this, SWT.NONE);
		x.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		x.setLabel("x     ");
		x.setUnit("m");
		x.setMinimum(0);
		x.setMaximum(1000);
				
		y = new ScaleBox(this, SWT.NONE);
		y.setLabel("y     ");
		y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		y.setUnit("m");
		y.setMinimum(0);
		y.setMaximum(new BoundsProvider() {
			
			@Override
			public double getBoundValue() {
				return x.getNumericValue()*10;
			}
			
			@Override
			public void addValueListener(ValueListener l) {
				x.addValueListener(l);
			}
		});
		
		r = new ScaleBox(this, SWT.NONE);
		r.setLabel("Radius      ");
		r.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		r.setUnit("m");
		r.setMinimum(0);
		r.setMaximum(1000);
		
		theta = new ScaleBox(this, SWT.NONE);
		theta.setLabel("Angle (θ)   ");
		theta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		theta.setUnit("rad");
		theta.setMinimum(0);
		theta.setMaximum(2*Math.PI);
		
		try {
			for(int i=0; i<100; ++i) {
				
				final String fname = "d"+i;
				ScaleBox scaleBox = new ScaleBox(this, SWT.NONE);
				scaleBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				scaleBox.setLabel(fname+"     ");
				scaleBox.setUnit("m");
				scaleBox.setMinimum(0);
				scaleBox.setMaximum(1000);
	
				Field f = getClass().getDeclaredField(fname);
				f.setAccessible(true);
				f.set(this, scaleBox);
			}
		} catch (Exception ne) {
			ne.printStackTrace();
		}

		GridUtils.setVisible(r,     false);
		GridUtils.setVisible(theta, false);

	}

	public IFieldWidget getItemName() {
		return itemName;
	}
	
	public IFieldWidget getX() {
		return x;
	}
	
	public IFieldWidget getY() {
		return y;
	}
	
	public IFieldWidget getR() {
		return r;
	}
	
	public IFieldWidget getTheta() {
		return theta;
	}
	
	public IFieldWidget getChoice() {
		return choice;
	}

	void updateVisibility() {
		GridUtils.setVisible(x,     choice.getValue() == ItemChoice.XY);
		GridUtils.setVisible(y,     choice.getValue() == ItemChoice.XY);
		GridUtils.setVisible(r,     choice.getValue() == ItemChoice.POLAR);
		GridUtils.setVisible(theta, choice.getValue() == ItemChoice.POLAR);
		layout();
	}

	public ScaleBox getD0() {
		return d0;
	}

	public void setD0(ScaleBox d0) {
		this.d0 = d0;
	}

	public ScaleBox getD1() {
		return d1;
	}

	public void setD1(ScaleBox d1) {
		this.d1 = d1;
	}

	public ScaleBox getD2() {
		return d2;
	}

	public void setD2(ScaleBox d2) {
		this.d2 = d2;
	}

	public ScaleBox getD3() {
		return d3;
	}

	public void setD3(ScaleBox d3) {
		this.d3 = d3;
	}

	public ScaleBox getD4() {
		return d4;
	}

	public void setD4(ScaleBox d4) {
		this.d4 = d4;
	}

	public ScaleBox getD5() {
		return d5;
	}

	public void setD5(ScaleBox d5) {
		this.d5 = d5;
	}

	public ScaleBox getD6() {
		return d6;
	}

	public void setD6(ScaleBox d6) {
		this.d6 = d6;
	}

	public ScaleBox getD7() {
		return d7;
	}

	public void setD7(ScaleBox d7) {
		this.d7 = d7;
	}

	public ScaleBox getD8() {
		return d8;
	}

	public void setD8(ScaleBox d8) {
		this.d8 = d8;
	}

	public ScaleBox getD9() {
		return d9;
	}

	public void setD9(ScaleBox d9) {
		this.d9 = d9;
	}

	public ScaleBox getD10() {
		return d10;
	}

	public void setD10(ScaleBox d10) {
		this.d10 = d10;
	}

	public ScaleBox getD11() {
		return d11;
	}

	public void setD11(ScaleBox d11) {
		this.d11 = d11;
	}

	public ScaleBox getD12() {
		return d12;
	}

	public void setD12(ScaleBox d12) {
		this.d12 = d12;
	}

	public ScaleBox getD13() {
		return d13;
	}

	public void setD13(ScaleBox d13) {
		this.d13 = d13;
	}

	public ScaleBox getD14() {
		return d14;
	}

	public void setD14(ScaleBox d14) {
		this.d14 = d14;
	}

	public ScaleBox getD15() {
		return d15;
	}

	public void setD15(ScaleBox d15) {
		this.d15 = d15;
	}

	public ScaleBox getD16() {
		return d16;
	}

	public void setD16(ScaleBox d16) {
		this.d16 = d16;
	}

	public ScaleBox getD17() {
		return d17;
	}

	public void setD17(ScaleBox d17) {
		this.d17 = d17;
	}

	public ScaleBox getD18() {
		return d18;
	}

	public void setD18(ScaleBox d18) {
		this.d18 = d18;
	}

	public ScaleBox getD19() {
		return d19;
	}

	public void setD19(ScaleBox d19) {
		this.d19 = d19;
	}

	public ScaleBox getD20() {
		return d20;
	}

	public void setD20(ScaleBox d20) {
		this.d20 = d20;
	}

	public ScaleBox getD21() {
		return d21;
	}

	public void setD21(ScaleBox d21) {
		this.d21 = d21;
	}

	public ScaleBox getD22() {
		return d22;
	}

	public void setD22(ScaleBox d22) {
		this.d22 = d22;
	}

	public ScaleBox getD23() {
		return d23;
	}

	public void setD23(ScaleBox d23) {
		this.d23 = d23;
	}

	public ScaleBox getD24() {
		return d24;
	}

	public void setD24(ScaleBox d24) {
		this.d24 = d24;
	}

	public ScaleBox getD25() {
		return d25;
	}

	public void setD25(ScaleBox d25) {
		this.d25 = d25;
	}

	public ScaleBox getD26() {
		return d26;
	}

	public void setD26(ScaleBox d26) {
		this.d26 = d26;
	}

	public ScaleBox getD27() {
		return d27;
	}

	public void setD27(ScaleBox d27) {
		this.d27 = d27;
	}

	public ScaleBox getD28() {
		return d28;
	}

	public void setD28(ScaleBox d28) {
		this.d28 = d28;
	}

	public ScaleBox getD29() {
		return d29;
	}

	public void setD29(ScaleBox d29) {
		this.d29 = d29;
	}

	public ScaleBox getD30() {
		return d30;
	}

	public void setD30(ScaleBox d30) {
		this.d30 = d30;
	}

	public ScaleBox getD31() {
		return d31;
	}

	public void setD31(ScaleBox d31) {
		this.d31 = d31;
	}

	public ScaleBox getD32() {
		return d32;
	}

	public void setD32(ScaleBox d32) {
		this.d32 = d32;
	}

	public ScaleBox getD33() {
		return d33;
	}

	public void setD33(ScaleBox d33) {
		this.d33 = d33;
	}

	public ScaleBox getD34() {
		return d34;
	}

	public void setD34(ScaleBox d34) {
		this.d34 = d34;
	}

	public ScaleBox getD35() {
		return d35;
	}

	public void setD35(ScaleBox d35) {
		this.d35 = d35;
	}

	public ScaleBox getD36() {
		return d36;
	}

	public void setD36(ScaleBox d36) {
		this.d36 = d36;
	}

	public ScaleBox getD37() {
		return d37;
	}

	public void setD37(ScaleBox d37) {
		this.d37 = d37;
	}

	public ScaleBox getD38() {
		return d38;
	}

	public void setD38(ScaleBox d38) {
		this.d38 = d38;
	}

	public ScaleBox getD39() {
		return d39;
	}

	public void setD39(ScaleBox d39) {
		this.d39 = d39;
	}

	public ScaleBox getD40() {
		return d40;
	}

	public void setD40(ScaleBox d40) {
		this.d40 = d40;
	}

	public ScaleBox getD41() {
		return d41;
	}

	public void setD41(ScaleBox d41) {
		this.d41 = d41;
	}

	public ScaleBox getD42() {
		return d42;
	}

	public void setD42(ScaleBox d42) {
		this.d42 = d42;
	}

	public ScaleBox getD43() {
		return d43;
	}

	public void setD43(ScaleBox d43) {
		this.d43 = d43;
	}

	public ScaleBox getD44() {
		return d44;
	}

	public void setD44(ScaleBox d44) {
		this.d44 = d44;
	}

	public ScaleBox getD45() {
		return d45;
	}

	public void setD45(ScaleBox d45) {
		this.d45 = d45;
	}

	public ScaleBox getD46() {
		return d46;
	}

	public void setD46(ScaleBox d46) {
		this.d46 = d46;
	}

	public ScaleBox getD47() {
		return d47;
	}

	public void setD47(ScaleBox d47) {
		this.d47 = d47;
	}

	public ScaleBox getD48() {
		return d48;
	}

	public void setD48(ScaleBox d48) {
		this.d48 = d48;
	}

	public ScaleBox getD49() {
		return d49;
	}

	public void setD49(ScaleBox d49) {
		this.d49 = d49;
	}

	public ScaleBox getD50() {
		return d50;
	}

	public void setD50(ScaleBox d50) {
		this.d50 = d50;
	}

	public ScaleBox getD51() {
		return d51;
	}

	public void setD51(ScaleBox d51) {
		this.d51 = d51;
	}

	public ScaleBox getD52() {
		return d52;
	}

	public void setD52(ScaleBox d52) {
		this.d52 = d52;
	}

	public ScaleBox getD53() {
		return d53;
	}

	public void setD53(ScaleBox d53) {
		this.d53 = d53;
	}

	public ScaleBox getD54() {
		return d54;
	}

	public void setD54(ScaleBox d54) {
		this.d54 = d54;
	}

	public ScaleBox getD55() {
		return d55;
	}

	public void setD55(ScaleBox d55) {
		this.d55 = d55;
	}

	public ScaleBox getD56() {
		return d56;
	}

	public void setD56(ScaleBox d56) {
		this.d56 = d56;
	}

	public ScaleBox getD57() {
		return d57;
	}

	public void setD57(ScaleBox d57) {
		this.d57 = d57;
	}

	public ScaleBox getD58() {
		return d58;
	}

	public void setD58(ScaleBox d58) {
		this.d58 = d58;
	}

	public ScaleBox getD59() {
		return d59;
	}

	public void setD59(ScaleBox d59) {
		this.d59 = d59;
	}

	public ScaleBox getD60() {
		return d60;
	}

	public void setD60(ScaleBox d60) {
		this.d60 = d60;
	}

	public ScaleBox getD61() {
		return d61;
	}

	public void setD61(ScaleBox d61) {
		this.d61 = d61;
	}

	public ScaleBox getD62() {
		return d62;
	}

	public void setD62(ScaleBox d62) {
		this.d62 = d62;
	}

	public ScaleBox getD63() {
		return d63;
	}

	public void setD63(ScaleBox d63) {
		this.d63 = d63;
	}

	public ScaleBox getD64() {
		return d64;
	}

	public void setD64(ScaleBox d64) {
		this.d64 = d64;
	}

	public ScaleBox getD65() {
		return d65;
	}

	public void setD65(ScaleBox d65) {
		this.d65 = d65;
	}

	public ScaleBox getD66() {
		return d66;
	}

	public void setD66(ScaleBox d66) {
		this.d66 = d66;
	}

	public ScaleBox getD67() {
		return d67;
	}

	public void setD67(ScaleBox d67) {
		this.d67 = d67;
	}

	public ScaleBox getD68() {
		return d68;
	}

	public void setD68(ScaleBox d68) {
		this.d68 = d68;
	}

	public ScaleBox getD69() {
		return d69;
	}

	public void setD69(ScaleBox d69) {
		this.d69 = d69;
	}

	public ScaleBox getD70() {
		return d70;
	}

	public void setD70(ScaleBox d70) {
		this.d70 = d70;
	}

	public ScaleBox getD71() {
		return d71;
	}

	public void setD71(ScaleBox d71) {
		this.d71 = d71;
	}

	public ScaleBox getD72() {
		return d72;
	}

	public void setD72(ScaleBox d72) {
		this.d72 = d72;
	}

	public ScaleBox getD73() {
		return d73;
	}

	public void setD73(ScaleBox d73) {
		this.d73 = d73;
	}

	public ScaleBox getD74() {
		return d74;
	}

	public void setD74(ScaleBox d74) {
		this.d74 = d74;
	}

	public ScaleBox getD75() {
		return d75;
	}

	public void setD75(ScaleBox d75) {
		this.d75 = d75;
	}

	public ScaleBox getD76() {
		return d76;
	}

	public void setD76(ScaleBox d76) {
		this.d76 = d76;
	}

	public ScaleBox getD77() {
		return d77;
	}

	public void setD77(ScaleBox d77) {
		this.d77 = d77;
	}

	public ScaleBox getD78() {
		return d78;
	}

	public void setD78(ScaleBox d78) {
		this.d78 = d78;
	}

	public ScaleBox getD79() {
		return d79;
	}

	public void setD79(ScaleBox d79) {
		this.d79 = d79;
	}

	public ScaleBox getD80() {
		return d80;
	}

	public void setD80(ScaleBox d80) {
		this.d80 = d80;
	}

	public ScaleBox getD81() {
		return d81;
	}

	public void setD81(ScaleBox d81) {
		this.d81 = d81;
	}

	public ScaleBox getD82() {
		return d82;
	}

	public void setD82(ScaleBox d82) {
		this.d82 = d82;
	}

	public ScaleBox getD83() {
		return d83;
	}

	public void setD83(ScaleBox d83) {
		this.d83 = d83;
	}

	public ScaleBox getD84() {
		return d84;
	}

	public void setD84(ScaleBox d84) {
		this.d84 = d84;
	}

	public ScaleBox getD85() {
		return d85;
	}

	public void setD85(ScaleBox d85) {
		this.d85 = d85;
	}

	public ScaleBox getD86() {
		return d86;
	}

	public void setD86(ScaleBox d86) {
		this.d86 = d86;
	}

	public ScaleBox getD87() {
		return d87;
	}

	public void setD87(ScaleBox d87) {
		this.d87 = d87;
	}

	public ScaleBox getD88() {
		return d88;
	}

	public void setD88(ScaleBox d88) {
		this.d88 = d88;
	}

	public ScaleBox getD89() {
		return d89;
	}

	public void setD89(ScaleBox d89) {
		this.d89 = d89;
	}

	public ScaleBox getD90() {
		return d90;
	}

	public void setD90(ScaleBox d90) {
		this.d90 = d90;
	}

	public ScaleBox getD91() {
		return d91;
	}

	public void setD91(ScaleBox d91) {
		this.d91 = d91;
	}

	public ScaleBox getD92() {
		return d92;
	}

	public void setD92(ScaleBox d92) {
		this.d92 = d92;
	}

	public ScaleBox getD93() {
		return d93;
	}

	public void setD93(ScaleBox d93) {
		this.d93 = d93;
	}

	public ScaleBox getD94() {
		return d94;
	}

	public void setD94(ScaleBox d94) {
		this.d94 = d94;
	}

	public ScaleBox getD95() {
		return d95;
	}

	public void setD95(ScaleBox d95) {
		this.d95 = d95;
	}

	public ScaleBox getD96() {
		return d96;
	}

	public void setD96(ScaleBox d96) {
		this.d96 = d96;
	}

	public ScaleBox getD97() {
		return d97;
	}

	public void setD97(ScaleBox d97) {
		this.d97 = d97;
	}

	public ScaleBox getD98() {
		return d98;
	}

	public void setD98(ScaleBox d98) {
		this.d98 = d98;
	}

	public ScaleBox getD99() {
		return d99;
	}

	public void setD99(ScaleBox d99) {
		this.d99 = d99;
	}

}
