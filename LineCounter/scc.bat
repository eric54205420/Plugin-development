@echo off
set CLASSPATH=lib\appframework.jar;lib\jdom.jar;lib\swing-worker-1.1.jar
start javaw nrs.scc.SourceCodeCounter %1

