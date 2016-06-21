package org.micron.nve.mydaemon;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Latch {

    private CountDownLatch latch = null;
    private Thread mythread = null;
    private long interval;

    /**
     * This is a simple Thread to just sleep a certain interval and decrement
     * the CountDownLatch until latch count is zero.
     */
    public class MyThread extends Thread {

        private long interval;
        CountDownLatch latch;

        MyThread(CountDownLatch latch, long interval) {
            this.latch = latch;
            this.interval = interval;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            try {
                while (this.latch.getCount() > 0) {
                    Thread.sleep(this.interval);
                    this.latch.countDown();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Input integer > 0
     *
     * @param numLatches
     * @param interval
     */
    public Latch(int numLatches, long interval) {
        this.resetLatch(numLatches, interval);
    }

    /**
     * If for some reason you need to reset the CountDownLatch.
     *
     * @param numLatches
     */
    public void resetLatch(int numLatches, long interval) {
        if (numLatches <= 0) {
            this.latch = new CountDownLatch(1);
        } else {
            this.latch = new CountDownLatch(numLatches);
        }
        if (this.interval <= 0) {
            this.interval = 1000;
        } else {
            this.interval = interval;
        }
        this.mythread = new MyThread(this.latch, this.interval);
    }

    public void startLatchCountdown() {
        this.mythread.start();
    }

    public void waitForLatchToComplete() throws InterruptedException {
        this.latch.await();
    }
}
