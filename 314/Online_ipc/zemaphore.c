#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <wait.h>
#include "zemaphore.h"

//initializing the zemaphore
void zem_init(zem_t *s, int value) {
    s->zem_val = value;
    pthread_mutex_init(&s->zem_lock, NULL);
    pthread_cond_init(&s->zem_cond, NULL);
}

//zemaphore wait
void zem_down(zem_t *s) {
    // pthread_mutex_lock(&s->zem_lock);
    // printf("val: %d\n", s->zem_val);
    
    s->zem_val--;
    //waiting for signal
    if(s->zem_val<0){
        pthread_cond_wait(&s->zem_cond, &s->zem_lock);
    }
    pthread_mutex_unlock(&s->zem_lock);
}

//zemaphore post
void zem_up(zem_t *s) {
    pthread_mutex_lock(&s->zem_lock);
    s->zem_val++;
    // printf("val: %d\n", s->zem_val);

    //signaling
    pthread_cond_signal(&s->zem_cond);
    pthread_mutex_unlock(&s->zem_lock);
}

//destroying the zemaphore
void zem_destroy(zem_t *s) {
    pthread_mutex_destroy(&s->zem_lock);
    pthread_cond_destroy(&s->zem_cond);
}
