from functions import *

Y_output_flow = {
    'throughput': [],
    'avgDelay': [],
    'deliveryRatio': [],
    'dropRatio': [],
}
Y_output_node = {
    'throughput': [],
    'avgDelay': [],
    'deliveryRatio': [],
    'dropRatio': [],
}
Y_output_area = {
    'throughput': [],
    'avgDelay': [],
    'deliveryRatio': [],
    'dropRatio': [],
}

X_input = {}
X_input['flow'] = [10, 20, 30, 40, 50]
X_input['node'] = [20, 40, 60, 80, 100]
X_input['area'] = [250, 500, 750, 1000, 1250]

fileNamesFlow = ['500_40_10.tr', '500_40_20.tr', '500_40_30.tr',
             '500_40_40.tr', '500_40_50.tr']  # trace files generated by varying num of flow

fileNamesNode = ['500_20_20.tr', '500_40_20.tr', '500_60_20.tr',
              '500_80_20.tr', '500_100_20.tr']  # trace files generated by varying num of nodes

fileNamesArea = ['250_40_20.tr', '500_40_20.tr', '750_40_20.tr',
              '1000_40_20.tr', '1250_40_20.tr']  # trace files generated by varying num of area

i = 0
for name in fileNamesFlow:

    temp_List = {}
    print("Flow Variation File{}    :{}".format(i+1, name))
    file = open('Output/'+name, 'r')
    temp_List = myFunctionFlow(file)
    Y_output_flow['throughput'].append(temp_List['throughput'])
    Y_output_flow['avgDelay'].append(temp_List['avgDelay'])
    Y_output_flow['deliveryRatio'].append(temp_List['deliveryRatio'])
    Y_output_flow['dropRatio'].append(temp_List['dropRatio'])

    print("-------------------------------------\n")
    i = i+1

temp_List.clear()
i = 0
for name in fileNamesNode:

    temp_List = {}
    print("Node Variation File{}    :{}".format(i+1, name))
    file = open('Output/'+name, 'r')
    temp_List = myFunctionFlow(file)
    Y_output_node['throughput'].append(temp_List['throughput'])
    Y_output_node['avgDelay'].append(temp_List['avgDelay'])
    Y_output_node['deliveryRatio'].append(temp_List['deliveryRatio'])
    Y_output_node['dropRatio'].append(temp_List['dropRatio'])

    print("-------------------------------------\n")
    i = i+1

temp_List.clear()
i = 0
for name in fileNamesArea:
    temp_List = {}
    print("Area Variation File{}    :{}".format(i+1, name))
    file = open('Output/'+name, 'r')
    temp_List = myFunctionFlow(file)
    Y_output_area['throughput'].append(temp_List['throughput'])
    Y_output_area['avgDelay'].append(temp_List['avgDelay'])
    Y_output_area['deliveryRatio'].append(temp_List['deliveryRatio'])
    Y_output_area['dropRatio'].append(temp_List['dropRatio'])

    print("-------------------------------------\n")
    i = i+1


y_Attributes=['throughput','avgDelay','deliveryRatio','dropRatio']
for y_attribute in y_Attributes:
    plotGraph(X_input['flow'], Y_output_flow[y_attribute],
          'Flow', y_attribute, 'Flow Variation: {} Vs Flow'.format(y_attribute))

for y_attribute in y_Attributes:
    plotGraph(X_input['node'], Y_output_node[y_attribute],
          'Node', y_attribute, 'Node Variation: {} Vs Node'.format(y_attribute))

for y_attribute in y_Attributes:
    plotGraph(X_input['area'], Y_output_area[y_attribute],
          'Area', y_attribute, 'Area Variation: {} Vs Area'.format(y_attribute))