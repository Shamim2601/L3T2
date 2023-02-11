#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include "zemaphore.h"

#ifndef min
#define min(a,b) (((a) < (b)) ? (a) : (b))
#endif

//default count of oxygen (hydrogen is double)
#define DefaultSize 5
    
int total_oxygen_count, total_hydrogen_count;
int max_bond_possible;
int is_done_h2o = 0;

int print_h2o = 1;

int oxygenCount;
int hydrogenCount;
int barrierCount;
int barrierSize;
int bondCount;
zem_t zem_lock, zem_oxygen, zem_hydrogen;
zem_t zem_barrier, zem_barrier_lock;
zem_t zem_bond_lock;

//initializing barrier
void barrier_init(int n){
    barrierCount = 0;
    barrierSize = n;
    zem_init(&zem_barrier, 0);
    zem_init(&zem_barrier_lock, 1);
}

//destroying barrier
void barrier_destroy(){
    zem_destroy(&zem_barrier);
    zem_destroy(&zem_barrier_lock);
}

//barrier wait
void barrier_wait(){
    zem_down(&zem_barrier_lock);
    barrierCount++;
    zem_up(&zem_barrier_lock);
    if(barrierCount==barrierSize){
        zem_up(&zem_barrier);
    }
    zem_down(&zem_barrier);
    zem_up(&zem_barrier);
}

//initialize problem
void problem_init(int n){
    oxygenCount = 0;
    hydrogenCount = 0;
    bondCount = 0;
    zem_init(&zem_oxygen, 0);
    zem_init(&zem_hydrogen, 0);
    zem_init(&zem_lock, 1);
    zem_init(&zem_bond_lock, 1);
    barrier_init(n);
}

//destroy and exit
void problem_exit(){
    zem_destroy(&zem_oxygen);
    zem_destroy(&zem_hydrogen);
    zem_destroy(&zem_lock);
    zem_destroy(&zem_bond_lock);
    barrier_destroy();
}

//funtion to show that h2o is formed
void bond(){
    zem_down(&zem_bond_lock);
    bondCount++;
    if((bondCount%3)==0){
        printf("A H20 molecule is created\n\n");
        if(print_h2o){
            sleep(1);
        }else{
            sleep(0.5);
        }
    }
    
    zem_up(&zem_bond_lock);
}

//common function to post other threads
void utilizeElements(){
    zem_up(&zem_hydrogen);
    zem_up(&zem_hydrogen);
    hydrogenCount-=2;
    zem_up(&zem_oxygen);
    oxygenCount--;
}

//call others to finish and leave
int call_others(){  
    if(((int)((bondCount)/3))>=max_bond_possible){
        zem_up(&zem_oxygen);
        zem_up(&zem_hydrogen);
        zem_up(&zem_lock);
        is_done_h2o = 1;
        return 1;
    }
    return 0;
}

//oxygen thread function
void *oxygen(void *data)
{       
    int thread_id = *((int *)data);

    zem_down(&zem_lock);
    
    //go in it only if h2o making is not yet done
    if(!is_done_h2o){
        oxygenCount++;
        if(hydrogenCount>=2){
            utilizeElements();
        }else{
            zem_up(&zem_lock);
        }

        //wait
        zem_down(&zem_oxygen);

        if(!is_done_h2o){
            printf("An Oxygen atom arrives\n");
            barrier_wait();
            bond();
        }

    }

    //try to call others before leaving
    //if h2o making is done, then calling will be successful
    call_others();

    zem_up(&zem_lock);

    return 0;
}

//hydrogen thread function
void *hydrogen(void *data)
{
    int thread_id = *((int *)data);

    zem_down(&zem_lock);

    //go in it only if h2o making is not yet done
    if(!is_done_h2o){
        hydrogenCount++;
        if(oxygenCount>=1 && hydrogenCount>=2){
            utilizeElements();
        }else{
            zem_up(&zem_lock);
        }

        //wait
        zem_down(&zem_hydrogen);
        
        if(!is_done_h2o){
            printf("A Hydrogen atom arrives\n");
            barrier_wait();
            bond();
        }
    }

    //try to call others before leaving
    //if h2o making is done, then calling will be successful
    call_others();

    return 0;
}


int main(int argc, char *argv[])
{   
    total_oxygen_count = DefaultSize;
    total_hydrogen_count = total_oxygen_count*2;
    
    if(argc==2){
        total_oxygen_count = atoi(argv[1]);
        total_hydrogen_count = total_oxygen_count*2;
    }else if(argc==3){
        total_oxygen_count = atoi(argv[1]);
        total_hydrogen_count = atoi(argv[2]);
        if(total_hydrogen_count<2){
            printf("Hydrogen count should be at least 2\n");
            return 0;
        }
    }else if(argc==4){
        total_oxygen_count = atoi(argv[1]);
        total_hydrogen_count = atoi(argv[2]);
        print_h2o = atoi(argv[3]);
        if(total_hydrogen_count<2){
            printf("Hydrogen count should be at least 2\n");
            return 0;
        }
    }

    if(total_hydrogen_count<1 || total_oxygen_count<1){
            printf("Invalid count input\n");
            return 0;
    }

    //counting how many h2o can be made
    max_bond_possible = min(total_oxygen_count, (int)(total_hydrogen_count/2));

    pthread_t oxy1, hydro1, hydro2;
    int oxy1_ID=1, hydro1_ID=2, hydro2_ID=3;
    
    pthread_t oxy[total_oxygen_count], hydro[total_hydrogen_count];
    int oxyThread_id[total_oxygen_count], hydroThread_id[total_hydrogen_count];
    
    problem_init(3);


    for(int i=0; i<total_oxygen_count; i++){
        oxyThread_id[i] = i;
        pthread_create(&oxy[i], NULL, oxygen, (void *)&oxyThread_id[i]);
    }


    for(int i=0; i<total_hydrogen_count; i++){
        hydroThread_id[i] = i;
        pthread_create(&hydro[i], NULL, hydrogen, (void *)&hydroThread_id[i]);
    }

    
    for(int i=0; i<total_oxygen_count; i++){
        pthread_join(oxy[i], NULL);
    }


    for(int i=0; i<total_hydrogen_count; i++){
        pthread_join(hydro[i], NULL);
    }


    problem_exit();
    
    return 0;
}
