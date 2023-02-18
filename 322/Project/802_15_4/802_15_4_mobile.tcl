# simulator
set ns [new Simulator]


# ======================================================================
# Define options

set cbr_size          64
set cbr_rate          11.0Mb
set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4 ;# network interface type
set val(mac)          Mac/802_15_4             ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 
set val(area_size)    500                      ;# area size = 500*500
set val(nn)           [lindex $argv 0]         ;# nodes 20,  40*  ,60,80,100
set val(flow)         [lindex $argv 1]         ;# flow  10,  20*  ,30,40,50
set val(packet_rate)  [lindex $argv 2]         ;# 100, 200*, 300, 400, and 500
set val(node_speed)   [lindex $argv 3]         ;# 5 m/s, 10* m/s, 15 m/s, 20 m/s, and 25 m/s
set cbr_interval	  [expr 1.0/$val(packet_rate)]
set val(colNo)        6
set val(rowNo)        [expr int(ceil(double($val(nn))/double($val(colNo))))]

# trace file
set trace_file [open Output/$val(nn)_$val(flow)_$val(packet_rate)_$val(node_speed).tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open Output/$val(nn)_$val(flow)_$val(packet_rate)_$val(node_speed).nam w]
$ns namtrace-all-wireless $nam_file $val(area_size) $val(area_size)

# topology
set topo [new Topography]
$topo load_flatgrid $val(area_size) $val(area_size) 

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

        $node($currentNode) set X_ [expr int(rand() * $val(area_size)) + 0.5]
        $node($currentNode) set Y_ [expr int(rand() * $val(area_size)) + 0.5]
        $node($currentNode) set Z_ 0

        $ns initial_node_pos $node($currentNode) 10  ;#node size

        incr currentNode   
    }
} 

# producing node movements with uniform random speed
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at [expr int(10 * rand()) + 2] "$node($i) setdest [expr int(10000 * rand()) % $val(area_size) + 0.5] [expr int(10000 * rand()) % $val(area_size) + 0.5] [expr int(100 * rand()) % $val(node_speed) + 1]"
}

# Traffic
set val(nf)         $val(flow)                

for {set i 0} {$i < $val(nf)} {incr i} {
    set src   [expr int($val(nn)*rand())] ;# Random source
    set dest  [expr int($val(nn)*rand())] ;# Random sink
    while {$src==$dest } {
        #source itself cant be destination
        set dest [expr int($val(nn)*rand())] ;# Update sink
    } 

    # Traffic config
    # create agent
    set tcp_($i) [new Agent/TCP/Reno]
    set tcp_sink_($i) [new Agent/TCPSink]
    # attach to nodes
    $ns attach-agent $node($src) $tcp_($i)
    $ns attach-agent $node($dest) $tcp_sink_($i)
    # connect agents
    $ns connect $tcp_($i) $tcp_sink_($i)
    $tcp_($i) set fid_ $i

    #-----------Application Layer-----------
    set cbr_($i) [new Application/Traffic/CBR]
	$cbr_($i) set packetSize_ $cbr_size
	$cbr_($i) set rate_ $cbr_rate
	$cbr_($i) set interval_ $cbr_interval
	$cbr_($i) attach-agent $tcp_($i)
	$ns at [expr int(9 * rand()) + 1] "$cbr_($i) start"
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
