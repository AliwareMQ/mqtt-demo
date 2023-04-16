#pragma once
//
//  cbase64.h
//  cbase64
//
//  Created by guofu on 2017/5/25.
//  Copyright © 2017年 guofu. All rights reserved.
//

#ifndef cbase64_h
#define cbase64_h

#include <stdio.h>

#if __cplusplus
extern "C" {
#endif

    char* cbase64_encode(const char* buf, const long size, char* base64Char);
    char* cbase64_decode(const char* base64Char, const long base64CharSize, char* originChar, long originCharSize);

#if __cplusplus
}
#endif

#endif /* base64_h */