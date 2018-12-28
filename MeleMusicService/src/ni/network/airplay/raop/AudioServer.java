/**
 * The class that process audio data
 * 
 * @author bencall
 */

package ni.network.airplay.raop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import ni.common.util.Log;
import ni.network.airplay.IAirplayAudioImpl;
import ni.network.util.NetUtils;

/**
 * Main class that listen for new packets.
 * 
 * @author bencall
 */
public class AudioServer implements UDPDelegate {
    // Constantes
    public static final int BUFFER_FRAMES = 512; // Total buffer size (number of frame)
    public static final int START_FILL = 282; // Alac will wait till there are START_FILL frames in buffer
    public static final int MAX_PACKET = 2048; // Also in UDPListener (possible to merge it in one place?)

    // Sockets
    private DatagramSocket sock, csock;
    private UDPListener l1;

    // client address
    private InetAddress rtpClient;

    // Audio infos and datas
    private final AudioSession session;
    private final AudioBuffer audioBuf;

    // The audio player
    private final PCMPlayer player;
	private InetAddress clientAddr;

    /**
     * Constructor. Initiate instances vars
     * 
     * @param aesiv
     * @param aeskey
     * @param fmtp
     * @param controlPort
     * @param timingPort
     */
    public AudioServer(final InetAddress addr, final AudioSession session, final IAirplayAudioImpl aAudioImpl) {
        // Init instance var
        this.session = session;
        this.clientAddr = addr;
        // Init functions
        audioBuf = new AudioBuffer(session, this);
        this.initRTP();
        player = new PCMPlayer(session, audioBuf, aAudioImpl);
        player.start();
    }

    public void stop() {
        player.stopThread();
        l1.stopThread();
        sock.close();
        csock.close();
    }

    public void setVolume(final double vol) {
        player.setVolume(vol);
    }

    /**
     * Return the server port for the bonjour service
     * 
     * @return
     */
    public int getServerPort() {
        return sock.getLocalPort();
    }

    /**
     * Opens the sockets and begin listening
     */
    private void initRTP() {

        int port = NetUtils.getPortInRange(7000, 60000);
        while (true) {
            try {
                sock = new DatagramSocket(port);
                csock = new DatagramSocket(port + 1);
            }
            catch (final IOException e) {
                port = port + 2;
                continue;
            }
            Log.d("", "guan initRTP port = " + port);
            break;
        }

        l1 = new UDPListener(sock, this);
    }
    /**
     * When udpListener gets a packet
     */
    @Override
    public void packetReceived(final DatagramSocket socket, final DatagramPacket packet) {
    	final InetAddress address = packet.getAddress();
        if (!address.equals(clientAddr)) {
        	return;
        }
        this.rtpClient = packet.getAddress(); // The client address
        final int type = packet.getData()[1] & (byte)~0x80;
        if (type == 0x60 || type == 0x56) { // audio data / resend
            // Decale de 4 bytes supplementaires
            int off = 0;
            if (type == 0x56) {
                off = 4;
            }

            //seqno is on two byte
            final int seqno = ((packet.getData()[2 + off] & 0xff) * 256 + (packet.getData()[3 + off] & 0xff));
            if (seqno <= 0) {
            	Log.d("", "why seqno <= 0?");
            	return;
            }
            // + les 12 (cfr. RFC RTP: champs a ignorer)
            final byte[] pktp = new byte[packet.getLength() - off - 12];
            for (int i = 0; i < pktp.length; i++) {
                pktp[i] = packet.getData()[i + 12 + off];
            }

            audioBuf.putPacketInBuffer(seqno, pktp);
        }
    }

    /**
     * Ask iTunes to resend packet FUNCTIONAL??? NO PROOFS
     * 
     * @param first
     * @param last
     */
    public void request_resend(final int first, final int last) {
        Log.d("ShairPort", "Resend Request: " + first + "::" + last);
        if (last < first) {
            return;
        }

        final int len = last - first + 1;
        final byte[] request = new byte[] { (byte)0x80, (byte)(0x55 | 0x80), 0x01, 0x00, (byte)((first & 0xFF00) >> 8), (byte)(first & 0xFF), (byte)((len & 0xFF00) >> 8), (byte)(len & 0xFF) };

        try {
            final DatagramPacket temp = new DatagramPacket(request, request.length, rtpClient, session.getControlPort());            
            //csock.send(temp); //guanshuai: resend request through audio channel
            sock.send(temp);

        }
        catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Flush the audioBuffer
     */
    public void flush() {
        audioBuf.flush();
    }

	@Override
	public void notifyLock() {
		// TODO Auto-generated method stub
		audioBuf.notifyLock();
	}
}
