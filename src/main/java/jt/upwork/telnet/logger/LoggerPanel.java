package jt.upwork.telnet.logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jamestravol
 */
public class LoggerPanel extends JPanel {

    private javax.swing.JButton connectButton;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JButton fileButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JButton addHostButton;
    private javax.swing.JPanel connectionPanels;
    private javax.swing.JButton openButton;

    private final AtomicInteger running = new AtomicInteger();

    private volatile Timer timer;

    private void connectionEvent(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {
            case "deleted":
                delete((ConnectionPanel) evt.getSource());
                break;
        }

    }

    private void delete(final ConnectionPanel source) {
        SwingUtilities.invokeLater(() -> {
            connectionPanels.remove(source);
            Config.INSTANCE.setProperty("app.hosts.count", "" + connectionPanels.getComponentCount());
            connectionPanels.updateUI();
            final Container topLevelAncestor = this.getTopLevelAncestor();
            if (topLevelAncestor instanceof JFrame) {
                ((JFrame) topLevelAncestor).pack();
            }
            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                component.setIndex(i);
                component.save();
            }

            if (connectionPanels.getComponentCount() < Integer.parseInt(Config.INSTANCE.getProperty("app.hosts.max"))) {
                addHostButton.setEnabled(true);
            }

        });
    }

    private void updateExcel() {

        try {
            HashMap<String, List<String[]>> result = new HashMap<>();

            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                component.getLock().lock();
                String name = component.getNameOrIp();
                if (!name.isEmpty()) {
                    result.put(name, component.getDataList());
                }
            }

            File file = new File(Application.INSTANCE.getSelectedDir(), Utils.getFilename(
                    Application.INSTANCE.getStartMills(),
                    Integer.parseInt(Config.INSTANCE.getProperty("app.shift.count", "1"))));

            if (!file.exists()) {
                ExcelProcessor.createEmpty(file);
            }

            ExcelProcessor.update(file, result);

            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                component.clearDataList();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                component.getLock().unlock();
            }
        }

    }

    public LoggerPanel() {
        initComponents();
        afterInit();
        initListeners();
    }

    private void afterInit() {

        final int hostCount = Integer.parseInt(Config.INSTANCE.getProperty("app.hosts.count"));

        for (int i = 0; i < hostCount; i++) {
            final ConnectionPanel additionalPanel = new ConnectionPanel(i);
            additionalPanel.addPropertyChangeListener(this::connectionEvent);
            additionalPanel.load();
            this.connectionPanels.add(additionalPanel);
        }

        if (hostCount >= Integer.parseInt(Config.INSTANCE.getProperty("app.hosts.max"))) {
            addHostButton.setEnabled(false);
        }

    }

    private void initListeners() {

        addHostButton.addActionListener(e -> {
            final int count = connectionPanels.getComponentCount();
            final ConnectionPanel comp = new ConnectionPanel(count);
            comp.addPropertyChangeListener(this::connectionEvent);
            comp.save();
            connectionPanels.add(comp);
            connectionPanels.updateUI();
            final Container topLevelAncestor = this.getTopLevelAncestor();
            if (topLevelAncestor instanceof JFrame) {
                ((JFrame) topLevelAncestor).pack();
            }
            Config.INSTANCE.setProperty("app.hosts.count", "" + (count + 1));
            if (count + 1 >= Integer.parseInt(Config.INSTANCE.getProperty("app.hosts.max"))) {
                addHostButton.setEnabled(false);
            }
        });

        fileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            final int returnValue = chooser.showDialog(Application.INSTANCE.getFrame(), "Choose");

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                Application.INSTANCE.setSelectedFile(chooser.getSelectedFile());
                fileLabel.setText(chooser.getSelectedFile().getAbsolutePath());
                connectButton.setEnabled(true);
                openButton.setEnabled(true);
            }
        });

        connectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Application.INSTANCE.getSelectedDir().mkdirs();

                final String filename = Utils.getFilename(0);

                File file = new File(Application.INSTANCE.getSelectedDir(), filename);

                if (file.exists()) {
                    final int result = JOptionPane.showConfirmDialog(Application.INSTANCE.getFrame(),
                            String.format("Do you really want to overwrite the file %s?",
                                    file.getName()),
                            "Overwrite?",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.OK_OPTION) {
                        cleanUpFile(file);
                        execute();
                    }
                } else {
                    execute();
                }
            }
        });

        disconnectButton.addActionListener(e -> {
            disconnectButton.setEnabled(false);

            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                component.cancel();
            }
        });

        openButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(Application.INSTANCE.getSelectedDir());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void cleanUpFile(File selectedFile) {
        ExcelProcessor.createEmpty(selectedFile);
    }

    private void execute() {
        connectButton.setEnabled(false);
        fileButton.setEnabled(false);

        running.set(connectionPanels.getComponentCount());

        for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
            ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
            component.execute(() -> onStopped(component));
        }
        disconnectButton.setEnabled(true);
        final int flushIntervalMills = Integer.parseInt(Config.INSTANCE.getProperty("app.flush.interval.mills"));
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateExcel();
            }
        }, flushIntervalMills, flushIntervalMills);
        Application.INSTANCE.showActiveStatus();
        Application.INSTANCE.setStartMills(System.currentTimeMillis());
    }

    private void onStopped(ConnectionPanel connectionPanel) {
        System.out.println(String.format("Stopping process index %d. Name %s", connectionPanel.getIndex(), connectionPanel.getNameOrIp()));
        if (running.decrementAndGet() == 0) {
            System.out.println("Stopping all");
            fileButton.setEnabled(true);
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            Application.INSTANCE.showInactiveStatus();
            timer.cancel();

            updateExcel();

            for (int i = 0; i < connectionPanels.getComponentCount(); i++) {
                final ConnectionPanel component = (ConnectionPanel) connectionPanels.getComponent(i);
                SwingUtilities.invokeLater(component::setIdle);
            }
        } else {
            Application.INSTANCE.showWarnStatus();
        }
    }

    private void initComponents() {

        connectionPanels = new javax.swing.JPanel();
        connectButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        fileButton = new javax.swing.JButton();
        fileLabel = new javax.swing.JLabel();
        addHostButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();

        connectionPanels.setLayout(new BoxLayout(connectionPanels, BoxLayout.Y_AXIS));

        connectButton.setText("Start");
        connectButton.setEnabled(false);

        disconnectButton.setText("Stop");
        disconnectButton.setEnabled(false);

        fileButton.setText("Browse");

        fileLabel.setText("Select directory");

        addHostButton.setText("Add host");

        openButton.setEnabled(false);
        openButton.setText("Open");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(connectionPanels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(addHostButton)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                        .addComponent(openButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(fileButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(fileLabel))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(connectButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(disconnectButton)))))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(connectionPanels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addHostButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileButton)
                                        .addComponent(fileLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(connectButton)
                                        .addComponent(disconnectButton))
                                .addContainerGap())
        );
    }


}
