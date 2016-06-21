package org.micron.nve.mydaemon;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MyServer extends Thread {

    private ServerSocket serverSocket = null;
    private int port = 0;
    private boolean debug;

    public MyServer(int port) {
        this.debug = false;
        this.port = port;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void interrupt() {  // Sockets do not honor interrupt unless we do this.
        try {
            if (this.serverSocket != null) {
                this.closeSocket();
            }
        } catch (IOException ignored) {
            // finally will pass interrupt up the chain.
        } finally {
            super.interrupt();
        }
    }

    public void listenSocket() {
        try {
            if (debug) {
                System.out.println("DEBUG: MyServer openSocket() on port " + this.port + "\n");
            }
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setSoTimeout(0);  // accept() will be indefinite timeout if set to zero.

            while (true) {
                MyClientWorker clientWorker;
                if (this.debug) {
                    System.out.println("DEBUG: MyServer listenSocket()");
                }
                clientWorker = new MyClientWorker(this.serverSocket.accept());
                clientWorker.setDebug(false);
                
                Thread t = new Thread(clientWorker);
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void closeSocket() throws IOException {

        if (this.serverSocket != null) {
            if (debug) {
                System.out.println("DEBUG: MyServer closeSocket()");
            }
            this.serverSocket.close();
            this.serverSocket = null;
        }
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() {
        try {
            this.closeSocket();
        } catch (IOException ex) {
            Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
                Logger.getLogger(MyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
