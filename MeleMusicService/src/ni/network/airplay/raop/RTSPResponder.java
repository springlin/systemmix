package ni.network.airplay.raop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import android.util.Log;
import ni.network.airplay.IAirplayAudioImpl;
import ni.network.airplay.IAirplayImpl;
import ni.network.util.Base64;

/**
 * An primitive RTSP responder for replying iTunes
 * 
 * @author bencall
 */
public class RTSPResponder extends Thread {

    private final IAirplayAudioImpl audioImpl;
    private final IAirplayImpl airplayImpl;
    private Socket socket; // Connected socket
    private int[] fmtp;
    private byte[] aesiv, aeskey; // ANNOUNCE request infos
    private AudioServer serv; // Audio listener
    byte[] hwAddr;
    private final BufferedReader in;
    private static final Pattern completedPacket = Pattern.compile("(.*)\r\n\r\n");
    static private PrivateKey pk = null;
    private final String TAG = "RTSPResponder";
	private boolean stopThread = false;

    public RTSPResponder(final byte[] hwAddr, final Socket socket, final IAirplayImpl airplayImpl) throws IOException {
        this.audioImpl = airplayImpl.getAudio();;
        this.airplayImpl = airplayImpl;
        this.hwAddr = hwAddr;
        this.socket = socket;
        this.setName(TAG);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        if (pk == null) {
            final String strPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDn10TyouJ4i2wf" +
                                         "VaCOtwVEqPp5RaqL5sYs5fUcvdTcaEL+PRCD3S7ewb/UJS3ALm85i98OYUjqhIVe" +
                                         "LkQtptYmZPZ0ofMEkpreT2iT7y325xGox3oNkcnZgIIuUNEpIq/qQOqfDhTA92k4" +
                                         "xfOIL8AyPdn+VRVfUbtZIcIBYp/XM1LV4u+qv5ugSNe4E6K2dn9sPM8etM5nPQN7" +
                                         "DS6jDF//6wb40Ird5AlXGpxon+8QcohV3Yz7movvXIlD7ztfqhXd5pi+3fNZlgPr" +
                                         "Pm9hNyu2KPZVn1maeL9QBoeqf0l2wFYtQSlW+JieGKY1W9gVl4JeD8h1ND7HghF2" +
                                         "Jc2/mER7AgMBAAECggEBAOXwDHL1d9YEuaTOQSKqhLAXQ+yZWs/Mf0qyfAsYf5Bm" +
                                         "W+NZ3xJZgY3u7XnTse+EXk3d2smhVTc7XicNjhMVABouUn1UzfkACldovJjURGs3" +
                                         "u70Asp3YtTBiEzsqbnf07jJQViKQTacg+xwSwDmW2nE6BQYJjtvt7Pk20PqcvVkp" +
                                         "q7Dto1eZUC+YlNy4/FaaiS0XeAMkorbDFm40ZwkTS4VAQbhncGtY/vKg25Ird2KL" +
                                         "aOaWk8evQ78qc9C3Mjd6C6F7RPBR6b95hJ3LMzJXH9inCTPC1gvexHmTSj2spAu2" +
                                         "8vN8Cp0HEG6tyLNpoD8vQciACY6K3UYkDaxozFNU82ECgYEA9+C/Wh5nGDGai2IJ" +
                                         "wxcURARZ+XOFZhOxeuFQi7PmMW5rf0YtL31kQSuEt2vCPysMNWJFUnmyQ6n3MW+V" +
                                         "gAezTGH3aOLUTtX/KycoF+wys+STkpIo+ueOd0yg9169adWSAnmPEW42DGQ4sy4b" +
                                         "2LncHjIy8NMJGIg8xD743aIsNpECgYEA72//+ZTx5WRBqgA1/RmgyNbwI3jHBYDZ" +
                                         "xIQgeR30B8WR+26/yjIsMIbdkB/S+uGuu2St9rt5/4BRvr0M2CCriYdABgGnsv6T" +
                                         "kMrMmsq47Sv5HRhtj2lkPX7+D11W33V3otA16lQT/JjY8/kI2gWaN52kscw48V1W" +
                                         "CoPMMXFTyEsCgYEA0OuvvEAluoGMdXAjNDhOj2lvgE16oOd2TlB7t9Pf78fWeMZo" +
                                         "LT+tcTRBvurnJKCewJvcO8BwnJEz1Ins4qUa3QUxJ0kPkobRc8ikBU3CCldcfkwM" +
                                         "mDT0od6HSRej5ADq+IUGLbXLfjQ2iecR91/ng9fhkZL9dpzVQr6kuQEH7NECgYB/" +
                                         "QBjcfeopLaUwQjhvMQWgd4rcbz3mkNordMUFWYPt9XRmGi/Xt96AU8zA4gjwyKxi" +
                                         "b1l9PZnSzlGjezmuS36e8sB18L89g8rNMtqWkZLCiZI1glwH0c0yWaGQbNzUmcth" +
                                         "PiLJTLHqlxkGYJ3xsPSLBj8XNyA0NpSZtf35cO9EDQKBgQCQTukg+UTvWq98lCCg" +
                                         "D16bSAgsC4Tg+7XdoqImd9+3uEiNsr7mTJvdPKxm+jIOdvcc4q8icru9dsq5TghK" +
                                         "DEHZsHcdxjNAwazPWonaAbQ3mG8mnPDCFuFeoUoDjNppKvDrbbAOeIArkyUgTS0g" +
                                         "Aoo/jLE0aOgPZBiOEEa6G+RYpg==";
            final byte[] keyData = Base64.decode(strPrivateKey, Base64.DEFAULT);
            try {
                pk = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyData));
            }
            catch (final Exception e) {
                Log.i("AirPlay",e.toString());
            }
        }
    }

    public RTSPResponse handlePacket(final RTSPPacket packet) {
        // We init the response holder
        final RTSPResponse response = new RTSPResponse("RTSP/1.0 200 OK");
        response.append("Audio-Jack-Status", "connected; type=analog");
        response.append("CSeq", packet.valueOfHeader("CSeq"));

        // Apple Challenge-Response field if needed
        String challenge;
        if ((challenge = packet.valueOfHeader("Apple-Challenge")) != null) {
            // BASE64 DECODE
            final byte[] decoded = Base64.decode(challenge, Base64.DEFAULT);

            // IP byte array
            //byte[] ip = socket.getLocalAddress().getAddress();
            final SocketAddress localAddress = socket.getLocalSocketAddress(); //.getRemoteSocketAddress();

            final byte[] ip = ((InetSocketAddress)localAddress).getAddress().getAddress();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Challenge
            try {
                out.write(decoded);
                // IP-Address
                out.write(ip);
                // HW-Addr
                out.write(hwAddr);

                // Pad to 32 Bytes
                final int padLen = 32 - out.size();
                for (int i = 0; i < padLen; ++i) {
                    out.write(0x00);
                }

            }
            catch (final IOException e) {
                e.printStackTrace();
            }

            // RSA
            final byte[] crypted = this.encryptRSA(out.toByteArray());

            // Encode64
            String ret = Base64.encodeToString(crypted, Base64.DEFAULT);

            // On retire les ==
            ret = ret.replace("=", "").replace("\r", "").replace("\n", "");

            // Write
            response.append("Apple-Response", ret);
        }

        // Paquet request
        final String REQ = packet.getReq();
        if (null != REQ) {
        	if (REQ.contentEquals("OPTIONS")) {
                // The response field
                response.append("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");

            }
            else if (REQ.contentEquals("ANNOUNCE")) {
                // Nothing to do here. Juste get the keys and values
                final Pattern p = Pattern.compile("^a=([^:]+):(.+)", Pattern.MULTILINE);
                final Matcher m = p.matcher(packet.getContent());
                while (m.find()) {
                    if (m.group(1).contentEquals("fmtp")) {
                        // Parse FMTP as array
                        final String[] temp = m.group(2).split(" ");
                        fmtp = new int[temp.length];
                        for (int i = 0; i < temp.length; i++) {
                            fmtp[i] = Integer.valueOf(temp[i]);
                        }

                    }
                    else if (m.group(1).contentEquals("rsaaeskey")) {
                        aeskey = this.decryptRSA(Base64.decode(m.group(2), Base64.DEFAULT));
                    }
                    else if (m.group(1).contentEquals("aesiv")) {
                        aesiv = Base64.decode(m.group(2), Base64.DEFAULT);
                    }
                }

            }
            else if (REQ.contentEquals("SETUP")) {
            	
            	
            	try {
					
            		int controlPort = 0;
                    int timingPort = 0;

                    final String value = packet.valueOfHeader("Transport");

                    // Control port
                    Pattern p = Pattern.compile(";control_port=(\\d+)");
                    Matcher m = p.matcher(value);
                    if (m.find()) {
                        controlPort = Integer.valueOf(m.group(1));
                    }

                    // Timing port
                    p = Pattern.compile(";timing_port=(\\d+)");
                    m = p.matcher(value);
                    if (m.find()) {
                        timingPort = Integer.valueOf(m.group(1));
                    }
                    final InetAddress addr = socket.getInetAddress();
                    // Launching audioserver
                    
                    
                    /////////////////////////// ???
                    if (fmtp == null) {
                    	fmtp = new int[12];
            		}
                    ////////////////////////
                    
                    serv = new AudioServer(addr,
                            new AudioSession(aesiv, aeskey, fmtp, controlPort, timingPort),
                            audioImpl);

                    response.append("Transport", packet.valueOfHeader("Transport") + ";server_port=" + serv.getServerPort());

                    // ??? Why ???
                    response.append("Session", "DEADBEEF");
                    airplayImpl.stopApp();
                    
                    
				} catch (Exception e) {
					Log.i("ShairPort RTSPResponse handlePacket SETUP",e.toString());
				}
                
            }
            else if (REQ.contentEquals("RECORD")) {
                //        	Headers	
                //        	Range: ntp=0-
                //        	RTP-Info: seq={Note 1};rtptime={Note 2}
                //        	Note 1: Initial value for the RTP Sequence Number, random 16 bit value
                //        	Note 2: Initial value for the RTP Timestamps, random 32 bit value

            }
            else if (REQ.contentEquals("FLUSH")) {
                serv.flush();

            }
            else if (REQ.contentEquals("TEARDOWN")) {
                response.append("Connection", "close");

            }
            else if (REQ.contentEquals("SET_PARAMETER")) {
            	if (null != packet.getContent()) {
            		// Timing port
                    final Pattern p = Pattern.compile("volume: (.+)");
                    final Matcher m = p.matcher(packet.getContent());
                    if (m.find()) {
                        final double volume = Math.pow(10.0, 0.05 * Double.parseDouble(m.group(1)));
                        serv.setVolume(65536.0 * volume);
                    }
            	}
                
                Log.d(TAG , " SET_PARAMETER " + packet.getContent());//guan

            } else if (REQ.contentEquals("POST") && packet.getDirectory().contentEquals("/fp-setup")) {
            	Log.i("AirPlay", "FP¡ªSETUP...............sleep...........");
            	
            	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	Log.i("AirPlay", "FP¡ªSETUP..........................");
            //	stopThread();
//            	 response.append("Apple-Response", ret);
            	
            	
            	
            	
            	 
            	// 2 1 1 -> 4 : 02 00 02 bb
        		byte[] fply_1 = {
        				(byte)0x46, (byte)0x50, (byte)0x4c, (byte)0x59, 
        				(byte)0x02, (byte)0x01, (byte)0x01, (byte)0x00, 
        				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, 
        				(byte)0x02, (byte)0x00,(byte) 0x02, (byte) 0xbb
        		};

        		// 2 1 2 -> 130 : 02 02 xxx
        		byte[] fply_2 = {
        			(byte)0x46, (byte)0x50, (byte)0x4c, (byte)0x59, (byte)0x02, (byte)0x01, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x82,
        			(byte)0x02, (byte)0x02, (byte)0x2f, (byte)0x7b, (byte)0x69, (byte)0xe6, (byte)0xb2, (byte)0x7e, (byte)0xbb, (byte)0xf0, (byte)0x68, (byte)0x5f, (byte)0x98, (byte)0x54, (byte)0x7f, (byte)0x37,
        			(byte)0xce, (byte)0xcf, (byte)0x87, (byte)0x06, (byte)0x99, (byte)0x6e, (byte)0x7e, (byte)0x6b, (byte)0x0f, (byte)0xb2, (byte)0xfa, (byte)0x71, (byte)0x20, (byte)0x53, (byte)0xe3, (byte)0x94,
        			(byte)0x83, (byte)0xda, (byte)0x22, (byte)0xc7, (byte)0x83, (byte)0xa0, (byte)0x72, (byte)0x40, (byte)0x4d, (byte)0xdd, (byte)0x41, (byte)0xaa, (byte)0x3d, (byte)0x4c, (byte)0x6e, (byte)0x30,
        			(byte)0x22, (byte)0x55, (byte)0xaa, (byte)0xa2, (byte)0xda, (byte)0x1e, (byte)0xb4, (byte)0x77, (byte)0x83, (byte)0x8c, (byte)0x79, (byte)0xd5, (byte)0x65, (byte)0x17, (byte)0xc3, (byte)0xfa,
        			(byte)0x01, (byte)0x54, (byte)0x33, (byte)0x9e, (byte)0xe3, (byte)0x82, (byte)0x9f, (byte)0x30, (byte)0xf0, (byte)0xa4, (byte)0x8f, (byte)0x76, (byte)0xdf, (byte)0x77, (byte)0x11, (byte)0x7e,
        			(byte)0x56, (byte)0x9e, (byte)0xf3, (byte)0x95, (byte)0xe8, (byte)0xe2, (byte)0x13, (byte)0xb3, (byte)0x1e, (byte)0xb6, (byte)0x70, (byte)0xec, (byte)0x5a, (byte)0x8a, (byte)0xf2, (byte)0x6a,
        			(byte)0xfc, (byte)0xbc, (byte)0x89, (byte)0x31, (byte)0xe6, (byte)0x7e, (byte)0xe8, (byte)0xb9, (byte)0xc5, (byte)0xf2, (byte)0xc7, (byte)0x1d, (byte)0x78, (byte)0xf3, (byte)0xef, (byte)0x8d,
        			(byte)0x61, (byte)0xf7, (byte)0x3b, (byte)0xcc, (byte)0x17, (byte)0xc3, (byte)0x40, (byte)0x23, (byte)0x52, (byte)0x4a, (byte)0x8b, (byte)0x9c, (byte)0xb1, (byte)0x75, (byte)0x05, (byte)0x66,
        			(byte)0xe6, (byte)0xb3
        		};

        		// 2 1 3 -> 152
        		// 4 : 02 8f 1a 9c
        		// 128 : xxx
        		// 20 : 5b ed 04 ed c3 cd 5f e6 a8 28 90 3b 42 58 15 cb 74 7d ee 85

        		byte[] fply_3 = {
        			(byte)0x46, (byte)0x50, (byte)0x4c, (byte)0x59, (byte)0x02, (byte)0x01, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x98, (byte)0x02, (byte)0x8f,
        			(byte)0x1a, (byte)0x9c, (byte)0x6e, (byte)0x73, (byte)0xd2, (byte)0xfa, (byte)0x62, (byte)0xb2, (byte)0xb2, (byte)0x07, (byte)0x6f, (byte)0x52, (byte)0x5f, (byte)0xe5, (byte)0x72, (byte)0xa5,
        			(byte)0xac, (byte)0x4d, (byte)0x19, (byte)0xb4, (byte)0x7c, (byte)0xd8, (byte)0x07, (byte)0x1e, (byte)0xdb, (byte)0xbc, (byte)0x98, (byte)0xae, (byte)0x7e, (byte)0x4b, (byte)0xb4, (byte)0xb7,
        			(byte)0x2a, (byte)0x7b, (byte)0x5e, (byte)0x2b, (byte)0x8a, (byte)0xde, (byte)0x94, (byte)0x4b, (byte)0x1d, (byte)0x59, (byte)0xdf, (byte)0x46, (byte)0x45, (byte)0xa3, (byte)0xeb, (byte)0xe2,
        			(byte)0x6d, (byte)0xa2, (byte)0x83, (byte)0xf5, (byte)0x06, (byte)0x53, (byte)0x8f, (byte)0x76, (byte)0xe7, (byte)0xd3, (byte)0x68, (byte)0x3c, (byte)0xeb, (byte)0x1f, (byte)0x80, (byte)0x0e,
        			(byte)0x68, (byte)0x9e, (byte)0x27, (byte)0xfc, (byte)0x47, (byte)0xbe, (byte)0x3d, (byte)0x8f, (byte)0x73, (byte)0xaf, (byte)0xa1, (byte)0x64, (byte)0x39, (byte)0xf7, (byte)0xa8, (byte)0xf7,
        			(byte)0xc2, (byte)0xc8, (byte)0xb0, (byte)0x20, (byte)0x0c, (byte)0x85, (byte)0xd6, (byte)0xae, (byte)0xb7, (byte)0xb2, (byte)0xd4, (byte)0x25, (byte)0x96, (byte)0x77, (byte)0x91, (byte)0xf8,
        			(byte)0x83, (byte)0x68, (byte)0x10, (byte)0xa1, (byte)0xa9, (byte)0x15, (byte)0x4a, (byte)0xa3, (byte)0x37, (byte)0x8c, (byte)0xb7, (byte)0xb9, (byte)0x89, (byte)0xbf, (byte)0x86, (byte)0x6e,
        			(byte)0xfb, (byte)0x95, (byte)0x41, (byte)0xff, (byte)0x03, (byte)0x57, (byte)0x61, (byte)0x05, (byte)0x00, (byte)0x73, (byte)0xcc, (byte)0x06, (byte)0x7e, (byte)0x4f, (byte)0xc7, (byte)0x96,
        			(byte)0xae, (byte)0xba, (byte)0x5b, (byte)0xed, (byte)0x04, (byte)0xed, (byte)0xc3, (byte)0xcd, (byte)0x5f, (byte)0xe6, (byte)0xa8, (byte)0x28, (byte)0x90, (byte)0x3b, (byte)0x42, (byte)0x58,
        			(byte)0x15, (byte)0xcb, (byte)0x74, (byte)0x7d, (byte)0xee, (byte)0x85
        		};

        		// 2 1 4 -> 20 : 5b ed 04 ed c3 cd 5f e6 a8 28 90 3b 42 58 15 cb 74 7d ee 85
        		byte[] fply_4 = {
        			(byte)0x46, (byte)0x50, (byte)0x4c, (byte)0x59, (byte)0x02, (byte)0x01, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0x5b,
        			(byte)0xed, (byte)0x04, (byte)0xed, (byte)0xc3, (byte)0xcd, (byte)0x5f, (byte)0xe6, (byte)0xa8, (byte)0x28, (byte)0x90, (byte)0x3b, (byte)0x42, (byte)0x58, (byte)0x15, (byte)0xcb, (byte)0x74,
        			(byte)0x7d, (byte)0xee, (byte)0x85
        		};

//        		NSLog(@" content:%@", content);

        		byte[] fply_header = new byte[12];
        		
        		byte[] content = packet.getContent().getBytes();
        		
        		System.arraycopy(content, 0, fply_header, 0, fply_header.length); 
        		
        		int fply_header_len = fply_header.length;
        		int payload_range = content.length - fply_header_len;
        				
        		byte[] payload = new byte[payload_range];		
        		System.arraycopy(content, fply_header_len, payload, 0, payload_range); 
        		
        		
        		if(fply_header[6] == 1){
        			Log.i("AirPlay", "fply_header[6] == 1");
        			
        			int fply_id_range = 12 + 2;
        			int fply_id_range_len = 1;
        			System.arraycopy(content, fply_id_range, fply_2, 13, fply_id_range_len); 

        			response.setSendData(true);
        			response.setData(fply_2);
        			
        		}else if(fply_header[6] == 3){
        			Log.i("AirPlay", "fply_header[6] == 3");
        			
        			int fply_4_range = payload.length - 20;
        			int fply_4_range_len = 20;
        			
        			byte[] data = new byte[fply_4_range_len];	
        			System.arraycopy(payload, fply_4_range, data, 0, fply_4_range_len); 
        			
        			byte[] data_appand = new byte[fply_4_range_len+12];	
        			System.arraycopy(fply_4, 0, data_appand, 0, 12); 
        			System.arraycopy(data, 0, data_appand, 13, fply_4_range_len); 
        			
        			response.setSendData(true);
        			response.setData(data_appand);
        			
        		}
//        		uint8_t fply_header[12];
//        		[content getBytes:fply_header length:sizeof(fply_header)];
//        		NSRange payload_range = {
//        			.location = sizeof(fply_header),
//        			.length = [content length] - sizeof(fply_header),
//        		};
//        		NSData *payload = [content subdataWithRange:payload_range];
////        		NSLog(@" fply seq:%u len:%u %@", fply_header[6], fply_header[11], payload);
//
//        		NSMutableData *data;
//        		if (fply_header[6] == 1) {
//        			NSRange fply_id_range = {
//        				.location = 12 + 2,
//        				.length = 1,
//        			};
//        			[content getBytes:fply_2 + 13 range:fply_id_range];
//        			data = [NSData dataWithBytesNoCopy:fply_2 length:sizeof(fply_2) freeWhenDone:NO];
//        			[self replyOK:sock withHeaders:nil withData:data];
//
//        		} else if (fply_header[6] == 3) {
//        			NSRange fply_4_range = {
//        				.location = [payload length] - 20,
//        				.length = 20,
//        			};
//        			data = [NSMutableData dataWithBytes:fply_4 length:12];
//        			[data appendData:[payload subdataWithRange:fply_4_range]];
//        			[self replyOK:sock withHeaders:nil withData:data];
//        		}
            }
            else {
                Log.i("AirPlay", "REQUEST(" + REQ + "): Not Supported Yet!");
                Log.i("AirPlay", packet.getRawPacket());
            }

        }
        
        // We close the response
        response.finalize();
        return response;
    }

    /**
     * Crypts with private key
     * 
     * @param array data to encrypt
     * @return encrypted data
     */
    private byte[] encryptRSA(final byte[] array) {
        try {

            // Encrypt
            //final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            return cipher.doFinal(array);

        }
        catch (final Exception e) {
            Log.e("Encrypt", e.toString());
        }

        return null;
    }

    /**
     * Decrypt with RSA priv key
     * 
     * @param array
     * @return
     */
    private byte[] decryptRSA(final byte[] array) {
        try {
            // La clef RSA

            // Encrypt
            final Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, pk);
            return cipher.doFinal(array);

        }
        catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Thread to listen packets
     */
    @Override
    public void run() {
        try {
            do {
                Log.i("AirPlay", "listening packets ... ");
                // feed buffer until packet completed
                final StringBuffer packet = new StringBuffer();
                int ret = 0;
                do {
                    final char[] buffer = new char[4096];
                    ret = in.read(buffer);
                    packet.append(new String(buffer));
                } while (ret != -1 && !completedPacket.matcher(packet.toString()).find());

                if (ret != -1) {
                    // We handle the packet
                    final RTSPPacket request = new RTSPPacket(packet.toString());
                    final RTSPResponse response = this.handlePacket(request);
                   // Log.i("AirPlay", request.toString());
                  //  Log.i("AirPlay", response.toString());

                    // Write the response to the wire
                    try {
                    	
                    	OutputStream  os = socket.getOutputStream();
                        final BufferedWriter oStream = new BufferedWriter(new OutputStreamWriter(os));
                        oStream.write(response.getRawPacket());
                        oStream.flush();
                        if(response.isSendData()){
                        	os.write(response.getData());
                        	os.flush();
                        }
                    }
                    catch (final IOException e) {
                        e.printStackTrace();
                    }catch (final Exception e) {
                        e.printStackTrace();
                    }

                    if ("TEARDOWN".equals(request.getReq())) {
                        socket.close();
                        socket = null;
                    }
                }
                else {
                    socket.close();
                    socket = null;
                }
            } while (socket != null && !socket.isClosed() && !stopThread);

        }
        catch (final Throwable e) {
            Log.e("Responder", e.toString());
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch (final IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (socket != null)
                        socket.close();
                }
                catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (serv != null) {//guan
        	serv.stop();
        	serv = null;
        }
        Log.i("AirPlay", "connection ended.");
    }

	public void stopThread() {
		Log.i("AirPlay", "stopThread..........................");
		stopThread = true;
	}

}