
# react-native-vh75-reader

## Getting started

`$ npm install react-native-vh75-reader --save`

### Mostly automatic installation

`$ react-native link react-native-vh75-reader`

### Manual installation



#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.FabacusVh75ReaderPackage;` to the imports at the top of the file
  - Add `new FabacusVh75ReaderPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-vh75-reader'
  	project(':react-native-vh75-reader').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-vh75-reader/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-vh75-reader')
  	```



## Usage
With the rn-vh75-reader plugin for React Native, you will be able to connect to a Bluetooth paired Vanch VH-75 device from Vanch Technologies.

Right now, just suporting Android platforms.

At the moment, the main features includes: 

- Paired devices detection.
- Connection to the choosen paired device.
- Connection status retrieval.
- Start and Stop scanning process.
- Scanning process status retrieval.
- Array of tags Id's rewtrieval.

Basic init:
```javascript
import RFID from "rn-vh75-reader";

constructor(props) {
        super(props);
        
        // Assign the callback to device connection satus:
        RFID.onDeviceConnectionStatusChanged(this.onDeviceConnectionStatusChanged.bind(this));
        
        // Assign the callback to device scanning satus:
        RFID.onDeviceScanningStatusChanged(this.onDeviceScanningStatusChanged.bind(this));
 }

```
In order to get an array of the paired devices:
```javascript
  RFID.getDevices(this.onDevicesDetected.bind(this));
```
 
To activate/deactivate scan process:
```javascript
 RFID.activateScan();
```
 
 To add a callback for when the device is sending a tags ID's hash:
```javascript
RFID.onTagReceived(this.onTagReceived.bind(this));
```


