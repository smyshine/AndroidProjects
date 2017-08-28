package com.lib.ble.manager;

/**
 * Created by SMY on 2017/3/15.
 */

public class BleMessage {
    String uuid;
    String value;
    int operation;

    BleMessage(String uuid, String value, int operation){
        this.uuid = uuid;
        this.value = value;
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null){
            return false;
        }
        if (!(o instanceof BleMessage)){
            return false;
        }
        BleMessage other = (BleMessage) o;
        if (this.operation != other.operation){
            return false;
        }
        return true;
    }
}
