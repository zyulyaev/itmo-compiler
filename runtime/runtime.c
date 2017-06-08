#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct rc_array_header_t {
    int ref_counter;
    int length;
} rc_array_header;

typedef struct rc_header_t {
    int ref_counter;
} rc_header;

void rc_write(int val) {
  printf("%d\n", val);
}

int rc_read() {
  int val;
  printf("> ");
  scanf("%d", &val);
  return val;
}

static rc_header* _strmake(int length) {
    int header_size = sizeof(rc_header);
    rc_header* header = malloc(header_size + length + 1);
    char* chars = (char*) &header[1];
    chars[length] = 0;
    return header;
}

rc_header* rc_strmake(int length, char c) {
    rc_header* header = _strmake(length);
    memset(&header[1], c, length);
    return header;
}

rc_header* rc_strinit(int length, char* chars) {
    rc_header* header = _strmake(length);
    memcpy((char*) &header[1], chars, length);
    return header;
}

char rc_strget(rc_header* header, int index) {
    return ((char*) &header[1])[index];
}

rc_header* rc_strsub(rc_header* header, int from, int length) {
    char* chars = (char*) &header[1];
    rc_header* result = _strmake(length);
    memcpy(&result[1], chars + from, length);
    return result;
}

rc_header* rc_strcat(rc_header* a, rc_header* b) {
    char* left = (char*) &a[1];
    char* right = (char*) &b[1];
    int leftLen = strlen(left);
    int rightLen = strlen(right);
    rc_header* result = _strmake(leftLen + rightLen);
    char* chars = (char*) &result[1];
    memcpy(chars, left, leftLen);
    memcpy(chars + leftLen, right, rightLen);
    return result;
}

int rc_strlen(rc_header* str) {
    return strlen((char*) &str[1]);
}

rc_header* rc_strdup(rc_header* str) {
    int length = rc_strlen(str);
    rc_header* result = _strmake(length);
    memcpy(&result[1], &str[1], length);
    return result;
}

void rc_strset(rc_header* str, int idx, char value) {
    char* chars = (char*) &str[1];
    chars[idx] = value;
}

int rc_strcmp(rc_header* a, rc_header* b) {
    return strcmp((char*) &a[1], (char*) &b[1]);
}

static rc_array_header* _arrcrt(int length) {
    rc_array_header* header = malloc(length * 4 + sizeof(rc_array_header));
    header->length = length;
    return header;
}

static rc_array_header* _arrmake(int length, int value) {
    rc_array_header* header = _arrcrt(length);
    int* data = (int*) &header[1];
    for (int i = 0; i < length; i++) {
        data[i] = value;
    }
    return header;
}

void* rc_arrmake(int length, int value) {
    return _arrmake(length, value);
}

void* rc_Arrmake(int length, int value) {
    return _arrmake(length, value);
}

void* rc_arrinit(void* src, int length) {
    rc_array_header* header = _arrcrt(length);
    memcpy(&header[1], src, length * 4);
    return header;
}

int rc_arrlen(rc_array_header* ptr) {
    return ptr->length;
}