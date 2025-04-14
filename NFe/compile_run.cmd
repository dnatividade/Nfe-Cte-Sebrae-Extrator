#echo off
del *.jar
del *.class
del derby.log

javac -cp "..\derby-10_16_1_1\lib\derby.jar" ExtractNfeZipGUI.java
jar cfm ExtractNfeZipGUI.jar MANIFEST.MF *.class
del *.class

java -jar ExtractNfeZipGUI.jar

exit 0
