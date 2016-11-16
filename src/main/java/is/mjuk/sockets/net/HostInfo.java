package is.mjuk.sockets.net;
import java.util.ArrayList;
import java.net.InetSocketAddress;

public class HostInfo {
    public PeerState state = PeerState.UNKNOWN;
    public InetSocketAddress address;

    public HostInfo (InetSocketAddress address) {
        this.address = address;
    }
}

