#pragma once

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

#endif