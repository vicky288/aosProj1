#!/bin/bash


# Change this to your netid
netid=bxp131030

#
# Root directory of your project
PROJDIR=$HOME/aos2

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
echo "Enter the topology file: "
read name

CONFIG=$PROJDIR/$name

#
# Directory your java classes are in
#
BINDIR=$PROJDIR

#
# Your main project class
#
PROG=NodeStarter
isNodeInfo=0
#n=1

cat $CONFIG | sed -e "/^\s*$/d" | 
(
    read i
    #echo $i
    while read line 
    do
    isComment=0
    if [[ $line == *"Start of Node Info"* ]] 
    then
       isNodeInfo=1
       isComment=1
       #echo $line
    fi

    if [ $isNodeInfo == 1 ] && [ $isComment == 0 ]  
    then
    	#echo $line
	node=$( echo $line | cut -f1 -d"#" )
	echo $node
	host=$( echo $line | cut -f2 -d"#" | cut -f1 -d"@" )
	echo $host
	port=$( echo $line | cut -f2 -d"#" | cut -f2 -d"@" )
	echo $port
	#echo "!!"

	ssh -l "$netid" "$host" "cd $BINDIR;java Starter03 $node $host $port " &
    fi
    done
   
)


