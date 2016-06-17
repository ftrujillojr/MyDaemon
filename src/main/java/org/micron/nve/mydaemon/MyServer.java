package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class MyServer extends Thread {

    private ServerSocket serverSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private int port = 0;
    private Socket clientSocket = null;

    private boolean debug;

    public MyServer() {
        this.debug = false;
    }
    
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

    public void openSocket() throws IOException {
        if (this.serverSocket == null) {
            if (debug) {
                System.out.println("DEBUG: MyServer openSocket() on port " + this.port);
            }
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setSoTimeout(10000);
            this.clientSocket = this.serverSocket.accept();
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (debug) {
                System.out.println("DEBUG: MyServer openSocket() streams opened ");
            }
        }
    }

    public void writeSocket(String message) throws IOException {
        if (this.out != null) {
            if (debug) {
                System.out.println("DEBUG: MyServer writeSocket() ");
            }
            this.out.println("<START>");
            this.out.println(message);
            this.out.println("<END>");
            this.out.flush();
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    public String readSocket() throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        String line;

        if (this.in != null) {
            if (debug) {
                System.out.println("DEBUG: MyServer readSocket() ");
            }

            while (true) {
                while ((line = this.in.readLine()) == null) {
                    // waiting for non-null response.
                    if (debug) {
                        System.out.println("DEBUG: MyServer readSocket() WAIT");
                    }
                    Thread.sleep(3000);
                }
                if (line.contains("<START>")) {
                    if (debug) {
                        System.out.println("DEBUG: MyServer <START>");
                    }
                    break;
                }
            }

            while ((line = this.in.readLine()) != null) {
                if (line.contains("<END>")) {
                    if (debug) {
                        System.out.println("DEBUG: MyServer <END>");
                    }
                    break;
                } else {
                    if (debug) {
                        System.out.println("DEBUG: MyServer MESSAGE " + line);
                    }
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void closeSocket() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }

        if (this.out != null) {
            this.out.close();
            this.out = null;
        }

        if (this.clientSocket != null) {
            this.clientSocket.close();
            this.clientSocket = null;
        }

        if (this.serverSocket != null) {
            if (debug) {
                System.out.println("DEBUG: MyServer closeSocket()");
            }
            this.serverSocket.close();
            this.serverSocket = null;
        }
    }

}
