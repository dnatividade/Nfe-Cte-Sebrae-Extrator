#!/bin/bash
rm *.jar
rm *.class
rm derby.log

javac -cp "derby-10_16_1_1/lib/derby.jar" ExtractNfeZipGUI.java
jar cfm ExtractNfeZipGUI.jar MANIFEST.MF *.class
rm *.class

exit 0

