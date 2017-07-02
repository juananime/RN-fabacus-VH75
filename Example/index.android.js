

import React, { Component } from 'react';
import {
    NativeModules,
    AppRegistry,
    StyleSheet,
    Text,
    View
} from 'react-native';

import AppContainer from './AppContainer'
export default class App extends Component {


    _onPressButton(sender) {
        console.log("XSXSXS ::: STTTTTTTTTTTs :: "+sender.nativeEvent.onPress);
        song1.play();

    }


    render() {
        return (
            <View style={styles.container}>



              <AppContainer>

              </AppContainer>



            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection:'column',
        padding:20,
        paddingTop:50,
        backgroundColor:'lightcyan'

    },
    waveform: {
        flex:1,
        margin:10,
        backgroundColor:'lightslategray'


    },
    welcome:{
        flex:0.2,
        marginLeft:10,
    }

});

AppRegistry.registerComponent('rn_vh75', () => App);
