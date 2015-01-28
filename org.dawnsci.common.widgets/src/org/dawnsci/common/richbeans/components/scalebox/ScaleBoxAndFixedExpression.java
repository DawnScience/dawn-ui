/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.common.richbeans.components.scalebox;

import java.text.NumberFormat;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.common.richbeans.beans.BeansFactory;
import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for entering a value, also automatically re-evaluates the expression.
 * 
 * You cannot call setExpressionManager on this class.
 * 
 * @author Matthew Gerring
 *
 */
public class ScaleBoxAndFixedExpression extends ScaleBox{

	public interface ExpressionProvider {
        double getValue(double val);

		IFieldWidget[] getPrecedents();
	}

	private static Logger logger = LoggerFactory.getLogger(ScaleBoxAndFixedExpression.class);
	
	private Label       fixedExpressionLabel;
	private JEP         jepParser;
	private SymbolTable symbolTable;
	private Object      dataProvider;
	private String      expression;
	private String      thisVariable;
	private String      labelUnit;
	private String      prefix;

	private ExpressionProvider provider;

	private NumberFormat labelNumberFormat;

    
	/**
	 */
	public ScaleBoxAndFixedExpression(final Composite parent, 
                                 final int       style) {
		super(parent, style);

		createFixedExpressionLabel();
	}
	/**
	 */
	public ScaleBoxAndFixedExpression(final Composite parent, 
                                 final int       style,
                                 final ExpressionProvider ep) {
		super(parent, style);


		createFixedExpressionLabel();
	    this.provider     = ep;
	    
	    final IFieldWidget[] fw = provider.getPrecedents();
	    if (fw!=null) for (int i = 0; i < fw.length; i++) {
			fw[i].addValueListener(new ValueAdapter(fw[i].toString() + " Listener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateLabel();
				}
			});
	    }
	}
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param expression
	 * @param dataProvider
	 * @throws ParseException 
	 */
	public ScaleBoxAndFixedExpression(final Composite parent, 
			                     final int       style, 
			                     final String    thisVariable, 
			                     final String    expression, 
			                     final Object    dataProvider) throws ParseException {
		
		super(parent, style);

		createFixedExpressionLabel();
	    
	    this.thisVariable = thisVariable;
	    this.expression   = expression;
	    this.symbolTable  = getSymbolTable(expression);
	    this.dataProvider = dataProvider;
	}
	
	private void createFixedExpressionLabel() {

		if (!label.isVisible()) {
			final GridData data = (GridData) label.getLayoutData();
			data.exclude = true;
		}

		// We allow user expressions
    	createExpressionLabel(-1);
    	
    	final GridLayout gridLayout = new GridLayout(4,false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		if (fixedExpressionLabel!=null) return;
	    this.fixedExpressionLabel = new Label(this, SWT.RIGHT);
	    
	    final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
	    gd.widthHint = 80;
		this.fixedExpressionLabel.setLayoutData(gd);
		
		layout();
	}
    
 	public void setFixedExpressionValue(final double numericalValue) {
		final String labelText = getLabel(numericalValue);
		fixedExpressionLabel.setText(labelText);
		if (labelText==null||"".equals(labelText.trim())||"-".equals(labelText.trim())) {
		    GridUtils.setVisible(this.fixedExpressionLabel, false);
		} else {
			GridUtils.setVisible(this.fixedExpressionLabel, true);
		}
		getShell().layout();
    }
    
	public void setExpressionLabelTooltip(final String tip) {
    	fixedExpressionLabel.setToolTipText(tip);
    }
	/**
	 * Recalculates expression
	 */
	@Override
	protected void checkValue(final String txt) {
        
		super.checkValue(txt);
		if (txt==null||"".equals(txt.trim())||"-".equals(txt.trim())) {
			GridUtils.setVisible(this.fixedExpressionLabel, false);
			getShell().layout();
			return;
		}
		final double num = getNumericValue();
        updateLabel(num);
	}
	
	private void updateLabel() {
		updateLabel(getNumericValue());
	}
	
	private void updateLabel(final double value) {
		
		try {
			if (provider==null&&jepParser==null)  return;
			double labelVal = Double.NaN;
			if (provider!=null) {
				labelVal = provider.getValue(value);
			} else {
				final SymbolTable vars   = symbolTable;
				@SuppressWarnings("unchecked")
				final Set<Entry<?, ?>> entries = vars.entrySet();
			    for (Entry<?, ?> entry : entries) {
			    	if (entry.getValue()==null && !entry.getKey().equals(thisVariable)) {
			    		final double val = (Double) BeansFactory.getBeanValue(dataProvider, entry.getKey().toString());
			    		jepParser.addVariable(entry.getKey().toString(), val);
			    	}
				}
			    
			    jepParser.addVariable(thisVariable, value);
			    
			    jepParser.parseExpression(expression);
			    labelVal = jepParser.getValue();
			}
		    
            setFixedExpressionValue(labelVal);
		    layout();
		    
		} catch (Exception ne) {
			logger.error("Cannot compute value "+expression, ne);
			fixedExpressionLabel.setText("");
		}
 		
	}

	private String getLabel(double labelVal) {
		final StringBuilder buf = new StringBuilder();
		if (getPrefix()!=null) {
			buf.append(getPrefix());
			buf.append(" ");
		}
		
		String text = labelNumberFormat!=null
		            ? labelNumberFormat.format(labelVal)
		            : numberFormat.format(labelVal);
		
		buf.append(text);
		
		if (getLabelUnit()!=null) {
			buf.append(" ");
			buf.append(getLabelUnit());
		}
		return buf.toString();
	}
	
	public void setLabelDecimalPlaces(int decimalPlaces) {
		if (labelNumberFormat==null) {
			labelNumberFormat = NumberFormat.getInstance();
		}
		labelNumberFormat.setMaximumFractionDigits(decimalPlaces);
	}
	
	private SymbolTable getSymbolTable(final String expression) throws ParseException {
		jepParser = new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(true);
		jepParser.setImplicitMul(true);
		
	    jepParser.parse(expression);
	    return jepParser.getSymbolTable();
	}

	/**
	 * @return Returns the dataProvider.
	 */
	public Object getDataProvider() {
		return dataProvider;
	}

	/**
	 * @param dataProvider The dataProvider to set.
	 */
	public void setDataProvider(Object dataProvider) {
		this.dataProvider = dataProvider;
	}

	/**
	 * @return Returns the expression.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression The expression to set.
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * @return Returns the thisVariable.
	 */
	public String getThisVariable() {
		return thisVariable;
	}

	/**
	 * @param thisVariable The thisVariable to set.
	 */
	public void setThisVariable(String thisVariable) {
		this.thisVariable = thisVariable;
	}

	/**
	 * @return Returns the labelUnit.
	 */
	public String getLabelUnit() {
		return labelUnit;
	}

	/**
	 * @param labelUnit The labelUnit to set.
	 */
	public void setLabelUnit(String labelUnit) {
		this.labelUnit = labelUnit;
	}
	/**
	 * @return Returns the prefix.
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @param prefix The prefix to set.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}

	