# Peer-to-Peer (P2P) Service 

<ins>Tracker</ins>
```console
java Tracker PATH_OF_LISTFILE
```
- PATH_OF_LISTFILE: The path of the file which contains all the files in the P2P system <br>
File Format: each line of the file contains the name of a file <br>
file's name is unique within the system and we use it as identifier


<ins>Peer</ins>
```console
java Tracker IP PORT USER_NAME PASSWORD PATH_OF_SHARED_DIRECTORY AUTO_MODE
```
- IP: PEER'S IP 
- PORT: PEER'S PORT
- USER_NAME: PEER'S USERNAME
- PASSWORD: PEER'S PASSWORD
- PATH_OF_SHARED_DIRECTORY: all files in that directory will be shared 
- AUTO_MODE: {true, false} If we set auto-mode to false, a command line menu will be shown and we will download whatever file we want.
On the other hand, if we set auto-mode to true, the peer will try to download all system's files

***

#### Architecture:
![](architecture.PNG)

*** 
This project was developed with Pair Programming technique.
