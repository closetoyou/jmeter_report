package org.test.ant.taskdefs.jmeter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class utf8bom extends Reader {

    private static final int BOM_SIZE = 4;
    PushbackInputStream internalIn;
    InputStreamReader internalIn2 = null;
    String defaultEnc;
   
    utf8bom(InputStream in, String defaultEnc) {
        internalIn = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEnc = defaultEnc;
    }
    
    utf8bom() {
    	
    }

    private static void readContentAndSaveWithEncoding(String filePath, String readEncoding, String saveEncoding) throws Exception {
        saveContent(filePath, readContent(filePath, readEncoding), saveEncoding);
    }

    private static void saveContent(String filePath, String content, String encoding) throws Exception {
        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter w = new OutputStreamWriter(fos, encoding);
        w.write(content);
        w.flush();
    }

    private static String readContent(String filePath, String encoding) throws Exception {
        FileInputStream file = new FileInputStream(new File(filePath));
        BufferedReader br = new BufferedReader(new utf8bom(file, encoding));
        String line = null;
        String fileContent = "";
        while ((line = br.readLine()) != null) {
            fileContent = fileContent + line;
            fileContent += "\r\n";
        }
        return fileContent;
    }

    private static List<String> getPerlineFileName(String filePath) throws Exception {
        FileInputStream file = new FileInputStream(new File(filePath));
        BufferedReader br = new BufferedReader(new InputStreamReader(file, "UTF-8"));
        String line = null;
        List<String> list = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            list.add(line);
        }
        return list;
    }

    private static List<String> getAllFilePaths(File filePath, List<String> filePaths) {
        File[] files = filePath.listFiles();
        if (files == null) {
            return filePaths;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                filePaths.add(f.getPath());
                getAllFilePaths(f, filePaths);
            } else {
                filePaths.add(f.getPath());
            }
        }
        return filePaths;
    }

    public static void main(String[] args) throws Exception {
        String suffix = ".js";
        List<String> paths = new ArrayList<String>();
        paths = getAllFilePaths(new File("F:\\apache-tomcat\\apache-tomcat-8.5.35\\webapps\\report"), paths);
        List<String> pathList = new ArrayList<String>();
        for (String path : paths) {
            if (path.endsWith(suffix)) {
                pathList.add(path);
            }
        }
        for (String path : pathList) {
            readContentAndSaveWithEncoding(path, "UTF-8", "UTF-8");
            System.out.println(path + "转换成功");
        }
    }
    
    public static void Process() throws Exception {
        String suffix = ".js";
        List<String> paths = new ArrayList<String>();
        paths = getAllFilePaths(new File("F:\\apache-tomcat\\apache-tomcat-8.5.35\\webapps\\report"), paths);
        List<String> pathList = new ArrayList<String>();
        for (String path : paths) {
            if (path.endsWith(suffix)) {
                pathList.add(path);
            }
        }
        for (String path : pathList) {
            readContentAndSaveWithEncoding(path, "UTF-8", "UTF-8");
            System.out.println(path + "转换成功");
        }
    }

    public String getDefaultEncoding() {
        return defaultEnc;
    }

    public String getEncoding() {
        if (internalIn2 == null) return null;
        return internalIn2.getEncoding();
    }

    protected void init() throws IOException {
        if (internalIn2 != null) return;
        String encoding;
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;
        n = internalIn.read(bom, 0, bom.length);
        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) &&
        (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) &&
        (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) &&
        (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEnc;
            unread = n;
        }
        //System.out.println("read=" + n + ", unread=" + unread);
        if (unread > 0) internalIn.unread(bom, (n - unread), unread);
        // Use given encoding
        if (encoding == null) {
            internalIn2 = new InputStreamReader(internalIn);
        } else {
            internalIn2 = new InputStreamReader(internalIn, encoding);
        }
    }

    public void close() throws IOException {
        init();
        internalIn2.close();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return internalIn2.read(cbuf, off, len);
    }
}
