#!/bin/bash
rm *.jar
rm *.class
rm derby.log

javac -cp "../derby-10_16_1_1/lib/derby.jar" ExtractCteZipGUI.java
jar cfm ExtractCteZipGUI.jar MANIFEST.MF *.class
rm *.class

java -jar ExtractCteZipGUI.jar

exit 0

