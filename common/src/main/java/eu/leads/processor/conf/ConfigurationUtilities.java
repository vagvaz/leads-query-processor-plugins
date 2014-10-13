package eu.leads.processor.conf;

import org.apache.commons.configuration.Configuration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by vagvaz on 6/1/14.
 */
public class ConfigurationUtilities {

    public static File[] getConfigurationFiles(String directory) {
        return ConfigurationUtilities.getConfigurationFiles(directory, null);
    }

    public static File[] getConfigurationFiles(String directory, String pattern) {
        File[] result = null;
        File folder = new File(directory);
        if (pattern == null) {
            result = folder.listFiles();
        } else {
            PatternFileNameFilter filter = new PatternFileNameFilter(pattern);
            result = folder.listFiles(filter);
        }
        return result;
    }


    public static String resolveHostname() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
    }

    public static String resolveIp() {

        String ip = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    public static String resolveIp(String interfaceFilter) {


        String ip = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                if (!iface.getDisplayName().equals(interfaceFilter))
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    public static String getString(Configuration conf) {
        Iterator<String> iterator = conf.getKeys();
        StringBuilder builder = new StringBuilder();
        builder.append("Config{");
        while (iterator.hasNext()) {
            String key = iterator.next();
            builder.append("\t" + key + "--> " + conf.getProperty(key).toString() + "\n");
        }
        builder.append("}");
        return builder.toString();

    }

    public static void addToClassPath(String base_dir) {
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        File f = new File(base_dir);
        URL u = null;
        try {
            u = f.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Class urlClass = URLClassLoader.class;
        Method method = null;
        try {
            method = urlClass.getDeclaredMethod("addURL", new Class[] {URL.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        try {
            method.invoke(urlClassLoader, new Object[] {u});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
