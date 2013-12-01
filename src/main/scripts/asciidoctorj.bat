@echo off

if not %JAVA_HOME% == "" goto :execute

echo.
echo ERROR: Environment variable JAVA_HOME has not been set.
echo Attempting to find JAVA_HOME from PATH also failed.
goto common_error

:execute
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set CMD_LINE_ARGS=%*
%JAVA_EXE% %JAVA_OPTS% -jar *.jar %CMD_LINE_ARGS%
goto :end

:common_error
echo Please set the JAVA_HOME variable in your environment
echo to match the location of your Java installation.

:end



