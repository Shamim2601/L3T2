#include "rwlock.h"

int hasReaderEntered = false;

//inititalizing the lock
void InitalizeReadWriteLock(struct read_write_lock * rw)
{
  rw->reader_count = 0;
  pthread_mutex_init(&rw->lock, NULL);
  rw->writer_count = 0;
  pthread_cond_init(&rw->reader_cv, NULL);
  pthread_cond_init(&rw->writer_cv, NULL);
}

//code for reader lock
void ReaderLock(struct read_write_lock * rw)
{
  pthread_mutex_lock(&rw->lock);
  if(rw->writer_count>0){
    pthread_cond_wait(&rw->reader_cv, &rw->lock);
  }
  //make has entered true so that writer can enter
  hasReaderEntered = true;
  rw->reader_count++;
  pthread_mutex_unlock(&rw->lock);
}

//code for reader unlock
void ReaderUnlock(struct read_write_lock * rw)
{
  pthread_mutex_lock(&rw->lock);
  rw->reader_count--;
  if(rw->reader_count==0){
    pthread_cond_signal(&rw->writer_cv);
  }
  pthread_mutex_unlock(&rw->lock);
}

//code for writer lock
void WriterLock(struct read_write_lock * rw)
{
  pthread_mutex_lock(&rw->lock);
  if(rw->reader_count>0 || rw->writer_count>0 || !hasReaderEntered){
    pthread_cond_wait(&rw->writer_cv, &rw->lock);
  }
  rw->writer_count++;
  pthread_mutex_unlock(&rw->lock);
}

//code for writer unlock
void WriterUnlock(struct read_write_lock * rw)
{
  pthread_mutex_lock(&rw->lock);
  rw->writer_count--;
  pthread_cond_signal(&rw->writer_cv);
  pthread_cond_broadcast(&rw->reader_cv);
  pthread_mutex_unlock(&rw->lock);
}
