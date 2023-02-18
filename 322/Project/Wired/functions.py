import matplotlib.pyplot as plt

def myFunctionFlow(file):
    Y_output = {}
    received_packets = 0
    sent_packets = 0
    dropped_packets = 0
    total_delay = 0
    received_bytes = 0

    start_time = 1000000
    end_time = 0

    header_bytes = 40  # constants
    sent_time = {}  # Empty Dict
    for line in file:
        count = 1
        words = line.split()
        event = words[0]
        time_sec = float(words[1])
        if event == "M":
            continue
        
        node_src = words[2]
        node_dst = words[3]
        # layer = words[4]
        packet_id = words[11]
        packet_type = words[4]
        packet_bytes = int(words[5])

        # set start time for the first line
        if start_time > time_sec:
            start_time = time_sec

        if packet_type == "tcp":
            # packet_id = words[11]
            if event == "+":
                sent_time[packet_id] = time_sec
                sent_packets += 1

            elif event == "r":
                delay = time_sec - sent_time[packet_id]

                total_delay += delay

                bytes = (packet_bytes - header_bytes)
                received_bytes += bytes

                received_packets += 1

        if packet_type == "tcp" and event == "d":
            dropped_packets += 1

    end_time = time_sec
    simulation_time = end_time - start_time

    print("Sent Packets     :{}".format(sent_packets))
    print("Dropped Packets  :{}".format(dropped_packets))
    print("Received Packets :{}".format(received_packets))

    print("-------------------------------------------------------------")
    print("Throughput       :{} bits/sec".format(((received_bytes * 8) / simulation_time)))
    print("Average Delay    :{} seconds".format((total_delay / received_packets)))
    print("Delivery ratio   :{} ".format((received_packets / sent_packets)))
    print("Drop ratio       :{} ".format((dropped_packets / sent_packets)))

    Y_output['throughput'] = (received_bytes * 8) / simulation_time
    Y_output['avgDelay'] = total_delay / received_packets
    Y_output['deliveryRatio'] = received_packets / sent_packets
    Y_output['dropRatio']= dropped_packets / sent_packets

    return Y_output


def plotGraph(X_input,Y_output,x_Label,y_Label,title):
    plt.figure(figsize=(7,4))
    plt.plot(X_input, Y_output,color='green',marker='o', markerfacecolor='blue', markersize=10)
   
    # naming the x axis
    plt.xlabel(x_Label)
    # naming the y axis
    plt.ylabel(y_Label)

    # title of graph
    plt.title(title)

    for i_x, i_y in zip(X_input, Y_output):
        plt.text(i_x, i_y, '({}, {:.2f})'.format(i_x, i_y))

    # function to show the plot
    plt.show()