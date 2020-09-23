package org.test.ant.taskdefs.jmeter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;

public class JMeterTask extends Task {

	private File jmeterHome;

	private File jmeterProperties;

	private File testPlan;

	private File resultLog;
	
	private File jmeterLogFile;
	
	private File resultLogDir;

	private ArrayList testPlans = new ArrayList();

	private boolean runRemote = false;

	private String proxyHost;

	private String proxyPort;

	private String proxyUser;

	private String proxyPass;

	private File jmeterJar;

	private ArrayList jvmArgs = new ArrayList();

	private ArrayList jmeterArgs = new ArrayList();

	private ArrayList jmProperties = new ArrayList();

	private String failureProperty;

	private ArrayList resultLogFiles = new ArrayList();
	public String testPlanFileName = "";
	
	public static void main(String[] args) {
		JMeterTask j= new JMeterTask();
		j.execute();
	}

	public void execute() throws BuildException {
		if (jmeterHome == null || !jmeterHome.isDirectory()) {
			throw new BuildException("请为JMeter安装目录设置jmeterhome", getLocation());
		}

		jmeterJar = new File(jmeterHome.getAbsolutePath() + File.separator + "bin" + File.separator + "ApacheJMeter.jar");

		validate();

		log("使用JMeter Home: " + jmeterHome.getAbsolutePath(), Project.MSG_VERBOSE);
		log("使用JMeter Jar: " + jmeterJar.getAbsolutePath(), Project.MSG_VERBOSE);
	
		if (testPlan != null) {
			File resultLogFile = resultLog;
			if (resultLogDir != null) {
//				String testPlanFileName = testPlan.getName();
				testPlanFileName = testPlan.getName();
				String resultLogFilePath = this.resultLogDir + File.separator + testPlanFileName.replaceFirst("\\.jmx", "\\.jtl");
				resultLogFile = new File(resultLogFilePath);
			}
			executeTestPlan(testPlan, resultLogFile);
		}

		Iterator testPlanIter = testPlans.iterator();
		while (testPlanIter.hasNext()) {
			FileSet fileSet = (FileSet)testPlanIter.next();
			DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
			File baseDir = scanner.getBasedir();
			String[] files = scanner.getIncludedFiles();
            StringBuilder sb = new StringBuilder();
			for (int i = 0; i < files.length; i++) {
				String testPlanFilePath = baseDir + File.separator + files[i];
				File testPlanFile = new File(testPlanFilePath);
				File resultLogFile = resultLog;
				if (resultLogDir != null) {
					String resultLogFilePath = this.resultLogDir + File.separator + files[i].replaceFirst("\\.jmx", "\\.jtl");
					resultLogFile = new File(resultLogFilePath);
				}
				executeTestPlan(testPlanFile, resultLogFile);
				sb =sb.append(files[i]+"\\|");
			}
			FileUtil.writeFile("E:\\log1.txt", sb.toString());
		}

		checkForFailures();
	}
	
	public String getfilename() {
		return testPlanFileName;
	}

	private void checkForFailures() throws BuildException {
		if (failureProperty != null && failureProperty.trim().length() > 0) {
			for (Iterator i = resultLogFiles.iterator(); i.hasNext();) {
				File resultLogFile = (File)i.next();
				log("检查 result log文件 " + resultLogFile.getName() + ".", Project.MSG_VERBOSE);
				LineNumberReader reader = null;
				try {
					reader = new LineNumberReader(new FileReader(resultLogFile));
					
					String line = null;
					while ((line = reader.readLine()) != null) {
						line = line.toLowerCase();
						
						if (line.indexOf("success=\"false\"") > 0 || line.indexOf(" s=\"false\"") > 0) {
							log("检查失败 at line: " + reader.getLineNumber(), Project.MSG_VERBOSE);
							setFailure(getFailureProperty());
							return;
						}
					}
				}
				catch (IOException e) {
					throw new BuildException("无法读取jmeter resultLog: " + e.getMessage());
				}
				finally {
					try {
						reader.close();
					}
					catch (Exception e) { 
					}
				}
			}
		}
	}

	private void validate() throws BuildException {
		if (!(jmeterJar.exists() && jmeterJar.isFile())) {
			throw new BuildException("jmeter jar文件没有找到: " + jmeterJar.getAbsolutePath(), getLocation());
		}

		if (resultLog == null && resultLogDir == null) {
			throw new BuildException("请设置resultLog或者resultLogDir.", getLocation());
		}

		if (resultLogDir != null && !(resultLogDir.exists() && resultLogDir.isDirectory())) {
			throw new BuildException("resultLogDir没有找到: " + resultLog.getAbsolutePath(), getLocation());
		}
	}

	private void executeTestPlan(File testPlanFile, File resultLogFile) {
		log("执行test plan: " + testPlanFile + " ==> " + resultLogFile, Project.MSG_INFO);
		resultLogFiles.add(resultLogFile);

		CommandlineJava cmd = new CommandlineJava();

		cmd.setJar(jmeterJar.getAbsolutePath());


		Iterator jvmArgIterator = jvmArgs.iterator();
		while (jvmArgIterator.hasNext()) {
			Arg jvmArg = (Arg)jvmArgIterator.next();
			cmd.createVmArgument().setValue(jvmArg.getValue());
		}

		Iterator jmeterArgIterator = jmeterArgs.iterator();
		while (jmeterArgIterator.hasNext()) {
			Arg jmeterArg = (Arg)jmeterArgIterator.next();
			cmd.createArgument().setValue(jmeterArg.getValue());
		}

		cmd.createArgument().setValue("-n");
		if (jmeterProperties != null) {
			cmd.createArgument().setValue("-p");
			cmd.createArgument().setValue(jmeterProperties.getAbsolutePath());
		}
		if (jmeterLogFile != null) {
			cmd.createArgument().setValue("-j");
			cmd.createArgument().setValue(jmeterLogFile.getAbsolutePath());
		}
		cmd.createArgument().setValue("-t");
		cmd.createArgument().setValue(testPlanFile.getAbsolutePath());
		cmd.createArgument().setValue("-l");
		cmd.createArgument().setValue(resultLogFile.getAbsolutePath());
		if (runRemote) {
			cmd.createArgument().setValue("-r");
		}

		if ((proxyHost != null) && (proxyHost.length() > 0)) {
			cmd.createArgument().setValue("-H");
			cmd.createArgument().setValue(proxyHost);
		}
		if ((proxyPort != null) && (proxyPort.length() > 0)) {
			cmd.createArgument().setValue("-P");
			cmd.createArgument().setValue(proxyPort);
		}
		if ((proxyUser != null) && (proxyUser.length() > 0)) {
			cmd.createArgument().setValue("-u");
			cmd.createArgument().setValue(proxyUser);
		}
		if ((proxyPass != null) && (proxyPass.length() > 0)) {
			cmd.createArgument().setValue("-a");
			cmd.createArgument().setValue(proxyPass);
		}

		Iterator jmPropertyIterator = jmProperties.iterator();
		while (jmPropertyIterator.hasNext()) {
			Property jmProperty = (Property)jmPropertyIterator.next();
			if (jmProperty.isValid()) {
				cmd.createArgument().setValue((jmProperty.isRemote() ? "-G" : "-J") + jmProperty.toString());
			}
		}

		Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
		execute.setCommandline(cmd.getCommandline());
		execute.setAntRun(getProject());

		execute.setWorkingDirectory(new File(jmeterHome.getAbsolutePath() + File.separator + "bin"));
		log(cmd.describeCommand(), Project.MSG_VERBOSE);

		try {
			execute.execute();
		}
		catch (IOException e) {
			throw new BuildException("JMeter执行失败", e, getLocation());
		}
	}

	public void setJmeterHome(File jmeterHome) {
		this.jmeterHome = jmeterHome;
	}

	public File getJmeterHome() {
		return jmeterHome;
	}

	public void setJmeterProperties(File jmeterProperties) {
		this.jmeterProperties = jmeterProperties;
	}

	public File getJmeterProperties() {
		return jmeterProperties;
	}

	public void setTestPlan(File testPlan) {
		this.testPlan = testPlan;
	}

	public File getTestPlan() {
		return testPlan;
	}

	public void setResultLog(File resultLog) {
		this.resultLog = resultLog;
	}

	public File getResultLog() {
		return resultLog;
	}	

	public File getJmeterLogFile() {
		return jmeterLogFile;
	}

	public void setJmeterLogFile(File jmeterLogFile) {
		this.jmeterLogFile = jmeterLogFile;
	}

	public void setResultLogDir(File resultLogDir) {
		this.resultLogDir = resultLogDir;
	}

	public File getResultLogDir() {
		return this.resultLogDir;
	}

	public void addTestPlans(FileSet set) {
		testPlans.add(set);
	}

	public void addJvmarg(Arg arg) {
		jvmArgs.add(arg);
	}

	public void addJmeterarg(Arg arg) {
		jmeterArgs.add(arg);
	}

	public void setRunRemote(boolean runRemote) {
		this.runRemote = runRemote;
	}

	public boolean getRunRemote() {
		return runRemote;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyPass(String proxyPass) {
		this.proxyPass = proxyPass;
	}

	public String getProxyPass() {
		return proxyPass;
	}

	public void addProperty(Property property) {
		jmProperties.add(property);
	}

	public void setFailureProperty(String failureProperty) {
		this.failureProperty = failureProperty;
	}

	public String getFailureProperty() {
		return failureProperty;
	}

	public void setFailure(String failureProperty) {
		getProject().setProperty(failureProperty, "true");
	}

}
