package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyClientWorker extends Thread {

    private Socket clientSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean debug = false;

    public MyClientWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
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

    @Override
    public void run() {
        StringBuilder responseString = new StringBuilder();
        String line;

        try {
            if (this.debug) {
                System.out.println("DEBUG: ClientWorker running..." + this);
            }
            this.openSocketStreams();
            String requestString = this.readSocket();

//            if (this.debug) {
                System.err.println("REQUEST: " + requestString);
                System.err.flush();
//            }

            // do something with requestString.  In this case, prepend RESPONSE: and return data.
            responseString.append("RESPONSE: ").append(requestString).append("\n");

            this.writeSocket(responseString.toString());
            this.closeSocket();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MyClientWorker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private int generateRandomNumberInclusive(int low, int high) {
        Random r = new Random();
        return (r.nextInt((high + 1) - low) + low);
    }

    private void writeSocket(String message) throws IOException {
        if (this.out != null) {
            if (debug) {
                System.out.println("DEBUG: ClientWorker writeSocket() ");
            }
            this.out.println("<START>");
            this.out.println(message);
            this.out.println("<END>");
            this.out.flush();
        }
    }

    private void openSocketStreams() throws IOException {
        if (this.clientSocket != null) {
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (debug) {
                System.out.println("DEBUG: ClientWorker openSocket() streams opened ");
            }
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    private String readSocket() throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        String line;

        if (this.in != null) {
            if (debug) {
                System.out.println("DEBUG: ClientWorker readSocket() ");
            }

            while (true) {
                while ((line = this.in.readLine()) == null) {
                    // waiting for non-null response.
                    if (debug) {
                        System.out.println("DEBUG: ClientWorker readSocket() WAIT");
                    }
                    Thread.sleep(3000);
                }
                if (line.contains("<START>")) {
                    if (debug) {
                        System.out.println("DEBUG: ClientWorker <START>");
                    }
                    break;
                }
            }

            while ((line = this.in.readLine()) != null) {
                if (line.contains("<END>")) {
                    if (debug) {
                        System.out.println("DEBUG: ClientWorker <END>");
                    }
                    break;
                } else {
                    if (debug) {
                        System.out.println("DEBUG: ClientWorker MESSAGE " + line);
                    }
                    if (line.equals("") == false) {
                        sb.append(line).append("\n");
                    }
                }
            }
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
            this.clientSocket.close();
            this.clientSocket = null;
            if (debug) {
                System.out.println("DEBUG: ClientWorker closeSocket()");
            }
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
