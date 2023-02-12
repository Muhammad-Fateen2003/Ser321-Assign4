# Assignment 4 Activity 1
## Description
Three programs for manipulating a list of strings: adding, clearing, finding, displaying, sorting, and prepending. The program works by using the following protocols.

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=clear, 3=find, 4=display, 5=sort, 6=prepend
0=quit>, "data": <thing to send>}

- add: data <string> -- a string to add to the list, this should already mostly work
- clear: data <> -- no data given, clears the whole list
- find: data <string> -- display index of string if found, else -1
- display: data <> -- no data given, displays the whole list
- sort: data <> -- no data given, sorts the list
- prepend: data <int> <string> -- index and string, prepends given string to string at index

### Responses

success response: {"ok" : true, "type": <String> "data": <thing to return> }

- type <String>: echoes original selected from request
- data <string>:
- add: return current list
- clear: return empty list
- find: return integer value of index where that string was found or -1
- display: return current
- sort: return current list
- prepend: return current list


error response: {"ok" : false, "message"": <error string> }
- error string: Should give good error message of what went wrong


## How to run the program
### Terminal
Base Code, please use the following commands:

To run task 1 (the server task), execute the following command:

```
gradle runTask1 -Pport=port
```

By default, `port` is set to `8000`. Replace `port` with the desired port number.

To run task 2 (the threaded server task), execute the following command:

```
gradle runTask2 -Pport=port
```

By default, `port` is set to `8000`. Replace `port` with the desired port number

To run task 3 (the threaded pool server task), execute the following command:

```
gradle runTask3 -Pport=port -Ppool=pool
```

By default, `port` is set to `8000` and `pool` is set to `2`. Replace `port` with the desired port number, and `pool` with the desired number of threads in the thread pool.

To run the client task, execute the following command:

```
gradle runClient -Pport=port -Phost=host
```

By default, `port` is set to `8000` and `host` is set to `localhost`. Replace `port` with the desired port number and `host` with the IP address of the host.

## Working with the Program:

The program consists of a client and a server component. The client sends requests to the server, which processes the requests and returns a response.

To use the program, first start the server component on a machine that is accessible to the client. Then, start the client component on the machine you want to use to send requests. The client will prompt you to enter the IP address or hostname of the machine running the server, and the port number to use for the connection.

Once the connection is established, you will be given choices on different ways you can manipulate a list of strings (i.e. add, clear, find, display, sort, prepend, and quit) one you choose one you will send a request to the server and then get a response this will go on in an infinite loop until quit is chosen or until the client is closed. The server will process each request and return a response. The client will display the response received from the server.

Threaded and Pooled Versions:

In addition to the basic client-server implementation, the program also includes threaded and pooled versions. The threaded version uses multiple threads to handle incoming requests, which can result in improved performance for high-concurrency scenarios. The pooled version uses a pool of worker threads to process requests, which can help reduce the overhead of creating and destroying threads for each request.

To use the threaded or pooled version of the program, simply specify the appropriate option when starting the server. The client does not need to be changed for either version.

Note: The exact implementation details of the threaded and pooled versions may vary depending on the programming language and platform used to implement the program.

## Video Demonstration

A 2-4 minute video showing how to run the program and a brief demonstration of its functionality.

## Requirements Fulfilled

Task 1:

- [x]  Make Performer more interesting.

Task 2: Make the server multi-threaded

- [x]  A new server class named "ThreadedServer" has been added.
- [x]  The "ThreadedServer" allows unbounded incoming connections to the server.
- [x]  No client should block.
- [x]  The shared state of the string list is properly managed.

Task 3: Make the multi-threaded server bounded

- [x]  A new server class named "ThreadPoolServer" has been added.
- [x]  The "ThreadPoolServer" only allows a set number of incoming connections at any given time, as specified when calling the program through Gradle.
- [x]  The number of incoming connections can be specified when calling the program through Gradle.



