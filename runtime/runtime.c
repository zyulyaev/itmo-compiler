#include <stdio.h>
#include <string.h>
#include <stdlib.h>

void rc_write(int val) {
  printf("%d\n", val);
}

int rc_read() {
  int val;
  printf("> ");
  scanf("%d", &val);
  return val;
}

char* rc_strmake(int length, char c) {
    char* buf = malloc(length + 1);
    memset(buf, c, length);
    buf[length] = 0;
    return buf;
}

char rc_strget(char* str, int index) {
    return str[index];
}

char* rc_strsub(char* str, int from, int length) {
    char* buf = malloc(length + 1);
    memcpy(buf, str + from, length);
    buf[length] = 0;
    return buf;
}

char* rc_strcat(char* a, char* b) {
    int aLen = strlen(a);
    int bLen = strlen(b);
    int tot = aLen + bLen;
    char* res = malloc(tot + 1);
    memcpy(res, a, aLen);
    memcpy(res + aLen, b, bLen);
    res[tot] = 0;
    return res;
}

int rc_strlen(char* str) {
    return strlen(str);
}

char* rc_strdup(char* str) {
    int length = strlen(str);
    char* res = malloc(length + 1);
    memcpy(res, str, length + 1);
    return res;
}

void rc_strset(char* str, int idx, char value) {
    str[idx] = value;
}

int rc_strcmp(char* a, char* b) {
    return strcmp(a, b);
}

typedef enum rc_type_t {
    SCALAR_ARRAY,
    POINTER_ARRAY,
    STRING
} rc_type;

typedef struct rc_array_header_t {
    rc_type type;
    int length;
} rc_array_header;

typedef struct rc_str_header_t {
    rc_type type;
    int length;
} rc_str_header;

static rc_array_header* _arrcrt(int length, rc_type type) {
    rc_array_header* header = malloc(length * 4 + sizeof(rc_array_header));
    header->type = type;
    header->length = length;
    return header;
}

static rc_array_header* _arrmake(int length, int value, rc_type type) {
    rc_array_header* header = _arrcrt(length, type);
    int* data = (int*) &header[1];
    for (int i = 0; i < length; i++) {
        data[i] = value;
    }
    return header;
}

void* rc_arrmake(int length, int value) {
    return _arrmake(length, value, SCALAR_ARRAY);
}

void* rc_Arrmake(int length, int value) {
    return _arrmake(length, value, POINTER_ARRAY);
}

void* rc_arrinit(void* src, int length) {
    rc_array_header* header = _arrcrt(length, SCALAR_ARRAY);
    memcpy(&header[1], src, length * 4);
    return header;
}

int rc_arrlen(rc_array_header* ptr) {
    return ptr->length;
}

void* rc_arrget(rc_array_header* ptr, int index) {
    return &((int*) &ptr[1])[index];
}