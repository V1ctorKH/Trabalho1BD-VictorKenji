package Conect;

import bean.Destinatario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinatarioDAO {
private final Connection conn = new Conexao().getConexao();
 
    public void inserir(Destinatario o) {
        String sql = "INSERT INTO destinatario (nome, documento) VALUES (?,?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getDocumento());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir: " + ex.getMessage(), ex);
        }
    }
 
    public void editar(Destinatario o) {
        String sql = "UPDATE destinatario SET nome=?, documento=? WHERE id=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getDocumento());
            st.setInt(3, o.getId());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar: " + ex.getMessage(), ex);
        }
    }
 
    /** Devolve o registro ou null se não existir. */
    public Destinatario buscarPorId(int id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM destinatario WHERE id=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Destinatario o = new Destinatario();
                    o.setId(rs.getInt("id"));
                    o.setNome(rs.getString("nome"));
                    o.setDocumento(rs.getString("documento"));
                    return o;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar: " + ex.getMessage(), ex);
        }
        return null;
    }
 
    public void excluir(int id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM destinatario WHERE id=?")) {
            st.setInt(1, id);
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao excluir (ha transacoes vinculadas?): " + ex.getMessage(), ex);
        }
    }
 
    public List<Destinatario> listar() {
        List<Destinatario> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM destinatario ORDER BY nome");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Destinatario o = new Destinatario();
                o.setId(rs.getInt("id"));
                o.setNome(rs.getString("nome"));
                o.setDocumento(rs.getString("documento"));
                lista.add(o);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar: " + ex.getMessage(), ex);
        }
        return lista;
    }
}
