cmake_minimum_required(VERSION 3.7)
project(mqttdemo C)

set(CMAKE_C_STANDARD 99)

# ===== Tongsuo / OpenSSL 路径，根据实际情况修改 =====
set(TONGSUO_ROOT "/home/admin/tongsuo")          # 如果你装在别处，这里改掉
link_directories(${TONGSUO_ROOT}/lib64)

include_directories(
    .
    ${CMAKE_SOURCE_DIR}/src
    ${CMAKE_BINARY_DIR}
)

set(CMAKE_BUILD_TYPE "Debug")
set(CMAKE_C_FLAGS_DEBUG   "$ENV{CFLAGS} -O0 -Wall -g2 -ggdb")
set(CMAKE_C_FLAGS_RELEASE "$ENV{CFLAGS} -O3 -Wall")

add_executable(mqttDemo tongsuoDemo.c)

# 一次性把需要的库都链上：先 paho-mqtt3as，再 ssl/crypto
target_link_libraries(mqttDemo
    paho-mqtt3as     # 异步 + SSL 的 Paho 库
    ssl              # Tongsuo / OpenSSL 的 libssl.so.3
    crypto           # Tongsuo / OpenSSL 的 libcrypto.so.3
)

# 运行时 rpath，让程序自己能找到 libssl.so.3 / libcrypto.so.3（可选但推荐）
set_target_properties(mqttDemo PROPERTIES
    BUILD_RPATH "${TONGSUO_ROOT}/lib64"
    INSTALL_RPATH "${TONGSUO_ROOT}/lib64"
)