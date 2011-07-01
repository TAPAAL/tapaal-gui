TAPAAL v2.0.0
June 2011

===== Introduction ===== 

TAPAAL is a tool for modelling, simulation and verification of 
Timed-Arc Petri nets, developed at Department of Computer Science at 
Aalborg University in Denmark.

To use TAPAAL the JRE 6.0 has to be installed on your system. To
use the integrated verification features of TAPAAL, you will need to have 
UPPAAL (www.uppaal.com) installed on your system. You will need the latest 
development version of UPPAAL. 

See more on the project webpage: www.tapaal.net

===== To run TAPAAL on Windows ===== 

To run TAPAAL double click tapaal.bat .

Optional: to use the verification part of TAPAAL that translates to UPPAAL, 
you will need to have UPPAAL installed. TAPAAL assumes the location of 
verifyta is in c:\%PROGRAMFILES%\Uppaal-dev\bin-Win32\verifyta.exe
(where %PROGRAMFILES% normally corresponds to Program Files)
If it is not, you will have to set the system variable verifyta to the path 
set verifyta=/path/to/verifyta
or edit the script tapaal.bat to point the the location of verifyta.

