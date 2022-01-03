package jpass.ui;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import java.awt.event.ActionEvent;
import java.io.Serial;

class CancelAction extends AbstractAction {
    @Serial
    private static final long serialVersionUID = -2316585419066944600L;
    private final JDialog messageDialog;

    public CancelAction(JDialog dialog) {
        this.messageDialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        messageDialog.setVisible(false);
        messageDialog.dispose();
    }
}
