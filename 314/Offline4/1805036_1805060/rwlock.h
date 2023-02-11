#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <iostream>

using namespace std;

struct read_write_lock
{
    pthread_mutex_t lock;       //common lock for atomicity
    int reader_count;           //to count readers who acquired lock
    int writer_count;           //to count writers who acquired lock
    int waiting_writer_count;   //to count writers who are waiting
    pthread_cond_t reader_cv;   //cv for reader
    pthread_cond_t writer_cv;   //cv for writer
};

void InitalizeReadWriteLock(struct read_write_lock * rw);
void ReaderLock(struct read_write_lock * rw);
void ReaderUnlock(struct read_write_lock * rw);
void WriterLock(struct read_write_lock * rw);
void WriterUnlock(struct read_write_lock * rw);
