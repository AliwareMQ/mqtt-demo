package com.aliyun.openservices.lmq.example.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

/**
 * 工具类：负责封装 MQ4IOT 客户端的初始化参数设置
 */
public class ConnectionOptionWrapper {
    /**
     * 内部连接参数
     */
    private MqttConnectOptions mqttConnectOptions;
    /**
     * MQ4IOT 实例 ID，购买后控制台获取
     */
    private String instanceId;
    /**
     * 账号 accesskey，从账号系统控制台获取
     */
    private String accessKey;
    /**
     * 账号 secretKey，从账号系统控制台获取，仅在Signature鉴权模式下需要设置
     */
    private String secretKey;
    /**
     * MQ4IOT clientId，由业务系统分配，需要保证每个 tcp 连接都不一样，保证全局唯一，如果不同的客户端对象（tcp 连接）使用了相同的 clientId 会导致连接异常断开。
     * clientId 由两部分组成，格式为 GroupID@@@DeviceId，其中 groupId 在 MQ4IOT 控制台申请，DeviceId 由业务方自己设置，clientId 总长度不得超过64个字符。
     */
    private String clientId;
    /**
     * 客户端使用的 Token 参数，仅在 Token 鉴权模式下需要设置，Key 为 token 类型，一个客户端最多存在三种类型，R，W，RW，Value 是 token内容。
     * 应用需要保证 token 在过期及时更新。否则会导致连接异常。
     */
    private Map<String, String> tokenData = new ConcurrentHashMap<String, String>();

    /**
     * Token 鉴权模式下构造方法
     *
     * @param instanceId MQ4IOT 实例 ID，购买后控制台获取
     * @param accessKey 账号 accesskey，从账号系统控制台获取
     * @param clientId MQ4IOT clientId，由业务系统分配
     * @param tokenData 客户端使用的 Token 参数，仅在 Token 鉴权模式下需要设置
     */
    public ConnectionOptionWrapper(String instanceId, String accessKey, String clientId,
                                      Map<String, String> tokenData) {
        this.instanceId = instanceId;
        this.accessKey = accessKey;
        this.clientId = clientId;
        if (tokenData != null) {
            this.tokenData.putAll(tokenData);
        }
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("Token|" + accessKey + "|" + instanceId);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : tokenData.entrySet()) {
            builder.append(entry.getKey()).append("|").append(entry.getValue()).append("|");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        mqttConnectOptions.setPassword(builder.toString().toCharArray());
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setMqttVersion(MQTT_VERSION_3_1_1);
        mqttConnectOptions.setConnectionTimeout(5000);
    }

    /**
     * Signature 鉴权模式下构造方法
     *
     * @param instanceId MQ4IOT 实例 ID，购买后控制台获取
     * @param accessKey 账号 accesskey，从账号系统控制台获取
     * @param clientId MQ4IOT clientId，由业务系统分配
     * @param secretKey 账号 secretKey，从账号系统控制台获取
     */
    public ConnectionOptionWrapper(String instanceId, String accessKey, String secretKey,
                                      String clientId) throws NoSuchAlgorithmException, InvalidKeyException {
        this.instanceId = instanceId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.clientId = clientId;
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("Signature|" + accessKey + "|" + instanceId);
        mqttConnectOptions.setPassword(Tools.macSignature(clientId, secretKey).toCharArray());
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setMqttVersion(MQTT_VERSION_3_1_1);
        mqttConnectOptions.setConnectionTimeout(5000);
    }

    public ConnectionOptionWrapper(String instanceId, String accessKey, String secretKey,
                                      String clientId, String rootCAPath, String deviceCrtPath, String deviceKeyPath,
                                      String passwd) throws Exception {
        this.instanceId = instanceId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.clientId = clientId;
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setKeepAliveInterval(90);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setMqttVersion(MQTT_VERSION_3_1_1);
        mqttConnectOptions.setConnectionTimeout(5000);

        SSLContext ctx = getSSLContext(rootCAPath, deviceCrtPath, deviceKeyPath, passwd);
        mqttConnectOptions.setSocketFactory(ctx.getSocketFactory());
    }

    public SSLContext getSSLContext(String caPath, String crtPath, String keyPath, String password) throws Exception {
        /*
         * CA证书是用来认证服务端的，这里的CA就是一个公认的认证证书
         * TrustManagerFactory 管理的是授信的CA证书，所以KeyStore里面存放的不需要私钥信息，通常也不可能有
         */
        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
        FileInputStream caIn = new FileInputStream(caPath);
        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
        KeyStore caKs = KeyStore.getInstance("JKS");
        caKs.load(null, password.toCharArray());
        caKs.setCertificateEntry("ca1", ca); //可以通过设置alias不同，配置多个ca实例，即配置多个可信的root CA。
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(caKs);
        caIn.close();

        //这个客户端证书，是用来发送给服务端的，准备做双向验证用的。
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream crtIn = new FileInputStream(crtPath);
        Collection<? extends Certificate> certs = cf.generateCertificates(crtIn);
        X509Certificate[] x509Certs = new X509Certificate[certs.size()];
        int i=0;
        for(Certificate cert: certs) {
            x509Certs[i++] = (X509Certificate) cert;
        }
        crtIn.close();

        //客户端私钥，是用来处理双向SSL验证中服务端用客户端证书加密的数据的解密（解析签名）工具
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, password.toCharArray());
        ks.setCertificateEntry("certificate3", x509Certs[0]);

        Key privateKey = getPrivateKey(new File(keyPath));
        /*
         * 注意：下面这行代码中非常重要的一点是：
         * setKeyEntry这个函数的第二个参数 password，他不是指私钥的加密密码，只是KeyStore对这个私钥进行管理设置的密码
         *
         * setKeyEntry中最后一个参数，chain的顺序是证书链中越靠近当前privateKey节点的证书，越靠近数字下标0的位置。即chain[0]是privateKey对应的证书，
         * chain[1]是签发chain[0]的证书，以此类推，有chain[i+1]签发chain[i]的关系。
         */
        ks.setKeyEntry("private-key", privateKey, password.toCharArray(), x509Certs);
        /*
         * KeyManagerFactory必须是证书和私钥配对使用，即KeyStore里面装载客户端证书以及对应的私钥，双向SSL验证需要。
         */
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(ks, password.toCharArray());

        /*
         * 最后创建SSL套接字工厂 SSLSocketFactory
         * 注意：这里，SSLContext不支持TLSv2创建
         */
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        KeyManager[] kms = kmf.getKeyManagers();
        TrustManager[] tms = tmf.getTrustManagers();
        context.init(kms, tms, new SecureRandom());
        return context;
    }

    public Key getPrivateKey(File file) throws InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
        if (file == null) {
            return null;
        }
        PrivateKey privKey = null;
        PemReader pemReader = null;
        try {
            pemReader = new PemReader(new FileReader(file));
            PemObject pemObject = pemReader.readPemObject();
            byte[] pemContent = pemObject.getContent();
            PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(null, pemContent);
            try {
                return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
            } catch (InvalidKeySpecException ignore) {
                try {
                    return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
                } catch (InvalidKeySpecException ignore2) {
                    try {
                        return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
                    } catch (InvalidKeySpecException e) {
                        throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("read private key fail,the reason is the file not exist");
            e.printStackTrace();
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            System.out.println("read private key fail,the reason is :"+e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pemReader != null) {
                    pemReader.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return privKey;
    }

    protected static PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException {

        if (password == null) {
            return new PKCS8EncodedKeySpec(key);
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }

}
