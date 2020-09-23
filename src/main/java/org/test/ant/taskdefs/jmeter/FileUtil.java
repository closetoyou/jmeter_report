package org.test.ant.taskdefs.jmeter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {
    public static void writeFile(String path, String msg) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            osw.write(msg);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean DeleteFolder(String sPath, boolean y) {
        boolean flag = false;
        File file = new File(sPath);
        
        if (!file.exists()) {  
            return flag;
        } else {
            
            if (file.isFile()) {  
                return deleteFile(sPath);
            } else {  
                if (y) {
                    return deleteDirectory(sPath);
                } else {
                    return deleteDirectoryChildFile(sPath);
                }
            }
        }
    }

    int i = 0;

    private static boolean deleteDirectory(String sPath) {
        
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } 
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }


    private static boolean deleteDirectoryChildFile(String sPath) {
        
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } 
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        return flag;
    }

    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath); 
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
}
