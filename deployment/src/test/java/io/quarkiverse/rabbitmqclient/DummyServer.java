package io.quarkiverse.rabbitmqclient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

import javax.net.ServerSocketFactory;

class DummyServer {

    public static final int PORT_RANGE_MIN = 1025;
    public static final int PORT_RANGE_MAX = 65535;
    private static final Random random = new Random(System.nanoTime());

    private ServerSocket socket = null;
    private int port;

    public int getPort() {
        return port;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static DummyServer newDummyServer() {
        try {
            DummyServer ds = new DummyServer();
            ds.port = findFreePort();
            ds.socket = ServerSocketFactory.getDefault().createServerSocket(ds.port, 50, InetAddress.getByName("localhost"));
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
