package com.xiaoyi.camera.sdk;

public class P2PMessage {
    public int reqId;
    public int resId;
    public byte[] data;
    public IMessageResponse resp;
    public boolean needWaitResponse;
    public int error;

    public P2PMessage(int reqId, byte[] data) {
        this.reqId = reqId;
        this.data = data;
        needWaitResponse = false;
    }

    public P2PMessage(int reqId, int resId, byte[] data, IMessageResponse resp) {
        this.reqId = reqId;
        this.resId = resId;
        this.data = data;
        if (resp != null) {
            this.resp = resp;
        }
        needWaitResponse = true;
    }

    public static interface IMessageResponse {
        public boolean onResponse(byte[] data);

        public void onError(int error);// -1超时 -2摄像头没打开 -3rom版本低
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + reqId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        P2PMessage other = (P2PMessage) obj;
        if (reqId != other.reqId) return false;
        return true;
    }

}
