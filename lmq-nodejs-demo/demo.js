
// 开源sdk地址：https://github.com/mqttjs/MQTT.js
var mqtt = require('mqtt')
var CryptoJS = require("crypto-js");

//账号 accesskey，从账号系统控制台获取
//阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
//强烈建议不要把AccessKey ID和AccessKey Secret保存到工程代码里，否则可能导致AccessKey泄露，威胁您账号下所有资源的安全。
//可以把AccessKey ID和AccessKey Secret保存在环境变量。运行本代码示例之前，请先配置环境变量MQTT_AK_ENV和MQTT_SK_ENV
//例如：export MQTT_AK_ENV=access_key_id
//     export MQTT_SK_ENV=access_key_secret
//需要将access_key_id替换为已准备好的AccessKey ID，access_key_secret替换为AccessKey Secret
var accessKey='xxxx'
//账号的的 SecretKey，在阿里云控制台查看
var secretKey='xxxx'

var clientId = 'xxxx'
var instanceId='xxxx'

// https://help.aliyun.com/document_detail/48271.html
var username = 'Signature|' + accessKey + '|' + instanceId;
var password = CryptoJS.HmacSHA1(clientId, secretKey).toString(CryptoJS.enc.Base64);

var options={
	'username':username,
	'password':password,
	'clientId':clientId,
    	'keepalive':90,
    	'connectTimeout': 3000
}

//tls安全连接："tls://host:8883"
var client  = mqtt.connect('tcp://xxxx:1883',options)

var topic='xxxxx'

client.on('connect', function () {
  client.subscribe(topic, {'qos':1})
})

client.on('message', function (topic, message) {
  console.log('topic:'+topic+' msg:'+message.toString())
})

var i=0
setInterval(function(){
	client.publish(topic, 'Hello mqtt ' + (i++))
	client.publish(topic + '/p2p/' + clientId, 'Hello mqtt ' + (i++))
},1000)



