# simulator
set ns [new Simulator]


# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type
set val(mac)          Mac/802_11               ;# MAC type
set val(rp)           AODV                     ;# ad-hoc routing protocol 
set val(nn)           40                       ;# number of mobilenodes 20,40,60,80,100
set val(motherSource) 0                        ;# fixed Source
set val(gridX)        1250                    ;#250,500,750,1000,1250
set val(gridY)        1250
set val(colNo)          6
set val(rowNo)       [expr int(ceil(double($val(nn))/double($val(colNo))))]

# =======================================================================
set val(flow)        10                        ;#10,20,30,40,50
# trace file
set trace_file [open Output/1250_40_10.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open Output/1250_40_10.nam w]
$ns namtrace-all-wireless $nam_file $val(gridX) $val(gridY)

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(gridX) $val(gridY) ;# 500m x 500m area


# general operation director for mobilenodes
create-god $val(nn)


# node configs
# ======================================================================

# $ns node-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -topoInstance $topo \
                -channelType $val(chan) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

# # create nodes
# for {set i 0} {$i < $val(nn) } {incr i} {
#     set node($i) [$ns node]
#     $node($i) random-motion 0       ;# disable random motion

#     $node($i) set X_ [expr (500 * $i) / $val(nn)]
#     $node($i) set Y_ [expr (500 * $i) / $val(nn)]
#     $node($i) set Z_ 0

#     $ns initial_node_pos $node($i) 20
# } 
set currentNodeNo 0
# create nodes in Grid
for {set i 0} {$i < $val(rowNo) } {incr i} {
    for {set col 0} {$col < $val(colNo)} {incr col} {
        
        if { $currentNodeNo >= $val(nn) } {
            break
        }
        set node($currentNodeNo) [$ns node]
        $node($currentNodeNo) random-motion 0       ;# disable random motion

        $node($currentNodeNo) set X_ [expr ($val(gridX) / $val(colNo)) + ($col*100)]
        $node($currentNodeNo) set Y_ [expr ($val(gridY) / $val(rowNo)) + ($i*100)]
        $node($currentNodeNo) set Z_ 0

        $ns initial_node_pos $node($currentNodeNo) 20  ;#node size
        incr currentNodeNo   
    }
} 



# Traffic
set val(nf)         $val(flow)                ;# number of flows // Change kora lagbe

for {set i 0} {$i < $val(nf)} {incr i} {
    set src $val(motherSource) 
    #set dest [expr $i + 10]
    set dest [expr int($val(nn)*rand())] ;# Random Destination
    while {$dest==$val(motherSource) } {
        #source itself cant be a destination
        set dest [expr int($val(nn)*rand())] ;# Random Destination
    } 

    # Traffic config
    # create agent
    set tcp [new Agent/TCP/Reno]
    set tcp_sink [new Agent/TCPSink]
    # attach to nodes
    $ns attach-agent $node($src) $tcp
    $ns attach-agent $node($dest) $tcp_sink
    # connect agents
    $ns connect $tcp $tcp_sink
    $tcp set fid_ $i

    #-----------Application Layer-----------#
    # Traffic generator
    set ftp [new Application/FTP]
    # attach to agent
    $ftp attach-agent $tcp
    
    # start traffic generation
    $ns at 1.0 "$ftp start"
}



# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 50.0001 "finish"
$ns at 50.0002 "halt_simulation"




# Run simulation
puts "Simulation starting"
$ns run

