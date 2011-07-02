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

===== To run TAPAAL on Linux ===== 

Run TAPAAL by double clicking on

tapaal

or by typing

sh tapaal

Optional: to use the verification part of TAPAAL that translates to UPPAAL, 
you have to modify the script tapaal by adding the path 
to the file verifyta (part of UPPAAL distribution) as follows:
export verifyta=/path/to/verifyta  

