# About

This software contains two programs

 * TopBottom5 - A Word count program to produce top 5 and bottom 5 most frequent words in a text document.
 * Psil - An implementation of Psil, which is a minimal Lisp like expression interpreter.


## Build requirements

The software is written in Java ( JDK 5 and above ), using Maven build system.

How to compile?

    mvn package

How to run?

    java -cp takehome-1.0-SNAPSHOT.jar hs.takehome.<ClassName> <ARGS>

Where `<ClassName>` must be replaced with the class name of one of the programs descibed below and `<ARGS>` must be replaced with command line arguments for the respective program.


# TopBottom5

TopBottom5 program makes assumptions about following:

   * It expects that a file named "stop_words.txt" is present in the folder from which it is run.
   * There are two optional arguments, first for the input text file, and second for the strategy. Strategy can be one of "inmemory" or "externalsort".
   * First optional argument defaults to input file "works-of-shakespeare.txt", so make sure you have this file present on the current location.
   * Second optional argument defaults to strategy "externalsort".

Inmemory strategy is based on a HashMap where, all the vocabulary is always in memory. This will not work when the vocabulary size is big enough to produce out of memory exceptions.

External sort is based on external sort mechanism where only a fixed number of words are present in memory at a time. The processing is split up into phases, where first phase simply reads tokens from an input stream, and emits `(word, 1)` pairs. These are then dumped into a file, which forms an input for next phase that calculates word counts. However till now the words are sorted lexicographically. In next phase these are sorted by word counts. All the sorts are done using external-merge-sort mechanism, so this process will never produce out of memory exceptions. A final phase does a linear scan to get five most frequent and five least frequent words.


Sample invocation

    $ wget -O works-of-shakespeare.txt -c https://www.gutenberg.org/cache/epub/100/pg100.txt
    $ wget -O stop_words.txt -c http://pastebin.com/raw.php?i=nfAxL1Bi
    $ java -cp target/takehome-1.0-SNAPSHOT.jar hs.kwords.TopBottom5 works-of-shakespeare.txt externalsort
    Most frequent 5 words
    thee -> 3178
    shall -> 3593
    thy -> 4032
    will -> 4983
    thou -> 5485
    
    Least frequent 5 words
    yokedevils -> 1
    yokefellows -> 1
    yoketh -> 1
    yonds -> 1
    yongrey -> 1


# Psil


Psil is a minimal Lisp-like interpreter, which takes input from Standard input and produces output of the entered expression.

This minimal implementation supports the following:

 * `+` opreator for addition
 * `*` opreator for multiplication
 * `bind` opreator for variable declaration
 * `-` opreator for subtraction

Once you have build the software as show previously, you can invoke it as show below.


Sample invocation

    $ java -cp target/takehome-1.0-SNAPSHOT.jar hs.psil.Psil 
    (+ (- (* 1 2 3) (bind hello (- 10))))
    16


Please check this class `hs.psil.PsilTest` for more examples.


