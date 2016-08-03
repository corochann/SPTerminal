package ui;

import serial.CommPortSender;
import serial.ProtocolImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class TerminalPanel extends JPanel implements ActionListener {

    public static final String ACTION_OUTPUT_STREAM = "actionOutputStream";
    private final JTextArea mInputStreamTextArea;
    private final JTextArea mOutputStreamTextArea;
    private final JTextField mOutputStreamTextField;

    TerminalPanel() {
        super(new BorderLayout());


        JPanel inputStreamTextAreaRowPanel = new JPanel(new BorderLayout());
        JPanel outputStreamTextAreaRowPanel = new JPanel();
        outputStreamTextAreaRowPanel.setLayout(new BoxLayout(outputStreamTextAreaRowPanel, BoxLayout.Y_AXIS));

        mInputStreamTextArea = new JTextArea(30, 75);
        mInputStreamTextArea.setLineWrap(false);

        mOutputStreamTextField = new JTextField();
        mOutputStreamTextField.setActionCommand(ACTION_OUTPUT_STREAM);
        mOutputStreamTextField.addActionListener(this);

        mOutputStreamTextArea = new JTextArea(30, 75);
        mOutputStreamTextArea.setLineWrap(false);

        JScrollPane inputStreamScrollPane = new JScrollPane(mInputStreamTextArea);
        JScrollPane outputStreamScrollPane = new JScrollPane(mOutputStreamTextArea);


        inputStreamTextAreaRowPanel.add(inputStreamScrollPane, BorderLayout.CENTER);

        outputStreamTextAreaRowPanel.add(mOutputStreamTextField);
        outputStreamTextAreaRowPanel.add(outputStreamScrollPane);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(inputStreamTextAreaRowPanel);
        this.add(outputStreamTextAreaRowPanel);
    }

    public void appendText(final String str) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                mInputStreamTextArea.append(str + "\n");
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand() + " received");
        if(e.getActionCommand().equals(ACTION_OUTPUT_STREAM)) {
            /* stdout TextArea */
            CommPortSender.send(new ProtocolImpl().getMessage(mOutputStreamTextField.getText()));
        }
    }


}
