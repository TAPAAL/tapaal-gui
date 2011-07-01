echo off 
set verifytapath="%PROGRAMFILES%\uppaal-dev\bin-Win32\verifyta.exe" 
if exist %verifytapath% set verifyta=%verifytapath% 
 
:::: Remove :: from the next line to set path to verifyta 
:: set verifyta="C:\Programmer\uppaal-dev\bin-Win32\verifyta.exe" 


cd lib 
set verifytapn=%CD%\verifytapn.exe
java -cp  .;* TAPAAL
