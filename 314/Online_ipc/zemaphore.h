#include <pthread.h>

typedef struct zemaphore {
    int zem_val;                //value of zemaphore
    pthread_mutex_t zem_lock;   //mutex lock
    pthread_cond_t zem_cond;    //conditional variable
} zem_t;

void zem_init(zem_t *, int);
void zem_up(zem_t *);
void zem_down(zem_t *);
void zem_destroy(zem_t *);
