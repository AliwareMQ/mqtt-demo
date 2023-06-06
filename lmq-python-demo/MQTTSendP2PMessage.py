#!/usr/bin/env python
#coding=utf-8
import hmac
import base64
import os
from hashlib import sha1
import time
from paho.mqtt.client import MQTT_LOG_INFO, MQTT_LOG_NOTICE, MQTT_LOG_WARNING, MQTT_LOG_ERR, MQTT_LOG_DEBUG
from paho.mqtt import client as mqtt
# 实例 ID，购买后从产品控制台获取
instanceId ='XXXX'

##此处填写阿里云帐号 AccessKey
##账号 accesskey，从账号系统控制台获取
##阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
##强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
##本示例以把AccessKey ID和AccessKey Secret保存在环境变量为例说明。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
##例如：export MQTT_AK_ENV=access_key_id
##     export MQTT_SK_ENV=access_key_secret
##需要将access_key_id替换为已准备好的AccessKey ID，access_key_secret替换为AccessKey Secret
accessKey = os.getenv('MQTT_AK_ENV')

#账号secretKey 从阿里云账号控制台获取
secretKey = os.getenv('MQTT_SK_ENV')

#MQTT GroupID,创建实例后从 MQTT 控制台创建
groupId = 'GID_XXXX'

#MQTT ClientID，由 GroupID 和后缀组成，需要保证全局唯一
client_id=groupId+'@@@'+'XXXXX'

# Topic， 其中第一级父级 Topic 需要从控制台创建
topic = 'XXXXX'

#MQTT 接入点域名，实例初始化之后从控制台获取
brokerUrl='XXXXXX.mqtt.aliyuncs.com'


def on_log(client, userdata, level, buf):
    if level == MQTT_LOG_INFO:
        head = 'INFO'
    elif level == MQTT_LOG_NOTICE:
        head = 'NOTICE'
    elif level == MQTT_LOG_WARNING:
        head = 'WARN'
    elif level == MQTT_LOG_ERR:
        head = 'ERR'
    elif level == MQTT_LOG_DEBUG:
        head = 'DEBUG'
    else:
        head = level
    print('%s: %s' % (head, buf))
def on_connect(client, userdata, flags, rc):
    print('Connected with result code ' + str(rc))
    client.subscribe(topic, 0)
    for i in range(1, 11):
        print(i)
        ## 发送 P2P 消息，二级子 topic 是/p2p/ 三级子 topic 是目标 clientId
        rc = client.publish(topic + '/p2p/'+ client_id, str(i), qos=0)
        print ('rc: %s' % rc)
        time.sleep(0.1)
def on_message(client, userdata, msg):
    print(msg.topic + ' ' + str(msg.payload))
def on_disconnect(client, userdata, rc):
    if rc != 0:
        print('Unexpected disconnection %s' % rc)
client = mqtt.Client(client_id, protocol=mqtt.MQTTv311, clean_session=True)
client.on_log = on_log
client.on_connect = on_connect
client.on_message = on_message
client.on_disconnect = on_disconnect
## username和 Password 签名模式下的设置方法，参考文档 https://help.aliyun.com/document_detail/48271.html?spm=a2c4g.11186623.6.553.217831c3BSFry7
userName ='Signature'+'|'+accessKey+'|'+instanceId;
password = base64.b64encode(hmac.new(secretKey.encode(), client_id.encode(), sha1).digest()).decode()
client.username_pw_set(userName, password)
# ssl设置，并且port=8883
#client.tls_set(ca_certs=None, certfile=None, keyfile=None, cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLS, ciphers=None)
client.connect(brokerUrl, 1883, 60)
client.loop_forever()
