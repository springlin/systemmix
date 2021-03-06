/*
 * plist - An open source library to parse and generate property lists
 * Copyright (C) 2011 Daniel Dreibrodt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ni.network.plist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Parses property lists that are in Apple's binary format. Use this class when
 * you are sure about the format of the property list. Otherwise use the
 * PropertyListParser class. Parsing is done by calling the static
 * <code>parse</code> methods.
 * 
 * @author Daniel Dreibrodt
 */
public class BinaryPropertyListParser {

    /** property list in bytes **/
    private byte[] bytes;
    /** Length of an offset definition in bytes **/
    private int offsetSize;
    /** Length of an object reference in bytes **/
    private int objectRefSize;
    /** Number of objects stored in this property list **/
    private int numObjects;
    /** Reference to the top object of the property list **/
    private int topObject;
    /** Offset of the offset table from the beginning of the file **/
    private int offsetTableOffset;
    /** The table holding the information at which offset each object is found **/
    private int[] offsetTable;

    /**
     * Private constructor so that instantiation is fully controlled by the
     * static parse methods.
     * 
     * @see BinaryPropertyListParser#parse(byte[])
     */
    private BinaryPropertyListParser() {
        /** empty **/
    }

    /**
     * Parses a binary property list from a byte array.
     * 
     * @param data The binary property list's data.
     * @return The root object of the property list. This is usally a
     *         NSDictionary but can also be a NSArray.
     * @throws Exception When an error occurs during parsing.
     */
    public static NSObject parse(final byte[] data) throws Exception {
        final BinaryPropertyListParser parser = new BinaryPropertyListParser();
        return parser.doParse(data);
    }

    /**
     * Parses a binary property list from a byte array.
     * 
     * @param data The binary property list's data.
     * @return The root object of the property list. This is usally a
     *         NSDictionary but can also be a NSArray.
     * @throws When an error occurs during parsing.
     */
    private NSObject doParse(final byte[] data) throws Exception {
        bytes = data;
        final String magic = new String(copyOfRange(bytes, 0, 8));
        if (!magic.startsWith("bplist")) {
            throw new Exception("The given data is no binary property list. Wrong magic bytes: " + magic);
        }

        //int version = Integer.parseInt(magic.substring(6));
        // 00 - OS X Tiger and earlier
        // 01 - Leopard
        // 0? - Snow Leopard and later

        /*
         * Handle trailer, last 32 bytes of the file
         */
        final byte[] trailer = copyOfRange(bytes, bytes.length - 32, bytes.length);
        //6 null bytes (index 0 to 5)
        offsetSize = (int)parseUnsignedInt(copyOfRange(trailer, 6, 7));
        //System.out.println("offsetSize: "+offsetSize);
        objectRefSize = (int)parseUnsignedInt(copyOfRange(trailer, 7, 8));
        //System.out.println("objectRefSize: "+objectRefSize);
        numObjects = (int)parseUnsignedInt(copyOfRange(trailer, 8, 16));
        //System.out.println("numObjects: "+numObjects);
        topObject = (int)parseUnsignedInt(copyOfRange(trailer, 16, 24));
        //System.out.println("topObject: "+topObject);
        offsetTableOffset = (int)parseUnsignedInt(copyOfRange(trailer, 24, 32));
        //System.out.println("offsetTableOffset: "+offsetTableOffset);

        /*
         * Handle offset table
         */
        offsetTable = new int[numObjects];

        for (int i = 0; i < numObjects; i++) {
            final byte[] offsetBytes = copyOfRange(bytes, offsetTableOffset + i * offsetSize, offsetTableOffset + (i + 1) * offsetSize);
            offsetTable[i] = (int)parseUnsignedInt(offsetBytes);
            /*System.out.print("Offset for Object #"+i+" is "+offsetTable[i]+" [");
            for(byte b:offsetBytes) System.out.print(Integer.toHexString(b)+" ");
            System.out.println("]");*/
        }

        return parseObject(topObject);
    }

    /**
     * Parses a binary property list from an input stream.
     * 
     * @param is The input stream that points to the property list's data.
     * @return The root object of the property list. This is usally a
     *         NSDictionary but can also be a NSArray.
     * @throws Exception When an error occurs during parsing.
     */
    public static NSObject parse(final InputStream is) throws Exception {
        //Read all bytes into a list
        final byte[] buf = PropertyListParser.readAll(is, Integer.MAX_VALUE);
        is.close();
        return parse(buf);
    }

    /**
     * Parses a binary property list file.
     * 
     * @param f The binary property list file
     * @return The root object of the property list. This is usally a
     *         NSDictionary but can also be a NSArray.
     * @throws Exception When an error occurs during parsing.
     */
    public static NSObject parse(final File f) throws Exception {
        if (f.length() > Runtime.getRuntime().freeMemory()) {
            throw new Exception("To little heap space available! Wanted to read " + f.length() + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
        }
        return parse(new FileInputStream(f));
    }

    /**
     * Parses an object inside the currently parsed binary property list. For
     * the format specification check <a
     * href="http://www.opensource.apple.com/source/CF/CF-635/CFBinaryPList.c">
     * Apple's binary property list parser implementation</a>.
     * 
     * @param obj The object ID.
     * @return The parsed object.
     * @throws java.lang.Exception When an error occurs during parsing.
     */
    private NSObject parseObject(final int obj) throws Exception {
        final int offset = offsetTable[obj];
        final byte type = bytes[offset];
        final int objType = (type & 0xF0) >> 4; //First  4 bits
        final int objInfo = (type & 0x0F); //Second 4 bits
        switch (objType) {
        case 0x0: {
            //Simple
            switch (objInfo) {
            case 0x0: {
                //null
                return null;
            }
            case 0x8: {
                //false
                return new NSNumber(false);
            }
            case 0x9: {
                //true
                return new NSNumber(true);
            }
            case 0xF: {
                //filler byte
                return null;
            }
            }
            break;
        }
        case 0x1: {
            //integer
            final int length = (int)Math.pow(2, objInfo);
            if (length < Runtime.getRuntime().freeMemory()) {
                return new NSNumber(copyOfRange(bytes, offset + 1, offset + 1 + length), NSNumber.INTEGER);
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0x2: {
            //real
            final int length = (int)Math.pow(2, objInfo);
            if (length < Runtime.getRuntime().freeMemory()) {
                return new NSNumber(copyOfRange(bytes, offset + 1, offset + 1 + length), NSNumber.REAL);
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0x3: {
            //Date
            if (objInfo != 0x3) {
                System.err.println("Unknown date type :" + objInfo + ". Parsing anyway...");
            }
            return new NSDate(copyOfRange(bytes, offset + 1, offset + 9));
        }
        case 0x4: {
            //Data
            int dataoffset = 1;
            int length = objInfo;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                dataoffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            if (length < Runtime.getRuntime().freeMemory()) {
                return new NSData(copyOfRange(bytes, offset + dataoffset, offset + dataoffset + length));
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0x5: {
            //ASCII String
            int length = objInfo;
            int stroffset = 1;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                stroffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            if (length < Runtime.getRuntime().freeMemory()) {
                return new NSString(copyOfRange(bytes, offset + stroffset, offset + stroffset + length), "ASCII");
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0x6: {
            //UTF-16-BE String
            int length = objInfo;
            int stroffset = 1;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                stroffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            //length is String length -> to get byte length multiply by 2, as 1 character takes 2 bytes in UTF-16
            length *= 2;
            if (length < Runtime.getRuntime().freeMemory()) {
                return new NSString(copyOfRange(bytes, offset + stroffset, offset + stroffset + length), "UTF-16BE");
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0x8: {
            //UID
            final int length = objInfo + 1;
            if (length < Runtime.getRuntime().freeMemory()) {
                return new UID(String.valueOf(obj), copyOfRange(bytes, offset + 1, offset + 1 + length));
            }
            else {
                throw new Exception("To little heap space available! Wanted to read " + length + " bytes, but only " + Runtime.getRuntime().freeMemory() + " are available.");
            }
        }
        case 0xA: {
            //Array
            int length = objInfo;
            int arrayoffset = 1;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                arrayoffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            if (length * objectRefSize > Runtime.getRuntime().freeMemory()) {
                throw new Exception("To little heap space available!");
            }
            final NSArray array = new NSArray(length);
            for (int i = 0; i < length; i++) {
                final int objRef = (int)parseUnsignedInt(copyOfRange(bytes,
                        offset + arrayoffset + i * objectRefSize,
                        offset + arrayoffset + (i + 1) * objectRefSize));
                array.setValue(i, parseObject(objRef));
            }
            return array;

        }
        case 0xC: {
            //Set
            int length = objInfo;
            int arrayoffset = 1;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                arrayoffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            if (length * objectRefSize > Runtime.getRuntime().freeMemory()) {
                throw new Exception("To little heap space available!");
            }
            final NSSet set = new NSSet();
            for (int i = 0; i < length; i++) {
                final int objRef = (int)parseUnsignedInt(copyOfRange(bytes,
                        offset + arrayoffset + i * objectRefSize,
                        offset + arrayoffset + (i + 1) * objectRefSize));
                set.addObject(parseObject(objRef));
            }
            return set;
        }
        case 0xD: {
            //Dictionary
            int length = objInfo;
            int dictoffset = 1;
            if (objInfo == 0xF) {
                final int int_type = bytes[offset + 1];
                final int intType = (int_type & 0xF0) / 0xF;
                if (intType != 0x1) {
                    System.err.println("UNEXPECTED LENGTH-INT TYPE! " + intType);
                }
                final int intInfo = int_type & 0x0F;
                final int intLength = (int)Math.pow(2, intInfo);
                dictoffset = 2 + intLength;
                if (intLength < 3) {
                    length = (int)parseUnsignedInt(copyOfRange(bytes, offset + 2, offset + 2 + intLength));
                }
                else {
                    length = new BigInteger(copyOfRange(bytes, offset + 2, offset + 2 + intLength)).intValue();
                }
            }
            if (length * 2 * objectRefSize > Runtime.getRuntime().freeMemory()) {
                throw new Exception("To little heap space available!");
            }
            //System.out.println("Parsing dictionary #"+obj);
            final NSDictionary dict = new NSDictionary();
            for (int i = 0; i < length; i++) {
                final int keyRef = (int)parseUnsignedInt(copyOfRange(bytes,
                        offset + dictoffset + i * objectRefSize,
                        offset + dictoffset + (i + 1) * objectRefSize));
                final int valRef = (int)parseUnsignedInt(copyOfRange(bytes,
                        offset + dictoffset + (length * objectRefSize) + i * objectRefSize,
                        offset + dictoffset + (length * objectRefSize) + (i + 1) * objectRefSize));
                final NSObject key = parseObject(keyRef);
                final NSObject val = parseObject(valRef);
                //System.out.println("  DICT #"+obj+": Mapped "+key.toString()+" to "+val.toString());
                dict.put(key.toString(), val);
            }
            return dict;
        }
        default: {
            System.err.println("Unknown object type: " + objType);
        }
        }
        return null;
    }

    /**
     * Parses an unsigned integers from a byte array.
     * 
     * @param bytes The byte array containing the unsigned integer.
     * @return The unsigned integer represented by the given bytes.
     */
    public static final long parseUnsignedInt(final byte[] bytes) {
        long l = 0;
        for (final byte b : bytes) {
            l <<= 8;
            l |= b & 0xFF;
        }
        l &= 0xFFFFFFFFL;
        return l;
    }

    /**
     * Parses longs from a (big-endian) byte array.
     * 
     * @param bytes The bytes representing the long integer.
     * @return The long integer represented by the given bytes.
     */
    public static final long parseLong(final byte[] bytes) {
        long l = 0;
        for (final byte b : bytes) {
            l <<= 8;
            l |= b & 0xFF;
        }
        return l;
    }

    /**
     * Parses doubles from a (big-endian) byte array.
     * 
     * @param bytes The bytes representing the double.
     * @return The double represented by the given bytes.
     */
    public static final double parseDouble(final byte[] bytes) {
        if (bytes.length == 8) {
            return Double.longBitsToDouble(parseLong(bytes));
        }
        else if (bytes.length == 4) {
            return Float.intBitsToFloat((int)parseLong(bytes));
        }
        else {
            throw new IllegalArgumentException("bad byte array length " + bytes.length);
        }
    }

    /**
     * Copies a part of a byte array into a new array.
     * 
     * @param src The source array.
     * @param startIndex The index from which to start copying.
     * @param endIndex The index until which to copy.
     * @return The copied array.
     */
    public static byte[] copyOfRange(final byte[] src, final int startIndex, final int endIndex)
    {
        final int length = endIndex - startIndex;
        if (length < 0) {
            throw new IllegalArgumentException("startIndex (" + startIndex + ")" + " > endIndex (" + endIndex + ")");
        }
        final byte[] dest = new byte[length];
        System.arraycopy(src, startIndex, dest, 0, length);
        return dest;
    }
}
