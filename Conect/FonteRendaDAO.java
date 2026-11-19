package Conect;

import bean.FonteRenda;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FonteRendaDAO {
     private final Connection conn = new Conexao().getConexao();
 
    public void inserir(FonteRenda o) {
        String sql = "INSERT INTO fonte_renda (nome, descricao) VALUES (?,?)";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getDescricao());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir: " + ex.getMessage(), ex);
        }
    }
 
    public void editar(FonteRenda o) {
        String sql = "UPDATE fonte_renda SET nome=?, descricao=? WHERE id=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, o.getNome());
            st.setString(2, o.getDescricao());
            st.setInt(3, o.getId());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar: " + ex.getMessage(), ex);
        }
    }
 
    /** Devolve o registro ou null se não existir. */
    public FonteRenda buscarPorId(int id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM fonte_renda WHERE id=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    FonteRenda o = new FonteRenda();
                    o.setId(rs.getInt("id"));
                    o.setNome(rs.getString("nome"));
                    o.setDescricao(rs.getString("descricao"));
                    return o;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar: " + ex.getMessage(), ex);
        }
        return null;
    }
 
    public void excluir(int id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM fonte_renda WHERE id=?")) {
            st.setInt(1, id);
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao excluir (ha transacoes vinculadas?): " + ex.getMessage(), ex);
        }
    }
 
    public List<FonteRenda> listar() {
        List<FonteRenda> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM fonte_renda ORDER BY nome");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                FonteRenda o = new FonteRenda();
                o.setId(rs.getInt("id"));
                o.setNome(rs.getString("nome"));
                o.setDescricao(rs.getString("descricao"));
                lista.add(o);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar: " + ex.getMessage(), ex);
        }
        return lista;
    }
}
