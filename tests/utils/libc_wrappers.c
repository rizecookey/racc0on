#include "libc_wrappers.h"
#include <stdio.h>

int print(int c) {
  putchar(c);
  return 0;
}

int read(void) {
  int result = getchar();
  if (result < 0) {
    return -1;
  }

  return result;
}

int flush(void) {
  fflush(stdout);
  return 0;
}
