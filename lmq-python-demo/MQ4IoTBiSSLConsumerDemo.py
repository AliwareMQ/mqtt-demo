#!/usr/bin/env python
#coding=utf-8
from paho.mqtt.client import MQTT_LOG_INFO, MQTT_LOG_NOTICE, MQTT_LOG_WARNING, MQTT_LOG_ERR, MQTT_LOG_DEBUG
from paho.mqtt import client as mqtt
# 实例 ID，购买后从产品控制台获取
instanceId ='XXXX'

#账号AccessKey 从阿里云账号控制台获取
accessKey = 'XXXX'

#账号secretKey 从阿里云账号控制台获取
secretKey = 'XXXX'

#MQTT GroupID,创建实例后从 MQTT 控制台创建
groupId = 'GID_XXXX'

#MQTT ClientID，由 GroupID 和后缀组成，需要保证全局唯一
client_id=groupId+'@@@'+'XXXX'

# Topic， 其中第一级父级 Topic 需要从控制台创建
topic = 'XXXX'

#MQTT 接入点域名，实例初始化之后从控制台获取
brokerUrl='XXXX.mqtt.aliyuncs.com'


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
##ssl模式下
ca_certs = "XXXX/CA.crt"
certfile = "XXXX/client_chain.crt"
keyfile = "XXXX/client.key"
client.tls_set(ca_certs=ca_certs, certfile=certfile, keyfile=keyfile)

client.connect(brokerUrl, 8883, 60)
client.loop_forever()
