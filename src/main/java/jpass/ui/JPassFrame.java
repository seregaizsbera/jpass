package jpass.ui;

import jpass.data.DataModel;
import jpass.ui.action.CloseListener;
import jpass.ui.action.ListListener;
import jpass.ui.action.MenuActionType;
import jpass.ui.helper.EntryHelper;
import jpass.ui.helper.FileHelper;
import jpass.util.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.List;
import java.util.Objects;

import static jpass.ui.MessageDialog.NO_OPTION;
import static jpass.ui.MessageDialog.YES_NO_CANCEL_OPTION;
import static jpass.ui.MessageDialog.YES_OPTION;
import static jpass.ui.MessageDialog.getIcon;
import static jpass.ui.MessageDialog.showQuestionMessage;

/**
 * The main frame for JPass.
 *
 * @author Gabor_Bata
 */
public final class JPassFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = -4114209356464342368L;
    private static JPassFrame instance = null;

    public static final String PROGRAM_NAME = "JPass Password Manager";
    public static final String PROGRAM_VERSION = "0.1.16-SNAPSHOT";

    private final JPopupMenu popup;
    private final SearchPanel searchPanel;
    private final JList<String> entryTitleList;
    private final DefaultListModel<String> entryTitleListModel;
    private final transient DataModel model = DataModel.getInstance();
    private final StatusPanel statusPanel;
    private volatile boolean processing = false;

    private JPassFrame(String fileName) {
        setIconImage(Objects.requireNonNull(getIcon("lock")).getImage());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(MenuActionType.NEW_FILE.getAction());
        toolBar.add(MenuActionType.OPEN_FILE.getAction());
        toolBar.add(MenuActionType.SAVE_FILE.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.ADD_ENTRY.getAction());
        toolBar.add(MenuActionType.EDIT_ENTRY.getAction());
        toolBar.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        toolBar.add(MenuActionType.DELETE_ENTRY.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.COPY_URL.getAction());
        toolBar.add(MenuActionType.COPY_USER.getAction());
        toolBar.add(MenuActionType.COPY_PASSWORD.getAction());
        toolBar.add(MenuActionType.CLEAR_CLIPBOARD.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.ABOUT.getAction());
        toolBar.add(MenuActionType.EXIT.getAction());

        this.searchPanel = new SearchPanel(enabled -> {
            if (enabled) {
                refreshEntryTitleList(null);
            }
        });

        JPanel topContainerPanel = new JPanel(new BorderLayout());
        topContainerPanel.add(toolBar, BorderLayout.NORTH);
        topContainerPanel.add(this.searchPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(MenuActionType.NEW_FILE.getAction());
        fileMenu.add(MenuActionType.OPEN_FILE.getAction());
        fileMenu.add(MenuActionType.SAVE_FILE.getAction());
        fileMenu.add(MenuActionType.SAVE_AS_FILE.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.EXPORT_XML.getAction());
        fileMenu.add(MenuActionType.IMPORT_XML.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.CHANGE_PASSWORD.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.EXIT.getAction());
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(MenuActionType.ADD_ENTRY.getAction());
        editMenu.add(MenuActionType.EDIT_ENTRY.getAction());
        editMenu.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        editMenu.add(MenuActionType.DELETE_ENTRY.getAction());
        editMenu.addSeparator();
        editMenu.add(MenuActionType.COPY_URL.getAction());
        editMenu.add(MenuActionType.COPY_USER.getAction());
        editMenu.add(MenuActionType.COPY_PASSWORD.getAction());
        editMenu.addSeparator();
        editMenu.add(MenuActionType.FIND_ENTRY.getAction());
        menuBar.add(editMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.add(MenuActionType.GENERATE_PASSWORD.getAction());
        toolsMenu.add(MenuActionType.CLEAR_CLIPBOARD.getAction());
        menuBar.add(toolsMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(MenuActionType.LICENSE.getAction());
        helpMenu.addSeparator();
        helpMenu.add(MenuActionType.ABOUT.getAction());
        menuBar.add(helpMenu);

        this.popup = new JPopupMenu();
        this.popup.add(MenuActionType.ADD_ENTRY.getAction());
        this.popup.add(MenuActionType.EDIT_ENTRY.getAction());
        this.popup.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        this.popup.add(MenuActionType.DELETE_ENTRY.getAction());
        this.popup.addSeparator();
        this.popup.add(MenuActionType.COPY_URL.getAction());
        this.popup.add(MenuActionType.COPY_USER.getAction());
        this.popup.add(MenuActionType.COPY_PASSWORD.getAction());
        this.popup.addSeparator();
        this.popup.add(MenuActionType.FIND_ENTRY.getAction());

        this.entryTitleListModel = new DefaultListModel<>();
        this.entryTitleList = new JList<>(this.entryTitleListModel);
        this.entryTitleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.entryTitleList.addMouseListener(new ListListener());
        this.entryTitleList.setCellRenderer(new DefaultListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(this.entryTitleList);
        MenuActionType.bindAllActions(this.entryTitleList);

        this.statusPanel = new StatusPanel();

        refreshAll();

        getContentPane().add(topContainerPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(this.statusPanel, BorderLayout.SOUTH);

        setJMenuBar(menuBar);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(420, 400);
        setMinimumSize(new Dimension(420, 200));
        addWindowListener(new CloseListener());
        setLocationRelativeTo(null);
        setVisible(true);
        FileHelper.doOpenFile(fileName, this);

        // set focus to the list for easier keyboard navigation
        this.entryTitleList.requestFocusInWindow();

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Exit");
        rootPane.getActionMap().put("Exit", new AbstractAction() {
            @Serial
            private static final long serialVersionUID = -923374154043418939L;
            @Override
            public void actionPerformed(ActionEvent e) {
                exitFrame();
            }
        });
    }

    public static JPassFrame getInstance() {
        return getInstance(null);
    }

    public static JPassFrame getInstance(String fileName) {
        synchronized (JPassFrame.class) {
            if (instance == null) {
                instance = new JPassFrame(fileName);
            }
        }
        return instance;
    }

    /**
     * Gets the entry title list.
     *
     * @return entry title list
     */
    public JList<String> getEntryTitleList() {
        return this.entryTitleList;
    }

    /**
     * Gets the data model of this frame.
     *
     * @return data model
     */
    public DataModel getModel() {
        return this.model;
    }

    /**
     * Clears data model.
     */
    public void clearModel() {
        this.model.clear();
        this.entryTitleListModel.clear();
    }

    /**
     * Refresh frame title based on data model.
     */
    public void refreshFrameTitle() {
        setTitle((getModel().isModified() ? "*" : "")
                + (getModel().getFileName() == null ? "Untitled" : getModel().getFileName()) + " - "
                + PROGRAM_NAME);
    }

    /**
     * Refresh the entry titles based on data model.
     *
     * @param selectTitle title to select, or {@code null} if nothing to select
     */
    public void refreshEntryTitleList(String selectTitle) {
        this.entryTitleListModel.clear();
        List<String> titles = this.model.getTitles();
        titles.sort(String.CASE_INSENSITIVE_ORDER);

        String searchCriteria = this.searchPanel.getSearchCriteria();
        for (String title : titles) {
            if (searchCriteria.isEmpty() || title.toLowerCase().contains(searchCriteria.toLowerCase())) {
                this.entryTitleListModel.addElement(title);
            }
        }

        if (selectTitle != null) {
            this.entryTitleList.setSelectedValue(selectTitle, true);
        }

        if (searchCriteria.isEmpty()) {
            this.statusPanel.setText("Entries count: " + titles.size());
        } else {
            this.statusPanel.setText("Entries found: " + this.entryTitleListModel.size() + " / " + titles.size());
        }
    }

    /**
     * Refresh frame title and entry list.
     */
    public void refreshAll() {
        refreshFrameTitle();
        refreshEntryTitleList(null);
    }

    /**
     * Exits the application.
     */
    public void exitFrame() {
        if (Configuration.is("clear.clipboard.on.exit.enabled", false)) {
            EntryHelper.copyEntryField(this, null);
        }
        if (this.processing) {
            return;
        }
        if (this.model.isModified()) {
            int option = showQuestionMessage(this,
                    "The current file has been modified.\nDo you want to save the changes before closing?", YES_NO_CANCEL_OPTION);
            if (option == YES_OPTION) {
                FileHelper.saveFile(this, false, result -> {
                    if (result) {
                        System.exit(0);
                    }
                });
                return;
            } else if (option != NO_OPTION) {
                return;
            }
        }
        System.exit(0);
    }

    public JPopupMenu getPopup() {
        return this.popup;
    }

    /**
     * Sets the processing state of this frame.
     *
     * @param processing processing state
     */
    public void setProcessing(boolean processing) {
        this.processing = processing;
        for (MenuActionType actionType : MenuActionType.values()) {
            actionType.getAction().setEnabled(!processing);
        }
        this.searchPanel.setEnabled(!processing);
        this.entryTitleList.setEnabled(!processing);
        this.statusPanel.setProcessing(processing);
    }

    /**
     * Gets the processing state of this frame.
     *
     * @return processing state
     */
    public boolean isProcessing() {
        return this.processing;
    }

    /**
     * Get search panel.
     *
     * @return the search panel
     */
    public SearchPanel getSearchPanel() {
        return searchPanel;
    }
}
