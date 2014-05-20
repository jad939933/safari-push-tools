/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jadyounan;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Formatter;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jadyounan
 */
public class Packager {
    
    /**
     *
     * @param authenticationToken
     * @return
     * @throws Exception
     */
    static String getJSON(String authenticationToken) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("websiteName", "Jad Y.");
        obj.put("websitePushID", "web.com.jadyounan");
        obj.put("allowedDomains", new JSONArray());
        obj.getJSONArray("allowedDomains").put("https://jadyounan.com");
        obj.put("urlFormatString", "https://www.jadyounan.com/%@");
        obj.put("authenticationToken", authenticationToken);
        obj.put("webServiceURL", "https://www.jadyounan.com/w/com.apple.safari");//callback URL

        return obj.toString();
    }

    /**
     *
     * @param authenticationToken
     * @return
     * @throws Exception
     */
    static byte[] createPackageFile(String authenticationToken) throws Exception {

        System.out.println("packaging safari file with token: " + authenticationToken);
        createPackageFile zip = new createPackageFile();

        byte icon_bytes_16[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_16x16.png"));
        byte icon_bytes_16_2[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_16x16@2x.png"));
        byte icon_bytes_32[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_32x32.png"));
        byte icon_bytes_32_2[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_32x32@2x.png"));
        byte icon_bytes_128[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_128x128.png"));
        byte icon_bytes_128_2[] = IO.readFully(Safari.class.getResourceAsStream("/resources/icons/icon_128x128@2x.png"));

        zip.addFile("icon.iconset", "icon_16x16.png", icon_bytes_16);
        zip.addFile("icon.iconset", "icon_16x16@2x.png", icon_bytes_16_2);
        zip.addFile("icon.iconset", "icon_32x32.png", icon_bytes_32);
        zip.addFile("icon.iconset", "icon_32x32@2x.png", icon_bytes_32_2);
        zip.addFile("icon.iconset", "icon_128x128.png", icon_bytes_128);
        zip.addFile("icon.iconset", "icon_128x128@2x.png", icon_bytes_128_2);

        zip.addFile("", "website.json", getJSON(authenticationToken).getBytes());

        byte[] manifest = zip.getManifest();
        zip.addFile("", "manifest.json", manifest);

        byte signature[] = sign(manifest);

        zip.addFile("", "signature", signature);

        return zip.getBytes();

    }
 

    /**
     *
     * @param bytesToSign
     * @return
     * @throws Exception
     */
    static byte[] sign(byte bytesToSign[]) throws Exception {
        return new PKCS7Singer().sign(bytesToSign);
    }

    /**
     * Servlet handler , should listen on the callback URL (as in webServiceURL)
     * @param requestPath
     * @param req
     * @param servletRequest
     * @param servletResponse
     * @throws Exception
     */
    public static void handle(String requestPath, Request req, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {

        StringTokenizer st = new StringTokenizer(requestPath, "/");
        st.nextToken();
        String companyDomain = st.nextToken();

        String version = st.nextToken();

        byte bytes[] = new byte[]{};

        switch (st.nextToken().toLowerCase()) {
            case "log": {
                bytes = new byte[]{};

                byte r[] = org.eclipse.jetty.util.IO.readBytes(servletRequest.getInputStream());

                System.out.println("LOG " + new String(r));
            }
            break;
            case "devices": {
                String deviceID = st.nextToken();

                String authToken = req.getHeader("Authorization").split(" ")[1];

                // use the authToken to get the userID who started the request
 
                switch (servletRequest.getMethod().toUpperCase()) {
                    case "DELETE":
                        //handle deleting the token from your database
                        break;
                    default: {
                        //handle adding the token/deviceID to your database
                    }
                    break;
                }

                FLUSH.updatePushDevices(userID);

                bytes = new byte[]{};
            }
            break;
            case "pushpackages": {
                /**
                 * Safari requests the pacakge
                 */
                String id = st.nextToken();

                JSONObject obj = new JSONObject(new String(org.eclipse.jetty.util.IO.readBytes(servletRequest.getInputStream())));

                String userID = obj.getString("user_id");

                String authenticationToken="..a random string so you can later identify the user who started the request";

                bytes = createPackageFile(authenticationToken);
            }
            break;
            default:
                bytes = new byte[]{};
                break;
        }

        servletResponse.setStatus(200);
        servletResponse.setContentLength(bytes.length);
        try (OutputStream out = servletResponse.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }

}

