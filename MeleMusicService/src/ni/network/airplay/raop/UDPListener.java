package ni.network.airplay.raop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import ni.common.util.Log;

/**
 * Listen on a given socket
 * 
 * @author bencall
 */
public class UDPListener extends Thread {
    // Constantes
    public static final int MAX_PACKET = 2048;

	private static final String TAG = "UDPListener";

    // Variables d'instances
    private final DatagramSocket socket;
    private final UDPDelegate delegate;
    private boolean stopThread = false;

    public UDPListener(final DatagramSocket socket, final UDPDelegate delegate) {
        super();
        this.socket = socket;
        this.delegate = delegate;
        this.setName(TAG);
        this.start();
    }

    @Override
	public void run() {
		boolean fin = stopThread;
		while (!fin) {
			final byte[] buffer = new byte[MAX_PACKET];
			final DatagramPacket p = new DatagramPacket(buffer, buffer.length);
			try {
				synchronized (socket) {
					if (socket != null) {
						socket.receive(p);
						delegate.packetReceived(socket, p);
					}
				}
			} catch (final IOException e) {
				Log.e("UDPlistener", e);
			}
			// Stop
			synchronized (this) {
				fin = this.stopThread;
			}
		}
		delegate.notifyLock();
		Log.d(TAG, "guan UDPListener thread exit");
	}

    public synchronized void stopThread() {
        this.stopThread = true;
    }
}
