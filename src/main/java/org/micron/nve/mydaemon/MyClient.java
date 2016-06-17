package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public final class MyClient extends Thread {

    private Socket clientSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String host = null;
    private int port = 0;
    private boolean debug = false;

    public MyClient(String host, int port) {
        this.host = host;
        this.port = port;
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

    public void openSocket() throws IOException {
        if (this.clientSocket == null) {
            if (debug) {
                System.out.println("DEBUG: MyClient openSocket() on port " + this.port);
            }
            this.clientSocket = new Socket(this.host, this.port);
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (debug) {
                System.out.println("DEBUG: MyClient openSocket() streams opened");
            }
        }
    }

    public void writeSocket(String message) throws IOException {
        if (this.out != null) {
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
                        System.out.println("DEBUG: MyClient MESSAGE " + line);
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
            if (debug) {
                System.out.println("DEBUG: MyClient closeSocket()");
            }
            this.clientSocket.close();
            this.clientSocket = null;
        }

    }

}
