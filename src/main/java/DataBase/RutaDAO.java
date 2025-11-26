package DataBase;

import models.Ruta;

import java.sql.*;
import java.util.HashMap;

public class RutaDAO {
    private static RutaDAO instance = null;

    private RutaDAO() { }

    public static RutaDAO getInstance() {
        if (instance == null) {
            instance = new RutaDAO();
        }
        return instance;
    }

    // Guardar una nueva ruta
    public void guardarRuta(Ruta ruta) {
        final String sql = "INSERT INTO route (\"nombre\", \"inicio\", \"destino\", \"distancia\", \"tiempo\", \"costo\", \"estado\", \"evento\", \"transbordo\") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, ruta.getNombre());
            ps.setLong(2, ruta.getInicio().getId());
            ps.setLong(3, ruta.getDestino().getId());
            ps.setDouble(4, ruta.getDistancia());
            ps.setDouble(5, ruta.getTiempo());
            ps.setDouble(6, ruta.getCosto());
            ps.setBoolean(7, ruta.isEstado());
            ps.setString(8, ruta.getEvento());
            ps.setString(9, ruta.getTransbordo());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ruta.setId(rs.getLong(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtener todas las rutas
    public HashMap<Long, Ruta> obtenerRutas(HashMap<Long, Ruta> rutas, HashMap<Long, models.Parada> paradas) {
        HashMap<Long, Ruta> resultado = new HashMap<>();
        final String sql = "SELECT * FROM route";

        try (Connection connection = DataBaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                long inicioId = rs.getLong("inicio");
                long destinoId = rs.getLong("destino");
                double distancia = rs.getDouble("distancia");
                double tiempo = rs.getDouble("tiempo");
                double costo = rs.getDouble("costo");
                boolean estado = rs.getBoolean("estado");
                String evento = rs.getString("evento");
                String transbordo = rs.getString("transbordo");

                Ruta ruta = new Ruta(
                        nombre,
                        paradas.get(inicioId),
                        paradas.get(destinoId),
                        distancia,
                        tiempo,
                        costo
                );
                ruta.setId(id);
                ruta.setEstado(estado);
                ruta.setEvento(evento);
                ruta.setTransbordo(transbordo);

                resultado.put(id, ruta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultado;
    }

    // Actualizar una ruta existente
    public void actualizarRuta(Ruta ruta) {
        final String sql = "UPDATE route SET nombre = ?, inicio = ?, destino = ?, distancia = ?, tiempo = ?, costo = ?, estado = ?, evento = ?, transbordo = ? WHERE id = ?";

        try (Connection connection = DataBaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, ruta.getNombre());
            ps.setLong(2, ruta.getInicio().getId());
            ps.setLong(3, ruta.getDestino().getId());
            ps.setDouble(4, ruta.getDistancia());
            ps.setDouble(5, ruta.getTiempo());
            ps.setDouble(6, ruta.getCosto());
            ps.setBoolean(7, ruta.isEstado());
            ps.setString(8, ruta.getEvento());
            ps.setString(9, ruta.getTransbordo());
            ps.setLong(10, ruta.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Eliminar ruta por ID
    public void eliminarRuta(long rutaId) {
        final String sql = "DELETE FROM route WHERE id = ?";

        try (Connection connection = DataBaseConnection.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, rutaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Eliminar rutas que tengan una parada específica como inicio o destino
    public void eliminarRutasPorParada(long paradaId) {
        final String sql = "DELETE FROM route WHERE inicio = ? OR destino = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, paradaId);
            ps.setLong(2, paradaId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
