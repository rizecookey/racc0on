#ifndef LIBC_WRAPPERS_H
#define LIBC_WRAPPERS_H

#include <stdlib.h>

int print(int);
int read(void);
int flush(void);

#define alloc(type) calloc(1, sizeof(type))
#define alloc_array(type, num) calloc(num, sizeof(type))

#endif
