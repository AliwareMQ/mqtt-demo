using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using uPLibrary.Networking.M2Mqtt;
using uPLibrary.Networking.M2Mqtt.Messages;
namespace MQTTDemo
{
    class MQTTDoNetDemo
    {
        static void Main(string[] args)
        {
            //实例 ID，购买后从控制台获取
            String instanceId = "XXXXX";
            //此处填写购买得到的 MQTT 接入点域名
            String brokerUrl = "XXX.mqtt.aliyuncs.com";
            //此处填写阿里云帐号 AccessKey
            //账号 accesskey，从账号系统控制台获取
            //阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
            //强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
            //可以把AccessKey ID和AccessKey Secret保存在环境变量。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
            //例如：export MQTT_AK_ENV=<access_key_id>
            //     export MQTT_SK_ENV=<access_key_secret>
            //需要将<access_key_id>替换为已准备好的AccessKey ID，<access_key_secret>替换为AccessKey Secret。通过环境来获取accessKey，secretKey
            String accessKey = "XXXXXX";
            //此处填写阿里云帐号 SecretKey
            String secretKey = "XXXXXX";
            //此处填写在 MQ 控制台创建的 Topic，作为 MQTT 的一级 Topic
            String parentTopic = "XXXXX";
            //此处填写客户端 ClientId，需要保证全局唯一，其中前缀部分即 GroupId 需要先在 MQ 控制台创建
            String clientId = "GID_XXXX@@@XXXX";
            MqttClient client = new MqttClient(brokerUrl);
            client.MqttMsgPublishReceived += client_recvMsg;
            client.MqttMsgPublished += client_publishSuccess;
            client.ConnectionClosed += client_connectLose;
            //username和 Password 签名模式下的设置方法，参考文档 https://help.aliyun.com/document_detail/48271.html?spm=a2c4g.11186623.6.553.217831c3BSFry7
            String userName = "Signature|"+accessKey+"|"+instanceId;
            String passWord = HMACSHA1(secretKey, clientId);
            client.Connect(clientId, userName,passWord,true,60);
            //订阅 Topic，支持多个 Topic，以及多级 Topic
            string[] subTopicArray = { parentTopic+"/subDemo1", parentTopic+"/subDemo2/level3" };
            byte[] qosLevels = { MqttMsgBase.QOS_LEVEL_AT_MOST_ONCE, MqttMsgBase.QOS_LEVEL_AT_MOST_ONCE };
            client.Subscribe(subTopicArray, qosLevels);
            client.Publish(parentTopic + "/subDemo1", Encoding.UTF8.GetBytes("hello mqtt"),MqttMsgBase.QOS_LEVEL_AT_LEAST_ONCE,false);
            //发送 P2P 消息，二级 topic 必须是 p2p,三级 topic 是接收客户端的 clientId
            client.Publish(parentTopic + "/p2p/"+clientId, Encoding.UTF8.GetBytes("hello mqtt"), MqttMsgBase.QOS_LEVEL_AT_LEAST_ONCE, false);
            System.Threading.Thread.Sleep(10000);
            client.Disconnect();
        }
        static void client_recvMsg(object sender, MqttMsgPublishEventArgs e)
        {
            // access data bytes throug e.Message
            Console.WriteLine("Recv Msg : Topic is "+e.Topic+" ,Body is "+Encoding.UTF8.GetString(e.Message));
        }
        static void client_publishSuccess(object sender, MqttMsgPublishedEventArgs e)
        {
            // access data bytes throug e.Message
            Console.WriteLine("Publish Msg  Success");
        }
        static void client_connectLose(object sender, EventArgs e)
        {
            // access data bytes throug e.Message
            Console.WriteLine("Connect Lost,Try Reconnect");
        }
        public static string HMACSHA1(string key, string dataToSign)
        {
            Byte[] secretBytes = UTF8Encoding.UTF8.GetBytes(key);
            HMACSHA1 hmac = new HMACSHA1(secretBytes);
            Byte[] dataBytes = UTF8Encoding.UTF8.GetBytes(dataToSign);
            Byte[] calcHash = hmac.ComputeHash(dataBytes);
            String calcHashString = Convert.ToBase64String(calcHash);
            return calcHashString;
        }
    }
}