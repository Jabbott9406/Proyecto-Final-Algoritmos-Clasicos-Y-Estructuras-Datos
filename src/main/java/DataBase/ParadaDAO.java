package DataBase;

import models.Parada;

import java.sql.*;
import java.util.HashMap;

public class ParadaDAO {
    private static ParadaDAO instance = null;
    private ParadaDAO() {}

    public static ParadaDAO getInstance() {
        if (instance == null) {
            instance = new ParadaDAO();
        }
        return instance;
    }

    // Guardar una nueva parada
    public void guardarParada(Parada parada) {
        final String sql = "INSERT INTO parada (\"nombre\", \"tipo\") VALUES (?, ?)";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipo());
            preparedStatement.executeUpdate();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                parada.setId(rs.getLong(1));  // ←🔥 AQUÍ SE ASIGNA EL ID
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    // Obtener todas las paradas
    public HashMap<Long, Parada> obtenerParadas() {
        HashMap<Long, Parada> paradas = new HashMap<>();
        final String sql = "SELECT * FROM parada";

        try(Connection connection = DataBaseConnection.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()){
                long id = resultSet.getLong("id");
                String nombre = resultSet.getString("nombre");
                String tipo = resultSet.getString("tipo");

                Parada parada = new Parada(nombre, tipo);
                parada.setId(id);
                paradas.put(id, parada);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return paradas;
    }

    // Actualizar una parada existente
    public void actualizarParada(Parada parada) {
        final String sql = "UPDATE parada SET nombre = ?, \"tipo\" = ? WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipo());
            preparedStatement.setLong(3, parada.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    // Eliminar parada por ID
    public void eliminarParada(Long id) {
        final String sql = "DELETE FROM parada WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
