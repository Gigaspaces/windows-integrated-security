@echo off

set cp=

for %%i in (.\lib\*.jar) do call .\cp.bat %%i

java -Xmx256m -classpath %CP%; com.gigaspaces.wis.Server %1 %2 %3 %4 %5 %6 %7 %8 %9