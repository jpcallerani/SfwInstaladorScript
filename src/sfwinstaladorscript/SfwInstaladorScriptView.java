/*
 * SfwInstaladorScriptView.java
 */
package sfwinstaladorscript;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jdesktop.layout.GroupLayout;
import java.util.Iterator;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import sfwinstaladorscript.atualizacmschema.SfwInstaladorAtualizaCmSchema;
import sfwinstaladorscript.config.ClientIni;
import sfwinstaladorscript.config.InstallIni;
import sfwinstaladorscript.objects.PackageList;
import sfwinstaladorscript.config.ProductIni;
import sfwinstaladorscript.interfaces.SfwWizardPage;
import sfwinstaladorscript.objects.Product;
import sfwinstaladorscript.xmlvalidation.SfwXMLValidatorProduct;
import sfwinstaladorscript.comparabase.SfwInstaladorComparaBase;
import sfwinstaladorscript.createusers.SfwInstaladorCreateUsers;
import sfwinstaladorscript.showlogs.SfwShowLogPage;

/**
 * Frame principal da aplica��o.
 */
public class SfwInstaladorScriptView extends FrameView {

    //Variáveis para localizar páginas do wizard
    private SfwWizardPage _wzCurrent;
    private Hashtable _wzPages = new Hashtable();
    //Páginas do Wizard de instalação.
    private SfwWizardWelcome _wzWelcome = new SfwWizardWelcome();
    private SfwWizardSetup _wzSetup;
    //Objetos de configuração da instalação.
    private InstallIni _cfgInstallIni;
    private ProductIni _cfgProductIni;
    private ClientIni _cfgClientIni;

    public SfwInstaladorScriptView(SingleFrameApplication app) {

        super(app);

        FileInputStream v_fileinputstream_client;
        FileInputStream v_fileinputstream_install;
        FileInputStream v_fileinputstream_product;
        PackageList v_packagelist_pkglist;

        this.getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage("modules.png"));


        // Altera vizualização do aplicativo para sistemas Linux
        try {
            if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        initComponents();

        this.getFrame().setResizable(false);
        this.setWizardPage(this._wzWelcome);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        try {
            v_fileinputstream_client = new FileInputStream(new File("client.ini"));
            v_fileinputstream_install = new FileInputStream(new File("install.ini"));
            v_fileinputstream_product = new FileInputStream(new File("product.ini"));

            this._cfgInstallIni = new InstallIni(v_fileinputstream_install);
            this._cfgProductIni = new ProductIni(v_fileinputstream_product);
            this._cfgClientIni = new ClientIni(v_fileinputstream_client);

            Install.set_product(_cfgProductIni.get_products());

            Collections.sort(this._cfgProductIni.get_products(), new Product.ProductComparator());
            resourceMap = org.jdesktop.application.Application.getInstance(sfwinstaladorscript.SfwInstaladorScriptApp.class).getContext().getResourceMap(SfwInstaladorScriptView.class);

            try {
                v_packagelist_pkglist = new PackageList();
                v_packagelist_pkglist.read(this._cfgProductIni.get_products());
                Install.set_packagelist(v_packagelist_pkglist);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, resourceMap.getString("JOptionPane.text.versaoErro"), resourceMap.getString("JOptionPane.text.fatalError"), JOptionPane.ERROR_MESSAGE);
                this.jButtonBack.setEnabled(false);
                this.jButtonNext.setEnabled(false);
            }

            this._wzSetup = new SfwWizardSetup(this._cfgClientIni.get_clients(), this._cfgInstallIni.get_systems(), this._cfgInstallIni.get_databases());
            this._wzPages.put("wzWelcome", this._wzWelcome);
            this._wzPages.put("wzSetup", this._wzSetup);

            Utils._view = this;

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, resourceMap.getString("JOptionPane.text.configuracaoErro"), resourceMap.getString("JOptionPane.text.fatalError"), JOptionPane.ERROR_MESSAGE);
            this.jButtonBack.setEnabled(false);
            this.jButtonNext.setEnabled(false);
        }
    }

    /**
     * Altera página exibida no Wizard
     * @param page Painel da página
     */
    public void setWizardPage(JPanel page) {
        JPanel v_jpanel_currentpage;
        GroupLayout v_grouplayout_pagelayout = (GroupLayout) this.jPanelWizPage.getLayout();

        Iterator i = this._wzPages.values().iterator();
        while (i.hasNext()) {
            v_jpanel_currentpage = (JPanel) i.next();
            v_jpanel_currentpage.setVisible(false);
        }
        this.jPanelWizPage.removeAll();

        v_grouplayout_pagelayout.setHorizontalGroup(
                v_grouplayout_pagelayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, v_grouplayout_pagelayout.createSequentialGroup().addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(page, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        v_grouplayout_pagelayout.setVerticalGroup(
                v_grouplayout_pagelayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, v_grouplayout_pagelayout.createSequentialGroup().addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(page, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));

        page.setVisible(true);
        this._wzCurrent = (SfwWizardPage) page;
        this._wzCurrent.flowSetup(this);
    }

    /**
     * Instância página que dará continuidade a instalação de acordo com o tipo de banco de dados escolhido.
     * @param flowwizard Página a ser instanciada.
     */
    public void setWizardDatabase(String flowwizard) {
        Class v_class_c;
        Class[] v_arr_class_constrpartypes;
        Constructor v_constructor_ctr;

        try {
            v_class_c = Class.forName(flowwizard);
            v_arr_class_constrpartypes = new Class[]{ArrayList.class};
            v_constructor_ctr = v_class_c.getConstructor(v_arr_class_constrpartypes);
            this._wzPages.put("wzFlowDatabase", v_constructor_ctr.newInstance(this._cfgProductIni.get_products()));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retorna o botão de volta.
     * @return Botão de volta.
     */
    public javax.swing.JLabel getBackButton() {
        return this.jButtonBack;
    }

    /**
     * Retorna o botão de próximo.
     * @return Botão de próximo.
     */
    public javax.swing.JLabel getNextButton() {
        return this.jButtonNext;
    }

    /**
     * 
     * @return 
     */
    public Timer getBusyIconTimer() {
        return busyIconTimer;
    }

    /**
     * 
     * @return 
     */
    public JLabel getStatusAnimationLabel() {
        return statusAnimationLabel;
    }

    /**
     * 
     * @return 
     */
    public Icon getIdleIcon() {
        return idleIcon;
    }

    /**
     * 
     * @return 
     */
    public JLabel getStatusMessageLabel() {
        return statusMessageLabel;
    }

    /**
     * 
     * @return 
     */
    public Hashtable getWzPages() {
        return _wzPages;
    }

    @Action
    /**
     * Exibe tela com informações do aplicativo.
     */
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
            aboutBox = new SfwInstaladorScriptAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SfwInstaladorScriptApp.getApplication().show(aboutBox);
    }

    @Action
    /**
     * Exibe tela com informações do sistema corrente.
     */
    public void showSystemInfo() {
        if (systemInfo == null) {
            JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
            systemInfo = new SfwInstaladorScriptSystemInfo(mainFrame);
            systemInfo.setLocationRelativeTo(mainFrame);
        }
        SfwInstaladorScriptApp.getApplication().show(systemInfo);
    }

    @Action
    public void showAtualizaCmSchema() {
        if (atualizaCmSchema == null) {
            JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
            atualizaCmSchema = new SfwInstaladorAtualizaCmSchema(mainFrame);
            atualizaCmSchema.setLocationRelativeTo(mainFrame);
        }
        SfwInstaladorScriptApp.getApplication().show(atualizaCmSchema);
        atualizaCmSchema = null;
    }

    @Action
    public void showCreateUsers() {
        if (createUsers == null) {
            JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
            createUsers = new SfwInstaladorCreateUsers(mainFrame);
            createUsers.setLocationRelativeTo(mainFrame);
        }
        SfwInstaladorScriptApp.getApplication().show(createUsers);
        createUsers = null;
    }

    @Action
    /**
     * Exibe tela com informações do aplicativo.
     */
    public void showXMLValidatorProduct() {

        SfwXMLValidatorProduct v_sfwxmlvalidatorproduct_tela;
        JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
        v_sfwxmlvalidatorproduct_tela = new SfwXMLValidatorProduct(mainFrame, true, this._cfgProductIni.get_products(), this._cfgInstallIni.get_systems(), this._cfgInstallIni.get_databases());
        SfwInstaladorScriptApp.getApplication().show(v_sfwxmlvalidatorproduct_tela);
    }

    @Action
    public void showComparaBase() {

        SfwInstaladorComparaBase v_sfwinstaladorcomparabase_tela;
        JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
        v_sfwinstaladorcomparabase_tela = new SfwInstaladorComparaBase(mainFrame, true, this._cfgProductIni.get_products());
        SfwInstaladorScriptApp.getApplication().show(v_sfwinstaladorcomparabase_tela);
    }

    @Action
    public void showLogPage() {

        SfwShowLogPage v_sfwinstaladorcomparabase_tela;
        JFrame mainFrame = SfwInstaladorScriptApp.getApplication().getMainFrame();
        v_sfwinstaladorcomparabase_tela = new SfwShowLogPage(mainFrame, true);
        SfwInstaladorScriptApp.getApplication().show(v_sfwinstaladorcomparabase_tela);
    }

    @Action
    /**
     * a��o do bot�o "pr�ximo" do wizard.
     */
    public void nextClick() {
        this._wzCurrent.nextClick(this._wzPages, this);
    }

    @Action
    /**
     * Ação do botão "Anterior" do wizard.
     */
    public void backClick() {
        this._wzCurrent.backClick(this._wzPages, this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jPanelWizFlow = new javax.swing.JPanel();
        jButtonBack = new javax.swing.JLabel();
        jButtonNext = new javax.swing.JLabel();
        jPanelWizPage = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuXMLValidator = new javax.swing.JMenu();
        jMenuItemProductValidator = new javax.swing.JMenuItem();
        jMenuComparaBase = new javax.swing.JMenuItem();
        jMenuItemLog = new javax.swing.JMenuItem();
        jMenuCmSchema = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItemInfo = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jPanelWizFlow.setName("jPanelWizFlow"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sfwinstaladorscript.SfwInstaladorScriptApp.class).getContext().getResourceMap(SfwInstaladorScriptView.class);
        jButtonBack.setIcon(resourceMap.getIcon("jButtonBack.icon")); // NOI18N
        jButtonBack.setText(resourceMap.getString("jButtonBack.text")); // NOI18N
        jButtonBack.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonBack.setName("jButtonBack"); // NOI18N
        jButtonBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonBackMousePressed(evt);
            }
        });

        jButtonNext.setIcon(resourceMap.getIcon("jButtonNext.icon")); // NOI18N
        jButtonNext.setText(resourceMap.getString("jButtonNext.text")); // NOI18N
        jButtonNext.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonNext.setName("jButtonNext"); // NOI18N
        jButtonNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonNextMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonNextMousePressed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelWizFlowLayout = new org.jdesktop.layout.GroupLayout(jPanelWizFlow);
        jPanelWizFlow.setLayout(jPanelWizFlowLayout);
        jPanelWizFlowLayout.setHorizontalGroup(
            jPanelWizFlowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelWizFlowLayout.createSequentialGroup()
                .addContainerGap(432, Short.MAX_VALUE)
                .add(jButtonBack)
                .add(18, 18, 18)
                .add(jButtonNext)
                .add(25, 25, 25))
        );
        jPanelWizFlowLayout.setVerticalGroup(
            jPanelWizFlowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelWizFlowLayout.createSequentialGroup()
                .add(jPanelWizFlowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonNext)
                    .add(jButtonBack, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelWizPage.setName("jPanelWizPage"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelWizPageLayout = new org.jdesktop.layout.GroupLayout(jPanelWizPage);
        jPanelWizPage.setLayout(jPanelWizPageLayout);
        jPanelWizPageLayout.setHorizontalGroup(
            jPanelWizPageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 555, Short.MAX_VALUE)
        );
        jPanelWizPageLayout.setVerticalGroup(
            jPanelWizPageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 324, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelWizPage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelWizFlow, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .add(jPanelWizPage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelWizFlow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuXMLValidator.setText(resourceMap.getString("jMenuXMLValidator.text")); // NOI18N
        jMenuXMLValidator.setName("jMenuXMLValidator"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sfwinstaladorscript.SfwInstaladorScriptApp.class).getContext().getActionMap(SfwInstaladorScriptView.class, this);
        jMenuItemProductValidator.setAction(actionMap.get("showXMLValidatorProduct")); // NOI18N
        jMenuItemProductValidator.setText(resourceMap.getString("jMenuItemProductValidator.text")); // NOI18N
        jMenuItemProductValidator.setName("jMenuItemProductValidator"); // NOI18N
        jMenuXMLValidator.add(jMenuItemProductValidator);

        fileMenu.add(jMenuXMLValidator);

        jMenuComparaBase.setAction(actionMap.get("showComparaBase")); // NOI18N
        jMenuComparaBase.setAction(actionMap.get("showComparaBase")); // NOI18N
        jMenuComparaBase.setText(resourceMap.getString("jMenuComparaBase.text")); // NOI18N
        jMenuComparaBase.setName("jMenuComparaBase"); // NOI18N
        fileMenu.add(jMenuComparaBase);

        jMenuItemLog.setAction(actionMap.get("showLogPage")); // NOI18N
        jMenuItemLog.setText(resourceMap.getString("jMenuItemLog.text")); // NOI18N
        jMenuItemLog.setName("jMenuItemLog"); // NOI18N
        fileMenu.add(jMenuItemLog);

        jMenuCmSchema.setAction(actionMap.get("showAtualizaCmSchema")); // NOI18N
        jMenuCmSchema.setText(resourceMap.getString("jMenuCmSchema.text")); // NOI18N
        jMenuCmSchema.setName("jMenuCmSchema"); // NOI18N
        jMenuCmSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCmSchemaActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuCmSchema);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem1.setAction(actionMap.get("showCreateUsers")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenu1.add(jMenuItem1);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItemInfo.setAction(actionMap.get("showSystemInfo")); // NOI18N
        jMenuItemInfo.setText(resourceMap.getString("jMenuItemInfo.text")); // NOI18N
        jMenuItemInfo.setName("jMenuItemInfo"); // NOI18N
        helpMenu.add(jMenuItemInfo);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 385, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonNextMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonNextMousePressed
        if (this.jButtonNext.isEnabled()) {
            this.nextClick();
            Utils.setDefaultCursor();
        }
    }//GEN-LAST:event_jButtonNextMousePressed

    private void jButtonBackMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonBackMousePressed
        if (this.jButtonBack.isEnabled()) {
            this.backClick();
        }
    }//GEN-LAST:event_jButtonBackMousePressed

    private void jButtonNextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonNextMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonNextMouseClicked

    private void jMenuCmSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCmSchemaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuCmSchemaActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jButtonBack;
    private javax.swing.JLabel jButtonNext;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuCmSchema;
    private javax.swing.JMenuItem jMenuComparaBase;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemInfo;
    private javax.swing.JMenuItem jMenuItemLog;
    private javax.swing.JMenuItem jMenuItemProductValidator;
    private javax.swing.JMenu jMenuXMLValidator;
    private javax.swing.JPanel jPanelWizFlow;
    private javax.swing.JPanel jPanelWizPage;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JDialog systemInfo;
    private JDialog atualizaCmSchema;
    private JDialog createUsers;
}
