package com.desarrollo.proyectocrud.controller;

import com.desarrollo.proyectocrud.dao.CategoriaDAO;
import com.desarrollo.proyectocrud.dao.MarcaDAO;
import com.desarrollo.proyectocrud.dao.ProductoDAO;
import com.desarrollo.proyectocrud.model.Categoria;
import com.desarrollo.proyectocrud.model.Marca;
import com.desarrollo.proyectocrud.model.Producto;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ProductosController {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final MarcaDAO marcaDAO = new MarcaDAO();

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private DatePicker dpFecha;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtPrecioMin;
    @FXML private TextField txtPrecioMax;
    @FXML private CheckBox chkConStock;
    @FXML private ComboBox<String> cmbOrdenar;

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, LocalDate> colFecha;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        cmbOrdenar.setItems(FXCollections.observableArrayList(ProductoDAO.ORDENAMIENTOS.keySet()));

        try {
            List<Categoria> categorias = categoriaDAO.listarActivas();
            cmbFiltroCategoria.setItems(FXCollections.observableArrayList(categorias));
            cmbCategoria.setItems(FXCollections.observableArrayList(categorias));
            cmbMarca.setItems(FXCollections.observableArrayList(marcaDAO.listarActivas()));
        } catch (SQLException e) {
            mostrarError("No se pudo conectar a la base de datos", e.getMessage());
            return;
        }

        buscarProductos();
    }

    @FXML
    private void nuevoProducto() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFecha.setValue(null);
    }

    @FXML
    private void guardarProducto() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        Categoria categoria = cmbCategoria.getValue();
        Marca marca = cmbMarca.getValue();

        if (codigo.isEmpty() || nombre.isEmpty() || categoria == null || marca == null) {
            mostrarError("Datos incompletos", "Código, nombre, categoría y marca son obligatorios.");
            return;
        }

        Double precio;
        int stock;

        try {
            precio = leerDecimal(txtPrecio);
            stock = txtStock.getText().isBlank() ? 0 : Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Datos inválidos", "El precio debe ser un número (ej. 10.50) y el stock un entero.");
            return;
        }

        if (precio == null || precio < 0 || stock < 0) {
            mostrarError("Datos inválidos", "El precio es obligatorio y precio y stock no pueden ser negativos.");
            return;
        }

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(txtDescripcion.getText().isBlank() ? null : txtDescripcion.getText().trim());
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setFechaCreacion(dpFecha.getValue() != null ? dpFecha.getValue() : LocalDate.now());
        producto.setIdCategoria(categoria.getIdCategoria());
        producto.setIdMarca(marca.getIdMarca());

        try {
            productoDAO.insertar(producto);
        } catch (SQLException e) {
            // 23505 = violacion de UNIQUE en PostgreSQL (codigo repetido)
            if ("23505".equals(e.getSQLState())) {
                mostrarError("Código duplicado", "Ya existe un producto con el código " + codigo + ".");
            } else {
                mostrarError("Error al guardar el producto", e.getMessage());
            }
            return;
        }

        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle("Producto guardado");
        aviso.setHeaderText(null);
        aviso.setContentText("El producto " + nombre + " se guardó correctamente.");
        aviso.showAndWait();

        nuevoProducto();
        buscarProductos();
    }

    @FXML
    private void actualizarProducto() { }

    @FXML
    private void eliminarProducto() { }

    @FXML
    private void buscarProductos() {
        Double precioMin;
        Double precioMax;

        try {
            precioMin = leerDecimal(txtPrecioMin);
            precioMax = leerDecimal(txtPrecioMax);
        } catch (NumberFormatException e) {
            mostrarError("Precio inválido", "Los precios deben ser números, por ejemplo 10.50");
            return;
        }

        if (precioMin != null && precioMax != null && precioMin > precioMax) {
            mostrarError("Rango de precios inválido", "El precio mínimo no puede ser mayor que el máximo.");
            return;
        }

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarError("Rango de fechas inválido", "La fecha 'Desde' no puede ser posterior a 'Hasta'.");
            return;
        }

        Categoria categoria = cmbFiltroCategoria.getValue();
        Integer idCategoria = categoria != null ? categoria.getIdCategoria() : null;

        try {
            List<Producto> productos = productoDAO.buscar(
                    txtBuscar.getText(),
                    idCategoria,
                    desde,
                    hasta,
                    precioMin,
                    precioMax,
                    chkConStock.isSelected(),
                    cmbOrdenar.getValue()
            );

            tablaProductos.setItems(FXCollections.observableArrayList(productos));

        } catch (SQLException e) {
            mostrarError("Error al buscar productos", e.getMessage());
        }
    }

    private Double leerDecimal(TextField campo) {
        String texto = campo.getText();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        chkConStock.setSelected(false);
        cmbOrdenar.setValue(null);
        buscarProductos();
    }
}