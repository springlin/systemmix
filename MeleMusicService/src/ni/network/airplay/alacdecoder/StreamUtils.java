/*
** StreamUtils.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

package ni.network.airplay.alacdecoder;

class StreamUtils
{
    public static void stream_read(final MyStream mystream, final int size, final int[] buf, final int startPos)
    {
        final byte[] byteBuf = new byte[size];
        final int bytes_read = stream_read(mystream, size, byteBuf, 0);
        for (int i = 0; i < bytes_read; i++) {
            buf[startPos + i] = byteBuf[i];
        }
    }

    public static int stream_read(final MyStream mystream, final int size, final byte[] buf, final int startPos)
    {
        int bytes_read = 0;

        try
        {
            bytes_read = mystream.stream.read(buf, startPos, size);
        }
        catch (final Exception err)
        {
            System.err.println("stream_read: exception thrown: " + err);
        }
        mystream.currentPos = mystream.currentPos + bytes_read;
        return bytes_read;
    }

    public static int stream_read_uint32(final MyStream mystream)
    {
        int v = 0;
        int tmp = 0;
        final byte[] bytebuf = mystream.read_buf;
        int bytes_read = 0;

        try
        {
            bytes_read = mystream.stream.read(bytebuf, 0, 4);
            mystream.currentPos = mystream.currentPos + bytes_read;
            tmp = (bytebuf[0] & 0xff);

            v = tmp << 24;
            tmp = (bytebuf[1] & 0xff);

            v = v | (tmp << 16);
            tmp = (bytebuf[2] & 0xff);

            v = v | (tmp << 8);

            tmp = (bytebuf[3] & 0xff);
            v = v | tmp;

        }
        catch (final Exception err)
        {
            System.err.println("stream_read_uint32: exception thrown: " + err);
        }

        return v;
    }

    public static int stream_read_int16(final MyStream mystream)
    {
        int v = 0;
        try
        {
            v = mystream.stream.readShort();
            mystream.currentPos = mystream.currentPos + 2;
        }
        catch (final Exception err)
        {
        }

        return v;
    }

    public static int stream_read_uint16(final MyStream mystream)
    {
        int v = 0;
        int tmp = 0;
        final byte[] bytebuf = mystream.read_buf;
        int bytes_read = 0;

        try
        {
            bytes_read = mystream.stream.read(bytebuf, 0, 2);
            mystream.currentPos = mystream.currentPos + bytes_read;
            tmp = (bytebuf[0] & 0xff);
            v = tmp << 8;
            tmp = (bytebuf[1] & 0xff);

            v = v | tmp;
        }
        catch (final Exception e)
        {
        }

        return v;
    }

    public static int stream_read_uint8(final MyStream mystream)
    {
        int v = 0;
        final byte[] bytebuf = mystream.read_buf;

        try
        {
            mystream.stream.read(bytebuf, 0, 1);
            v = (bytebuf[0] & 0xff);
            mystream.currentPos = mystream.currentPos + 1;
        }
        catch (final Exception e)
        {
        }

        return v;
    }

    public static void stream_skip(final MyStream mystream, final int skip)
    {
        final int toskip = skip;
        int bytes_read = 0;

        if (toskip < 0)
        {
            System.err.println("stream_skip: request to seek backwards in stream - not supported, sorry");
            return;
        }

        try
        {
            bytes_read = mystream.stream.skipBytes(toskip);
            mystream.currentPos = mystream.currentPos + bytes_read;
        }
        catch (final java.io.IOException ioe)
        {
        }
    }

    public static int stream_eof(final MyStream mystream)
    {
        // TODO

        return 0;
    }

    public static int stream_tell(final MyStream mystream)
    {
        return (mystream.currentPos);
    }

    public static int stream_setpos(final MyStream mystream, final int pos)
    {
        return -1;
    }

}
