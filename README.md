# Title
Source code of MEEDA algorithm

# Getting Started
These instructions will get you a copy of the project up and running on your local machine.

# Prerequisites

To run the source code of MEEDA, the following jar files need to be bult in the path of libarary.
1. Java jar: JAVASE 1.8
2. third party jars: Guava, Lang3, Math3, and JGraphT are provided in lib folder.

# Run 

Main class is located at /src/wsc/problem/WSCProblem.java

1. Generate a runnable jar (e.g., with name MENHBSA4SWSC.jar) with all the jars provided in the lib folder. You can use jar command to create jar at the root of the file. JFI, it is relatively easiy to use build tools, such as ant to take care of that.
2. To run the jar file by execute with five pramarters: `java -jar MENHBSA4SWSC out.stat problem.xml service-out.XML taxonomy.owl 0`
    The first paramter: out.state is an output file.
    The second parameter: problem.xml.
    The third parameter: service-out.XML taxonomy : three files of WSC dataset
  
  
  0 : seed number

  1: 
