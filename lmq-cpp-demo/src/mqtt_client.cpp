#include <WiFi.h>
#include <PubSubClient.h>
#include "csha1.h"
#include "cbase64.h"

/*
 * 适用于：阿里云微消息服务队列mqtt版
 * 测试芯片: ESP32
 * https://github.com/quarkape/ali-mqtt 上面有一个配套的非常简单的小程序demo，可以通过小程序发送消息控制ESP32的LED亮灭
 */

// Wi-Fi名称
const char* SSID = "";
// Wi-Fi密码
const char* WIFI_PASSWORD = "";
// MQTT服务器接入点，如 mqtt-cn-zxu35xmti02.mqtt.aliyuncs.com
const char* MQTT_SERVER = "";
// 当前主题
const char* TOPIC = "xfsystem";
// 设备ID，如 GID_ecnu_smartlib@@@001，根据官方要求，比如使用GID_groupID@@@deviceID的格式
const char* CLIENT_ID = "GID_test@@@esp";
// 实例ID，可以在相关服务中查到，如 mqtt-cn-zxu35xmti02
const String INSTANCE_ID = "";
// accessKey，需要在阿里云AccessKey管理页面创建
const String ACCESS_KEY = "";
// secretKey，创建accessKey时会生成
const String SECRET_KEY = "";
// 连接端口，默认1883
const uint16_t PORT = 1883;

// 加密
const String get_password(const char* data_text, const String secretKey);
const String mqtt_passWord = get_password(CLIENT_ID, SECRET_KEY);

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;

const String get_password(const char* data_text, const String secretKey) {

  unsigned char out_bytes[50];
  int secretKeyLength, data_length;
  char base64Char[50];
  int hmac_len = 0;

  secretKeyLength = strlen(secretKey.c_str());
  data_length = strlen(data_text);

  memset(out_bytes, '\0', sizeof(out_bytes));
  hmac_len = hmac_sha(secretKey.c_str(), secretKeyLength, data_text, data_length, out_bytes, 20);

  memset(base64Char, '\0', sizeof(base64Char));
  cbase64_encode((const char*) out_bytes, hmac_len, base64Char);
  const String ret_data(base64Char);
  return ret_data;

}

void setup_wifi() {

  delay(10);
  Serial.println();
  Serial.print("CONNECTING TO WiFi ");
  Serial.println(SSID);

  WiFi.begin(SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("==> WiFi CONNECTED.");
  // Serial.println("IP address: ");
  // Serial.println(WiFi.localIP());

}

// 接收到消息的回调函数
void onReceiveMessage(char* topic, byte* payload, unsigned int length) {

  Serial.print("======MESSAGE FROM [");
  Serial.print(topic);
  Serial.print("] ======");
  Serial.print("");

  for (int i = 0; i < length; i++) {
    Serial.print((char) payload[i]);
  }
  Serial.println();

  // 接收消息1，亮灯；0，灭灯
  if ((char) payload[0] == '1') {
    digitalWrite(BUILTIN_LED, HIGH);
  } else if((char) payload[0] == '0')  {
    digitalWrite(BUILTIN_LED, LOW);
  } else {
    Serial.println("LED STATUS NOT CHANGED.");
  }

}

void connect2MQTTServer() {

  while (!client.connected()) {
    Serial.print("CONNECTING TO MQTT SERVER...");

    if (client.connect(CLIENT_ID, (String("Signature|") + ACCESS_KEY + "|" + INSTANCE_ID).c_str(), mqtt_passWord.c_str())) {
      Serial.println("");
      Serial.println("==> MQTT SERVER CONNECTED.");
      if (client.subscribe(TOPIC)) {
        Serial.println("");
        Serial.println("==> SUBSCRIB TOPIC SUCCEED.");
      } else {
        Serial.println("");
        Serial.println("==> SUBSCRIB TOPIC FAILED.");
      }
    } else {
      Serial.println("");
      Serial.println("==> MQTT SERVER CONNECTION FAILED. CODE: ");
      Serial.print(client.state());
      Serial.println("TRY AGAIN IN 5S");
      delay(5000);
    }
  }

}

void setup() {
  pinMode(BUILTIN_LED, OUTPUT);
  Serial.begin(115200); 
  setup_wifi();
  client.setServer(MQTT_SERVER, PORT);
  client.setCallback(onReceiveMessage);
}

void loop() {

  if (!client.connected()) {
    connect2MQTTServer();
  }
  client.loop();

  unsigned char result[1024];
  memset(result, '\0', sizeof(result));
  while(Serial.available())
  {
    Serial.readBytes(result, 1024);
    client.publish(TOPIC, (const char*) result);
  }

}
