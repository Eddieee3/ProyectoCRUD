package com.desarrollo.proyectocrud.dao;

import com.desarrollo.proyectocrud.database.ConexionBD;
import com.desarrollo.proyectocrud.model.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductoDAO {

    public static final Map<String, String> ORDENAMIENTOS = new LinkedHashMap<>();

    static {
        ORDENAMIENTOS.put("Nombre (A-Z)", "p.nombre ASC");
        ORDENAMIENTOS.put("Nombre (Z-A)", "p.nombre DESC");
        ORDENAMIENTOS.put("Precio (menor a mayor)", "p.precio ASC");
        ORDENAMIENTOS.put("Precio (mayor a menor)", "p.precio DESC");
        ORDENAMIENTOS.put("Stock (menor a mayor)", "p.stock ASC");
        ORDENAMIENTOS.put("Stock (mayor a menor)", "p.stock DESC");
        ORDENAMIENTOS.put("Fecha (más reciente)", "p.fecha_creacion DESC");
        ORDENAMIENTOS.put("Fecha (más antigua)", "p.fecha_creacion ASC");
    }

    public List<Producto> buscar(String texto, Integer idCategoria,
                                 LocalDate desde, LocalDate hasta,
                                 Double precioMin, Double precioMax,
                                 boolean soloConStock, String ordenarPor) throws SQLException {

        // CONSULTA LIMPIA SIN EL ESQUEMA "catalogo."
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.precio, p.stock, " +
                        "       p.fecha_creacion, p.id_categoria, c.nombre AS categoria, " +
                        "       p.id_marca, m.nombre AS marca, p.estado " +
                        "FROM productos p " +
                        "INNER JOIN categorias c ON c.id_categoria = p.id_categoria " +
                        "INNER JOIN marcas m ON m.id_marca = p.id_marca " +
                        "WHERE p.estado = 1");

        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (p.codigo ILIKE ? OR p.nombre ILIKE ?)");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
        }

        if (idCategoria != null) {
            sql.append(" AND p.id_categoria = ?");
            parametros.add(idCategoria);
        }

        if (desde != null) {
            sql.append(" AND p.fecha_creacion >= ?");
            parametros.add(Date.valueOf(desde));
        }

        if (hasta != null) {
            sql.append(" AND p.fecha_creacion <= ?");
            parametros.add(Date.valueOf(hasta));
        }

        if (precioMin != null) {
            sql.append(" AND p.precio >= ?");
            parametros.add(precioMin);
        }

        if (precioMax != null) {
            sql.append(" AND p.precio <= ?");
            parametros.add(precioMax);
        }

        if (soloConStock) {
            sql.append(" AND p.stock > 0");
        }

        sql.append(" ORDER BY ").append(ORDENAMIENTOS.getOrDefault(ordenarPor, "p.nombre ASC"));

        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fecha = rs.getDate("fecha_creacion");

                    productos.add(new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("descripcion"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            fecha != null ? fecha.toLocalDate() : null,
                            rs.getInt("id_categoria"),
                            rs.getString("categoria"),
                            rs.getInt("id_marca"),
                            rs.getString("marca"),
                            rs.getInt("estado")
                    ));
                }
            }
        }

        return productos;
    }

    public void insertar(Producto producto) throws SQLException {

        String sql = "INSERT INTO productos (codigo, nombre, descripcion, precio, stock, " +
                "fecha_creacion, id_categoria, id_marca, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getStock());
            ps.setDate(6, Date.valueOf(producto.getFechaCreacion()));
            ps.setInt(7, producto.getIdCategoria());
            ps.setInt(8, producto.getIdMarca());

            ps.executeUpdate();
        }
    }
}