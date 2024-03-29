package com.aliyun.openservices.lmq.example.demo;

import com.aliyun.openservices.lmq.example.util.Tools;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;

/**
 * 本代码提供一个同步方式查询设备在线数量的示例，该功能详细文档参考链接。https://help.aliyun.com/document_detail/50069.html?spm=a2c4g.11186623.6.564.601825b7zlu9yv
 * 实际线上环境是否支持该功能请参考文档描述。
 */
public class QueryOnlineClientNumDemo {
    public static void main(
        String[] args) throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException, InvalidKeyException {
        /**
         * 查询请求的 URL，其中域名请填写实例 Id 的接入点域名
         */
        String url = "http://XXXXX.mqtt.aliyuncs.com/route/clientId/get";
        /**
         * 账号 accesskey，从账号系统控制台获取
         * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
         * 强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
         * 本示例以把AccessKey ID和AccessKey Secret保存在环境变量为例说明。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
         * 例如：export MQTT_AK_ENV=<access_key_id>
         *      export MQTT_SK_ENV=<access_key_secret>
         * 需要将<access_key_id>替换为已准备好的AccessKey ID，<access_key_secret>替换为AccessKey Secret。
         */
        String ak = System.getenv("MQTT_AK_ENV");
        /**
         * 账号 secretKey，从账号系统控制台获取，仅在Signature鉴权模式下需要设置
         */
        String sk = System.getenv("MQTT_SK_ENV");
        /**
         * 需要查询的客户端 clientId
         */
        String clientId = "GID_XXXX@@@XXXX";
        Map<String, String> params = new HashMap<String, String>();
        params.put("accessKey", ak);
        params.put("resource", clientId);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        /**
         * 客户端连接的实例 Id
         */
        params.put("instanceId", "XXXX");
        String signature = Tools.doHttpSignature(params, sk);
        params.put("signature", signature);
        System.out.println(Tools.httpsGet(url, params));
    }
}
