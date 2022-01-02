/*
 * JPass
 *
 * Copyright (c) 2009-2017 Gabor Bata
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jpass.ui;

import jpass.util.Configuration;
import jpass.util.CryptUtils;
import jpass.util.SpringUtilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.Random;

/**
 * Dialog for generating random passwords.
 *
 * @author Gabor_Bata
 *
 */
public final class GeneratePasswordDialog extends JDialog implements ActionListener {
    @Serial
    private static final long serialVersionUID = -1807066563698740446L;

    /**
     * Characters for custom symbols generation.
     */
    private static final String SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~";

    /**
     * Options for password generation.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String[][] PASSWORD_OPTIONS = {
        {"Upper case letters (A-Z)", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"},
        {"Lower case letters (a-z)", "abcdefghijklmnopqrstuvwxyz"},
        {"Numbers (0-9)", "0123456789"}
    };

    private JCheckBox[] checkBoxes;
    private JCheckBox customSymbolsCheck;
    private JTextField customSymbolsField;
    private JTextField passwordField;
    private JSpinner lengthSpinner;
    private String generatedPassword;
    private final Random random = CryptUtils.getRandomNumberGenerator();

    /**
     * Constructor of GeneratePasswordDialog.
     *
     * @param parent JFrame parent component
     */
    public GeneratePasswordDialog(JFrame parent) {
        super(parent);
        initDialog(parent, false);
    }

    /**
     * Constructor of GeneratePasswordDialog.
     *
     * @param parent JDialog parent component
     */
    public GeneratePasswordDialog(JDialog parent) {
        super(parent);
        initDialog(parent, true);
    }

    /**
     * Initializes the GeneratePasswordDialog instance.
     *
     * @param parent parent component
     * @param showAcceptButton if true then the dialog shows an "Accept" and "Cancel" button,
     * otherwise only a "Close" button
     *
     */
    private void initDialog(Component parent, boolean showAcceptButton) {
        setModal(true);
        setTitle("Generate Password");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.generatedPassword = null;

        JPanel lengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel lengthLabel = new JLabel("Password length:");
        lengthPanel.add(lengthLabel);

        int passwordGenerationLength = Configuration.getInteger("default.password.generation.length", 8);
        if (passwordGenerationLength > 64) {
            passwordGenerationLength = 64;
        }
        if (passwordGenerationLength < 1) {
            passwordGenerationLength = 1;
        }

        this.lengthSpinner = new JSpinner(new SpinnerNumberModel(passwordGenerationLength, 1, 64, 1));
        lengthPanel.add(this.lengthSpinner);

        JPanel charactersPanel = new JPanel();
        charactersPanel.setBorder(new TitledBorder("Settings"));
        charactersPanel.add(lengthPanel);
        this.checkBoxes = new JCheckBox[PASSWORD_OPTIONS.length];
        for (int i = 0; i < PASSWORD_OPTIONS.length; i++) {
            this.checkBoxes[i] = new JCheckBox(PASSWORD_OPTIONS[i][0], true);
            charactersPanel.add(this.checkBoxes[i]);
        }
        this.customSymbolsCheck = new JCheckBox("Custom symbols");
        this.customSymbolsCheck.setActionCommand("custom_symbols_check");
        this.customSymbolsCheck.addActionListener(this);
        charactersPanel.add(this.customSymbolsCheck);
        this.customSymbolsField = TextComponentFactory.newTextField(SYMBOLS);
        this.customSymbolsField.setEditable(false);
        charactersPanel.add(this.customSymbolsField);

        charactersPanel.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(charactersPanel, 6, 1, 5, 5, 5, 5);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBorder(new TitledBorder("Generated password"));

        this.passwordField = TextComponentFactory.newTextField();
        passwordPanel.add(this.passwordField, BorderLayout.NORTH);
        JButton generateButton = new JButton("Generate", MessageDialog.getIcon("generate"));
        generateButton.setActionCommand("generate_button");
        generateButton.addActionListener(this);
        generateButton.setMnemonic(KeyEvent.VK_G);
        JPanel generateButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        generateButtonPanel.add(generateButton);
        passwordPanel.add(generateButtonPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton;
        if (showAcceptButton) {
            JButton acceptButton = new JButton("Accept", MessageDialog.getIcon("accept"));
            acceptButton.setActionCommand("accept_button");
            acceptButton.setMnemonic(KeyEvent.VK_A);
            acceptButton.addActionListener(this);
            buttonPanel.add(acceptButton);

            cancelButton = new JButton("Cancel", MessageDialog.getIcon("cancel"));
        } else {
            cancelButton = new JButton("Close", MessageDialog.getIcon("close"));
        }

        cancelButton.setActionCommand("cancel_button");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        getContentPane().add(charactersPanel, BorderLayout.NORTH);
        getContentPane().add(passwordPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        rootPane.getActionMap().put("Cancel", new CancelAction(this));

        setResizable(false);
        pack();
        setSize((int) (getWidth() * 1.5), getHeight());
        setLocationRelativeTo(parent);
    }

    private static char randomCharacter(String set, Random random) {
        return set.charAt(random.nextInt(set.length()));
    }
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("custom_symbols_check".equals(command)) {
            this.customSymbolsField.setEditable(((JCheckBox) e.getSource()).isSelected());
        } else if ("generate_button".equals(command)) {
            StringBuilder generated = generatePassword();
            this.passwordField.setText(generated.toString());
        } else if ("accept_button".equals(command)) {
            this.generatedPassword = this.passwordField.getText();
            if (this.generatedPassword.isEmpty()) {
                MessageDialog.showWarningMessage(this, "Please generate a password.");
                return;
            }
            dispose();
        } else if ("cancel_button".equals(command)) {
            dispose();
        }
    }

    private StringBuilder generatePassword() {
        int index = 0;
        StringBuilder generated = new StringBuilder();
        StringBuilder characterSet = new StringBuilder();
        for (int i = 0; i < PASSWORD_OPTIONS.length; i++) {
            if (this.checkBoxes[i].isSelected()) {
                characterSet.append(PASSWORD_OPTIONS[i][1]);
            }
            generated.append(randomCharacter(PASSWORD_OPTIONS[i][1], random));
            index++;
        }
        if (this.customSymbolsCheck.isSelected()) {
            characterSet.append(this.customSymbolsField.getText());
            generated.append(randomCharacter(customSymbolsField.getText(), random));
            index++;
        }
        if (characterSet.isEmpty()) {
            MessageDialog.showWarningMessage(this, "Cannot generate password.\nPlease select a character set.");
            return generated;
        }
        int passwordLength = Integer.parseInt(String.valueOf(this.lengthSpinner.getValue()));
        var symbols = characterSet.toString();
        for (int i = index; i < passwordLength; i++) {
            generated.append(randomCharacter(symbols, random));
        }
        for (int i = 0; i < passwordLength; i++) {
            int a = random.nextInt(passwordLength);
            int b = random.nextInt(passwordLength);
            if (a != b) {
                char c1 = generated.charAt(a);
                char c2 = generated.charAt(b);
                generated.setCharAt(a, c2);
                generated.setCharAt(b, c1);
            }
        }
        return generated;
    }

    /**
     * Gets the generated password.
     *
     * @return if the password is not generated than the return value is {@code null}, otherwise the
     * generated password
     */
    public String getGeneratedPassword() {
        return this.generatedPassword;
    }
}
