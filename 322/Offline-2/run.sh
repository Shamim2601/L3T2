#!/bin/bash

mkdir Output

baseline_gridX=500
baseline_nodes=40
baseline_flows=20

for((i=0; i<5; i++)); do
    gridX=`expr 250 + $i \* 250`

    ns 1805060.tcl $gridX $baseline_nodes $baseline_flows
done

for((i=0; i<5; i++)); do
    nodes=`expr 20 + $i \* 20`

    ns 1805060.tcl $baseline_gridX $nodes $baseline_flows
done

for((i=0; i<5; i++)); do
    flows=`expr 10 + $i \* 10`

    ns 1805060.tcl $baseline_gridX $baseline_nodes $flows
done

#nam Output/250_40_10.nam
python3 parser_1.py