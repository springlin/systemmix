package ni.network.airplay.raop;

public class RTSPResponse {

    private final StringBuilder response = new StringBuilder();

    private boolean isSendData;
    
    private byte[] data;
    
    
    public RTSPResponse(final String header) {
        response.append(header + "\r\n");
    }

    public void append(final String key, final String value) {
        response.append(key + ": " + value + "\r\n");
    }

    /**
     * close the response
     */
    @Override
    public void finalize() {
        response.append("\r\n");
    }

    public String getRawPacket() {
        return response.toString();
    }

    @Override
    public String toString() {
        return " > " + response.toString().replaceAll("\r\n", "\r\n > ");
    }


	public boolean isSendData() {
		return isSendData;
	}

	public void setSendData(boolean isSendData) {
		this.isSendData = isSendData;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

    
    
    
}
