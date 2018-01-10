# clojure-game-geek

A lacinia tutorial.

## Dependencies ##

This project required Java JDK +1.7 and Leiningen 2.8+.

## Usage

The easiest way to run this project is to use the repl to start the
server (which will also open a browser page to the embedded graphiql
browser).

### Start server in REPL ###

    $ lein repl
    ...
    user=> (start)
    ...

### Stop server and quit REPL ###

    user=> (stop)
    user=> (quit)
### Query API using graphiql

This project has a built in graphiql browser. To access this UI open
the following url in a browser:

     http://localhost:8888/


## License

Copyright © 2018 Chris Howe-Jones

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
