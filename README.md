# HomeSecurity
A prototype for video surveillance using Android Things and an rpi3

This project makes use of Sinch for video transmission and Firebase for remotely turning the monitoring on the rpi3 on or off. 

The rpi3 also makes use of a PIR sensor to trigger video capture and transmission when it detects movement.

The module 'app' contains code for the rpi3.

The module 'companionApp' contains code for a standard android mobile app. This app can control the rpi3 and also receives video transmission from the rpi3
