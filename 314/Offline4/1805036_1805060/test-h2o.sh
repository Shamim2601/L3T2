#!/bin/bash

total=0
correct=0


echo -e "\nTesting H2O problem with the lock and cv solution "

gcc build_h2o_lcv.c -o build_h2o_lcv

echo -e "\nFirstly, Taking Case where Hydrogen count is double of Oxygen count"

ox=10
hy=20 

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_lcv $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi


echo -e "\nSecondly, Taking Case where Hydrogen count is greater than double of Oxygen count"

ox=10 
hy=50

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_lcv $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi



echo -e "\nThirdly, Taking Case where Hydrogen count is less than double of Oxygen count"

ox=10 
hy=7

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_lcv $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi




echo -e "\n\nTesting H2O problem with the Zemaphore solution "

gcc build_h2o_zem.c zemaphore.c -o build_h2o_zem

echo -e "\nFirstly, Taking Case where Hydrogen count is double of Oxygen count"

ox=5
hy=10

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_zem $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi


echo -e "\nSecondly, Taking Case where Hydrogen count is greater than double of Oxygen count"

ox=5
hy=50

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_zem $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi



echo -e "\nThirdly, Taking Case where Hydrogen count is less than double of Oxygen count"

ox=10
hy=7

echo "Taking Oxygen count $ox and Hydrogen count $hy"

./build_h2o_zem $ox $hy 0 > output
awk -f test_count.awk Ox=$ox Hy=$hy output
out=`awk -f test_count.awk Ox=$ox Hy=$hy output | tail -n1`

total=$((total+1))
if [ "$out" = "Total number of error: 0" ]; then
    correct=$((correct+1))
fi




echo -e "\nTest Cases Total: $total"
echo "Test Cases Passed: $correct"



