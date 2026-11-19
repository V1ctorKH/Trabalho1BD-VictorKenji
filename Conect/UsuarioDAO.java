package Conect;

import bean.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
     private final Connection conn = new Conexao().getConexao();
 
    public void inserir(Usuario o) {
        String sql = "INSERT INTO usuario (nome, email) VALUES (?,?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getEmail());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir: " + ex.getMessage(), ex);
        }
    }
 
    public void editar(Usuario o) {
        String sql = "UPDATE usuario SET nome=?, email=? WHERE id=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getEmail());
            st.setInt(3, o.getId());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar: " + ex.getMessage(), ex);
        }
    }
 
    /** Devolve o registro ou null se não existir. */
    public Usuario buscarPorId(int id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM usuario WHERE id=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Usuario o = new Usuario();
                    o.setId(rs.getInt("id"));
                    o.setNome(rs.getString("nome"));
                    o.setEmail(rs.getString("email"));
                    return o;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar: " + ex.getMessage(), ex);
        }
        return null;
    }
 
    public void excluir(int id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM usuario WHERE id=?")) {
            st.setInt(1, id);
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao excluir (ha transacoes vinculadas?): " + ex.getMessage(), ex);
        }
    }
 
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM usuario ORDER BY nome");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Usuario o = new Usuario();
                o.setId(rs.getInt("id"));
                o.setNome(rs.getString("nome"));
                o.setEmail(rs.getString("email"));
                lista.add(o);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar: " + ex.getMessage(), ex);
        }
        return lista;
    }
}
