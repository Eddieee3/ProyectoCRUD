package com.desarrollo.proyectocrud.dao;

import com.desarrollo.proyectocrud.database.ConexionBD;
import com.desarrollo.proyectocrud.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categorias WHERE estado = 1 ORDER BY nombre";
    public List<Categoria> listarActivas() throws SQLException {

        String sql = "SELECT id_categoria, nombre, descripcion, estado " +
                "FROM categorias WHERE estado = 1 ORDER BY nombre";

        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("estado")
                ));
            }
        }

        return categorias;
    }
}