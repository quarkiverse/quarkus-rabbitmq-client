package io.quarkiverse.rabbitmqclient.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

import javax.net.ServerSocketFactory;

public class DummyServer {

    public static final int PORT_RANGE_MIN = 1025;
    public static final int PORT_RANGE_MAX = 65535;
    private static final Random random = new Random(System.nanoTime());

    private ServerSocket socket = null;
    private int port;
    private boolean available;
    private String client;
    private Runnable closeCallback;

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return "localhost";
    }

    public boolean isAvailable() {
        return available;
    }

    public String toString() {
        return (client == null ? "" : client + "|") + "localhost:" + port;
    }

    public void close() {
        try {
            available = false;
            if (socket != null) {
                socket.close();
            }
            closeCallback.run();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static DummyServer newDummyServer(String client, Runnable closeCallback, boolean failing) {
        try {
            DummyServer ds = new DummyServer();
            ds.port = findFreePort();
            if (!failing) {
                ds.socket = ServerSocketFactory.getDefault().createServerSocket(ds.port, 50,
                        InetAddress.getByName("localhost"));
            }
            ds.available = !failing;
            ds.client = client;
            ds.closeCallback = closeCallback;
            return ds;
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private static int findFreePort() {
        int candidate;
        int tryCount = 0;
        do {
            if (tryCount > (PORT_RANGE_MAX - PORT_RANGE_MIN)) {
                throw new IllegalStateException("Could not find a free port.");
            }
            candidate = random.nextInt(PORT_RANGE_MAX - PORT_RANGE_MIN) + PORT_RANGE_MIN;
            tryCount++;

        } while (!isAvailablePort(candidate));

        return candidate;
    }

    private static boolean isAvailablePort(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                    port, 50, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
