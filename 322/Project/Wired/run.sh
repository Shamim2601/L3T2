#!/bin/bash 
baseline_nodes=40
baseline_flows=20
baseline_packet_rate=200

for((i=0; i<5; i++)); do
    nodes=`expr 20 + $i \* 20`

    ns wired.tcl $nodes $baseline_flows $baseline_packet_rate
done

for((i=0; i<5; i++)); do
    flows=`expr 10 + $i \* 10`

    ns wired.tcl $baseline_nodes $flows $baseline_packet_rate 
done

for((i=0; i<5; i++)); do
    packet_rate=`expr 100 + $i \* 100`

    ns wired.tcl $baseline_nodes $baseline_flows $packet_rate 
done

python3 parser_1.py
