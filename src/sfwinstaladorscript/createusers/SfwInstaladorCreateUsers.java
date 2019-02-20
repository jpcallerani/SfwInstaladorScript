package sfwinstaladorscript.createusers;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;
import sfwinstaladorscript.Utils;
import sfwinstaladorscript.createusers.objects.Role;
import sfwinstaladorscript.createusers.objects.TablespaceData;
import sfwinstaladorscript.createusers.objects.TablespaceIndex;
import sfwinstaladorscript.createusers.objects.Usuario;
import sfwinstaladorscript.createusers.objects.UsuarioSombra;

public class SfwInstaladorCreateUsers extends javax.swing.JDialog {

    DefaultTableModel v_table_space;
    DefaultTableModel v_table_usuario;
    List<Usuario> v_list_usuario;
    TablespaceData v_table_space_data;
    TablespaceIndex v_table_space_index;
    Role v_role;
    UsuarioSombra v_usuario_sombra;

    public SfwInstaladorCreateUsers(java.awt.Frame parent) {
        super(parent);
        initComponents();
        //
        v_role = new Role();
        //
        v_usuario_sombra = new UsuarioSombra();
        //
        v_list_usuario = new ArrayList<Usuario>();
        //
        v_table_space = (DefaultTableModel) jTableTablespace.getModel();
        v_table_space.setNumRows(0);
        //
        v_table_usuario = (DefaultTableModel) jTableUsuarios.getModel();
        v_table_usuario.setNumRows(0);
        //
        jTextFieldSigla.requestFocus();
    }

    @Action
    /**
     * 
     */
    public void geraArquivoUsuariosAndTablespaces() {

        Thread geraArquivoUsuariosAndTablespaces = new Thread("geraArquivoUsuariosAndTablespaces") {

            @Override
            public void run() {
                setLoader();

                // Atualiza as informações da tela;
                atualizaInformações();

                // Cria a tela de visualização;
                ScriptCriacaoUsuario v_criacao = new ScriptCriacaoUsuario(v_list_usuario, v_table_space_data, v_table_space_index, v_role, v_usuario_sombra);

                // Reseta a tela anterior;
                v_table_space.setNumRows(0);
                v_table_usuario.setNumRows(0);
                v_list_usuario.removeAll(v_list_usuario);
                jTextFieldSigla.setText("");
                jTextFieldRole.setText("");
                jTextFieldUsuarioSombra.setText("");
                jTextFieldPassSombra.setText("");
                jButtonCriaArquivo.setEnabled(false);
                jButtonCriaBase.setEnabled(false);

                // Mostra a tela de visualização;
                v_criacao.spool();

                //
                jTextFieldSigla.requestFocus();

                //
                setLoaderDisables();
            }
        };
        geraArquivoUsuariosAndTablespaces.start();
    }

    @Action
    /**
     * 
     */
    public void geraUsuariosAndTablespaces() {

        Thread geraUsuariosAndTablespaces = new Thread("geraUsuariosAndTablespaces") {

            @Override
            public void run() {
                setLoader();

                // Reseta as variáveis;
                v_table_space.setNumRows(0);
                v_table_usuario.setNumRows(0);
                v_list_usuario.removeAll(v_list_usuario);

                // Se não for preenchida a sigla, mostra um alerta;
                if (jTextFieldSigla.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Digite a sigla.", Utils.getDefaultBundle().getString("Validation.warn"), JOptionPane.WARNING_MESSAGE);
                    jTextFieldSigla.requestFocus();
                    // Se for tudo ok, preenche as informações na tela;
                } else {
                    // Informações de tablespace de data;
                    v_table_space.addRow(new Object[]{"TR_" + jTextFieldSigla.getText() + "_DATA", "TR_" + jTextFieldSigla.getText() + "_DATA01.ora"});

                    // Informações de tablespace de index;
                    v_table_space.addRow(new Object[]{"TR_" + jTextFieldSigla.getText() + "_INDEX", "TR_" + jTextFieldSigla.getText() + "_INDEX01.ora"});

                    // Informações do usuário de Base Genérica;
                    v_table_usuario.addRow(new Object[]{"TR" + jTextFieldSigla.getText(), "TR" + jTextFieldSigla.getText()});

                    // Informações do usuário de InOut;
                    v_table_usuario.addRow(new Object[]{"TRIO" + jTextFieldSigla.getText(), "TRIO" + jTextFieldSigla.getText()});

                    // Informações do usuário de Import;
                    v_table_usuario.addRow(new Object[]{"TRIS" + jTextFieldSigla.getText(), "TRIS" + jTextFieldSigla.getText()});

                    // Informações do usuário de Broker;
                    v_table_usuario.addRow(new Object[]{"TRBS" + jTextFieldSigla.getText(), "TRBS" + jTextFieldSigla.getText()});

                    // Informações do usuário de Export;
                    v_table_usuario.addRow(new Object[]{"TRES" + jTextFieldSigla.getText(), "TRES" + jTextFieldSigla.getText()});

                    // Informações do usuário de Drawback;
                    v_table_usuario.addRow(new Object[]{"TRDB" + jTextFieldSigla.getText(), "TRDB" + jTextFieldSigla.getText()});

                    // Informações do usuário de Cambio Exp;
                    v_table_usuario.addRow(new Object[]{"TRCE" + jTextFieldSigla.getText(), "TRCE" + jTextFieldSigla.getText()});

                    // Informações do usuário de Cambio Imp;
                    v_table_usuario.addRow(new Object[]{"TRCI" + jTextFieldSigla.getText(), "TRCI" + jTextFieldSigla.getText()});

                    // Informações do usuário de Integração;
                    v_table_usuario.addRow(new Object[]{"TRIT" + jTextFieldSigla.getText(), "TRIT" + jTextFieldSigla.getText()});

                    // Informações do usuário de TrackingSys;
                    v_table_usuario.addRow(new Object[]{"TRTS" + jTextFieldSigla.getText(), "TRTS" + jTextFieldSigla.getText()});

                    // Informações do usuário de Integração;
                    v_table_usuario.addRow(new Object[]{"TRCST" + jTextFieldSigla.getText(), "TRCST" + jTextFieldSigla.getText()});


                    // Cria a role;
                    jTextFieldRole.setText("RTR" + jTextFieldSigla.getText());

                    // Cria usuario sombra;
                    jTextFieldUsuarioSombra.setText("STR" + jTextFieldSigla.getText());
                    jTextFieldPassSombra.setText("TR" + jTextFieldSigla.getText());

                    // jButtonCriaBase.setEnabled(true);
                    jButtonCriaArquivo.setEnabled(true);

                }

                setLoaderDisables();
            }
        };
        geraUsuariosAndTablespaces.start();
    }

    /**
     * 
     */
    private void atualizaInformações() {
        // Atualiza informações da ROLE;
        v_role.setRole(jTextFieldRole.getText());

        // Atualiza informações do sombra
        v_usuario_sombra.setUsuario(jTextFieldUsuarioSombra.getText());
        v_usuario_sombra.setPassword(jTextFieldPassSombra.getText());

        // Cria uma tablespace de data;
        v_table_space_data = new TablespaceData();
        v_table_space_data.setTable_space_data(v_table_space.getValueAt(0, 0).toString());
        v_table_space_data.setData_file_data(v_table_space.getValueAt(0, 1).toString());

        // Cria uma tablespace de index;
        v_table_space_index = new TablespaceIndex();
        v_table_space_index.setTable_space_index(v_table_space.getValueAt(1, 0).toString());
        v_table_space_index.setData_file_index(v_table_space.getValueAt(1, 1).toString());

        // Cria um usuário para cada linha;
        for (int i = 0; i < v_table_usuario.getRowCount(); i++) {
            Usuario usuario = new Usuario();

            usuario.setUsuario(v_table_usuario.getValueAt(i, 0).toString());
            usuario.setPassword(v_table_usuario.getValueAt(i, 1).toString());

            v_list_usuario.add(usuario);
        }
    }

    /**
     *
     * @return
     */
    private void setLoaderDisables() {
        jLabelLoader.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("sfwinstaladorscript/atualizacmschema/resources/loading.png")));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     *
     * @return
     */
    private void setLoader() {
        jLabelLoader.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("sfwinstaladorscript/atualizacmschema/resources/loading.gif")));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelCreateUsers = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldSigla = new javax.swing.JTextField();
        jButtonGerar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableTablespace = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableUsuarios = new javax.swing.JTable();
        jButtonCriaBase = new javax.swing.JButton();
        jButtonCriaArquivo = new javax.swing.JButton();
        jLabelLoader = new javax.swing.JLabel();
        jTextFieldRole = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldUsuarioSombra = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldPassSombra = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sfwinstaladorscript.SfwInstaladorScriptApp.class).getContext().getResourceMap(SfwInstaladorCreateUsers.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        setResizable(false);

        jPanelCreateUsers.setName("jPanelCreateUsers"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"), resourceMap.getColor("jPanel2.border.titleColor"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldSigla.setText(resourceMap.getString("jTextFieldSigla.text")); // NOI18N
        jTextFieldSigla.setName("jTextFieldSigla"); // NOI18N
        jTextFieldSigla.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldSiglaFocusGained(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sfwinstaladorscript.SfwInstaladorScriptApp.class).getContext().getActionMap(SfwInstaladorCreateUsers.class, this);
        jButtonGerar.setAction(actionMap.get("geraUsuariosAndTablespaces")); // NOI18N
        jButtonGerar.setIcon(resourceMap.getIcon("jButtonGerar.icon")); // NOI18N
        jButtonGerar.setText(resourceMap.getString("jButtonGerar.text")); // NOI18N
        jButtonGerar.setName("jButtonGerar"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel3)
                .addGap(41, 41, 41)
                .addComponent(jTextFieldSigla, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addGap(82, 82, 82)
                .addComponent(jButtonGerar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(jButtonGerar)
                    .addComponent(jTextFieldSigla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableTablespace.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tablespace", "Datafile"
            }
        ));
        jTableTablespace.setName("jTableTablespace"); // NOI18N
        jScrollPane1.setViewportView(jTableTablespace);
        jTableTablespace.getColumnModel().getColumn(0).setResizable(false);
        jTableTablespace.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableTablespace.columnModel.title0")); // NOI18N
        jTableTablespace.getColumnModel().getColumn(1).setResizable(false);
        jTableTablespace.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableTablespace.columnModel.title1")); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTableUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Usuário", "Senha"
            }
        ));
        jTableUsuarios.setName("jTableUsuarios"); // NOI18N
        jScrollPane2.setViewportView(jTableUsuarios);
        jTableUsuarios.getColumnModel().getColumn(0).setResizable(false);
        jTableUsuarios.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableUsuarios.columnModel.title0")); // NOI18N
        jTableUsuarios.getColumnModel().getColumn(1).setResizable(false);
        jTableUsuarios.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableUsuarios.columnModel.title1")); // NOI18N

        jButtonCriaBase.setIcon(resourceMap.getIcon("jButtonCriaBase.icon")); // NOI18N
        jButtonCriaBase.setText(resourceMap.getString("jButtonCriaBase.text")); // NOI18N
        jButtonCriaBase.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonCriaBase.setEnabled(false);
        jButtonCriaBase.setName("jButtonCriaBase"); // NOI18N

        jButtonCriaArquivo.setAction(actionMap.get("geraArquivoUsuariosAndTablespaces")); // NOI18N
        jButtonCriaArquivo.setIcon(resourceMap.getIcon("jButtonCriaArquivo.icon")); // NOI18N
        jButtonCriaArquivo.setText(resourceMap.getString("jButtonCriaArquivo.text")); // NOI18N
        jButtonCriaArquivo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonCriaArquivo.setEnabled(false);
        jButtonCriaArquivo.setName("jButtonCriaArquivo"); // NOI18N

        jLabelLoader.setIcon(resourceMap.getIcon("jLabelLoader.icon")); // NOI18N
        jLabelLoader.setName("jLabelLoader"); // NOI18N

        jTextFieldRole.setName("jTextFieldRole"); // NOI18N
        jTextFieldRole.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldRoleFocusGained(evt);
            }
        });

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jTextFieldUsuarioSombra.setName("jTextFieldUsuarioSombra"); // NOI18N
        jTextFieldUsuarioSombra.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldUsuarioSombraFocusGained(evt);
            }
        });

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextFieldPassSombra.setName("jTextFieldPassSombra"); // NOI18N
        jTextFieldPassSombra.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldPassSombraFocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanelCreateUsersLayout = new javax.swing.GroupLayout(jPanelCreateUsers);
        jPanelCreateUsers.setLayout(jPanelCreateUsersLayout);
        jPanelCreateUsersLayout.setHorizontalGroup(
            jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCreateUsersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCreateUsersLayout.createSequentialGroup()
                        .addComponent(jLabelLoader)
                        .addContainerGap())
                    .addGroup(jPanelCreateUsersLayout.createSequentialGroup()
                        .addGroup(jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                            .addGroup(jPanelCreateUsersLayout.createSequentialGroup()
                                .addComponent(jButtonCriaBase, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonCriaArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelCreateUsersLayout.createSequentialGroup()
                                .addGap(164, 164, 164)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldUsuarioSombra, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldPassSombra, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                        .addGap(17, 17, 17))
                    .addGroup(jPanelCreateUsersLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldRole, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanelCreateUsersLayout.setVerticalGroup(
            jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCreateUsersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldUsuarioSombra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldPassSombra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCreateUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCriaArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCriaBase, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelLoader, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(145, 145, 145))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelCreateUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelCreateUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldSiglaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldSiglaFocusGained
        jTextFieldSigla.selectAll();
    }//GEN-LAST:event_jTextFieldSiglaFocusGained

    private void jTextFieldRoleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldRoleFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldRoleFocusGained

    private void jTextFieldUsuarioSombraFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldUsuarioSombraFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldUsuarioSombraFocusGained

    private void jTextFieldPassSombraFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPassSombraFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldPassSombraFocusGained
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCriaArquivo;
    private javax.swing.JButton jButtonCriaBase;
    private javax.swing.JButton jButtonGerar;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelLoader;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelCreateUsers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableTablespace;
    private javax.swing.JTable jTableUsuarios;
    private javax.swing.JTextField jTextFieldPassSombra;
    private javax.swing.JTextField jTextFieldRole;
    private javax.swing.JTextField jTextFieldSigla;
    private javax.swing.JTextField jTextFieldUsuarioSombra;
    // End of variables declaration//GEN-END:variables
}
