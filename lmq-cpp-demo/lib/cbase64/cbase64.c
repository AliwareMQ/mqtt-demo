//
//  cbase64.c
//  cbase64
//
//  Created by guofu on 2017/5/25.
//  Copyright © 2017年 guofu. All rights reserved.
//
/**
 *  转解码过程
 *  3 * 8 = 4 * 6; 3字节占24位, 4*6=24
 *  先将要编码的转成对应的ASCII值
 *  如编码: s 1 3
 *  对应ASCII值为: 115 49 51
 *  对应二进制为: 01110011 00110001 00110011
 *  将其6个分组分4组: 011100 110011 000100 110011
 *  而计算机是以8bit存储, 所以在每组的高位补两个0如下:
 *  00011100 00110011 00000100 00110011对应:28 51 4 51
 *  查找base64 转换表 对应 c z E z
 *
 *  解码
 *  c z E z
 *  对应ASCII值为 99 122 69 122
 *  对应表base64_suffix_map的值为 28 51 4 51
 *  对应二进制值为 00011100 00110011 00000100 00110011
 *  依次去除每组的前两位, 再拼接成3字节
 *  即: 01110011 00110001 00110011
 *  对应的就是s 1 3
 */

#include "cbase64.h"

#include <stdio.h>
#include <stdlib.h>

static const char* ALPHA_BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";


char* cbase64_encode(const char* buf, const long size, char* base64Char) {
    int a = 0;
    int i = 0;
    while (i < size) {
        char b0 = buf[i++];
        char b1 = (i < size) ? buf[i++] : 0;
        char b2 = (i < size) ? buf[i++] : 0;

        int int63 = 0x3F; //  00111111
        int int255 = 0xFF; // 11111111
        base64Char[a++] = ALPHA_BASE[(b0 >> 2) & int63];
        base64Char[a++] = ALPHA_BASE[((b0 << 4) | ((b1 & int255) >> 4)) & int63];
        base64Char[a++] = ALPHA_BASE[((b1 << 2) | ((b2 & int255) >> 6)) & int63];
        base64Char[a++] = ALPHA_BASE[b2 & int63];
    }
    switch (size % 3) {
    case 1:
        base64Char[--a] = '=';
    case 2:
        base64Char[--a] = '=';
    }
    return base64Char;
}

char* cbase64_decode(const char* base64Char, const long base64CharSize, char* originChar, long originCharSize) {
    int toInt[128] = { -1 };
    for (int i = 0; i < 64; i++) {
        toInt[ALPHA_BASE[i]] = i;
    }
    int int255 = 0xFF;
    int index = 0;
    for (int i = 0; i < base64CharSize; i += 4) {
        int c0 = toInt[base64Char[i]];
        int c1 = toInt[base64Char[i + 1]];
        originChar[index++] = (((c0 << 2) | (c1 >> 4)) & int255);
        if (index >= originCharSize) {
            return originChar;
        }
        int c2 = toInt[base64Char[i + 2]];
        originChar[index++] = (((c1 << 4) | (c2 >> 2)) & int255);
        if (index >= originCharSize) {
            return originChar;
        }
        int c3 = toInt[base64Char[i + 3]];
        originChar[index++] = (((c2 << 6) | c3) & int255);
    }
    return originChar;
}