/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.jexl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * TODO: Resolve this difference/copy. This class is a copy of
 * org.dawb.workbench.ui.expressions.ExpressionFunctionProposalProvider however
 * it has been reworked to instead of rely on testing the provider instead
 * creating proposals that have the needed information (lastMatchBounds,
 * lastPosition). TODO: small additional TODO/Q about whether non-static methods
 * should be excluded.
 */
public class ExpressionFunctionProposalProvider implements
		IContentProposalProvider {

	private Map<String, List<JexlProposal>> proposalMap;
	private int[] lastMatchBounds = new int[] { 0, 0 };
	private int lastPosition = 0;
	private Pattern functionp = Pattern.compile("\\w++:\\w*+");
	private Pattern namespacep = Pattern.compile("\\w++");

	/**
	 * Construct a ExpressionFunctionProposalProvider whose content proposals
	 * are always the specified array of Objects.
	 *
	 * @param functions
	 *            the Map of Objects that proposals are extracted from
	 */
	public ExpressionFunctionProposalProvider(Map<String, Object> functions) {
		super();

		setProposals(functions);
	}

	@Override
	public JexlProposal[] getProposals(String contents, int position) {

		lastPosition = position;
		// empty string
		if (contents.isEmpty()) {
			Set<String> keys = proposalMap.keySet();
			JexlProposal[] proposals = new JexlProposal[keys.size()];
			int i = 0;
			for (String key : keys) {
				proposals[i++] = new JexlProposal(key, this);
			}

			return proposals;
		}

		// last with colon

		String sub = contents.substring(0, position);
		Matcher m = functionp.matcher(sub);

		String last = null;
		int lastEnd = 0;
		while (m.find()) {
			last = m.group();
			lastMatchBounds[0] = m.start();
			lastMatchBounds[1] = m.end();
			lastEnd = m.end();
		}

		if (last == null || lastEnd != position) {

			m = namespacep.matcher(sub);

			while (m.find()) {
				last = m.group();
				lastEnd = m.end();
				lastMatchBounds[0] = m.start();
				lastMatchBounds[1] = m.end();
			}

			if (last != null && lastEnd == position) {

				Set<String> keys = proposalMap.keySet();
				List<JexlProposal> content = new ArrayList<>();

				for (String key : keys) {
					if (key.startsWith(last))
						content.add(new JexlProposal(key, this));
				}

				return content.toArray(new JexlProposal[content.size()]);
			} else {
				Set<String> keys = proposalMap.keySet();
				JexlProposal[] content = new JexlProposal[keys.size()];
				int i = 0;

				for (String key : keys)
					content[i++] = new JexlProposal(key, this);

				return content;
			}
		}

		String[] strArray = last.split(":");

		if (strArray.length == 2) {

			List<JexlProposal> contentList = proposalMap.get(strArray[0]);

			if (contentList == null)
				return new JexlProposal[0];

			List<JexlProposal> content = new ArrayList<>();

			for (JexlProposal prop : contentList) {
				if (prop.getContent().startsWith(strArray[1]))
					content.add(prop);
			}

			return content.toArray(new JexlProposal[content.size()]);
		} else if (strArray.length == 1) {
			List<JexlProposal> contentList = proposalMap.get(strArray[0]);
			if (contentList == null)
				return new JexlProposal[0];
			return contentList.toArray(new JexlProposal[contentList.size()]);
		}

		return new JexlProposal[0];

	}

	public void setProposals(Map<String, Object> functions) {

		List<String> combinedNames = new ArrayList<String>();
		proposalMap = new HashMap<>();

		if (functions == null)
			return;
		for (String key : functions.keySet()) {
			Object funcClass = functions.get(key);
			Method[] methods = ((Class<?>) funcClass).getMethods();

			List<JexlProposal> methodNames = new ArrayList<>();

			for (Method method : methods) {
				// TODO: Apply this change to source of copy?
				if (!Modifier.isStatic(method.getModifiers()))
					continue;
				combinedNames.add(key + ":" + method.getName());
				Type[] types = method.getGenericParameterTypes();
				StringBuilder sb = new StringBuilder();
				sb.append(method.getName() + "(");

				for (int i = 0; i < types.length; ++i) {
					if (types[i] instanceof Class<?>) {
						sb.append(getSimplifiedName(((Class<?>) types[i])
								.getSimpleName()));
					} else if (types[i] instanceof Object) {
						sb.append(getSimplifiedName(types[i].toString()));
					} else
						sb.append(types[i].toString());

					if (i != types.length - 1)
						sb.append(", ");
				}

				sb.append(")");

				methodNames.add(new JexlProposal(sb.toString(), this));
			}

			proposalMap.put(key, methodNames);
		}
	}

	public int[] getLastMatchBounds() {
		return lastMatchBounds;
	}

	public int getLastPosition() {
		return lastPosition;
	}

	private String getSimplifiedName(final String name) {

		if (name.toLowerCase().contains("collection")) {
			return "collection";
		}

		if (name.toLowerCase().contains("dataset")) {
			return "dataset";
		}

		if (name.toLowerCase().contains("object")) {
			return "data";
		}

		return name;
	}

}
