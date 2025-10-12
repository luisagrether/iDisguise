# iDisguise
Plugin for CraftBukkit/Spigot 1.8–1.21 and Paper/Purpur 1.18–1.21.

## Basic information
This plugin allows you to turn into almost every entity that exists in Minecraft.  
More information can be found here: https://www.spigotmc.org/resources/idisguise.5509/

## Snapshot files
Compiled snapshot files may be downloaded over here: https://www.luisagrether.de/mc/repo/de/luisagrether/idisguise/idisguise/

## GitHub repository
The master branch should always hold a (hopefully) bug-free recommended release version of iDisguise. Small changes (such as bug fixes etc.) get pushed to the snapshot branch until they are released officially. If you would like to develop your own fork of this repository, make sure to build it upon the master branch to ensure proper functionality.

## Maven repository
````
<repository>
  <id>luisagrether-repo</id>
  <url>https://www.luisagrether.de/mc/repo/</url>
</repository>


<dependency>
  <groupId>de.luisagrether.idisguise</groupId>
  <artifactId>idisguise</artifactId>
  <version>6.0.1</version>
</dependency>
````

## Compiling
In order to compile the whole plugin you have to clone/download this repository and build the project _idisguise_ using Maven.  
On Windows, use _.\install-all.bat_ to compile and install all modules of iDisguise. The final jar file will be located under _/idisguise/target/idisguise-&lt;VERSION&gt;.jar_.
