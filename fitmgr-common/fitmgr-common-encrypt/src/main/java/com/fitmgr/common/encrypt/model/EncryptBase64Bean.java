package com.fitmgr.common.encrypt.model;

public class EncryptBase64Bean {

    private byte[] keyTypeBytes;

    private byte[] keyVersionBytes;

    private byte[] ivBytes;

    private byte[] dataEncryptBytes;

    public byte[] getKeyTypeBytes() {
        byte[] temp = keyTypeBytes;
        return temp;
    }

    public void setKeyTypeBytes(byte[] keyTypeBytes) {
        this.keyTypeBytes = keyTypeBytes.clone();
    }

    public byte[] getKeyVersionBytes() {
        byte[] temp = keyVersionBytes;
        return temp;
    }

    public void setKeyVersionBytes(byte[] keyVersionBytes) {
        this.keyVersionBytes = keyVersionBytes.clone();
    }

    public byte[] getIvBytes() {
        byte[] temp = ivBytes;
        return temp;
    }

    public void setIvBytes(byte[] ivBytes) {
        this.ivBytes = ivBytes.clone();
    }

    public byte[] getDataEncryptBytes() {
        byte[] temp = dataEncryptBytes;
        return temp;
    }

    public void setDataEncryptBytes(byte[] dataEncryptBytes) {
        this.dataEncryptBytes = dataEncryptBytes.clone();
    }
}