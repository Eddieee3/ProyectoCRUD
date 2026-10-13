package com.desarrollo.proyectocrud.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/biblioteca_fx";
    private static final String USUARIO = "postgres";
    // Se lee de la variable de entorno DB_PASSWORD para no subir la contraseña al repositorio
    private static final String CONTRASENA = System.getenv("DB_PASSWORD");

    public static Connection conectar() throws SQLException {
        try {
            Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            System.out.println("Conexion exitosa a PostgreSQL");
            return conexion;
        } catch (SQLException e) {
            System.out.println("Error al conectar con PostgreSQL");
            System.out.println(e.getMessage());
            throw e;
        }
    }
}