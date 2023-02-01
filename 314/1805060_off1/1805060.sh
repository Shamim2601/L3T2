#!/bin/bash
max_score=100
max_student_id=5
if (($#==1));then
    max_score=$1
elif (($#==2));then
    max_score=$1
    if(($2>=1 && $2<=9));then
        max_student_id=$2
    else
        echo "Value of arg2 should be between 1 and 9."
    fi
fi

echo "student_id,score">output.csv
cd Submissions
i=1
while((i<=$max_student_id))
do
if [[ -d "180512$i" ]];then  #check if folder exists
    score=$max_score
    if [[ -f "180512$i/180512$i.sh" ]];then  #check if sh file exists
        #output checking
        count=$(bash 180512$i/180512$i.sh | diff -wbZ ../AcceptedOutput.txt - | grep '[<>]' - | wc -l)
        score=$((score-count*5))
        if (($score<0));then    #lowest obtained score = 0
            score=0
        fi
        #copy checking
        for folder in $(ls)
        do
            if [[ "180512$i" != $folder ]];then
                diff_count=$(diff -wbZ 180512$i/180512$i.sh $folder/$folder.sh | wc -l)
                #echo "$i $folder $diff_count"
                if (($diff_count==0));then
                    score=$(($score*-1))   # negate the mark if found same with someone else's submission
                fi
            fi
        done
    else
        score=0
    fi
    echo "180512$i,$score">>../output.csv  #if no sh file, mark = 0
else
    echo "180512$i,0">>../output.csv   #if no submitted folder, mark = 0
fi
i=$(($i+1))
done
cd ..

echo "Submissions are checked successfully, please see output in the file: output.csv"
