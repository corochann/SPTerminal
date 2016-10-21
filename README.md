# SPTerminal
Serial Port Terminal

## Description
Terminal application SPecially developed for Serial Port. 

It is developed by JAVA.

## Dependencies
The software uses following libraries
 - JNA 4.1.0
 
 Enhance native support for JAVA.
 
 [github page](https://github.com/java-native-access/jna)
 
 [Download page](https://mvnrepository.com/artifact/net.java.dev.jna/jna/4.1.0)
 - PureJavaComm 1.0.0
 
 JAVA implementation library to support serial ports.
 
 [github page](https://github.com/nyholku/purejavacomm)
 
 [Official page](http://www.sparetimelabs.com/purejavacomm/purejavacomm.php)
 - Jansi
 
 ANSI Escape codes support for JAVA.
 
 [github page](https://github.com/fusesource/jansi)
 
 [Official page](http://fusesource.github.io/jansi/)
 - Webcam Capture
 
 Webcam library for JAVA
 
 [github page](https://github.com/sarxos/webcam-capture)
 
 [Official page](http://webcam-capture.sarxos.pl/)
 - JavaCV
 
 Java interface to OpenCV and more
 
 [github page](https://github.com/bytedeco/javacv)
 
 [Bytedeco page](http://bytedeco.org/) 
 - JUnit
 
 Unit test tool for JAVA
 
 [github page](https://github.com/junit-team/junit4)
 
 [Official page](http://junit.org/junit4/) 


## Plugin
Some plugins can be implemented in this SPTerminal.
Currently it is supporting following type of plugins. 
(See interface, located at com.corochann.spterminal.plugin)

- MenuItemPlugin
  The action can be invoked from "Plugins" menu.
  
To develop plugin, 

1. Create a class which implements above plugin interface 

2. Create a file with resources/META-INF/services/[fully-qualified-classpath-of-plugin-interface] (e.g. com.corochann.spterminal.plugin.MenuItemPlugin)
   In this file, write [fully-qualified-classpath-name-of-your-class] made in step 1.

3. Build jar file (e.g. 'gradlew jar' command)

4. Put the jar file into 'plugins' folder of spterminal application. 
