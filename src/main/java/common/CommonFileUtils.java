package main.java.common;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @author fpp
 * @version 1.0
 * @date 2020/5/20 15:10
 */
public class CommonFileUtils {

    public static String getFilePath(String fileName) {
        try {
            ClassLoader classLoader=getDefaultClassLoader();
            Enumeration<URL> urls = classLoader!=null?classLoader.getResources(fileName):ClassLoader.getSystemResources(fileName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String path;
                if (Objects.nonNull(url)) {
                    path = url.getPath();
                } else {
                    path = fileName;
                }
                return path;
            }
        }catch (IOException e){
        }
        return null;
    }

    public static InputStream getFileInputStream(String fileName) {
        return getDefaultClassLoader().getResourceAsStream(fileName);
    }


    public static ClassLoader getDefaultClassLoader(){
        ClassLoader classLoader=null;
        try{
            classLoader=Thread.currentThread().getContextClassLoader();
        }catch (Exception e){

        }
        if(null==classLoader){
            classLoader= CommonFileUtils.class.getClassLoader();
            if(null==classLoader){
                try{
                    classLoader= CommonFileUtils.class.getClassLoader().getParent();
                }catch (Exception e){

                }
            }
        }
        return classLoader;
    }

    public static InputStream getConfigFileInput(String fileName) throws UnsupportedEncodingException, FileNotFoundException {
        try {
            String path=getFilePath(fileName);
            String realPath=Utils.isNotEmpty(path)?URLDecoder.decode(path, "UTF-8"):URLDecoder.decode(fileName, "UTF-8");
            return new FileInputStream(realPath);
        }catch (IOException e){
            return getFileInputStream(URLDecoder.decode(fileName, "UTF-8"));
        }
    }

    public static FileOutputStream getConfigFileOut(String fileName) throws UnsupportedEncodingException, FileNotFoundException {
        String path=getFilePath(fileName);
        String realPath=Utils.isNotEmpty(path)?URLDecoder.decode(path, "UTF-8"):URLDecoder.decode(fileName, "UTF-8");
        return new FileOutputStream(realPath);
    }
}