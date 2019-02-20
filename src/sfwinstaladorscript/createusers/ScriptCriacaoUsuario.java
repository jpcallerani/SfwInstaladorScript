/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sfwinstaladorscript.createusers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import sfwinstaladorscript.createusers.objects.TablespaceData;
import sfwinstaladorscript.createusers.objects.TablespaceIndex;
import sfwinstaladorscript.createusers.objects.Role;
import sfwinstaladorscript.createusers.objects.Usuario;
import sfwinstaladorscript.createusers.objects.UsuarioSombra;

/**
 *
 * @author U0180463
 */
public class ScriptCriacaoUsuario {

    private List<Usuario> v_usuario;
    private TablespaceData v_table_space_data;
    private TablespaceIndex v_table_space_index;
    private Role v_role;
    private UsuarioSombra v_usuario_sombra;
    private StringBuilder sb_script_criacao;

    public ScriptCriacaoUsuario(List<Usuario> p_usuario, TablespaceData p_table_space_data, TablespaceIndex p_table_space_index, Role p_role, UsuarioSombra p_usuario_sombra) {
        this.v_usuario = p_usuario;
        this.v_table_space_data = p_table_space_data;
        this.v_table_space_index = p_table_space_index;
        this.v_role = p_role;
        this.v_usuario_sombra = p_usuario_sombra;

        sb_script_criacao = new StringBuilder();

        sb_script_criacao.append("-- data tablespace \n"
                + "CREATE TABLESPACE " + this.v_table_space_data.getTable_space_data() + " \n"
                + "DATAFILE '" + this.v_table_space_data.getData_file_data() + "' "
                + "SIZE 20M AUTOEXTEND ON NEXT  20M \n"
                + "MAXSIZE  4000M FORCE LOGGING; \n\n");
        sb_script_criacao.append("-- index tablespace \n"
                + "CREATE TABLESPACE " + this.v_table_space_index.getTable_space_index() + " \n"
                + "DATAFILE '" + this.v_table_space_index.getData_file_index() + "' \n"
                + "SIZE 20M AUTOEXTEND ON NEXT  20M \n"
                + "MAXSIZE 4000M FORCE LOGGING;\n\n");

        sb_script_criacao.append("-- ROLE creation\n");
        sb_script_criacao.append("CREATE ROLE " + this.v_role.getRole() + ";\n");
        sb_script_criacao.append("grant execute on dbms_crypto to " + this.v_role.getRole() + ";\n\n");

        sb_script_criacao.append("-- shadow user creation\n");
        sb_script_criacao.append("CREATE USER " + this.v_usuario_sombra.getUsuario() + " IDENTIFIED BY " + this.v_usuario_sombra.getPassword() + ";\n\n");
        sb_script_criacao.append("GRANT SELECT ON V_$SESSION TO " + this.v_usuario_sombra.getUsuario() + ";\n");
        sb_script_criacao.append("GRANT CREATE SESSION TO " + this.v_usuario_sombra.getUsuario() + ";\n");
        sb_script_criacao.append("GRANT ALTER SESSION TO " + this.v_usuario_sombra.getUsuario() + ";\n");
        sb_script_criacao.append("GRANT " + this.v_role.getRole() + " TO " + this.v_usuario_sombra.getUsuario() + ";\n");

        for (int i = 0; i < this.v_usuario.size(); i++) {
            Usuario usuario = this.v_usuario.get(i);
            sb_script_criacao.append("CREATE USER " + usuario.getUsuario() + "  \n"
                    + "IDENTIFIED BY " + usuario.getPassword() + " \n"
                    + "DEFAULT TABLESPACE " + this.v_table_space_data.getTable_space_data() + " \n"
                    + "QUOTA UNLIMITED ON " + this.v_table_space_data.getTable_space_data() + " \n"
                    + "QUOTA UNLIMITED ON " + this.v_table_space_index.getTable_space_index() + "; \n"
                    + "grant alter session, \n"
                    + "     create session, \n"
                    + "     create database link, \n"
                    + "     create procedure, \n"
                    + "     create sequence, \n"
                    + "     create synonym, \n"
                    + "     create table, \n"
                    + "     create trigger, \n"
                    + "     create type, \n"
                    + "     create view, \n"
                    + "     debug any procedure, \n"
                    + "     debug connect session to " + usuario.getUsuario() + "; \n"
                    + "grant select on v_$session to " + usuario.getUsuario() + "; \n"
                    + "grant execute on dbms_crypto to " + usuario.getUsuario() + ";\n\n");
        }
    }

    /**
     * 
     */
    public void spool() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Script de criação de usuário.");
        dialog.setSize(640, 480);
        dialog.setModal(true);
        dialog.setResizable(false);
        JTextArea area = new JTextArea();
        area.setText(sb_script_criacao.toString());
        JScrollPane scroll = new JScrollPane(area);
        dialog.add(scroll);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - dialog.getWidth()) / 2;
        final int y = (screenSize.height - dialog.getHeight()) / 2;
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }
}
