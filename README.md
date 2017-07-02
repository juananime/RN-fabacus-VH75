
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
```javascript
import FabacusVh75Reader from 'react-native-vh75-reader';

// TODO: What do with the module?
FabacusVh75Reader;
```
  
