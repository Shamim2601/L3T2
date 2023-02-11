#!/bin/bash
# script takes 4 arguments that are given to the master worker program

gcc -o master-worker master-worker.c -lpthread
./master-worker $1 $2 $3 $4 > output    ##total_items #max_buf_size #workers #masters
awk -f check.awk MAX=$1 output
