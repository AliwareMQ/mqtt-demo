<?php

use Mosquitto\Client;

##此处填写阿里云帐号 AccessKey
##账号 accesskey，从账号系统控制台获取
##阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
##强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
##可以把AccessKey ID和AccessKey Secret保存在环境变量。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
##例如：export MQTT_AK_ENV=access_key_id
##     export MQTT_SK_ENV=access_key_secret
##需要将access_key_id替换为已准备好的AccessKey ID，access_key_secret替换为AccessKey Secret
$accessKey = 'XXXX';
##此处填写阿里云帐号 SecretKey
$secretKey = 'XXXX';
## 接入点地址，购买实例后从控制台获取
$endPoint = 'XXXX.mqtt.aliyuncs.com';
##实例 ID，购买后从控制台获取
$instanceId = 'XXXX';
## MQTT Topic,其中第一级 Topic 需要在 MQTT 控制台提前申请
$topic = 'XXXX';
## MQTT 客户端ID 前缀， GroupID，需要在 MQTT 控制台申请
$groupId = 'GID_XXXX';
## MQTT 客户端ID 后缀，DeviceId，业务方自由指定，需要保证全局唯一，禁止 2 个客户端连接使用同一个 ID
$deviceId = 'XXXX';
$qos = 0;
$port = 1883;
$keepalive = 90;
$cleanSession = true;
$clientId = $groupId . '@@@' . $deviceId;
echo $clientId . "\n";

$mid = 0;
## 初始化客户端，需要设置 clientId 和 CleanSession 参数，参考官网文档规范
$mqttClient = new Mosquitto\Client($clientId, $cleanSession);


## 设置鉴权参数，参考 MQTT 客户端鉴权代码计算 username 和 password
$username = 'Token|' . $accessKey . '|' . $instanceId;
## Token模式，token 需要提前向自己的应用服务提供方申请，此处以读写权限类型的 token 为例，token内容以 xxx 为例。
$tokenStr = 'xxx';
$password = 'RW|' . $tokenStr;
echo "UserName:" . $username . "  Password:" . $password . "\n";
$mqttClient->setCredentials($username, $password);

## 设置连接成功回调
$mqttClient->onConnect(function ($rc, $message) use ($mqttClient, &$mid, $topic, $qos) {
    echo "Connnect to Server Code is " . $rc . " message is " . $message . "\n";
    $mqttClient->subscribe($topic, $qos);
});


## 设置订阅成功回调
$mqttClient->onSubscribe(function () use ($mqttClient, $topic, $qos) {
    $mqttClient->publish($topic, "Hello MQTT PHP Demo", $qos);
});

## 设置发送成功回调
$mqttClient->onPublish(function ($publishedId) use ($mqttClient, $mid) {
    echo "publish message success " . $mid . "\n";
});


## 设置消息接收回调
$mqttClient->onMessage(function ($message) {
    echo "Receive Message From mqtt, topic is " . $message->topic . "  qos is " . $message->qos . "  messageId is " . $message->mid . "  payload is " . $message->payload . "\n";

});
$mqttClient->connect($endPoint, $port, $keepalive);


$mqttClient->loopForever();

echo "Finished";