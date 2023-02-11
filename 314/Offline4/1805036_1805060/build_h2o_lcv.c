#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

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
int remainingOxygenCount;
int remainingHydrogenCount;
int bondCount;

int isFirst;

pthread_mutex_t atom_lock;
pthread_cond_t oxygen_cv, hydrogen_cv;

//initialize problem
void problem_init(){
    oxygenCount = 0;
    hydrogenCount = 0;
    remainingOxygenCount = 1;
    remainingHydrogenCount = 2;
    isFirst = 0;
    bondCount = 0;
    pthread_mutex_init(&atom_lock, NULL);
    pthread_cond_init(&oxygen_cv, NULL);
    pthread_cond_init(&hydrogen_cv, NULL);
}

//funtion to show that h2o is formed
void bond(){
    bondCount++;
    printf("A H20 molecule is created\n\n");
    if(print_h2o){
        sleep(1);
    }
}

//call others to finish and leave
int call_others(){
    if(bondCount>=max_bond_possible){
        pthread_cond_broadcast(&oxygen_cv);
        pthread_cond_broadcast(&hydrogen_cv);
        // pthread_mutex_unlock(&atom_lock);
        is_done_h2o = 1;
        return 1;
    }
    return 0;
}

//oxygen thread function
void *oxygen(void *data)
{   
    int thread_id = *((int *)data);

    pthread_mutex_lock(&atom_lock);

    //go in it only if h2o making is not yet done
    if(!is_done_h2o){
        oxygenCount++;

        //wait
        while((isFirst || hydrogenCount<2 || oxygenCount<1) && (!isFirst || remainingOxygenCount<=0)){
            pthread_cond_wait(&oxygen_cv, &atom_lock);
            
            //try to call others before leaving
            //if h2o making is done, then calling will be successful
            if(call_others()){
                pthread_mutex_unlock(&atom_lock);
                return 0;
            }
        }

        isFirst = 1;
        remainingOxygenCount--;
        oxygenCount--;

        //signal others 
        if(remainingHydrogenCount==1){
            pthread_cond_signal(&hydrogen_cv);
        }else if(remainingHydrogenCount==2){
            pthread_cond_broadcast(&hydrogen_cv);
        }

        printf("An Oxygen atom arrives\n");

        if(!remainingHydrogenCount && !remainingOxygenCount){
            bond();
            remainingHydrogenCount = 2;
            remainingOxygenCount = 1;
            isFirst = 0;
            call_others(thread_id);
        }

    }

    pthread_mutex_unlock(&atom_lock);
    
    return 0;
}

//hydrogen thread function
void *hydrogen(void *data)
{
    int thread_id = *((int *)data);

    pthread_mutex_lock(&atom_lock);

    //go in it only if h2o making is not yet done
    if(!is_done_h2o){

        hydrogenCount++;

        //wait
        while((isFirst || oxygenCount<1 || hydrogenCount<2) && (!isFirst || remainingHydrogenCount<=0)){
            pthread_cond_wait(&hydrogen_cv, &atom_lock);

            //try to call others before leaving
            //if h2o making is done, then calling will be successful
            if(call_others(thread_id)){
                pthread_mutex_unlock(&atom_lock);
                return 0;
            }
        }
        
        isFirst = 1;
        remainingHydrogenCount--;
        hydrogenCount--;

        //signal others 
        if(remainingHydrogenCount>0){
            pthread_cond_signal(&hydrogen_cv);
        }

        if(remainingOxygenCount>0){
            pthread_cond_signal(&oxygen_cv);
        }

        printf("A Hydrogen atom arrives\n");

        if(!remainingHydrogenCount && !remainingOxygenCount){
            bond();
            remainingHydrogenCount = 2;
            remainingOxygenCount = 1;
            isFirst = 0;
            call_others(thread_id);
        }
    }
    pthread_mutex_unlock(&atom_lock);
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
    
    problem_init();


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

    
  return 0;
}
