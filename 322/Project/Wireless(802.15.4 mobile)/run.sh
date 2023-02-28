#!/bin/bash 
baseline_nodes=40
baseline_flows=20
baseline_packet_rate=200
baseline_node_speed=10

for((i=0; i<5; i++)); do
    nodes=`expr 20 + $i \* 20`

    ns 802_15_4_mobile.tcl $nodes $baseline_flows $baseline_packet_rate $baseline_node_speed
done

for((i=0; i<5; i++)); do
    flows=`expr 10 + $i \* 10`

    ns 802_15_4_mobile.tcl $baseline_nodes $flows $baseline_packet_rate $baseline_node_speed
done

for((i=0; i<5; i++)); do
    packet_rate=`expr 100 + $i \* 100`

    ns 802_15_4_mobile.tcl $baseline_nodes $baseline_flows $packet_rate $baseline_node_speed
done

for((i=0; i<5; i++)); do
    node_speed=`expr 5 + $i \* 5`

    ns 802_15_4_mobile.tcl $baseline_nodes $baseline_flows $baseline_packet_rate $node_speed
done

python3 parser_1.py
