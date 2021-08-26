# Title
Source code of MEEDA algorithms for semantic web service composition.

# Getting Started
These instructions will get you a copy of the project and running on your local machine.

# Prerequisites

To run the source code of MEEDA, the following jar files need to be built in the path of library:

1. Java jar: JAVA_SE 1.8
2. Third party jars: Guava, Lang3, Math3, and JGraphT (these jars are provided in lib folder).

# Run 

1. We have generated and uploaded a runnable jar (i.e, a MENHBSA4SWSC.jar file) in our code repository.

2. To run MENHBSA4SWSC.jar via terminals, please use the following command line with five parameters: `java -jar MENHBSA4SWSC.jar out.stat problem.xml service-output.xml taxonomy.owl 0 0`. (Note that full paths of problem.xml service-output.xml and taxonomy.owl files must be given in the command line, you can download the dataset files from https://github.com/chenwangnida/Dataset).

   	- the first parameter: out.stat is a path of an output file.
   	
   	- the second parameter: problem.xml is a path of a composition task file.
   	
   	- the third parameter: service-output.xml is a path of web services file.
   	
   	- the fourth parameter: taxonomy.owl is is a path of an ontology file.
   	
   	- the fifth parameter: 0 is a seed number.
   	
   	- the sixth parameter: 
      - 0 refers to **MEEDA-LOP** 
      - 1 refers to **MEEDA-OP** 
      - 2 refers to **MEEDA-TP** 
      - 3 refers to **MEEDA-OB** 

3. The output file, out.stat, records 100 rows (i.e., the number of generations) of space-separated values. For example, one row of values (0 1974 4416 0.551528) represents that generation counter is 0, initialization time is 1974ms, computation time of generation 0 is 4416ms, the best fitness found so far is 0.551528). In addition, the last row of out.stat records the best composite service found by our algorithm. This composite service is described in a simple text language using Graphviz as below(please refer the link https://graphviz.org for more details on Graphivz).

`digraph g {startNode->serv1900589909_1; startNode->serv1277338356; serv1900589909_1->serv2108886608_1; serv1277338356->serv2108886608_1; serv1900589909_1->serv2039454375; serv1277338356->serv2039454375; serv2039454375->serv1346770589_1; serv1346770589_1->serv30835212_1; serv2108886608_1->serv30835212_1; serv1346770589_1->serv100267445; serv2108886608_1->serv100267445; serv100267445->serv1555067288; serv30835212_1->serv862383464_1; serv1555067288->serv862383464_1; serv862383464_1->serv1624499521; serv30835212_1->serv1624499521; serv862383464_1->serv308564144_1; serv30835212_1->serv308564144_1; serv862383464_1->serv239131911; serv30835212_1->serv239131911; serv1624499521->serv1832796220; serv239131911->serv1832796220; serv308564144_1->serv1832796220; serv1624499521->serv1140112396; serv239131911->serv1140112396; serv308564144_1->serv1140112396; serv1624499521->serv447428610; serv239131911->serv447428610; serv308564144_1->serv447428610; serv1624499521->serv1209544629; serv239131911->serv1209544629; serv308564144_1->serv1209544629; serv1624499521->serv377996377; serv239131911->serv377996377; serv308564144_1->serv377996377; serv1140112396->serv1348409095_1; serv1832796220->serv1348409095_1; serv377996377->serv1348409095_1; serv447428610->serv1348409095_1; serv1209544629->serv1348409095_1; serv1140112396->serv2041092919; serv1832796220->serv2041092919; serv377996377->serv2041092919; serv447428610->serv2041092919; serv1209544629->serv2041092919; serv1140112396->serv586293076; serv1832796220->serv586293076; serv377996377->serv586293076; serv447428610->serv586293076; serv1209544629->serv586293076; serv1348409095_1->endNode; serv2041092919->endNode; serv586293076->endNode; }`


To generate a graph in a png format, please save the above description in a dot file (e.g., namely, dag.dot), and then use the following command line to generate a composition graph as shown in Figure below.

`dot -Tpng dag.dot  -o dag.png`


![dag_best](https://user-images.githubusercontent.com/20468313/130166954-2f3ca3ab-48c3-4670-a799-d45ab37dc246.png)

## An Video Example for Running MEEDA algorithms 
https://user-images.githubusercontent.com/20468313/129655225-f214f0a1-4147-408a-9ed2-ed17cf696cf5.mov

(An alternative video link from Vimeo: https://vimeo.com/588134937)

The command lines used in the video are listed and explained below:

1. `git clone https://github.com/chenwangnida/MENHBSA4SWSC.git`: Download our code repository to local PC (i.e, Save it to Desktop directory, as shown in the video).

2. `git clone https://github.com/chenwangnida/Dataset.git`: Download WSC dataset to local PC (i.e, Save it to Desktop directory, as shown in the video).

3. `mv ~/Desktop/MENHBSA4SWSC/exportedJar/MENHBSA4SWSC.jar ~/Desktop`: Move the runnable jar to the Desktop directory.

4 `mv /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/problem.xml /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/services-output.xml /Users/chenwang/Desktop/Dataset/wsc08_1/Set01MetaData/taxonomy.owl ~/Desktop`: Move all WSC08-01 related files (that includes problem.xml, services-output.xml and taxonomy.owl) to the Desktop directory.

5. `java -version`: Check whether JDK version 1.8 is loaded.

6. `/usr/libexec/java_home -V`: Check all the installed JDK versions in the local PC.

7. `export JAVA_HOME="/usr/libexec/java_home -v 1.8"`: Load JDK version 1.8

8. `java -version`: Double check whether we have loaded the right JDK.

9. `java -jar ~/Desktop/MENHBSA4SWSC.jar ~/Desktop/out.stat ~/Desktop/problem.xml ~/Desktop/services-output.xml ~/Desktop/taxonomy.owl 0 0`: Run the jar.
