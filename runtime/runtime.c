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

typedef void (*destructor_t)(void*);

typedef struct rc_vtable_header_t {
    destructor_t destructor;
} rc_vtable_header;

typedef struct rc_fat_ptr_t {
    rc_header* data;
    rc_vtable_header* vtable;
} rc_fat_ptr;

void* rc_malloc(int size) {
    fprintf(stderr, "malloc\n");
    return malloc(size);
}

void rc_free(void* ptr) {
    fprintf(stderr, "free\n");
    free(ptr);
}

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
    rc_header* header = rc_malloc(header_size + length + 1);
    header->ref_counter = 1;
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

static void _dec_rc_str(rc_header* str) {
    str->ref_counter -= 1;
    if (str->ref_counter == 0) {
        rc_free(str);
    }
}

char rc_strget(rc_header* header, int index) {
    char result = ((char*) &header[1])[index];
    _dec_rc_str(header);
    return result;
}

rc_header* rc_strsub(rc_header* header, int from, int length) {
    char* chars = (char*) &header[1];
    rc_header* result = _strmake(length);
    memcpy(&result[1], chars + from, length);
    _dec_rc_str(header);
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
    _dec_rc_str(a);
    _dec_rc_str(b);
    return result;
}

int rc_strlen(rc_header* str) {
    int length = strlen((char*) &str[1]);
    _dec_rc_str(str);
    return length;
}

rc_header* rc_strdup(rc_header* str) {
    int length = strlen((char*) &str[1]);
    rc_header* result = _strmake(length);
    memcpy(&result[1], &str[1], length);
    _dec_rc_str(str);
    return result;
}

void rc_strset(rc_header* str, int idx, char value) {
    char* chars = (char*) &str[1];
    chars[idx] = value;
    _dec_rc_str(str);
}

int rc_strcmp(rc_header* a, rc_header* b) {
    int result = strcmp((char*) &a[1], (char*) &b[1]);
    _dec_rc_str(a);
    _dec_rc_str(b);
    return result;
}

static rc_array_header* _arrcrt(int length, int elem_size) {
    rc_array_header* header = rc_malloc(length * elem_size + sizeof(rc_array_header));
    header->ref_counter = 1;
    header->length = length;
    return header;
}

rc_array_header* rc_arrmake(int length, int value) {
    rc_array_header* header = _arrcrt(length, 4);
    int* data = (int*) &header[1];
    for (int i = 0; i < length; i++) {
        data[i] = value;
    }
    return header;
}

rc_array_header* rc_carrmake(int length, int main, int aux) {
    rc_array_header* header = _arrcrt(length, 8);
    int* data = (int*) &header[1];
    for (int i = 0; i < length; i++) {
        data[i * 2] = main;
        data[i * 2 + 1] = aux;
    }
    return header;
}

rc_array_header* rc_arrinit(void* src, int length, int elem_size) {
    rc_array_header* header = _arrcrt(length, elem_size);
    memcpy(&header[1], src, length * elem_size);
    return header;
}

void rc_arrdel(rc_array_header* ptr, int depth, destructor_t destructor) {
    int length = ptr->length;
    rc_header** data = (rc_header**) &ptr[1];
    for (int i = 0; i < length; i++) {
        rc_header* elem_header = data[i];
        elem_header->ref_counter -= 1;
        if (elem_header->ref_counter == 0) {
            if (depth == 1) {
                destructor(elem_header);
            } else {
                rc_arrdel((rc_array_header*) elem_header, depth - 1, destructor);
            }
        }
    }
    rc_free(ptr);
}

void rc_carrdel(rc_array_header* ptr) {
    int length = ptr->length;
    rc_fat_ptr* data = (rc_fat_ptr*) &ptr[1];
    for (int i = 0; i < length; i++) {
        rc_header* elem_header = data[i].data;
        elem_header->ref_counter -= 1;
        if (elem_header->ref_counter == 0) {
            data[i].vtable->destructor(elem_header);
        }
    }
    rc_free(ptr);
}