package com.aliyun.openservices.lmq.example.demo;

import com.aliyun.openservices.lmq.example.util.ConnectionOptionWrapper;
import com.aliyun.openservices.lmq.example.util.SignMode;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 本代码提供自定义鉴权的签名（SIGNED）模式下 MQ4IOT 客户端发送消息到 MQ4IOT 客户端的示例，其中初始化参数请根据实际情况修改
 * 自定义鉴权即使用用户自己提供的 UserName 和 Secret 对每个客户端计算出一个独立的签名供客户端识别使用。
 * 对于实际业务场景使用过程中，考虑到秘钥 Secret 的隐私性，可以将签名过程放在受信任的环境完成。
 * 完整 demo 工程，参考https://github.com/AliwareMQ/lmq-demo
 */
public class CustomAuthIdentityModeUseSigned {
    public static void main(String[] args) throws Exception {
        /**
         * MQ4IOT 实例 ID，购买后控制台获取
         */
        String instanceId = "XXXXX";
        /**
         * 接入点地址，购买 MQ4IOT 实例，且配置完成后即可获取，接入点地址必须填写分配的域名，不得使用 IP 地址直接连接，否则可能会导致客户端异常。
         */
        String endPoint = "XXXXX.mqtt.aliyuncs.com";
        /**
         * 自定义用户名 username，即调用添加账号身份认证信息时填写的username
         */
        String username = "XXXXX";
        /**
         * 秘钥 secret，即调用添加账号身份认证信息时填写的secret
         */
        String secret = "XXXXX";
        /**
         * MQ4IOT clientId，由业务系统分配，需要保证每个 tcp 连接都不一样，保证全局唯一，如果不同的客户端对象（tcp 连接）使用了相同的 clientId 会导致连接异常断开。
         * 在自定义鉴权中 client格式为 xxxxxx，即clientId可以任意填写，在Group降级时，一些控制台功能比如设备查询、设备轨迹查询暂且也被降级不能使用
         */
        String clientId = "XXXXX";
        /**
         * MQ4IOT 消息的一级 topic，需要在控制台申请才能使用。
         * 如果使用了没有申请或者没有被授权的 topic 会导致鉴权失败，服务端会断开客户端连接。
         * 在自定义鉴权中 如果该topic是第一次使用的话 需要调用接口添加topic资源授权信息才能正常使用 具体参考https://help.aliyun.com/zh/apsaramq-for-mqtt/developer-reference/api-onsmqtt-2020-04-20-addcustomauthpermission?spm=a2c4g.11186623.0.0.5dcc74c5U9nMNL
         */
        final String parentTopic = "XXXXX";
        /**
         * MQ4IOT支持子级 topic，用来做自定义的过滤，此处为示意，可以填写任何字符串，具体参考https://help.aliyun.com/document_detail/42420.html?spm=a2c4g.11186623.6.544.1ea529cfAO5zV3
         * 需要注意的是，完整的 topic 参考 https://help.aliyun.com/document_detail/63620.html?spm=a2c4g.11186623.6.554.21a37f05ynxokW。
         */
        final String mq4IotTopic = parentTopic + "/" + "testMq4Iot";
        /**
         * QoS参数代表传输质量，可选0，1，2，根据实际需求合理设置，具体参考 https://help.aliyun.com/document_detail/42420.html?spm=a2c4g.11186623.6.544.1ea529cfAO5zV3
         */
        final int qosLevel = 0;
        /**
         * 当签名模式为SIGNED
         */
        ConnectionOptionWrapper connectionOptionWrapper = new ConnectionOptionWrapper(instanceId, username, secret, clientId, SignMode.SIGNED);
        final MemoryPersistence memoryPersistence = new MemoryPersistence();
        /**
         * 客户端使用的协议和端口必须匹配，具体参考文档 https://help.aliyun.com/document_detail/44866.html?spm=a2c4g.11186623.6.552.25302386RcuYFB
         * 如果是 SSL 加密则设置ssl://endpoint:8883
         */
        final MqttClient mqttClient = new MqttClient("tcp://" + endPoint + ":1883", clientId, memoryPersistence);
        /**
         * 客户端设置好发送超时时间，防止无限阻塞
         */
        mqttClient.setTimeToWait(5000);
        final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                /**
                 * 客户端连接成功后就需要尽快订阅需要的 topic
                 */
                System.out.println("connect success");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                /**
                 * 消费消息的回调接口，需要确保该接口不抛异常，该接口运行返回即代表消息消费成功。
                 * 消费消息需要保证在规定时间内完成，如果消费耗时超过服务端约定的超时时间，对于可靠传输的模式，服务端可能会重试推送，业务需要做好幂等去重处理。超时时间约定参考限制
                 * https://help.aliyun.com/document_detail/63620.html?spm=a2c4g.11186623.6.546.229f1f6ago55Fj
                 */
                System.out.println(
                        "receive msg from topic " + s + " , body is " + new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println("send msg succeed topic is : " + iMqttDeliveryToken.getTopics()[0]);
            }
        });
        mqttClient.connect(connectionOptionWrapper.getMqttConnectOptions());
        for (int i = 0; i < 10; i++) {
            MqttMessage message = new MqttMessage("hello mq4Iot pub sub msg".getBytes());
            message.setQos(qosLevel);
            /**
             *  发送普通消息时，topic 必须和接收方订阅的 topic 一致，或者符合通配符匹配规则
             */
            mqttClient.publish(mq4IotTopic, message);
            /**
             * MQ4IoT支持点对点消息，即如果发送方明确知道该消息只需要给特定的一个设备接收，且知道对端的 clientId，则可以直接发送点对点消息。
             * 点对点消息不需要经过订阅关系匹配，可以简化订阅方的逻辑。点对点消息的 topic 格式规范是  {{parentTopic}}/p2p/{{targetClientId}}
             */
            String receiverId = "xxx";
            final String p2pSendTopic = parentTopic + "/p2p/" + receiverId;
            message = new MqttMessage("hello mq4Iot p2p msg".getBytes());
            message.setQos(qosLevel);
            mqttClient.publish(p2pSendTopic, message);
        }
        Thread.sleep(Long.MAX_VALUE);
    }
}
