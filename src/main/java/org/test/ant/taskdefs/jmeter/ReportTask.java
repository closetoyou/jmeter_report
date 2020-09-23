package org.test.ant.taskdefs.jmeter;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;

public class ReportTask extends Task {
    private String in;
    private String out;
    private String mailhost;
    private String mailport;
    private boolean ssl;
    private String user;
    private String password;
    private String mailTitle;
    private String from;
    private String toAddress;
    private String runUser;
    private String reportName;
    private String reportUrl;

    public void execute() throws BuildException {
        System.out.println("开始执行收集报告任务");
        if (StringUtils.isEmpty(in)) {
            System.out.println("in,Jtl文件不能为空");
            return;
        }
        if (StringUtils.isEmpty(out)) {
            System.out.println("out,输出目录不能为空");
            return;
        }
        System.out.println("out输出目录:"+out);
        if (out.indexOf("\\")!=-1) {
        	out = out.replace("\\", "/");
        }
        String mailContent = ReportHtml.write(in, out, runUser, reportName, reportUrl);
        File directory = new File(out);
        String path = "";
        try {
            path = directory.getParent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("报告生成完成");

        if (StringUtils.isNotEmpty(mailhost) && StringUtils.isNotEmpty(mailport)) {
            System.out.println("开始发送邮件...");
            MailObj mailObj = new MailObj();
            mailObj.setHost(mailhost);
            mailObj.setPort(mailport);
            mailObj.setMailTitle(mailTitle);
            mailObj.setName(user);
            mailObj.setPassword(password);
            mailObj.setFrom(from);
            mailObj.setToAddress(toAddress);
            mailObj.setSsl(ssl);
            mailObj.setMailContent(mailContent);
            mailObj.setReportPath(path);
            mailObj.setHtmlPath(out);
        }

//        try {
//        	String fileName = getfilename(out);
//            SendEmail.sendEmail(mailObj,fileName);
//        } catch (Exception e) {
//            System.out.println("发送邮件失败:"+e);
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        String a = "";
        System.out.println(StringUtils.isEmpty(a));
    }

    public static String getfilename(String htmlPath) {
    	File directory = new File(htmlPath);
    	String path = directory.getParent();
    	path = path.replace("\\report\\html", "");
//    	path = path.replace("webapps", "workspace").replace("/html", "").replace("/loglist", "").replace("/api", "");
    	String fileName = "";
    	File file = new File(path);
    	System.out.println("邮件名称目录:" + path);
    	File[] fileList = file.listFiles();
    	for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].toString().indexOf(".jmx")!=-1) {
                fileName = fileList[i].getName().replace(".jmx", "");
                System.out.println("文件：" + fileName);                
            }
    	}
		return fileName;  	
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public void setMailhost(String mailhost) {
        this.mailhost = mailhost;
    }

    public void setMailport(String mailport) {
        this.mailport = mailport;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getIn() {
        return in;
    }

    public String getOut() {
        return out;
    }

    public String getMailhost() {
        return mailhost;
    }

    public String getMailport() {
        return mailport;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getMailTitle() {
        return mailTitle;
    }

    public String getFrom() {
        return from;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getRunUser() {
        return runUser;
    }

    public void setRunUser(String runUser) {
        this.runUser = runUser;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
}
