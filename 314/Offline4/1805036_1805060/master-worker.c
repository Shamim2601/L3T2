#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <wait.h>
#include <pthread.h>

pthread_mutex_t mutex_lock;
pthread_cond_t not_full_cv;
pthread_cond_t not_empty_cv;

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
        pthread_mutex_lock(&mutex_lock);
        int doBreak = 0;

        //check for getting out if all items produced
        if(item_to_produce >= total_items){
            if(item_to_consume >= total_items) {
                pthread_cond_broadcast(&not_full_cv);
            }
            pthread_mutex_unlock(&mutex_lock);
            break;
        }

        //waiting for the not full signal
        while(curr_buf_size>=max_buf_size){
            pthread_cond_wait(&not_full_cv, &mutex_lock);
            
            //check for getting out if all items produced
            if(item_to_produce >= total_items){
                if(item_to_consume >= total_items) {
                    pthread_cond_broadcast(&not_full_cv);
                }
                pthread_mutex_unlock(&mutex_lock);
                doBreak = 1;
                break;
            }
        }

        if(doBreak){
            break;
        }

        //produce data
        buffer[curr_buf_size++] = item_to_produce;
        print_produced(item_to_produce, thread_id);
        item_to_produce++;

        pthread_cond_broadcast(&not_empty_cv);
        pthread_mutex_unlock(&mutex_lock);
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
        pthread_mutex_lock(&mutex_lock);
        int doBreak = 0;

        //check for getting out if all items consumed
        if(item_to_consume >= total_items) {
            if(item_to_produce >= total_items){
                pthread_cond_broadcast(&not_empty_cv);
            }
            pthread_mutex_unlock(&mutex_lock);
            break;
        }

        //waiting for the not empty signal
        while(curr_buf_size<=0){
            pthread_cond_wait(&not_empty_cv, &mutex_lock);
            //check for getting out if all items consumed
            if(item_to_consume >= total_items) {
                if(item_to_produce >= total_items){
                    pthread_cond_broadcast(&not_empty_cv);
                }
                pthread_mutex_unlock(&mutex_lock);
                doBreak = 1;
                break;
            }
        }

        if(doBreak){
            break;
        }

        //consume data
        int consumed_data = buffer[--curr_buf_size];
        print_consumed(consumed_data, thread_id);
        item_to_consume++;

        pthread_cond_broadcast(&not_full_cv);
        pthread_mutex_unlock(&mutex_lock);
    }
    return 0;
}



int main(int argc, char *argv[])
{
    int *master_thread_id, *worker_thread_id;
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
    

    buffer = (int *)malloc (sizeof(int) * max_buf_size);

    pthread_mutex_init(&mutex_lock, NULL);
    pthread_cond_init(&not_empty_cv, NULL);
    pthread_cond_init(&not_full_cv, NULL);

    //create master producer threads
    master_thread_id = (int *)malloc(sizeof(int) * num_masters);
    master_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_masters);
    for (i = 0; i < num_masters; i++){
        master_thread_id[i] = i;
    }
    
    for (i = 0; i < num_masters; i++){
        pthread_create(&master_thread[i], NULL, generate_requests_loop, (void *)&master_thread_id[i]);
    }
    
    
    //create worker consumer threads
    worker_thread_id = (int *)malloc(sizeof(int) * num_workers);
    worker_thread = (pthread_t *)malloc(sizeof(pthread_t) * num_workers);
    for (i = 0; i < num_workers; i++){
        worker_thread_id[i] = i;
    }
    
    for (i = 0; i < num_workers; i++){
        pthread_create(&worker_thread[i], NULL, consume_items_loop, (void *)&worker_thread_id[i]);
    }
    
    //wait for all threads to complete
    for (i = 0; i < num_masters; i++){
        pthread_join(master_thread[i], NULL);
        printf("master %d joined\n", i);
    }
    
    for (i = 0; i < num_workers; i++){
        pthread_join(worker_thread[i], NULL);
        printf("worker %d joined\n", i);
    }
    
    /*----Deallocating Buffers---------------------*/
    free(buffer);
    free(master_thread_id);
    free(master_thread);
    
    return 0;
}
