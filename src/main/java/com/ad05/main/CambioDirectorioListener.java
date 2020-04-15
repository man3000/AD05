/*
 * Manuel Alejandro Álvarez Pérez
 * AD05
 */
package com.ad05.main;

import com.ad05.util.DatosConexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 * Clase que implementa el hilo que se encarga de observar cambios de directorios en la BD
 * @author Manuel
 */
public class CambioDirectorioListener extends Thread {

    private Connection conn;
    private PGConnection pgconn;
    
    private Sincro sincro;

    /**
     * Constructor para esta clase
     * @param datosConexion
     * @param s
     */
    public CambioDirectorioListener(DatosConexion datosConexion, Sincro s) {
        
        this.sincro = s;
        
        try {
            
            this.conn = App.conectarDB(datosConexion);
            pgconn = conn.unwrap(PGConnection.class);

            Statement stmt = conn.createStatement();

            stmt.execute("LISTEN cambio_directorio");
            stmt.close();
            

        } catch (SQLException ex) {
            System.out.println("El error en la notificación es: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                PGNotification notifications[] = pgconn.getNotifications();
                
                if (notifications != null) {
                    
                    for (PGNotification notification : notifications) {
                        System.out.println("Se ha insertado un directorio con id " + notification.getParameter());
                        sincro.sincronizarDirectorio(Integer.parseInt(notification.getParameter()));
                        sincro.Log("Se ha insertado un directorio con id " + notification.getParameter());
                    }
                }
                
                Thread.sleep(500);
                
            } catch (SQLException ex) {
                Logger.getLogger(CambioDirectorioListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(CambioDirectorioListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
