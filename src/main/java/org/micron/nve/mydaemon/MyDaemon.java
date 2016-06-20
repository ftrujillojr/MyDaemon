package org.micron.nve.mydaemon;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

@SuppressWarnings("FieldMayBeFinal")
public final class MyDaemon implements Daemon {

    private Thread myThread;
    private boolean stopped;
    private MyServer myServer;
    private int port;
    private boolean debug = false;

    public MyDaemon() {
        this.port = 8099;
        this.stopped = false;
    }

    public MyDaemon(int port) {
        this.port = port;
        this.stopped = false;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

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
                myServer = new MyServer(port);
                if (debug) {
                    myServer.setDebug(true);
                }
                super.start();
            }

            @Override
            public void run() {
                
                myServer.listenSocket(); // This has a while(true) loop
                
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
        myThread.start();
        Thread.sleep(1000); // let daemon get rolling.
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
