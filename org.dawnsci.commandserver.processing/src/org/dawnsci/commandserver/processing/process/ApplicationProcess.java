package org.dawnsci.commandserver.processing.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.commandserver.processing.StreamGobbler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start a consumer based on a file which contains the correct properties
 * to start a consumer using Consumer.create(...)
 * 
 * This class will create a separate process which can be started 
 * 
 * @author Matthew Gerring
 *
 */
public class ApplicationProcess {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationProcess.class);
	
	private Map<String,String> progArgs;
	private Map<String,String> sysProps;
	private File               propertiesFile;
	private Process            process;
	private String             applicationName = "org.dawnsci.commandserver.consumer";
	private String             outFileName="consumer_out.log";
	private String             errFileName="consumer_err.log";
	private String             xms=null;
	private String             xmx=null;
	private boolean			   propagateSysProps = true;

	/**
	 * Same arguments as Consumer.start but you must set 'execLocation'
	 * so that the path to the DAWN executable is known.
	 * @param conf
	 * @throws Exception
	 */
	public ApplicationProcess(Map<String, String> progArgs, Map<String,String> sysProps) throws Exception {
		this.progArgs = progArgs;
		this.sysProps = sysProps;
	}
	
	public Process start() throws Exception {
		
		final String line = createExecutionLine();
		logger.debug("Execution line: "+line);
		final String[]  command;
		if (isLinuxOS()) {
			command = new String[]{"/bin/sh", "-c", line};
		} else {
			command = new String[]{"cmd", "/C", line};
		}
		 
		final Map<String,String> env = System.getenv();
		this.process     = Runtime.getRuntime().exec(command, getStringArray(env));
		
		final String workspace = getWorkspace();
		
		final File dir = new File(workspace);
		dir.mkdirs();

		PrintWriter   outs         = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir, outFileName))));
		outs.write("Execution line: "+line);
		StreamGobbler out          = new StreamGobbler(process.getInputStream(), outs, "output");
		out.setStreamLogsToLogging(true);
		out.start();
		
		PrintWriter   errs     = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir, errFileName))));
		StreamGobbler err      = new StreamGobbler(process.getErrorStream(), errs, "error");
		err.setStreamLogsToLogging(true);
		err.start();

		return process;
	}
	
	private String[] getStringArray(Map<String, String> env) {
		
		final String[] ret = new String[env.size()];
		int i = 0;
		for (String key : env.keySet()) {
			ret[i] = key+"="+env.get(key);
			++i;
		}
		return ret;
	}

	/*
	 *  For instance:
	 * "$DAWN_RELEASE_DIRECTORY/dawn -noExit -noSplash -application org.dawnsci.commandserver.consumer -data ~/command_server 
	 * 
	 */
	private String createExecutionLine() throws Exception {
		
		final StringBuilder buf = new StringBuilder();
		
		// Get the path to the workspace and the model path
		final String install   = getDawnInstallationPath();
		
		buf.append(install);
		buf.append(" -noSplash ");
		
		if (!progArgs.containsKey("application")) {
			buf.append(" -application ");
			buf.append(applicationName);
		}
		
		final String workspace = getWorkspace();
		if (!progArgs.containsKey("data")) {
			buf.append(" -data ");
			buf.append(workspace);
		}
		
		if (progArgs!=null) {
			for(String name : progArgs.keySet()) {
				buf.append(" -");
				buf.append(name);
				buf.append(" ");
				String value = progArgs.get(name).toString().trim();
				if (value.contains(" ")) buf.append("\"");
				buf.append(value);
				if (value.contains(" ")) buf.append("\"");
			}
		}
	
		if (propertiesFile==null && sysProps!=null && propagateSysProps) {
			buf.append(" -vmargs ");
			for(String name : sysProps.keySet()) {
				buf.append(" ");
				buf.append("-D");
				buf.append(name);
				buf.append("=");
				String value = sysProps.get(name).toString().trim();
				if (value.contains(" ")) buf.append("\"");
				buf.append(value);
				if (value.contains(" ")) buf.append("\"");
			}
		}
		if (xmx!=null || xms!=null) {
			if (!buf.toString().contains("-vmargs")) buf.append(" -vmargs ");
			if (xms!=null) {
				buf.append("-Xms");
				buf.append(xms);
			}
			if (xmx!=null) {
				buf.append("-Xmx");
				buf.append(xmx);
			}
		}
		
		if (sysProps.containsKey("logLocation") && propagateSysProps) {
			// Two spaces deals with the value of the last property being \
			buf.append("  > "+sysProps.get("logLocation"));
		} else if (propagateSysProps){
			// Two spaces deals with the value of the last property being \
			buf.append("  > "+workspace+"/consumer.log");
		}

		return buf.toString();
	}
	
	private String getDawnInstallationPath() throws Exception {
		String install   = isLinuxOS() ? sysProps.get("execLocation") : sysProps.get("winExecLocation");
		if (install == null) install = sysProps.get("execLocation");
		if (install == null) throw new Exception("'execLocation' must be set to the path to a DAWN executable!");
		return install;
	}

	private String getWorkspace() throws Exception {
		
		String workspace =  progArgs.get("data");
		if (workspace!=null) return workspace;
		
		String consumerName = sysProps.get("consumerName");
		if (consumerName==null) throw new Exception("You must set 'consumerName' because this is used for the workspace.");
		workspace = System.getProperty("user.home")+"/"+consumerName.replace(' ', '_');

		return workspace;
	}
	
	public void setProgramArgument(String argName, String argValue) {
		progArgs.put(argName, argValue);
	}
	
	/**
	 * @return true if linux
	 */
	public static boolean isLinuxOS() {
		String os = System.getProperty("os.name");
		return os != null && os.toLowerCase().startsWith("linux");
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getOutFileName() {
		return outFileName;
	}

	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}

	public String getErrFileName() {
		return errFileName;
	}

	public void setErrFileName(String errFileName) {
		this.errFileName = errFileName;
	}

	public String getXms() {
		return xms;
	}

	public void setXms(String xms) {
		this.xms = xms;
	}

	public String getXmx() {
		return xmx;
	}

	public void setXmx(String xmx) {
		this.xmx = xmx;
	}

	public boolean isPropagateSysProps() {
		return propagateSysProps;
	}

	public void setPropagateSysProps(boolean propagateSysProps) {
		this.propagateSysProps = propagateSysProps;
	}

}
