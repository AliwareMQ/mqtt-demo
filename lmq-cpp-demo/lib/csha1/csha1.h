#pragma once

#ifndef CSHA1_H_

#define CSHA1_H_

#if __cplusplus
extern "C" {
#endif

typedef unsigned long    __u32;
typedef unsigned char    __u8;

typedef struct
{
    __u32 state[5];
    __u32 count[2];
    __u8  buffer[64];
} SHA1_CTX;

#if defined(rol)
#undef rol
#endif

#define SHA1HANDSOFF

#define __LITTLE_ENDIAN 

#define rol(value, bits) (((value) << (bits)) | ((value) >> (32 - (bits))))

/* blk0() and blk() perform the initial expand. */
/* I got the idea of expanding during the round function from SSLeay */
#ifdef __LITTLE_ENDIAN
#define blk0(i) (block->l[i] = (rol(block->l[i],24)&0xFF00FF00) \
    |(rol(block->l[i],8)&0x00FF00FF))
#else
#define blk0(i) block->l[i]
#endif
#define blk(i) (block->l[i&15] = rol(block->l[(i+13)&15]^block->l[(i+8)&15] \
    ^block->l[(i+2)&15]^block->l[i&15],1))

/* (R0+R1), R2, R3, R4 are the different operations used in SHA1 */
#define R0(v,w,x,y,z,i) z+=((w&(x^y))^y)+blk0(i)+0x5A827999+rol(v,5);w=rol(w,30);
#define R1(v,w,x,y,z,i) z+=((w&(x^y))^y)+blk(i)+0x5A827999+rol(v,5);w=rol(w,30);
#define R2(v,w,x,y,z,i) z+=(w^x^y)+blk(i)+0x6ED9EBA1+rol(v,5);w=rol(w,30);
#define R3(v,w,x,y,z,i) z+=(((w|x)&y)|(w&x))+blk(i)+0x8F1BBCDC+rol(v,5);w=rol(w,30);
#define R4(v,w,x,y,z,i) z+=(w^x^y)+blk(i)+0xCA62C1D6+rol(v,5);w=rol(w,30);


/* Hash a single 512-bit block. This is the core of the algorithm. */

void __SHA1Transform(__u32 state[5], __u8 buffer[64]);
void __SHA1Init(SHA1_CTX* context);
void __SHA1Update(SHA1_CTX* context, unsigned char* data, __u32 len);
void __SHA1Final(unsigned char digest[20], SHA1_CTX* context);

//返回结果hmac_sha1处理后的bytes长度
int hmac_sha
(
    const char* secretKey,    /* 秘钥 secret key */
    int secretKeyLength,     /*  秘钥长度 length of the key in bytes */
    const char* data_text,    /* 数据 data */
    int data_length,     /*  数据长度 length of data in bytes */
    unsigned char* out_bytes,  /* 输出的字符串 output buffer, at least "t" bytes */
    int t
);

#if __cplusplus
}
#endif

#endif /* CSHA1_H_ */
