package xyz.gghost.jskype.internal.utils;

/**
 * Created by Ghost on 10/10/2015.
 */
public class NamingUtils {
    public static String getUsername(String a){
        if (a.contains("8:"))
            a = a.split("8:")[1];
        if (a.contains("/"))
            a = a.split("/")[0];
        if (a.contains("<"))
            a = a.split("<")[0];
        return a;
    }
    public static String getThreadId(String a){
        if (a.contains("19:"))
            a = a.split("19:")[1];
        if (a.contains("@"))
            a = a.split("@")[0];
        return a;
    }

}
