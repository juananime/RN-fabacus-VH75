/**
 * Created by Fabacus on 18/06/2017.
 */
import React, {Component} from 'react';
import {
    StyleSheet,
    Image,
    TextInput,
    TouchableHighlight,
    Text,
    View,
    Platform,
    ListView,

} from 'react-native';

import RFID from "rn-vh75-reader";
class AppContainer extends Component {

    constructor(props) {
        super(props);
        const ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});
        this.tags=[];
        this.state={
            devicesFound:false,
            connected:false,
            dataSource: ds.cloneWithRows(this.tags),
            statusConnection : 'NOT_CONNECTED',
            isScanning:false,
        }
        this.tagsHash = {};
        this.devicesList = [];
        this.getDevices=this.getDevices.bind(this)
        this.startStopScan=this.startStopScan.bind(this)
        RFID.onTagReceived(this.onTagReceived.bind(this));
        RFID.onDeviceConnectionStatusChanged(this.onDeviceConnectionStatusChanged.bind(this));
        RFID.onDeviceScanningStatusChanged(this.onDeviceScanningStatusChanged.bind(this));

    }

    /**
     * RFID getDevices
     */
    getDevices() {
        console.log('getDevices :::: ')
        RFID.getDevices(this.onDevicesDetected.bind(this));
	}

	/**
	* RFID getDevices callback, list of the devices paired and ready to connect
	*/
    onDevicesDetected(payload) {

        console.log('onDevicesDetected :::: ',payload)
        this.devicesList.push(payload)
		this.setState({devicesFound: true, connectedDevice:payload})
		this.connectToDevice(payload.DeviceName)
    }

	/**
	* onTagReceived, retrieves a payload with tags ID's hash
	*/
	onTagReceived(payload){

        var array_keys = new Array();
        var array_values = new Array();

        for (var key in payload) {
            this.tagsHash[key] = payload[key];
        }


        for (var key in this.tagsHash) {
            array_keys.push(key);
            array_values.push(this.tagsHash[key]);
        }


        this.tags = array_values;
        let newDataSource = this.state.dataSource.cloneWithRows(array_values);
        this.setState({

            dataSource: newDataSource,
        });
    }

	/**
     *  RFID.onDeviceConnectionStatusChanged callback, retrieves payload status
     *  payload.status => CONNECTING, CONNECTING_OK , CONNECTING_FAILE , DISCONNECT
     */
    onDeviceConnectionStatusChanged(payload){
        this.setState({statusConnection:payload.status})
        payload.status == 'CONNECTING_OK'? this.setState({connected:true}):this.setState({connected:false})
	}

	/**
	*  RFID.onDeviceScanningStatusChanged callback, retrieves payload scanningStatus
    *  payload.scanningStatus => ON, OFF
    */
    onDeviceScanningStatusChanged(payload){
        payload.scanningStatus === 'ON'? this.setState({isScanning:true}): this.setState({isScanning:false});

    }
    
	/**
     * 	RFID.connect(deviceName);
     *  Connect to the device identified by string name.
     */
    connectToDevice(deviceName){
        RFID.connect(deviceName);
    }
    
	/**
    * 	RFID.activateScan();
    *  Start/Stop scanning process, it needs always to be connected for such action.
    */
    startStopScan(){
        RFID.activateScan();
    }
    
    renderScanBtn(){
        if(this.state.connected){
            return(
                <TouchableHighlight  style={styles.btn} onPress={() => {this.startStopScan()}}>
                    <Text style={{color: 'white'}}>
                        Start/Stop Scan ==> {this.state.isScanning ? 'ON' : 'OFF'}
                    </Text>
                </TouchableHighlight>
            )
        }else{
            return null;
        }
    }

    render() {


        return (
            <View >
                <TouchableHighlight onPress={() => {this.getDevices()}} style={styles.btn}>
                    <Text style={{color: 'white'}}>
                        {this.state.devicesFound ? this.state.connectedDevice.DeviceName : 'Scan VH75 devices'}
                    </Text>
                </TouchableHighlight>

                <View style={styles.separator} />
                <Text style={styles.btn}>
                    {this.state.statusConnection}
                </Text>
                <View style={styles.separator} />

                {this.renderScanBtn()}

                <ListView
                    enableEmptySections
                    dataSource={this.state.dataSource}
                    renderRow={
                        (rowData) =>
                            <View style={{ height: 30, backgroundColor: 'white', textAlign:'center' }}>
                                <Text style={{color:'black', fontSize: 15 }}>{rowData}</Text>
                            </View>
                    }
                />
            </View>
        )
    }
}

const styles = StyleSheet.create({

    separator:{
        height:10,
        backgroundColor:'transparent',
    },
    btn: {
        color:'white',
        fontSize: 20,
        textAlign: 'center',
        padding:10,
        margin:20,
        backgroundColor: '#000',

    }

});
export default AppContainer;