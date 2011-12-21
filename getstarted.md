Quick tutorial on how to get running with clojure
=================================================

### Install Lein
Use the instructions on
[github](https://github.com/technomancy/leiningen)

### Install clojure-mode in emacs

### Install clojure-swank

    $ lein plugin install swank-clojure 1.3.1

### Start a new lein project

    $ lein new myProject

### Set up any dependencies in project.clj

Don't worry about this yet.

### Install dependencies (and clojure)

    $ lein deps

### Open src/myProject/core.clj in emacs

Once you have it open (and are in clojure mode) Type M-X
clojure-jack-in to start the swank server and attach slime to it. If
you didn't run lein deps this could take awhile because It has to
download dependencies first.

Once the slime repl starts up, in core.clj press C-k, this will load
the current file into slime. Then go into the slime repl and hit ','
(comma). That will open the slime command promp, type in (enter)
myProject.core (enter). This loads the namespace into the repl and you
should see the promp change to 'myProject>' Now the real hacking can
begin.

I think you said you've used slime before so it should't be much
different. The only commands I ever really use are C-c to compile the
current file and C-k to load the current file into the repl.
