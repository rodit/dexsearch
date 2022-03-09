@echo off
java -jar libs/antlr-4.9.2-complete.jar schema/SchemaGrammar.g4 -o antlr -package antlr -no-listener -visitor -Werror