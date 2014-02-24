package org.dawnsci.common.widgets.gda.function.detail;

import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class JexlExpressionFunctionDetailPane implements IFunctionDetailPane {

	private final class IDocumentListenerImplementation implements
			IDocumentListener {
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		public void documentChanged(DocumentEvent event) {
			String string = sourceViewer.getDocument().get();
			try {
				if (func != null) {
					func.setExpression(string);
					displayModel.refreshElement();
				}
			} catch (JexlExpressionFunctionException e) {
				// ignore error here, we handle it in the display
			}
		}
	}

	private SourceViewer sourceViewer;
	private JexlExpressionFunction func;
	private IDisplayModelSelection displayModel;
	private IDocumentListenerImplementation listener;

	@Override
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		composite.setLayout(new FillLayout());
		sourceViewer = new SourceViewer(composite, null, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		sourceViewer.setInput(this);

		IDocument document = new Document();
		sourceViewer.configure(new SourceViewerConfiguration());
		sourceViewer.setEditable(true);
		sourceViewer.setDocument(document);
		listener = new IDocumentListenerImplementation();
		document.addDocumentListener(listener);

		sourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());

		sourceViewer.getDocument().set("");

		return composite;
	}

	@Override
	public void display(IDisplayModelSelection displayModel) {
		this.displayModel = displayModel;
		Object element = displayModel.getElement();
		if (element instanceof JexlExpressionFunction) {
			func = (JexlExpressionFunction) element;

			IDocument document = sourceViewer.getDocument();
			document.removeDocumentListener(listener);
			document.set(func.getExpression());
			document.addDocumentListener(listener);
		}
	}

	@Override
	public void dispose() {
		IDocument document = sourceViewer.getDocument();
		document.removeDocumentListener(listener);
	}
}
