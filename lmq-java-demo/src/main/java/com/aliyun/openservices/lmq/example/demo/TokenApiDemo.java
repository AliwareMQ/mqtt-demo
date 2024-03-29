package com.aliyun.openservices.lmq.example.demo;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.lmq.example.util.Tools;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.onsmqtt.model.v20200420.ApplyTokenRequest;
import com.aliyuncs.onsmqtt.model.v20200420.ApplyTokenResponse;
import com.aliyuncs.onsmqtt.model.v20200420.RevokeTokenRequest;
import com.aliyuncs.profile.DefaultProfile;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本文件提供了 TokenAPI 的操作示例，实际场景中该代码应该由业务方的应用服务器调用，应用服务器负责管理设备的权限，向 MQ4IoT 申请 token 并发放给设备。具体交互参考：
 * https://help.aliyun.com/document_detail/54226.html?spm=a2c4g.11186623.6.573.193d73bezfU0dC
 */
public class TokenApiDemo {
    public static IAcsClient getIAcsClient(String accessKey, String secretKey, String regionId) {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKey, secretKey);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        return client;
    }
    public static void main(String[] args)
        throws UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException,
        KeyStoreException, InvalidKeyException, ClientException {
        List<String> resource = new ArrayList<String>();
        resource.add("XXXXX/AAA");
        // 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
        // 强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
        // 本示例以把AccessKey ID和AccessKey Secret保存在环境变量为例说明。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
        // 例如：export MQTT_AK_ENV=<access_key_id>
        //     export MQTT_SK_ENV=<access_key_secret>
        // 需要将<access_key_id>替换为已准备好的AccessKey ID，<access_key_secret>替换为AccessKey Secret。
        String token = applyToken( System.getenv("MQTT_AK_ENV"), System.getenv("MQTT_SK_ENV"),
            resource, "R,W", 100000L, "XXXX", "cn-XXXXX");
        System.out.println(token);
    }


    /**
     * 申请 Token 接口，具体参数参考链接https://help.aliyun.com/document_detail/54276.html?spm=a2c4g.11186623.6.562.f12033f5ay6nu5
     *
     * @param accessKey 账号 AccessKey，由控制台获取
     * @param secretKey 账号 SecretKey，由控制台获取
     * @param topics 申请的 topic 列表
     * @param action Token类型
     * @param expireTime Token 过期的时间戳
     * @param instanceId MQ4IoT 实例 Id
     * @param regionId 当前操作的 regionId
     * @return 如果申请成功则返回 token 内容
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static String applyToken(String accessKey, String secretKey, List<String> topics,
        String action,
        long expireTime,
        String instanceId, String regionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, ClientException {
        Collections.sort(topics);
        StringBuilder builder = new StringBuilder();
        for (String topic : topics) {
            builder.append(topic).append(",");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        IAcsClient iAcsClient = getIAcsClient(accessKey, secretKey, regionId);
        ApplyTokenRequest request = new ApplyTokenRequest();
        request.setInstanceId(instanceId);
        request.setResources(builder.toString());
        request.setActions(action);
        request.setExpireTime(System.currentTimeMillis() + expireTime);
        ApplyTokenResponse response = iAcsClient.getAcsResponse(request);
        return response.getToken();
    }

    /**
     * 提前注销 token，一般在 token 泄露出现安全问题时，提前禁用特定的客户端
     * @param regionId 当前操作的 regionId
     * @param accessKey 账号 AccessKey，由控制台获取
     * @param secretKey 账号 SecretKey，由控制台获取
     * @param token 禁用的 token 内容
     * @param instanceId
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public static void revokeToken(String regionId, String accessKey, String secretKey,
        String token, String instanceId) throws InvalidKeyException, NoSuchAlgorithmException, ClientException {
        IAcsClient iAcsClient = getIAcsClient(accessKey, secretKey, regionId);
        RevokeTokenRequest request = new RevokeTokenRequest();
        request.setInstanceId(instanceId);
        request.setToken(token);
        iAcsClient.getAcsResponse(request);
    }
}
