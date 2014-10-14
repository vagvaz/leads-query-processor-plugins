package eu.leads.crawler.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author P. Sutra
 */
public class Digest {


    public static byte[] digest(byte[] content) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // unreachable code
        }
        md.update(content);
        return md.digest();
    }

}
