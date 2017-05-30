#include <stdio.h>
#include <string.h>
#include <stdlib.h>

void write(int val) {
  printf("%d\n", val);
}

int read() {
  int val;
  printf("> ");
  scanf("%d", &val);
  return val;
}

char* strmake(int length, char c) {
    char* buf = malloc(length + 1);
    memset(buf, c, length);
    buf[length] = 0;
    return buf;
}

char strget(char* str, int index) {
    return str[index];
}

char* strsub(char* str, int from, int length) {
    char* buf = malloc(length + 1);
    memcpy(buf, str + from, length);
    buf[length] = 0;
    return buf;
}