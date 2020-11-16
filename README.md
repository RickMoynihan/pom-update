# pom-update

A simple tool for use with tools.deps and the clojure command line
tools, to update a `pom.xml` file with overriden values provided by
either `:exec-args` data or via the command line.

It is suitable for `clojure -X` invocation and usage as a small
library.

It is hoped that this library will soon be made redundant by
`tools.build` a rumoured build tool by the Clojure core team.

## FAQ

_What should I use this for?_

Automatically updating metadata in a Clojure libraries `pom.xml`,
typically setting the maven version and scm tag to a build number
provided by your CI system.

_Can't I just use `clojure -Spom` or `clojure -X:deps mvn-pom`?_

Yes if you don't mind manually deploying library changes and setting
the version number yourself.

If you want to automate the deployment to deploy your clojure library
with a version number determined by CI (e.g. the `BUILD_ID` or a
commit SHA) then you probably want to use this simple tool to assist.

Tools deps almost has the functionality to do this, however with
`tools.build` on the horizon it's not a feature they care to
prioritise.

## Usage

Add `rickmoynihan/pom-update {:mvn/version "0.1.6"}` to an appropriate
alias in your `deps.edn`, e.g.

``` clojure
{:deps { ,,, ,,, }
 :aliases {
   :update-version {
     :extra-deps {rickmoynihan/pom-update {:mvn/version "0.1.6"}}
     :exec-fn rickmoynihan.pom/update }}}
```

Then at build time, prior to creating a jar for your library run the
following command:

`clojure -X:update-version :mvn/version "\"$BUILD_NUMBER\"" :scm/tag "\"$BUILD_NUMBER\""`

You may also wish to automate calling `clojure -X:deps mvn-pom` prior
to the above call (to ensure dependencies etc are synced properly from
`deps.edn` into `pom.xml`). If you don't do this you'll need to make
sure you commit `pom.xml` after each meaningful update to your
`deps.edn`.

## Other libraries

This library is intended to work with
[depstar](https://github.com/seancorfield/depstar) for building
library jars, and
[deps-deploy](https://github.com/seancorfield/depstar) for deploying
them to either clojars or a private maven repository.

## License

Copyright © 2020 Rick Moynihan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
