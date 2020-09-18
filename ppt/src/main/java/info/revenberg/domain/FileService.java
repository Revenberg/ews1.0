package info.revenberg.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileService {
    public static void createFolderIfNotExists(String dirName) throws SecurityException {
        File theDir = new File(dirName);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }

    public static void deleteFolderIfExists(File f) throws IOException {
        if (f.isDirectory()) {
          for (File c : f.listFiles())
            deleteFolderIfExists(c);
        }   
        f.delete();
    }     

    private static List<String> mySort(List<String> filelist) {
        TreeMap<String, String> list = new TreeMap<String, String>();
        String key;
        for (String n : filelist) {
            if (n.contains("image")) {
                key = n.substring(n.lastIndexOf("image") + 5, n.lastIndexOf("."));
                while (key.length() < 8) {
                    key = "0" + key;
                }
                list.put(key, n);
            }

        }
        List<String> rc = new ArrayList();
        for (Map.Entry<String, String> me : list.entrySet()) {
            rc.add((String) me.getValue());
        }

        return rc;
    }

    public static List<String> unzip(String filename, String dest) throws IOException {
        FileService.createFolderIfNotExists(dest);

        List<String> rc = new ArrayList<String>();
        ZipFile zipFile;
        zipFile = new ZipFile(filename);

        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = dest + "/" + zipEntry.getName();

            File file = new File(name);
            rc.add(name);
            if (name.endsWith("/")) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is;
            is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();

        return mySort(rc);
    }

    public static String getExtension(String filename) {
        String[] s1 = filename.split("/");
        String versName = s1[s1.length - 1];
        String[] s2 = versName.split("\\.");
        return s2[s2.length - 1];
    }

    public static File copyFile(String srcFilename, String destPath, String destFilename) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        File directory = new File(String.valueOf(destPath));
        if (!directory.exists()) {
            directory.mkdir();
        }

        is = new FileInputStream(srcFilename);
        os = new FileOutputStream(destPath + "/" + destFilename);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
        return new File(destPath + "/" + destFilename);
    }

}