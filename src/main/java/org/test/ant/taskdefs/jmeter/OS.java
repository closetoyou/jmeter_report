package org.test.ant.taskdefs.jmeter;

import java.awt.*;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class OS {
    public static final int LINUX = 1;
    public static final int WINDOWS = 0;

    public static boolean runModeBaseBS = false;

    private static boolean osIsMacOsX;
    private static boolean osIsWindows;
    private static boolean osIsWindows10;
    private static boolean osIsLinux;
    public static String fileSeparator = System.getProperty("file.separator");

    public static void initOS() {

        String os = System.getProperty("os.name").toLowerCase();
        osIsMacOsX = "mac os x".equals(os);
        osIsWindows = os.indexOf("windows") != -1;
        osIsWindows10 = "windows 10".equals(os);
        osIsLinux = "linux".equalsIgnoreCase(os);
    }

    public static boolean isMacOSX() {
        return osIsMacOsX;
    }

    public static boolean isWindows() {
        return osIsWindows;
    }

    public static boolean isWindows10() {
        return osIsWindows10;
    }

    public static boolean isLinux() {
        return osIsLinux;
    }

    public static boolean isUsingWindowsVisualStyles() {
        if (!isWindows()) {
            return false;
        }

        boolean xpthemeActive = Boolean.TRUE.equals(Toolkit.getDefaultToolkit()
                .getDesktopProperty("win.xpstyle.themeActive"));
        if (!xpthemeActive) {
            return false;
        } else {
            try {
                return System.getProperty("swing.noxp") != null;
            } catch (RuntimeException e) {
                return true;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(getLocalIP());
    }

    public static String getLocalIP() {
        initOS();
        String ip = "";
        try {
            if (isLinux()) {
                Enumeration<?> e1 = (Enumeration<?>) NetworkInterface
                        .getNetworkInterfaces();
                while (e1.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface) e1.nextElement();
                    if (!ni.getName().equals("eth0")) {
                        continue;
                    } else {
                        Enumeration<?> e2 = ni.getInetAddresses();
                        while (e2.hasMoreElements()) {
                            InetAddress ia = (InetAddress) e2.nextElement();
                            if (ia instanceof Inet6Address)
                                continue;
                            ip = ia.getHostAddress();
                        }
                        break;
                    }
                }
            } else {
                ip = InetAddress.getLocalHost().getHostAddress().toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ip==null||ip.equalsIgnoreCase("")){
            ip=getCentOsIp();
        }
        return ip;
    }

    public static String getLocalHostName() {
        InetAddress addr;
        String hostname = "";
        try {
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName().toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return hostname;
    }

    public static String getMemory() {
        Runtime r = Runtime.getRuntime();
        return String.valueOf(r.totalMemory() / 1024 / 1024);
    }

    public static String getCpuFre() {
        Runtime r = Runtime.getRuntime();
        return String.valueOf(r.availableProcessors());
    }

    public static int getCpuNum() {
        Runtime r = Runtime.getRuntime();
        return r.availableProcessors();
    }

    public static String getDiskRam() {
        return null;
    }

    public static String getOsName() {
        String os = "";
        try {
            os = System.getProperty("os.name") + "("
                    + System.getProperty("os.version") + ")"
                    + System.getProperty("os.arch");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return os;
    }

    public static String getUserName() {
        String userName = "";
        try {
            userName = System.getProperty("user.name");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return userName;
    }

    public static String getJavaVersion() {
        String userName = "";
        try {
            userName = System.getProperty("java.version");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return userName;
    }

    public static String getCentOsIp() {
        InetAddress ia = null;
        String localip = "";
        try {
            ia = ia.getLocalHost();
            localip = ia.getHostAddress();
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        return localip;
    }
}