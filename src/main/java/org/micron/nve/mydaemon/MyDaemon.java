package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public final class MyDaemon implements Daemon {

    private Thread myThread;
    private boolean stopped = false;
    private MyServer myServer;
    private int port = 8099;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        /*
         * Construct objects and initialize variables here.
         * You can access the command line arguments that would normally be passed to your main() 
         * method as follows:
         */
        String[] args = daemonContext.getArguments();

        myThread = new Thread() {

            @Override
            public synchronized void start() {
                MyDaemon.this.stopped = false;
                System.out.println("start inside");
                try {
                    System.out.println("starting MyServer");
                    myServer = new MyServer(port);
                    System.out.println("open Socket");
                    myServer.openSocket();
                    System.out.println("socket opened.");
                } catch (IOException ex) {
                    System.out.println("Could not read port " + port);
                    Logger.getLogger(MyDaemon.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(-1);
                } 

                super.start();
            }

            @Override
            public void run() {
                System.out.println("run");

                while (!stopped) {
                    try {
                        System.out.println("Reading Socket");
                        String inputResponse = myServer.readSocket();
                        
                        // do something with inputResponse.
                        String outputResponse = "OUT: " + inputResponse;
                        
                        System.out.println("Write Socket");
                        myServer.writeSocket(outputResponse);
                        
                    } catch (InterruptedException ex) {
                        System.out.println("Caught  InterruptedException.");
                        System.out.println(ex.getMessage());
                        stopped = true;
                    } catch (IOException ex) {
                        System.out.println("Caught IOException");
                        System.out.println(ex.getMessage());
                        stopped = true;
                    }
                }
                try {
                    myServer.closeSocket();
                } catch (IOException ex) {
                    Logger.getLogger(MyDaemon.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }


    public Thread getMyThread() {
        return this.myThread;
    }

    @Override
    public void start() throws Exception {
        System.out.println("start outside");
        myThread.start();
    }

    @Override
    public void stop() throws Exception {
        stopped = true;
        try {
            myThread.join(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public void destroy() {
        myThread = null;
    }
}
