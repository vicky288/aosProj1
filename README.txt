1.Put the shell script(launcher.sh) and topology file(config.txt) in folder $HOME/aos2_1 along with the all the class files.
2.Change the permission of launcher.sh to execute using
	chmod +x launcher.sh
3.Start the program 
	sh launcher.sh
4.Log files per node will be created which contains the final out put of each node.

NB:- Need to change the PROJDIR in the launcher.sh if the files are to be executed in some other folder.






Time Complexity:-
In my algorithm each node sends knowledge to neighboring nodes in the first round and then to the new nodes which are neighbors of newly discovered nodes and so on. Hence the complexity will be O(d) , where d is the diameter of the graph.
(To discover the farthest node it has to send d messages in worst case.)
