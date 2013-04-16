package org.dawnsci.plotting.tools.expressions;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.dawnsci.jexl.utils.JexlUtils;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;


public class ExpressionConsole {
	final IOConsole console;
	final IOConsoleOutputStream stream;
	final JexlEngine engine;
    MapContext context;
    
	
	public ExpressionConsole() {
		console = new IOConsole("DawnScriptTool", null);
		stream = console.newOutputStream();
		engine = JexlUtils.getDawnJexlEngine();
		
		context = new MapContext();
		
		writeToConsole("Dawn>");
		console.getDocument().addDocumentListener(new IDocumentListener() {
			
			@Override
			public void documentChanged(DocumentEvent event) {
				if (event.getText().equals("\n")) {
					processText(console.getDocument().get());
				}
			}
			
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// TODO Auto-generated method stub
				
			}
		});

	}
	
	public Map<String,Object> getFunctions() {
		return engine.getFunctions();
	}
	
	public void setFuctions(Map<String,Object> functions) {
		engine.setFunctions(functions);
	}
	
	public void addToContext(String name, Object object) {
		this.context.set(name, object);
	}
	
	public IOConsole getConsole() {
		return console;
	}
	
	private void writeToConsole(String msg) {
        try {
            stream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
	
	private void processText(String text) {
        try {
        	int index = text.lastIndexOf("Dawn>");
        	String sub = text.substring(index+5);
        	
        	try {
        		Expression ex = engine.createExpression(sub);
        		
        		Object answer = ex.evaluate(context);
        		if (answer!=null) {
        			stream.write(answer.toString());
        		} else {
        			stream.write("No Output");
        		}
        	} catch (Exception e){
        		stream.write(e.getMessage());
        	}
        	
            
            stream.write("\n");
            writeToConsole("Dawn>");
            
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

	
}
