package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class BaseHandler extends AbstractHandler {
	protected FunctionTreeViewer viewer;
	private ISelectionChangedListener selectionListener;
	private FocusListener focusListener;
	private boolean requiresFocus;

	/**
	 *
	 * @param viewer
	 *            The viewer which the command operates on
	 * @param requiresFocus
	 *            Whether the viewer needs to have focus to be handled.
	 */
	public BaseHandler(IFunctionViewer viewer, boolean requiresFocus) {
		super();
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;
		this.requiresFocus = requiresFocus;

		selectionListener = new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablement();
			}
		};
		this.viewer.getTreeViewer().addSelectionChangedListener(
				selectionListener);

		if (requiresFocus) {
			focusListener = new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					if (isEnabled()) {
						setBaseEnabled(false);
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
					updateEnablement();
				}
			};
			this.viewer.getTreeViewer().getTree()
					.addFocusListener(focusListener);
		}
	}

	@Override
	public void dispose() {
		if (viewer != null && viewer.getTreeViewer() != null) {
			if (selectionListener != null) {
				viewer.getTreeViewer().removeSelectionChangedListener(
						selectionListener);
			}
			if (viewer.getTreeViewer().getTree() != null
					&& !viewer.getTreeViewer().getTree().isDisposed()) {
				if (focusListener != null) {
					viewer.getTreeViewer().getTree()
							.removeFocusListener(focusListener);
				}
			}
		}
		selectionListener = null;
		focusListener = null;
		viewer = null;
	}

	private void updateEnablement() {
		boolean rc = isHandled();
		if (rc != isEnabled()) {
			setBaseEnabled(rc);
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		updateEnablement();
	}

	/**
	 * Return true if the current selection in the viewer is valid.
	 *
	 * @param model
	 * @return
	 */
	protected abstract boolean isSelectionValid(FunctionModelElement model);

	public final boolean isHandled() {
		if (requiresFocus) {
			Control focusControl = Display.getCurrent().getFocusControl();
			if (focusControl == null
					|| !focusControl.equals(viewer.getTreeControl())) {
				return false;
			}
		}
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		return isSelectionValid(model);
	}
}
