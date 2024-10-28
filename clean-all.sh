#!/bin/bash

echo Limpando CT-e ...
rm ./CTe/*.class
rm ./CTe/*.jar
rm ./CTe/derby.log
rm ./CTe/*.csv
rm ./CTe/CTe_ZIP/ -rf

echo Limpando NF-e ...
rm ./NFe/*.class
rm ./NFe/*.jar
rm ./NFe/derby.log
rm ./NFe/*.csv
rm ./NFe/NFe_ZIP/ -rf

echo Limpo!


