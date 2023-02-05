#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <wait.h>
#include <pthread.h>
#include <semaphore.h>

pthread_mutex_t lock;
//pthread_cond_t  *cond_master;
//pthread_cond_t  *cond_worker;
sem_t semEmpty;
sem_t semFull;


int item_to_produce, item_to_consume, curr_buf_size;
int total_items, max_buf_size, num_workers, num_masters;

int *buffer;

void print_produced(int num, int master) {

  printf("Produced %d by master %d\n", num, master);
}

void print_consumed(int num, int worker) {

  printf("Consumed %d by worker %d\n", num, worker);
  
}


//produce items and place in buffer
//modify code below to synchronize correctly
void *generate_requests_loop(void *data)
{
  int thread_id = *((int *)data);

  while(1)
    {
      

      if(item_to_produce >= total_items) {
	      break;
      }

      sem_wait(&semEmpty);
      pthread_mutex_lock(&lock);
      // while(item_to_produce!=thread_id)
      //   pthread_cond_wait(&cond_master[thread_id], &lock);

      buffer[curr_buf_size++] = item_to_produce;
      print_produced(item_to_produce, thread_id);
      item_to_produce++;

      //pthread_cond_signal(&cond_master[item_to_produce]);
      pthread_mutex_unlock(&lock);
      sem_post(&semFull);
    }
  return 0;
}

//write function to be run by worker threads
//ensure that the workers call the function print_consumed when they consume an item
void *consume_items_loop(void *data)
{
  int thread_id = *((int *)data);

  while(1)
    {
      

      if(item_to_consume >= total_items) {
	      break;
      }
 
      sem_wait(&semFull);
      pthread_mutex_lock(&lock);
      // while(item_to_consume!=thread_id)
      //   pthread_cond_wait(&cond_worker[thread_id], &lock);

      int item_consumed = buffer[item_to_consume];
      print_consumed(item_consumed, thread_id);
      item_to_consume++;
      
      //pthread_cond_signal(&cond_worker[item_to_consume]);
      pthread_mutex_unlock(&lock);
      sem_post(&semEmpty);
    }
  return 0;
}

int main(int argc, char *argv[])
{
  int *master_thread_id;
  int *worker_thread_id;
  pthread_t *master_thread, *worker_thread;
  item_to_produce = 0;
  item_to_consume = 0;
  curr_buf_size = 0;
  
  int i;
  
  if (argc < 5) {
    printf("./master-worker #total_items #max_buf_size #num_workers #masters e.g. ./exe 10000 1000 4 3\n");
    exit(1);
  }
  else {
    num_masters = atoi(argv[4]);
    num_workers = atoi(argv[3]);
    total_items = atoi(argv[1]);
    max_buf_size = atoi(argv[2]);
  }

  pthread_mutex_init(&lock,NULL); 
  sem_init(&semEmpty, 0, total_items);
  sem_init(&semFull, 0, 0);

  buffer = (int *)malloc (sizeof(int) * max_buf_size);

  //create master producer threads
  master_thread_id = (int *)malloc(sizeof(int) * num_masters);
  master_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_masters);
  // cond_master = malloc(sizeof(pthread_cond_t) * num_masters);
  // for (i = 0; i < num_masters; i++)
  // {
  //   pthread_cond_init(&cond_master[i], NULL);
  // }

  for (i = 0; i < num_masters; i++)
  {
    master_thread_id[i] = i;
  }
  for (i = 0; i < num_masters; i++)
    pthread_create(&master_thread[i], NULL, generate_requests_loop, (void *)&master_thread_id[i]);

  //create worker consumer threads
  worker_thread_id = (int *)malloc(sizeof(int) * num_workers);
  worker_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_workers);
  // cond_worker = malloc(sizeof(pthread_cond_t) * num_workers);
  // for (i = 0; i < num_workers; i++)
  // {
  //   pthread_cond_init(&cond_worker[i], NULL);
  // }

  for (i = 0; i < num_workers; i++)
  {
    worker_thread_id[i] = i;
  }  
  for (i = 0; i < num_workers; i++)
    pthread_create(&worker_thread[i], NULL, consume_items_loop, (void *)&worker_thread_id[i]);

  //wait for all threads to complete
  for (i = 0; i < num_workers; i++)
  {
    pthread_join(worker_thread[i], NULL);
    //printf("worker %d joined\n", i);
  }

  for (i = 0; i < num_masters; i++)
  {
    pthread_join(master_thread[i], NULL);
    //printf("master %d joined\n", i);
  }
  
  /*----Deallocating Buffers---------------------*/
  free(buffer);
  free(master_thread_id);
  free(master_thread);
  free(worker_thread_id);
  free(worker_thread);
  
  sem_destroy(&semEmpty);
  sem_destroy(&semFull);
  pthread_mutex_destroy(&lock);
  return 0;
}
