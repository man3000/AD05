/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ad05.main;

import com.ad05.util.DatosConexion;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Manuel
 */
public class App extends javax.swing.JFrame {

    static String dir = System.getProperty("user.dir");
    static String sep = File.separator;
    static final File configJson = new File(dir + sep + "src" + sep + "main" + sep + "java" + sep + "com" + sep + "ad05" + sep + "util" + sep + "config.json");

    private static DatosConexion datosConexion;

    /**
     * Creates new form App
     */
    public App() {
        initComponents();

        cargarDatosConfigJson(configJson);

        crearTablas();

        File f = new File(datosConexion.getApp().get("directory"));
        sincroCarpetas(f);
        sincroArchivos(f);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        App app = new App();
        app.setVisible(true);
    }

    private void cargarDatosConfigJson(File f) {
        File file = f;
        if (file.exists()) {
            try {

                Gson gson2 = new Gson();
                BufferedReader input = new BufferedReader(new FileReader(file));

                StringBuilder injson = new StringBuilder("");

                String s;
                while ((s = input.readLine()) != null) {
                    injson.append(s);
                }
                App.datosConexion = gson2.fromJson(injson.toString(), DatosConexion.class);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (JsonSyntaxException e) {

                JOptionPane.showMessageDialog(this, "Error al procesar el archivo config.json", "Error", JOptionPane.CANCEL_OPTION);
                System.exit(0);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se encuentra el archivo provincias.json", "Error", JOptionPane.CANCEL_OPTION);
            System.exit(0);

        }

        for (Map.Entry<String, String> entry : App.datosConexion.getDbConnection().entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        for (Map.Entry<String, String> entry : App.datosConexion.getApp().entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

    }

    private Connection conectarDB(DatosConexion datos) throws SQLException {
        String url = datos.getDbConnection().get("address");
        String db = datos.getDbConnection().get("name");

        String user = datos.getDbConnection().get("user");
        String password = datos.getDbConnection().get("password");

        Properties props = new Properties();

        props.setProperty("user", user);
        props.setProperty("password", password);

        String postgres = "jdbc:postgresql://" + url + "/" + db;

        Connection conn = DriverManager.getConnection(postgres, props);

        return conn;

    }

    private void sincroCarpetas(File file) {

        File[] carpetas = file.listFiles();

        String dir = "";

        for (File carpeta : carpetas) {
            if (carpeta.isDirectory()) {
                dir = carpeta.getAbsolutePath().replace(datosConexion.getApp().get("directory"), ".");
                insertarCarpeta(dir);
                sincroCarpetas(carpeta);
            }
        }

    }

    private void sincroArchivos(File file) {

        File[] archivos = file.listFiles();

        String dir = "";
        String arch = "";

        for (File archivo : archivos) {
            if (archivo.isFile()) {
                dir = archivo.getParent().replace(datosConexion.getApp().get("directory"), ".");
                arch = archivo.getName();
                insertarArchivo(arch, dir);
            } else {
                sincroArchivos(archivo);
            }
        }

    }

    private void insertarCarpeta(String nombreCarpeta) {

        String sql = "insert into public.directorios(nombre) values(?)";

        long id = 0;

        try {
            Connection conn = conectarDB(datosConexion);

            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, nombreCarpeta);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) // get the ID back
            {
                try {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                    System.out.println("Se insertó la fila " + id);
                    conn.close();

                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void insertarArchivo(String archivo, String dir) {
        //Collemos o arquivo
        String ruta = datosConexion.getApp().get("directory") + dir.substring(1) + File.separator + archivo;
        File file = new File(ruta);

        Connection conn;

        try {
            conn = conectarDB(datosConexion);

            int idDir = obtenerIdDir(dir, conn);

            FileInputStream fis = new FileInputStream(file);

            //Creamos a consulta que inserta a imaxe na base de datos
            String sqlInsert = "insert into archivos (nombre, archivo, dir) VALUES (?, ?, ?);";
            PreparedStatement ps = conn.prepareStatement(sqlInsert);

            //Engadimos como primeiro parámetro o nome do arquivo
            ps.setString(1, archivo);
            ps.setInt(3, idDir);

            //Engadimos como segundo parámetro o arquivo e a súa lonxitude
            ps.setBinaryStream(2, fis, (int) file.length());

            //Executamos a consulta
            ps.executeUpdate();

            //Cerrramos a consulta e o arquivo aberto
            ps.close();
            fis.close();
        } catch (SQLException ex) {
            System.out.println("La entrada ya existe.");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void crearTablas() {

        try {
            Connection conn = conectarDB(datosConexion);

            String sqlTableCreationDirectorios = "create table if not exists "
                    + "directorios(id serial, nombre text, primary key (id), "
                    + "constraint nombre_unico unique (nombre));";
            String sqlTableCreationArchivos = "create table if not exists "
                    + "archivos(id serial, nombre text, archivo bytea, "
                    + "dir integer, primary key (id), "
                    + "constraint nombre_dir_unico unique (nombre, dir));";

            CallableStatement createFunction = conn.prepareCall(sqlTableCreationDirectorios);
            CallableStatement createFunction2 = conn.prepareCall(sqlTableCreationArchivos);
            createFunction.execute();
            createFunction2.execute();

            createFunction.close();
            createFunction2.close();

        } catch (SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int obtenerIdDir(String dir, Connection conn) {

        try {
            
            String sql = "select id from directorios where nombre = ?";
            
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, dir);
            ResultSet rs = pst.executeQuery();

            rs.next();
            return rs.getInt("id");


        } catch (SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
