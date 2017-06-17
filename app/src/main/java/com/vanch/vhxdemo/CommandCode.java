package com.vanch.vhxdemo;


public enum CommandCode {
	SaveLabelToSDCard((byte) 0x01),
	GetVersion((byte) 0x02), 
	AddLableID((byte) 0x03), 
	DelLableID((byte) 0x04), 
	GetLableID((byte) 0x05), 
	SetReportFilter((byte) 0x07), 
	GetReportFilter((byte) 0x08), 
	ReadHandsetParam((byte) 0x06), 
	WriteHanderParam((byte) 0x09),
	SetReaderMode((byte) 0x0b),
	WriteFactoryParam((byte) 0x0c), 
	ReadFactoryParameter((byte) 0x0d), 
	SetReaderTime((byte) 0x11), 
	GetReaderTime((byte) 0x12), 
	GetRecord((byte) 0x16), 
	DeleteAllRecord((byte) 0x17), 
	SetBtBaudRate((byte) 0x8f), 
	GetBtBaudRate((byte) 0x90), 
	GetHandsetID((byte) 0x8c), 
	SetHandsetID((byte) 0x8b), 
	GetBluetoothName((byte) 0x8e), 
	SetBluetoothName((byte) 0x8d), 
	readWordBlock((byte) 0xec), 
	writeWordBlock((byte) 0xeb), 
	setLock((byte) 0xea), 
	eraseBlock((byte) 0xe9), 
	killTag((byte) 0xe8), 
	writeEpc((byte) 0xe7), 
	blockLock((byte) 0xe6), 
	listTag((byte) 0xee), 
	getIdList((byte) 0xed), 
	InvalidCode((byte) -1);

	byte code;


	public byte getCode() {
		return code;
	}

	private CommandCode(byte code) {
		this.code = code;
	}


	public static CommandCode getInstance(byte code) {
		
		CommandCode[] codes = CommandCode.values();
		for (CommandCode commandCode : codes) {
			if (commandCode.getCode() == code) {
				return commandCode;
			}
		}
		
		return InvalidCode;
		

	}
}
