/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.passerelle.actors.dawn;

import java.util.ArrayList;
import java.util.List;

import org.dawb.passerelle.common.actors.AbstractDataMessageSource;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.message.IVariable;
import org.dawb.passerelle.common.message.IVariable.VARIABLE_TYPE;
import org.dawb.passerelle.common.message.MessageUtils;
import org.dawb.passerelle.common.message.Variable;

import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;

//////////////////////////////////////////////////////////////////////////
//// Scalar

/**
 * Sends a scalar message once on each output port.
 */
public class ScalarActor extends AbstractDataMessageSource {

	
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ScalarActor.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 6530803848861756427L;
	
	/** The value produced by this constant source.
	 *  By default, it contains an StringToken with an empty string.  
	 */
	public  Parameter        valueParam,nameParam,minParam, maxParam;
	@SuppressWarnings("unused")
	private String           strName, strValue, strMin, strMax;
	private List<Float> valueQueue;
	
	protected boolean firedStringValueAlready;
	
	/** Construct a constant source with the given container and name.
	 *  Create the <i>value</i> parameter, initialize its value to
	 *  the default value of an IntToken with value 1.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the entity cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public ScalarActor(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		
		super(container, name);
			       
	    nameParam = new StringParameter(this, "Name");
		nameParam.setExpression("x");
		nameParam.setDisplayName("Scalar Name");
		registerConfigurableParameter(nameParam);

		valueParam = new Parameter(this, "Value");
		valueParam.setExpression("1.0");
		valueParam.setDisplayName("Scalar Value");
		registerConfigurableParameter(valueParam);

		minParam = new Parameter(this, "Min");
		minParam.setExpression("0");
		minParam.setDisplayName("Min Value");
		registerConfigurableParameter(minParam);

		maxParam = new Parameter(this, "Max");
		maxParam.setExpression("1000");
		maxParam.setDisplayName("Max Value");
		registerConfigurableParameter(maxParam);

	}

	/**
	 *  @param attribute The attribute that changed.
	 *  @exception IllegalActionException   */
	public void attributeChanged(Attribute attribute) throws IllegalActionException {

		if (attribute == nameParam) {
			strName = nameParam.getExpression();
		}else if (attribute == valueParam) {
			strValue = valueParam.getExpression();
		}else if (attribute == minParam) {
			strMin = minParam.getExpression();
		}else if (attribute == maxParam) {
			strMax = maxParam.getExpression();
		}
		super.attributeChanged(attribute);
	}
	
	@Override
	protected void doInitialize() throws InitializationException {
	
		super.doInitialize();
		firedStringValueAlready = false;
		try {
			valueQueue = new ArrayList<Float>();//(strValue);
			valueQueue.add(Float.valueOf(strValue));
		} catch (Throwable ne) {
			valueQueue = null;			
		}
	}

	public boolean hasNoMoreMessages() {
	    if (valueQueue == null)   return true;
        return valueQueue.isEmpty() && super.hasNoMoreMessages();
    }
	
	@Override
	protected ManagedMessage getDataMessage() throws ProcessingException {
        
		if (valueQueue==null && firedStringValueAlready) return null;
		if (valueQueue!=null && valueQueue.isEmpty())    return null;
		
        Object value;
        if (valueQueue==null) {
        	firedStringValueAlready = true;
        	value = strValue;
        } else {
        	value = valueQueue.remove(0);
        }
        
		DataMessageComponent despatch = new DataMessageComponent();
		despatch.putScalar(strName, value.toString());

		try {
			return MessageUtils.getDataMessage(despatch);
		} catch (Exception e) {
			throw createDataMessageException("Cannot set scalar "+strName, e);
		}
	}	

    /**
	 * @see be.tuple.passerelle.engine.actor.Source#getInfo()
	 */
	protected String getExtendedInfo() {
		return valueParam.getExpression();
	}

	@Override
	public List<IVariable> getOutputVariables() {
		try {
			final String strName  = ((StringToken) nameParam.getToken()).stringValue();
			String strValue = "";
			Token tok = valueParam.getToken();
			if(tok instanceof IntToken){
				strValue = ((IntToken)valueParam.getToken()).toString();
			}else if(tok instanceof DoubleToken){
				strValue = ((DoubleToken)valueParam.getToken()).toString();
			}else if(tok instanceof FloatToken){
				strValue = ((FloatToken)valueParam.getToken()).toString();
			}
		    final List<IVariable> ret = new ArrayList<IVariable>(1);
		    ret.add(new Variable(strName, VARIABLE_TYPE.SCALAR, strValue, String.class));
		    return ret;
		} catch (Exception e) {
			logger.error("Cannot create outputs for "+getName(), e);
		}
		return null;
	}
	
	@Override
	public List<IVariable> getInputVariables() {
        return null;
	}

	@Override
	protected boolean mustWaitForTrigger() {
		return false;
	}

}
