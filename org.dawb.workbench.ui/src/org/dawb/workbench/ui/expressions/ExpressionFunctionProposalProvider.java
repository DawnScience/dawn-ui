package org.dawb.workbench.ui.expressions;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class ExpressionFunctionProposalProvider implements
		IContentProposalProvider {
	
	private Map<String,List<ContentProposal>> proposalMap;
	
	/**
	 * Construct a ExpressionFunctionProposalProvider whose content proposals are
	 * always the specified array of Objects.
	 * 
	 * @param functions
	 *            the Map of Objects that proposals are extracted from
	 */
	public ExpressionFunctionProposalProvider(Map<String, Object> functions) {
		super();
		
		setProposals(functions);
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		
		String sub = contents.substring(0, position-1);
		
		String foundKey = "";
		
		for (String key : proposalMap.keySet()) {
			if (sub.endsWith(key)) {
				if (key.length() > foundKey.length()) {
					foundKey = key;
				}
			}
		}
		
		if (foundKey.isEmpty()) {
			return new ContentProposal[]{};
		}
		
		return (IContentProposal[]) proposalMap.get(foundKey).toArray(new IContentProposal[proposalMap.get(foundKey).size()]);

	}
	
	public void setProposals(Map<String, Object> functions) {
		
		List<String> combinedNames = new ArrayList<String>();
		proposalMap = new HashMap<String, List<ContentProposal>>();
		
		for (String key : functions.keySet()) {
			Object funcClass = functions.get(key);
			Method[] methods = ((Class<?>)funcClass).getMethods();
			
			List<ContentProposal> methodNames = new ArrayList<ContentProposal>();
			
			for (Method method : methods){
				combinedNames.add(key +":" +method.getName());
				Type[] types = method.getGenericParameterTypes();
				StringBuilder sb = new StringBuilder();
				sb.append(method.getName() + "(");
				
				for(int i = 0; i< types.length ; ++i) {
					if (types[i] instanceof Class<?>)  {
						sb.append(getSimplifiedName(((Class<?>)types[i]).getSimpleName()));
					}  else if (types[i] instanceof Object){
						sb.append(getSimplifiedName(types[i].toString()));
					}else sb.append(types[i].toString());
					
					
					if (i != types.length -1) sb.append(", ");
				}
				
				sb.append(")");
				
				methodNames.add(new ContentProposal(sb.toString()));
			}
			
			proposalMap.put(key, methodNames);
		}
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
