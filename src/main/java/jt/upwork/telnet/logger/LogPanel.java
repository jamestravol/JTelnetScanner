package jt.upwork.telnet.logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author jamestravol
 */
public class LogPanel extends javax.swing.JPanel {

    private final DateTimeFormatter dateFormatter;
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;

    public LogPanel() {
        dateFormatter = DateTimeFormatter.ofPattern(Config.INSTANCE.getProperty("app.log.datetime.format", "dd/MM/yyyy-HH:mm:ss"));
        initComponents();
        initListeners();
    }

    private void initListeners() {
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);

        closeButton.addActionListener(e -> this.getTopLevelAncestor().setVisible(false));
    }

    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        closeButton = new javax.swing.JButton();

        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        closeButton.setText("Close");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 500, Short.MAX_VALUE)
                                                .addComponent(closeButton))
                                        .addComponent(scrollPane))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton)
                                .addContainerGap())
        );
    }


    public void log(String text) {

        if (textArea.getLineCount() >= 1000) {
            try {
                textArea.replaceRange("", 0, textArea.getLineEndOffset(0));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        textArea.append(dateFormatter.format(LocalDateTime.now()) + " " + text + "\n");
    }
}

