/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jadyounan;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

public final class PKCS7Signer {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyStore getKeystore(String storeLocation, String storePasswd) throws Exception {
        if (storeLocation == null) {
            System.out.println("Could not find store file (.p12)");
            return null;
        }
        // First load the keystore object by providing the p12 file path
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        // replace testPass with the p12 password/pin
        clientStore.load(new FileInputStream(storeLocation), storePasswd.toCharArray());
        return clientStore;
    }

    private X509CertificateHolder getCert(KeyStore keystore, String alias) throws Exception {
        java.security.cert.Certificate c = keystore.getCertificate(alias);
        return new X509CertificateHolder(c.getEncoded());
    }

    private PrivateKey getPrivateKey(KeyStore keystore, String alias, String storePasswd) throws Exception {
        return (PrivateKey) keystore.getKey(alias, storePasswd.toCharArray());
    }

    public byte[] sign(String storeLocation, String storePasswd, byte[] dataToSign) throws Exception {
        KeyStore clientStore = getKeystore(storeLocation, storePasswd);

        if (clientStore == null) {
            return null;
        }
        Enumeration aliases = clientStore.aliases();
        String alias = "";
        while (aliases.hasMoreElements()) {
            alias = (String) aliases.nextElement();
            if (clientStore.isKeyEntry(alias)) {
                break;
            }
        }

        CMSTypedData msg = new CMSProcessableByteArray(dataToSign); // Data to sign

        X509CertificateHolder x509Certificate = getCert(clientStore, alias);
        List certList = new ArrayList();
        certList.add(x509Certificate); // Adding the X509 Certificate

        Store certs = new JcaCertStore(certList);

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        // Initializing the the BC's Signer
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(
                getPrivateKey(clientStore, alias, storePasswd));

        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
                .setProvider("BC").build()).build(sha1Signer, x509Certificate));
        // adding the certificate
        gen.addCertificates(certs);
        // Getting the signed data
        CMSSignedData sigData = gen.generate(msg, false);
        return sigData.getEncoded();
    }
}

