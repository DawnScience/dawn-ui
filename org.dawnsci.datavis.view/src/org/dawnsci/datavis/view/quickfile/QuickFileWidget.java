package org.dawnsci.datavis.view.quickfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class QuickFileWidget extends Composite {
	
	private CLabel directoryPath;
	private Text fileNameText;
	private ContentProposalAdapter adapter;
	private Set<IQuickFileWidgetListener> listeners;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	
	public QuickFileWidget(Composite parent) {
		super(parent, SWT.NONE);
		listeners = new HashSet<>();
		this.setLayout(new FormLayout());
		
		directoryPath = new CLabel(this, SWT.NONE);
		FormData pathform = new FormData();
		pathform.top = new FormAttachment(0,0);
		pathform.left = new FormAttachment(0,0);
		pathform.right = new FormAttachment(65,0);
		pathform.bottom = new FormAttachment(100,0);
		directoryPath.setLayoutData(pathform);
		directoryPath.setText("");
		fileNameText = new Text(this, SWT.BORDER);
		FormData nameForm = new FormData();
		nameForm.top = new FormAttachment(0,0);
		nameForm.left = new FormAttachment(65,0);
		nameForm.right = new FormAttachment(100,0);
		nameForm.bottom = new FormAttachment(100,0);
		fileNameText.setLayoutData(nameForm);
		
		adapter = new ContentProposalAdapter(
				fileNameText, new TextContentAdapter(), null,
				null, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		
		fileNameText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				//do nothing
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				updateContext();
			}
		});
		
		fileNameText.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && !adapter.isProposalPopupOpen()) {
					
					String path = directoryPath.getText();
					String name = fileNameText.getText();
					
					File f = new File(path, name);
					
					if (f.isDirectory()) {
						try {
							setDirectoryPath(f.getCanonicalPath());
							updateContext();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return;
					}
					
					firesListeners(path, name);
				}
			}
		});
	}
	
	public void setTextToolTipText(String tip) {
		fileNameText.setToolTipText(tip);
	}
	
	private void updateContext() {
		adapter.setContentProposalProvider(null);
		
		Runnable r = () -> {
			String start = directoryPath.getText();
			File f = new File(directoryPath.getText());
			File[] listFiles = f.listFiles();
			final String[] names = new String[listFiles.length];
			for (int i = 0; i < listFiles.length; i++) {
				names[i] = listFiles[i].getName();
			}
			
			Arrays.sort(names);
			
			SimpleContentProposalProvider p = new SimpleContentProposalProvider(names) {
				
				@Override
				public IContentProposal[] getProposals(String contents, int position) {

						ArrayList<ContentProposal> list = new ArrayList<ContentProposal>();
						for (int i = 0; i < names.length; i++) {
							if (names[i].toLowerCase().contains(contents.toLowerCase())) {
								list.add(new ContentProposal(names[i]));
							}
						}
						
						return list.toArray(new IContentProposal[list
								.size()]);
					}

			};
			
			if (start.equals(directoryPath.getText())){
				adapter.setContentProposalProvider(p);
			}
			
		};
		
		executor.submit(r);
	}
	
	public void setDirectoryPath(String path) {
		if (path.equals(directoryPath.getText())) {
			return;
		}
		directoryPath.setText(path);
		directoryPath.getParent().layout();
		fileNameText.setText("");
	}
	
	private void firesListeners(String directory, String name){
		listeners.stream().forEach(l -> l.fileSelected(directory,name));
	}
	
	public void addListener(IQuickFileWidgetListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IQuickFileWidgetListener l) {
		listeners.remove(l);
	}
}
