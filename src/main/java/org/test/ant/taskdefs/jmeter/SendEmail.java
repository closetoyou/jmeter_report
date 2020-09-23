package org.test.ant.taskdefs.jmeter;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SendEmail {

    public static void sendEmail(MailObj mailObj,String fileName) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", Boolean.toString(mailObj.isSsl()));       
        properties.setProperty("mail.host", mailObj.getHost());
        
        if (mailObj.getPort() != null && !mailObj.getPort().equalsIgnoreCase("")) {
            properties.setProperty("mail.smtp.port", mailObj.getPort());
        }
     
        properties.setProperty("mail.transport.protocol", "smtp");

        if (mailObj.getFrom().endsWith("@test.com")) {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", sf);
        }
        final String userName = mailObj.getName();
        final String password = mailObj.getPassword();
        
        MimeMessage message = new MimeMessage(Session.getInstance(properties,
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                userName, password);
                    }
                }));
        
        message.setFrom(new InternetAddress(mailObj.getFrom()));

        String sendTo = mailObj.getToAddress();
        String sendToCC = mailObj.getToCc();
      
        if (null != sendTo) {
            InternetAddress[] senderList = new InternetAddress()
                    .parse(sendTo);
            message.setRecipients(Message.RecipientType.TO, senderList);
        } else {
            return;
        }
        if (null != sendToCC) {
            InternetAddress[] iaCCList = new InternetAddress()
                    .parse(sendToCC);
            message.setRecipients(Message.RecipientType.CC, iaCCList);
        }
    	String title = fileName+"接口自动化测试报告";
    	System.out.println("邮件title:" + title);
//        String subject = mailObj.getMailTitle();
        String subject = title;
        if (subject.equalsIgnoreCase("")) {
            message.setSubject(title);
        } else {
//            message.setSubject(subject);
        	 message.setSubject(title);
        }
       
        MimeBodyPart text = new MimeBodyPart();
        
        text.setContent(mailObj.getMailContent() + "<img src='cid:b'>",
                "text/html;charset=UTF-8");
      
        MimeBodyPart img = new MimeBodyPart();

        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text);
        
        File pngFile = new File(mailObj.getReportPath() + "\\report.png");
        if (pngFile.exists()) {
            DataHandler dh = new DataHandler(new FileDataSource(pngFile.getAbsolutePath()));
            img.setDataHandler(dh);
            img.setContentID("passRate");
            mm.addBodyPart(img);
        }
        mm.setSubType("related");
        
        String filename3 = mailObj.getHtmlPath();
        System.out.println("附件名称:" + filename3);
        filename3=filename3.replace("report.html", "");
    	Date d = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String newfilename = title+sdf.format(d)+".html";
        System.out.println("重命名文件名:" + newfilename);
//        renameFile(filename3,"report.html",newfilename);//need
//        filename3 = mailObj.getHtmlPath();
//        System.out.println("重命名附件名称:" + filename3);
//        File file = new File(mailObj.getHtmlPath());
        File file = new File(filename3+newfilename);
        if (file.exists()) {
            MimeBodyPart attch = new MimeBodyPart();
            DataHandler dh1 = new DataHandler(new FileDataSource(file.getAbsolutePath()));
            attch.setDataHandler(dh1);
            String filename1 = dh1.getName();           
            attch.setFileName(MimeUtility.encodeText(filename1));
            mm.addBodyPart(attch);
        }
       
        MimeBodyPart all = new MimeBodyPart();
        all.setContent(mm);        
        MimeMultipart mm2 = new MimeMultipart();
        mm2.addBodyPart(all);
        mm2.setSubType("mixed");

        message.setContent(mm);
        message.saveChanges(); 

        Transport.send(message);
        System.out.println("邮件发送成功");
    }
    
    public static void renameFile(String path,String oldname,String newname){ 
        if(!oldname.equals(newname)){ 
            File oldfile=new File(path+"/"+oldname); 
            File newfile=new File(path+"/"+newname); 
            if(!oldfile.exists()){
            	System.out.println("文件不存在！");
                return;
            }
            if(newfile.exists()) 
                System.out.println(newname+"已经存在！"); 
            else{ 
                oldfile.renameTo(newfile); 
                System.out.println(newname+"重命名成功！"); 
            } 
        }else{
            System.out.println("新文件名和旧文件名相同...");
        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("sendTo", "test@test.com");   
        try {
//            SendEmail.sendEmail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
