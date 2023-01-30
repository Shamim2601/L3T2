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
set val(commonSink)   0                        ;# fixed Sink
set val(gridX)        [lindex $argv 0]         ;# area  250, 500* ,750,1000,1250
set val(nn)           [lindex $argv 1]         ;# nodes 20,  40*  ,60,80,100
set val(flow)         [lindex $argv 2]         ;# flow  10,  20*  ,30,40,50
set val(gridY)        $val(gridX)
set val(colNo)        6
set val(rowNo)        [expr int(ceil(double($val(nn))/double($val(colNo))))]

# trace file
set trace_file [open Output/$val(gridX)_$val(nn)_$val(flow).tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open Output/$val(gridX)_$val(nn)_$val(flow).nam w]
$ns namtrace-all-wireless $nam_file $val(gridX) $val(gridY)

# topology
set topo [new Topography]
$topo load_flatgrid $val(gridX) $val(gridY) 

# general operation director for mobilenodes
create-god $val(nn)

# node configs
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

set currentNode 0
# random node positioning
for {set i 0} {$i < $val(rowNo) } {incr i} {
    for {set col 0} {$col < $val(colNo)} {incr col} {
        
        if { $currentNode >= $val(nn) } {
            break
        }
        set node($currentNode) [$ns node]
        $node($currentNode) random-motion 0       

        $node($currentNode) set X_ [expr int(rand() * $val(gridX)) + 0.5]
        $node($currentNode) set Y_ [expr int(rand() * $val(gridY)) + 0.5]
        $node($currentNode) set Z_ 0

        $ns initial_node_pos $node($currentNode) 10  ;#node size

        incr currentNode   
    }
} 

# producing node movements with uniform random speed
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at [expr int(20 * rand()) + 2] "$node($i) setdest [expr int(10000 * rand()) % $val(gridX) + 0.5] [expr int(10000 * rand()) % $val(gridY) + 0.5] [expr int(100 * rand()) % 5 + 1]"
}

# Traffic
set val(nf)         $val(flow)                

for {set i 0} {$i < $val(nf)} {incr i} {
    set src   [expr int($val(nn)*rand())] ;# Random source
    set dest  $val(commonSink)
    while {$src==$val(commonSink) } {
        #source itself cant be destination
        set src [expr int($val(nn)*rand())] ;# Random Source
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
    $ns at [expr int(9 * rand()) + 1] "$ftp start"
}



# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0000 "$node($i) reset"
}

proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Ending Simulation"
    $ns halt
}

$ns at 50.0001 "finish"
$ns at 50.0002 "halt_simulation"




# Run simulation
puts "Starting Simulation"
$ns run

