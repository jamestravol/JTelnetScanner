package jt.upwork.telnet.logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jamestravol
 */
public class TelnetReaderWorker extends SwingWorker<Integer, String> {

    private final String host;
    private final int port;
    private final ConnectionPanel connectionPanel;
    private final Runnable completeCallback;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;
    private final Socket socket = new Socket();
    private volatile List<String[]> dataList = new LinkedList<>();

    private ReentrantLock lock = new ReentrantLock();

    public TelnetReaderWorker(String host, int port, ConnectionPanel connectionPanel, Runnable completeCallback) {
        this.host = host;
        this.port = port;
        this.connectionPanel = connectionPanel;
        dateFormatter = DateTimeFormatter.ofPattern(Config.INSTANCE.getProperty("app.date.format"));
        timeFormatter = DateTimeFormatter.ofPattern(Config.INSTANCE.getProperty("app.time.format"));
        this.completeCallback = completeCallback;
    }

    @Override
    protected Integer doInBackground() {
        try {
            return doInBackgroundInternal();
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } finally {
            connectionPanel.setDisconnected();
            connectionPanel.cancel();
            SwingUtilities.invokeLater(completeCallback);
        }
    }

    private Integer doInBackgroundInternal() throws Exception {

        BufferedReader sIn;

        socket.connect(new InetSocketAddress(host, port));
        sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        connectionPanel.setConnected();

        System.out.println("The socket is listening:" + host + ":" + port);

        String response;
        while ((response = sIn.readLine()) != null) {
            store(response);
        }

        return 0;
    }

    private void store(String text) {

        System.out.println("Got the new line: " + text);

        final boolean appendDate = Boolean.parseBoolean(Config.INSTANCE.getProperty("app.append.date"));
        final int columnLimit = Integer.parseInt(Config.INSTANCE.getProperty("app.column.limit"));
        final String[] split = text.split(Config.INSTANCE.getProperty("app.separator.regex").trim());

        System.out.println("Splitted: " + Arrays.toString(split));

        LinkedList<String> vals = new LinkedList<>();

        final LocalDateTime localDate = LocalDateTime.now();

        if (appendDate) {
            vals.add(dateFormatter.format(localDate));
            vals.add(timeFormatter.format(localDate));
        }

        for (int i = 0; i < split.length && i < columnLimit; i++) {
            vals.add(split[i].trim());
        }

        lock.lock();
        try {
            dataList.add(vals.toArray(new String[0]));
        } finally {
            lock.unlock();
        }
    }

    public List<String[]> getDataList() {
        return dataList;
    }

    public void clearDataList() {
        dataList = new LinkedList<>();
    }

    public void cancel() {
        cancel(true);
        try {
            socket.close();
        } catch (IOException e) {
            store(Utils.getStackTrace(e));
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
