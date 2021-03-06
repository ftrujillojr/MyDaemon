package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MyClient extends Thread {

    private Socket clientSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String host = null;
    private int port = 0;
    private boolean debug = false;
    private int readTimeoutMilliseconds = 0;

    public MyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.readTimeoutMilliseconds = 0; // Infinite read
    }

    public MyClient(String host, int port, int readTimeoutMilliseconds) {
        this.host = host;
        this.port = port;
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void interrupt() {  // Sockets do not honor interrupt unless we do this.
        try {
            if (this.clientSocket != null) {
                this.closeSocket();
            }
        } catch (IOException ignored) {
            // finally will pass interrupt up the chain.
        } finally {
            super.interrupt();
        }
    }

    private void openSocket() throws IOException {
        if (this.clientSocket == null) {
            if (debug) {
                System.out.println("DEBUG: MyClient openSocket() on port " + this.port);
            }
            try {
                this.clientSocket = new Socket(this.host, this.port);
            } catch (ConnectException ex) {
                System.out.println("ERROR: Could not connect to host " + host + " port " + port);
                throw ex;
            }
            this.clientSocket.setSoTimeout(readTimeoutMilliseconds);
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (debug) {
                System.out.println("DEBUG: MyClient openSocket() streams opened");
            }
        }
    }

    public void writeSocket(String message) throws IOException {
        if (this.out == null) {
            this.openSocket();
            if (debug) {
                System.out.println("DEBUG: MyClient writeSocket() ");
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
                System.out.println("DEBUG: MyClient readSocket() ");
            }
            while (true) {
                while ((line = this.in.readLine()) == null) {
                    // waiting for non-null response.
                    if (debug) {
                        System.out.println("DEBUG: MyClient readSocket() WAIT");
                    }
                    Thread.sleep(3000);
                }
                if (line.contains("<START>")) {
                    if (debug) {
                        System.out.println("DEBUG: MyClient <START>");
                    }
                    break;
                }
            }

            while ((line = this.in.readLine()) != null) {
                if (line.contains("<END>")) {
                    if (debug) {
                        System.out.println("DEBUG: MyClient <END>");
                    }
                    break;
                } else {
                    if (debug) {
                        System.out.println("DEBUG: MyClient MESSAGE =>" + line + "<= ");
                    }
                    if (line.equals("") == false) {
                        sb.append(line).append("\n");
                    }
                }
            }

            this.closeSocket();
        }

        return sb.toString();
    }

    private void closeSocket() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }

        if (this.out != null) {
            this.out.close();
            this.out = null;
        }

        if (this.clientSocket != null) {
            if (debug) {
                System.out.println("DEBUG: MyClient closeSocket()");
            }
            this.clientSocket.close();
            this.clientSocket = null;
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
