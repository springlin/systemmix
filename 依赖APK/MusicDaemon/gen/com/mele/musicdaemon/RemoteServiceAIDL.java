/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\MusicDaemon\\src\\com\\mele\\musicdaemon\\RemoteServiceAIDL.aidl
 */
package com.mele.musicdaemon;
public interface RemoteServiceAIDL extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mele.musicdaemon.RemoteServiceAIDL
{
private static final java.lang.String DESCRIPTOR = "com.mele.musicdaemon.RemoteServiceAIDL";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mele.musicdaemon.RemoteServiceAIDL interface,
 * generating a proxy if needed.
 */
public static com.mele.musicdaemon.RemoteServiceAIDL asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mele.musicdaemon.RemoteServiceAIDL))) {
return ((com.mele.musicdaemon.RemoteServiceAIDL)iin);
}
return new com.mele.musicdaemon.RemoteServiceAIDL.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_installApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.installApp(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_startMirror:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.startMirror(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_stopMirror:
{
data.enforceInterface(DESCRIPTOR);
this.stopMirror();
reply.writeNoException();
return true;
}
case TRANSACTION_keyCode:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.keyCode(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_MouseEvent:
{
data.enforceInterface(DESCRIPTOR);
float _arg0;
_arg0 = data.readFloat();
float _arg1;
_arg1 = data.readFloat();
int _arg2;
_arg2 = data.readInt();
this.MouseEvent(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_killApp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.killApp(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_ioctrl:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.ioctrl(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_jni_i2c_open:
{
data.enforceInterface(DESCRIPTOR);
this.jni_i2c_open();
reply.writeNoException();
return true;
}
case TRANSACTION_jni_i2c_write:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int[] _arg1;
_arg1 = data.createIntArray();
int _arg2;
_arg2 = data.readInt();
this.jni_i2c_write(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeIntArray(_arg1);
return true;
}
case TRANSACTION_jni_i2c_writedev:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int[] _arg2;
_arg2 = data.createIntArray();
int _arg3;
_arg3 = data.readInt();
this.jni_i2c_writedev(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeIntArray(_arg2);
return true;
}
case TRANSACTION_jni_i2c_read:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int[] _result = this.jni_i2c_read(_arg0, _arg1);
reply.writeNoException();
reply.writeIntArray(_result);
return true;
}
case TRANSACTION_jni_i2c_close:
{
data.enforceInterface(DESCRIPTOR);
this.jni_i2c_close();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mele.musicdaemon.RemoteServiceAIDL
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public java.lang.String installApp(java.lang.String path, java.lang.String packagename) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
_data.writeString(packagename);
mRemote.transact(Stub.TRANSACTION_installApp, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void startMirror(java.lang.String ip, int port) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(ip);
_data.writeInt(port);
mRemote.transact(Stub.TRANSACTION_startMirror, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopMirror() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopMirror, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void keyCode(int code) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(code);
mRemote.transact(Stub.TRANSACTION_keyCode, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void MouseEvent(float x, float y, int action) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeFloat(x);
_data.writeFloat(y);
_data.writeInt(action);
mRemote.transact(Stub.TRANSACTION_MouseEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void killApp(java.lang.String packagename) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packagename);
mRemote.transact(Stub.TRANSACTION_killApp, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void ioctrl(java.lang.String cmd, java.lang.String var) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(cmd);
_data.writeString(var);
mRemote.transact(Stub.TRANSACTION_ioctrl, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void jni_i2c_open() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_jni_i2c_open, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void jni_i2c_write(int addr, int[] buf, int len) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(addr);
_data.writeIntArray(buf);
_data.writeInt(len);
mRemote.transact(Stub.TRANSACTION_jni_i2c_write, _data, _reply, 0);
_reply.readException();
_reply.readIntArray(buf);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void jni_i2c_writedev(int dev, int addr, int[] buf, int len) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(dev);
_data.writeInt(addr);
_data.writeIntArray(buf);
_data.writeInt(len);
mRemote.transact(Stub.TRANSACTION_jni_i2c_writedev, _data, _reply, 0);
_reply.readException();
_reply.readIntArray(buf);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int[] jni_i2c_read(int addr, int len) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(addr);
_data.writeInt(len);
mRemote.transact(Stub.TRANSACTION_jni_i2c_read, _data, _reply, 0);
_reply.readException();
_result = _reply.createIntArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void jni_i2c_close() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_jni_i2c_close, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_installApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startMirror = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_stopMirror = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_keyCode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_MouseEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_killApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_ioctrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_jni_i2c_open = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_jni_i2c_write = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_jni_i2c_writedev = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_jni_i2c_read = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_jni_i2c_close = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
}
public java.lang.String installApp(java.lang.String path, java.lang.String packagename) throws android.os.RemoteException;
public void startMirror(java.lang.String ip, int port) throws android.os.RemoteException;
public void stopMirror() throws android.os.RemoteException;
public void keyCode(int code) throws android.os.RemoteException;
public void MouseEvent(float x, float y, int action) throws android.os.RemoteException;
public void killApp(java.lang.String packagename) throws android.os.RemoteException;
public void ioctrl(java.lang.String cmd, java.lang.String var) throws android.os.RemoteException;
public void jni_i2c_open() throws android.os.RemoteException;
public void jni_i2c_write(int addr, int[] buf, int len) throws android.os.RemoteException;
public void jni_i2c_writedev(int dev, int addr, int[] buf, int len) throws android.os.RemoteException;
public int[] jni_i2c_read(int addr, int len) throws android.os.RemoteException;
public void jni_i2c_close() throws android.os.RemoteException;
}
