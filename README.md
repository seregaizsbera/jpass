JPass
=====

Overview
--------
JPass is a simple, small, portable password manager application with strong encryption. It allows you to store usernames, passwords, URLs and generic notes in an encrypted file protected by one master password.

Features:

* Strong encryption - AES-256-CBC algorithm (SHA-256 is used as password hash)
* Portable - single jar file which can be carried on a USB stick
* Built-in random password generator
* Organize all your username, password, URL and notes information in one file
* Data import/export in XML format

Usage
-----
Java 17 or later is recommended to run JPass.
You can also run the application from the command line by typing (the password file is optional):

    java -jar jpass.jar [password_file]

How to build
--------------
* Gradle: `gradle clean fatJar`