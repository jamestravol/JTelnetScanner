package jt.upwork.telnet.logger;

import javax.swing.*;

/**
 * @author jamestravol
 */
public class MainPanel extends JTabbedPane {

    public MainPanel() {
        add("Logger", new LoggerPanel());
        add("Settings", new SettingsPanel());
        add("Help", new HelpPanel());
        if (!Main.licensed) {
            add("Activation", new ActivationPanel());
        }
    }
}
