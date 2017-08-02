package service;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * Created by USER on 20.07.2017.
 */
public class Md5 {
    public static String getMd5Hash(String string) throws Exception{
        return DigestUtils.md5Hex(string);
    }
}
