/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Node {
	public static final Node ADD_NEW_FUNCTION = new Node("Add new function");
	public static final Node SET_FUNCTION = new Node("Set function");
	public static final Node GAUSSIAN = new Node("Gaussian", "posn", "fwhm",
			"area");
	public static final Node FERMI = new Node("Fermi", "mu", "kT", "scale",
			"Constant");

	public static Node ADD(Node... children) {
		return new Node("Add", children);
	}

	public static Node SUBTACT(Node... children) {
		return new Node("Subtract", children);
	}

	public static Node JEXL(String expression, String... params) {
		return new Node(expression, params);
	}


	public String text;
	public Node[] children;

	public Node(String text) {
		this.text = text;
		this.children = new Node[0];
	}

	public Node(String text, Node... children) {
		this.text = text;
		this.children = children;
	}

	public Node(String text, String...simpleChidren) {
		this.text = text;
		this.children = new Node[simpleChidren.length];
		for (int i = 0; i < children.length; i++) {
			this.children[i] = new Node(simpleChidren[i]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Node))
			return false;
		Node rhs = (Node) obj;
		return new EqualsBuilder().append(this.text, rhs.text)
				.append(this.children, rhs.children).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.text)
				.append(this.children).hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.text);
		builder.append("[");
		for (Node node : children) {
			builder.append(node.toString());
		}
		builder.append("]");
		return builder.toString();
	}
}