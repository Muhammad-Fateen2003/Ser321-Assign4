# Assignment 4 Activity 2

## Description

This program is designed to demonstrate the use of various concepts in computer programming. The focus is on implementing a basic client-server communication system.
***
The program's protocol (the one in PROTOCOL.md) describes the communication between our client and a server for a match game. The client can send requests to the server to log in, view the leader board, play the game, or exit. The server responds to these requests with the appropriate response.

The log in process starts with the client sending a NAME request with the field "name". The server responds with a GREETING response that includes a message.

To view the leader board, the client sends a LEADER request with no required fields. The server responds with a LEADER response that includes a repeated field "leader".

To play the game, the client sends a NEW request with no required fields. The server responds with a PLAY response that includes the current state of the board and information about whose turn it is and if the game is won or not. The client then makes moves by sending TILE1 or TILE2 requests with the field "tile". The server responds with the updated state of the board and information about the game until it is won or the user disconnects.

To exit, the client sends a QUIT request with no required fields. The server responds with a BYE response that includes a message.

The server also generates ERROR responses for any malformed or unexpected requests from the client. The client is responsible for keeping track of the state of the game and handling errors appropriately.
## How to run the program
## Terminal

Base Code, please use the following commands:

To run the server task, execute the following command:

```
gradle runServer -Pport=port`
```

By default, `port` is set to `9099`. Replace `port` with the desired port number.

To run the client task, execute the following command:

```
gradle runClient -Pport=port -Phost=host`
```

By default, `port` is set to `9099` and `host` is set to `localhost`. Replace `port` with the desired port number and `host` with the IP address of the host.

## Working with the Program

This program is run through Gradle and implements the given protocol (Protobuf). The main menu on the client side gives the user three options: (1) leaderboard, (2) play game, and (3) quit.

When the user chooses option 1, they will see the leaderboard which is the same for all users and persists even if the server crashes and is restarted. If the user chooses option 2, they can play a game with other users. In the game, users can turn tiles, which are represented as a 2 character string (row first as letter followed by column as number), and can win and receive 1 point for winning. After winning, the client goes back to the main menu. The client sends the first and second tile to the server for evaluation, and the server decides if the request is valid. The client presents the information well and updates the board as soon as the server updates it.

It is possible for multiple clients to connect to a server and win a game together and receive a point for winning. The user can exit the game at any time by typing "exit." If the user chooses option 3, the game quits gracefully. The server does not crash when the client disconnects and is running on an AWS instance for others to test the program. A log of who logs onto the server is kept and the exact Gradle command is posted on Slack. Error messages are descriptive and understandable by the client, and the server and client programs are well-designed and handle errors well.
## Video Demonstration

A video demonstrating the operation of the client-server system can be found [here](insert link to video).

## Requirements Fulfilled

- [x]  The project runs through Gradle
- [x]   Implement the given protocol (Protobuf)
- [x]   The main menu on the client side gives the user 3 options: 1. leaderboard, 2. play game, 3. quit
- [x]   Show a leader board when the user chooses option 1
-  [x]  The leader board is the same for all users and persists even if the server crashes and is restarted
- [x]   User can play a game when choosing option 2
- [x]   Multiple users can enter the same game and play faster
- [x]   Users win and receive 1 point for winning
- [x]   Client goes back to the main menu after winning
- [x]   Multiple clients can win and receive a point for winning
- [x]   Tiles are represented as a 2 character string (row first as letter followed by column as number)
- [ ]   The game boards can be changed in format for easier turning of tiles
- [x]   Client sends first tile to the server and the server evaluates if the request is valid
- [x]   Client sends second tile to the server and the server evaluates if the request is valid
- [x]   Client presents information well
- [x]   After both turned tiles are shown, user can press any key to show the current board
- [x]   Game quits gracefully when option 3 is chosen
- [x]   Server does not crash when the client disconnects
- [x]   Server is running on an AWS instance or somewhere reachable by others for testing
- [x]   A log of who logs onto the server is kept and the exact Gradle command is posted on Slack
- [x]   The client updates the board as soon as the server updates it
- [x]   User can exit the game by typing "exit"
- [x]   Error messages are descriptive and understandable by the client
- [x]   Server and client programs do not crash, handle errors well, and are well-designed.