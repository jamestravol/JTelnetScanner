package jt.upwork.telnet.logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jamestravol
 */
public class ConnectionPanel extends JPanel {

    private JTextField ipField;
    private JLabel ipLabel;
    private JButton removeButton;
    private JTextField nameField;
    private JLabel nameLabel;
    private JLabel portLabel;
    private JSpinner portSpinner;
    private JLabel statusLabel;
    private volatile int index;

    private volatile TelnetReaderWorker telnetReaderWorker;

    public ConnectionPanel(int index) {
        this.index = index;
        initComponents();
        initListeners();
    }

    public void load() {
        final String host = Config.INSTANCE.getProperty("app.ip" + (index + 1) + ".host");
        final String port = Config.INSTANCE.getProperty("app.ip" + (index + 1) + ".port");
        final String name = Config.INSTANCE.getProperty("app.ip" + (index + 1) + ".name");

        if (host != null) {
            ipField.setText(host);
        }

        if (port != null) {
            portSpinner.setValue(Integer.parseInt(port));
        }

        if (name != null) {
            nameField.setText(name);
        }
    }

    public void save() {
        Config.INSTANCE.setProperty("app.ip" + (index + 1) + ".host", ipField.getText());
        Config.INSTANCE.setProperty("app.ip" + (index + 1) + ".port", portSpinner.getValue() != null ? portSpinner.getValue().toString() : "23");
        Config.INSTANCE.setProperty("app.ip" + (index + 1) + ".name", nameField.getText());
    }

    private void initListeners() {
        removeButton.addActionListener(e -> {
            firePropertyChange("deleted", false, true);
        });

        ipField.addCaretListener(e -> save());
        portSpinner.addChangeListener(e -> save());
        nameField.addCaretListener(e -> save());

    }

    private void initComponents() {
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        ipLabel = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portSpinner = new javax.swing.JSpinner();
        removeButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        nameLabel.setText("Name:");

        ipLabel.setText("IP:");

        portLabel.setText("Port:");

        portSpinner.setModel(new javax.swing.SpinnerNumberModel());
        portSpinner.setValue(23);

        statusLabel.setIcon(Images.INSTANCE.getHostIdle());

        removeButton.setIcon(Images.INSTANCE.getDeleted());
        removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));

//        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
////        this.setLayout(new FlowLayout());
//        this.setBorder(new EmptyBorder(5, 5, 5, 5));
//        this.add(nameLabel);
//        nameField.setMinimumSize(new Dimension(200, nameField.getMaximumSize().height));
//        nameField.setPreferredSize(new Dimension(200, nameField.getPreferredSize().height));
//        nameField.setMaximumSize(new Dimension(200, nameField.getMaximumSize().height));
//        this.add(nameField);
//        this.add(ipLabel);
//        this.add(ipField);
//        this.add(portLabel);
//        this.add(portSpinner);
//        this.add(statusLabel);
//        this.add(removeButton);

        this.setBorder(new EmptyBorder(3, 3, 3, 3));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
                                        .addComponent(ipLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(portLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nameLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap(2, 2)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(ipLabel)
                                                        .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(portLabel)
                                                        .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(nameLabel)
                                                        .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(2, 2)
                        )
        );
    }

    public TelnetReaderWorker getTelnetReaderWorker() {
        return telnetReaderWorker;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setConnected() {
        statusLabel.setIcon(Images.INSTANCE.getHostConnected());
    }

    public void setDisconnected() {
        statusLabel.setIcon(Images.INSTANCE.getHostDisConnected());
    }

    public void setIdle() {
        statusLabel.setIcon(Images.INSTANCE.getHostIdle());
    }

    public void execute(Runnable completeCallback) {
        telnetReaderWorker = new TelnetReaderWorker(ipField.getText(),
                (Integer) portSpinner.getValue(),
                this,
                completeCallback);
        telnetReaderWorker.execute();
    }

    public void cancel() {
        telnetReaderWorker.cancel();
    }

    public List<String[]> getDataList() {
        return telnetReaderWorker.getDataList();
    }

    public void clearDataList() {
        telnetReaderWorker.clearDataList();
    }

    public String getNameOrIp() {
        return nameField.getText().trim().isEmpty() ? ipField.getText().trim() : nameField.getText().trim();
    }

    public ReentrantLock getLock() {
        return telnetReaderWorker.getLock();
    }

}
