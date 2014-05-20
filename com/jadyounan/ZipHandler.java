/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jadyounan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author jadyounan
 */
public class ZipHandler{

    private final ByteArrayOutputStream bos;
    private final ZipOutputStream zipfile;
    private final Map manifest;

    /**
     *
     */
    public ZipHandler() {
        bos = new ByteArrayOutputStream();
        zipfile = new ZipOutputStream(bos);
        manifest = new HashMap();
    }

    /**
     *
     * @return @throws UnsupportedEncodingException
     */
    public byte[] getManifest() throws UnsupportedEncodingException {
        Set keys = manifest.keySet();
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (Object key : keys) {
            sb.append(i <= 0 ? "{" : ",");
            sb.append("\"").append(key).append("\": \"").append(manifest.get(key)).append("\"");
            i++;
        }
        sb.append("}");
        return sb.toString().getBytes("UTF-8");
    }

    /**
     *
     * @return @throws IOException
     */
    public byte[] getBytes() throws IOException {
        zipfile.finish();
        bos.flush();
        zipfile.close();
        manifest.clear();
        return bos.toByteArray();
    }

    /**
     *
     * @param path
     * @param filename
     * @param file
     * @param addToManifest
     * @throws IOException
     */
    public void addFile(String path, String filename, byte[] file) throws IOException {
        if (file != null) {
            String completeFilename = path.length() > 0 ? path + "/" + filename : filename;
            ZipEntry zipEntry = new ZipEntry(completeFilename);
            CRC32 crc = new CRC32();
            crc.update(file);
            zipEntry.setCrc(crc.getValue());
            zipfile.putNextEntry(zipEntry);
            zipfile.write(file, 0, file.length);
            zipfile.flush();
            zipfile.closeEntry();
 
 
            manifest.put(completeFilename, SHAsum(file));

        } else {
            System.out.println("File " + filename + " was null!");
        }


    }

    /**
     *
     * @param in
     * @return
     * @throws NoSuchAlgorithmException
     */
    static String SHAsum(byte[] in) throws NoSuchAlgorithmException { 
        return DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(in));
    }
}

