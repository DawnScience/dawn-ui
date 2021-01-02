package org.dawnsci.january.ui.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionUtils {
	
	public static <U> List<U> getFromSelection(ISelection selection, Class<U> clazz){

		if (selection instanceof StructuredSelection) {

			return Arrays.stream(((StructuredSelection)selection).toArray())
					.filter(clazz::isInstance)
					.map(clazz::cast).collect(Collectors.toList());

		}

		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	public static <U> U getFirstFromSelection(ISelection selection, Class<U> clazz) {
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (clazz.isInstance(obj)) {
				return (U) obj;
			}
		}
		return null;
	}
}
