@echo off

REM Compile java code that uses ANTLR4 classes
javac -classpath %VIOCHECK_HOME%\lib\*;. %*