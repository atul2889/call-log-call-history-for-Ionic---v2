1) Install device plugin by using following command:

$ ionic cordova plugin add cordova-plugin-device
$ npm install --save @ionic-native/device

2) Replace the device.java file at this path :

platforms\android\src\org\apache\cordova\device

3) add the manifest code in AndroidManifest.xml at your root folder. 

4) finally add the code in your .ts file from component.ts

5) Add the html code in your view from view.html