#!/bin/sh

CLASSPATH='modules/cli/target/classes/'
CLASSPATH=$CLASSPATH':modules/core/target/classes/'
CLASSPATH=$CLASSPATH':modules/parser/target/classes/'
CLASSPATH=$CLASSPATH':modules/linmp-scala/target/classes/'
CLASSPATH=$CLASSPATH':lib/antlr-runtime.jar'
CLASSPATH=$CLASSPATH':lib/scribble-cli.jar'
CLASSPATH=$CLASSPATH':lib/scribble-core.jar'
CLASSPATH=$CLASSPATH':lib/scribble-parser.jar'
CLASSPATH=$CLASSPATH':lib/linmp-scala.jar'

java -cp $CLASSPATH main.Main $1 $2

