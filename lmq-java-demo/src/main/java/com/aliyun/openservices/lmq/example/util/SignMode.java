package com.aliyun.openservices.lmq.example.util;

/**
 * 调用AddCustomAuthIdentity添加身份认证时选择的签名方式SignMode
 *
 */
public enum SignMode {

    ORIGIN("ORIGIN", "原始值比较，即直接比较password 与 secret"),
    SIGNED("SIGNED", "对clientId进行HmacSHA1加签（使用 secret，参考签名模式）验证，然后比较password");

    private String value;
    private String desc;

    SignMode(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SignMode valuesOf(String value) {
        for (SignMode temp : SignMode.values()) {
            if (temp.getValue() == value) {
                return temp;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
