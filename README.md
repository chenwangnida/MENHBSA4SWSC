# Title
Source code of MEEDA algorithms for semantic web service composition.

# Getting Started
These instructions will get you a copy of the project and running on your local machine.

# Prerequisites

To run the source code of MEEDA, the following jar files need to be bult in the path of libarary.
1. Java jar: JAVA_SE 1.8
2. Third party jars: Guava, Lang3, Math3, and JGraphT (these jars are provided in lib folder).

# Run 

1. We have generated a runnable jar (i.e, a MENHBSA4SWSC.jar file in the exportedJar fold) with all the required jars. Those required jar are also provided in the lib fold of the repository. If you would like to generated jar on your own, you will need them.
2. To run the jar file by execute a command line with five pramarters. An example of command line to run a jar is `java -jar MENHBSA4SWSC.jar out.stat problem.xml service-output.xml taxonomy.owl 0 0`. Note that a full path of problem.xml service-output.xml and taxonomy.owl must be provided, see a complete example in the video demo.
   - the first paramter: out.state is an output file.
   - the second parameter: problem.xml is a service composition task file from WSC dataset.
   - the third parameter: service-output.xml is a set of services file from WSC dataset.
   - the fourth parameter: taxonomy.owl is an ontology from WSC dataset. 
   - the fifth parameter: 0 is the seed number. We use 30 seeds from 0 to 29 in our experiment.
   - the sixth paramters: 
      - 0 refers to **MEEDA-LOP** 
      - 1 refers to **MEEDA-OP** 
      - 2 refers to **MEEDA-TP** 
      - 3 refers to **MEEDA-OB**


## An Video Example for Running MEEDA algorithms 
https://user-images.githubusercontent.com/20468313/129655225-f214f0a1-4147-408a-9ed2-ed17cf696cf5.mov

(Alternative video link from Vimeo:https://vimeo.com/588134937)

The commands in the video are listed below:

1. `git clone https://github.com/chenwangnida/MENHBSA4SWSC.git` Download code repository to local PC (i.e, Desktop directory in the example).

2. `git clone https://github.com/chenwangnida/Dataset.git` Download all datasets to local PC (i.e, Desktop directory in the example).

3. `mv ~/Desktop/MENHBSA4SWSC/exportedJar/MENHBSA4SWSC.jar ~/Desktop` Move the runnable jar to the Desktop directory.

4 `mv /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/problem.xml /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/services-output.xml /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/taxonomy.owl ~/Desktop` Move WSC08-01 task related files (i.e, problem.xml, services-output.xml and taxonomy.owl) to the Desktop directory.

5. `java -version` Check if JDK version 1.8 is loaded

6. `/usr/libexec/java_home -V` Check all installed JDK versions

7. `export JAVA_HOME="/usr/libexec/java_home -v 1.8"` We load JDK version 1.8

8. `java -version` Double check we load the right JDK 1.8

9. `java -jar ~/Desktop/MENHBSA4SWSC.jar ~/Desktop/out.stat ~/Desktop/problem.xml ~/Desktop/services-output.xml ~/Desktop/taxonomy.owl 0 0` Run the jar.
