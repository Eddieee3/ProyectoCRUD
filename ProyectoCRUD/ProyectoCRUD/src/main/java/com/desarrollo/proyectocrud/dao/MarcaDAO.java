package com.desarrollo.proyectocrud.dao;

import com.desarrollo.proyectocrud.database.ConexionBD;
import com.desarrollo.proyectocrud.model.Marca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {

    public List<Marca> listarActivas() throws SQLException {

        String sql = "SELECT id_marca, nombre, estado " +
                "FROM marcas WHERE estado = 1 ORDER BY nombre";

        List<Marca> marcas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                marcas.add(new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre"),
                        null,
                        rs.getInt("estado")
                ));
            }
        }

        return marcas;
    }
}
