# JetBrains Academy JSON Database Project

An example of a passing solution to the final phase of the JetBrains Academy Java JSON Database project.

## Description

This project has two command line applications. One is a server that allows CRUD operations to a JSON data structure. The other is a client that can make CRUD requests to the server.

JCommander to used to interpret the client side commands.

The client and server communicate through Java sockets.

The server uses GSON to manipulate its JSON data structure. The client is allowed to modify any part of the JSON data structure.

## Notes

The relative directory structure was kept the same as the one used in my JetBrains Academy solution.

The server program logs to a file called "json-database-server.log" in your home directory. The client program logs to "json-database-client.log" in the same directory.

When testing the application, I would start the server in IntelliJ and then start the client in IntelliJ. It can be a bit tedious to test the client because the command line options need to be entered in the run configuration program arguments between each run.

Client commands look like the following:

```
java client.Main -t set -k person1 -v Bob Jones

java client.Main -t get -k person1
```

In the client console window, the commands produce the following:

```
Client started!
Sent: {"type":"set","value":"Bob Jones","key":"person1"}
Received: {"response":"OK"}

Client started!
Sent: {"type":"get","key":"person1"}
Received: {"response":"OK","value":"Bob Jones"}
```

In the server console window, the commands produce the following:

```
Server started!
Received: {"type":"set","value":"Bob Jones","key":"person1"}
Sent: {"response":"OK"}
Received: {"type":"get","key":"person1"}
Sent: {"response":"OK","value":"Bob Jones"}
```

To facilitate more rapid testing, I would sometimes comment out the entire command line portion of the client side and substitute commands like the following to send a sequence of commands to the server:

```
sender.setLogger(logger);
sender.sendRequest("{\"type\":\"set\",\"value\":\"Hello world!\",\"key\":\"1\"}");
sender.sendRequest("{\"type\":\"set\",\"key\":\"person\",\"value\":{\"name\":\"Elon Musk\",\"car\":{\"model\":\"Tesla Roadster\",\"year\":\"2018\"},\"rocket\":{\"name\":\"Falcon 9\",\"launches\":\"87\"}}}");
sender.sendRequest("{\"type\":\"get\",\"key\":[\"person\",\"name\"]}");
sender.sendRequest("{\"type\":\"set\",\"key\":[\"person\",\"rocket\",\"launches\"],\"value\":\"88\"}");
sender.sendRequest("{\"type\":\"get\",\"key\":[\"person\"]}");
sender.sendRequest("{\"type\":\"exit\"}");
```

The above commands replace all the code between the start and end logging lines in the client Main.java. 

In the client console window, the commands produce the following:

```
Client started!
Sent: {"type":"set","value":"Hello world!","key":"1"}
Received: {"response":"OK"}
Sent: {"type":"set","key":"person","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}}
Received: {"response":"OK"}
Sent: {"type":"get","key":["person","name"]}
Received: {"response":"OK","value":"Elon Musk"}
Sent: {"type":"set","key":["person","rocket","launches"],"value":"88"}
Received: {"response":"OK"}
Sent: {"type":"get","key":["person"]}
Received: {"response":"OK","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"88"}}}
Sent: {"type":"exit"}
Received: {"response":"OK"}
```

In the server console window, the commands produce the following:

```
Server started!
Received: {"type":"set","value":"Hello world!","key":"1"}
Sent: {"response":"OK"}
Received: {"type":"set","key":"person","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}}
Sent: {"response":"OK"}
Received: {"type":"get","key":["person","name"]}
Sent: {"response":"OK","value":"Elon Musk"}
Received: {"type":"set","key":["person","rocket","launches"],"value":"88"}
Sent: {"response":"OK"}
Received: {"type":"get","key":["person"]}
Sent: {"response":"OK","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"88"}}}
Received: {"type":"exit"}
Sent: {"response":"OK"}
```

The server logs are in debug mode and show the JSON data structure as it is modified:

```
2023-03-09 22:46:15.038 INFO [1] - JSON Database server started.
2023-03-09 22:46:15.043 INFO [1] - Server started!
2023-03-09 22:46:21.070 INFO [16] - Received: {"type":"set","value":"Hello world!","key":"1"}
2023-03-09 22:46:21.091 FINE [16] - Before update with "Hello world!"
2023-03-09 22:46:21.092 FINE [16] - {}
2023-03-09 22:46:21.092 FINE [16] - After update with "Hello world!"
2023-03-09 22:46:21.092 FINE [16] - {
  "1": "Hello world!"
}
2023-03-09 22:46:21.093 INFO [16] - Sent: {"response":"OK"}
2023-03-09 22:46:21.094 INFO [17] - Received: {"type":"set","key":"person","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}}
2023-03-09 22:46:21.095 FINE [17] - Before update with {"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}
2023-03-09 22:46:21.095 FINE [17] - {
  "1": "Hello world!"
}
2023-03-09 22:46:21.095 FINE [17] - After update with {"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}
2023-03-09 22:46:21.095 FINE [17] - {
  "1": "Hello world!",
  "person": {
    "name": "Elon Musk",
    "car": {
      "model": "Tesla Roadster",
      "year": "2018"
    },
    "rocket": {
      "name": "Falcon 9",
      "launches": "87"
    }
  }
}
2023-03-09 22:46:21.095 INFO [17] - Sent: {"response":"OK"}
2023-03-09 22:46:21.096 INFO [18] - Received: {"type":"get","key":["person","name"]}
2023-03-09 22:46:21.098 FINE [18] - Get personname
2023-03-09 22:46:21.099 FINE [18] - {
  "1": "Hello world!",
  "person": {
    "name": "Elon Musk",
    "car": {
      "model": "Tesla Roadster",
      "year": "2018"
    },
    "rocket": {
      "name": "Falcon 9",
      "launches": "87"
    }
  }
}
2023-03-09 22:46:21.099 INFO [18] - Sent: {"response":"OK","value":"Elon Musk"}
2023-03-09 22:46:21.099 INFO [19] - Received: {"type":"set","key":["person","rocket","launches"],"value":"88"}
2023-03-09 22:46:21.100 FINE [19] - Before update with "88"
2023-03-09 22:46:21.100 FINE [19] - {
  "1": "Hello world!",
  "person": {
    "name": "Elon Musk",
    "car": {
      "model": "Tesla Roadster",
      "year": "2018"
    },
    "rocket": {
      "name": "Falcon 9",
      "launches": "87"
    }
  }
}
2023-03-09 22:46:21.100 FINE [19] - After update with "88"
2023-03-09 22:46:21.100 FINE [19] - {
  "1": "Hello world!",
  "person": {
    "name": "Elon Musk",
    "car": {
      "model": "Tesla Roadster",
      "year": "2018"
    },
    "rocket": {
      "name": "Falcon 9",
      "launches": "88"
    }
  }
}
2023-03-09 22:46:21.100 INFO [19] - Sent: {"response":"OK"}
2023-03-09 22:46:21.101 INFO [20] - Received: {"type":"get","key":["person"]}
2023-03-09 22:46:21.101 FINE [20] - Get person
2023-03-09 22:46:21.101 FINE [20] - {
  "1": "Hello world!",
  "person": {
    "name": "Elon Musk",
    "car": {
      "model": "Tesla Roadster",
      "year": "2018"
    },
    "rocket": {
      "name": "Falcon 9",
      "launches": "88"
    }
  }
}
2023-03-09 22:46:21.102 INFO [20] - Sent: {"response":"OK","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"88"}}}
2023-03-09 22:46:21.102 INFO [21] - Received: {"type":"exit"}
2023-03-09 22:46:21.103 INFO [21] - Sent: {"response":"OK"}
2023-03-09 22:46:21.122 INFO [1] - JSON Database server ended.
```
