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
 * Clase que implementa el hilo que se encarga de observar cambios de archivos en la BD
 * @author Manuel
 */
public class CambioArchivoListener extends Thread {

    private PGConnection pgconn;
    private Connection conn;

    private Sincro sincro;

    /**
     * Constructor para esta clase
     * @param datos
     * @param s
     */
    public CambioArchivoListener(DatosConexion datos, Sincro s) {

        this.sincro = s;
        try {

            this.conn = App.conectarDB(datos);
            pgconn = conn.unwrap(PGConnection.class);

            Statement stmt = conn.createStatement();

            stmt.execute("LISTEN cambio_archivo");
            stmt.close();

            //conn.close();
        } catch (SQLException ex) {
            System.out.println("El error en la notificación es: " + ex.getMessage());
        }
    }

    //Hilo que observa cambios
    @Override
    public void run() {
        while (true) {
            try {

                PGNotification notifications[] = pgconn.getNotifications();

                if (notifications != null) {

                    for (PGNotification notification : notifications) {
                        System.out.println("Se ha insertado el archivo con id " + notification.getParameter());
                        sincro.sincronizarArchivo(Integer.parseInt(notification.getParameter()));
                        sincro.Log("Se ha insertado el archivo con id " + notification.getParameter());
                    }
                }

                Thread.sleep(500);
                sincro.blinkLED();

            } catch (SQLException | InterruptedException ex) {
                Logger.getLogger(CambioArchivoListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
