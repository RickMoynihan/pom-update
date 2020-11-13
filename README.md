# pom-update

A simple tool for use with tools.deps and the clojure command line
tools, to update a `pom.xml` file with overriden values provided by
either `:exec-args` data or via the command line.

It is suitable for `clojure -X` invocation and usage as a small
library.

## Usage

FIXME: write usage documentation!

Invoke a library API function from the command-line:

    $ clojure -X rickmoynihan.pom/foo :a 1 :b '"two"'
    {:a 1, :b "two"} "Hello, World!"

Run the project's tests (they'll fail until you edit them):

    $ clojure -M:test:runner

Build a deployable jar of this library:

    $ clojure -M:jar

Install it locally:

    $ clojure -M:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -M:deploy

## License

Copyright © 2020 Rick Moynihan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
