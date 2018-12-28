package ni.network.util;

import ni.common.math.Rand;

public class NetUtils {
    /**
     * Returns a port number in the specified range.
     * 
     * @param minPort is the minimum port number allowed.
     * @param maxPort is the maximum port number allowed.
     * @return a random port number in the specified range.
     */
    static public int getPortInRange(final int minPort, final int maxPort) {
        return Rand.getSingleton().range(minPort, maxPort);
    }
}
