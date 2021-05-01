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
java Tracker IP PORT USER_NAME PASSWORD PATH_OF_SHARED_DIRECTORY
```
- IP: PEER'S IP 
- PORT: PEER'S PORT
- USER_NAME: PEER'S USERNAME
- PASSWORD: PEER'S PASSWORD
- PATH_OF_SHARED_DIRECTORY: all files in that directory will be shared 

#### Architecture:
[<img src="https://www.researchgate.net/profile/Changhoon-Lee-2/publication/259635464/figure/fig1/AS:297232688533507@1447877203422/Structure-of-a-BitTorrent-network-for-data-sharing-with-peers-on-the-network-who-receive.png">]()
