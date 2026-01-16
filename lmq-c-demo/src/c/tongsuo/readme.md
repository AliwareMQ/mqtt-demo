# Tongsuo 适配版 Paho MQTT C SDK

## SDK 源码修改

paho.mqtt.c 的 tongsuo 适配版：https://github.com/AliwareMQ/paho.mqtt.c

## 前提条件

假设 Tongsuo 已安装在 `/home/admin/tongsuo`

## SDK 源码编译

```bash
git clone git@github.com:AliwareMQ/paho.mqtt.c.git
cd paho.mqtt.c
mkdir build && cd build

cmake -DPAHO_WITH_SSL=TRUE \
      -DPAHO_WITH_TONGSUO_NTLS=ON \
      -DOPENSSL_ROOT_DIR=/home/admin/tongsuo \
      ..

make
sudo make install
```

## Demo 源码编译

```bash
cd lmq-c-demo/src/c/tongsuo
mkdir build && cd build

cmake ..
make
```

## Demo 运行

### 设置环境变量

```bash
export LD_LIBRARY_PATH=/home/admin/tongsuo/lib64:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=/usr/local/lib64:/usr/local/lib:$LD_LIBRARY_PATH
```

### 运行 Demo

```bash
./mqttDemo
```