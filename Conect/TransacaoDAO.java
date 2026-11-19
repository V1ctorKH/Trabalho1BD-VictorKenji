package Conect;

import bean.ItemRelatorio;
import bean.Transacao;
import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {
    private final Connection conn = new Conexao().getConexao();

    public void inserir(Transacao t) {
        String sql = "INSERT INTO transacao (id_usuario,id_fonte,id_destinatario,data,valor,tipo,fatura,dia_fatura,descricao) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)"; // 9 colunas, 9 placeholders
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, t.getIdUsuario());
            st.setObject(2, t.getIdFonte(), Types.INTEGER);
            st.setObject(3, t.getIdDestinatario(), Types.INTEGER);
            st.setDate(4, Date.valueOf(t.getData()));
            st.setDouble(5, t.getValor());
            st.setString(6, t.getTipo());
            st.setBoolean(7, t.isFatura());
            st.setDate(8, t.getDiaFatura() == null ? null : Date.valueOf(t.getDiaFatura()));
            st.setString(9, t.getDescricao());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir transacao: " + ex.getMessage(), ex);
        }
    }

    public void editar(Transacao t) {
        String sql = "UPDATE transacao SET id_usuario=?, id_fonte=?, id_destinatario=?, data=?, valor=?, "
                   + "tipo=?, fatura=?, dia_fatura=?, descricao=? WHERE id=?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, t.getIdUsuario());
            st.setObject(2, t.getIdFonte(), Types.INTEGER);
            st.setObject(3, t.getIdDestinatario(), Types.INTEGER);
            st.setDate(4, Date.valueOf(t.getData()));
            st.setDouble(5, t.getValor());
            st.setString(6, t.getTipo());
            st.setBoolean(7, t.isFatura());
            st.setDate(8, t.getDiaFatura() == null ? null : Date.valueOf(t.getDiaFatura()));
            st.setString(9, t.getDescricao());
            st.setInt(10, t.getId());
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar transacao: " + ex.getMessage(), ex);
        }
    }

    /** Devolve a transação (com os IDs das chaves estrangeiras) ou null se não existir. */
    public Transacao buscarPorId(int id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM transacao WHERE id=?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return null;
                Transacao t = new Transacao();
                t.setId(rs.getInt("id"));
                t.setIdUsuario(rs.getInt("id_usuario"));
                int f = rs.getInt("id_fonte");
                t.setIdFonte(rs.wasNull() ? null : Integer.valueOf(f));
                int d = rs.getInt("id_destinatario");
                t.setIdDestinatario(rs.wasNull() ? null : Integer.valueOf(d));
                Date data = rs.getDate("data");
                t.setData(data == null ? null : data.toLocalDate());
                t.setValor(rs.getDouble("valor"));
                t.setTipo(rs.getString("tipo"));
                t.setFatura(rs.getBoolean("fatura"));
                Date df = rs.getDate("dia_fatura");
                t.setDiaFatura(df == null ? null : df.toLocalDate());
                t.setDescricao(rs.getString("descricao"));
                return t;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar transacao: " + ex.getMessage(), ex);
        }
    }

    public void excluir(int id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM transacao WHERE id=?")) {
            st.setInt(1, id);
            st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao excluir transacao: " + ex.getMessage(), ex);
        }
    }

    /** Linhas prontas para a JTable, com nomes vindos dos JOINs. */
    public List<Object[]> listarLinhas() {
        String sql = "SELECT t.id, u.nome AS usuario, f.nome AS fonte, d.nome AS destinatario, "
                   + "t.data, t.valor, t.tipo, t.fatura, t.dia_fatura, t.descricao "
                   + "FROM transacao t JOIN usuario u ON u.id=t.id_usuario "
                   + "LEFT JOIN fonte_renda f ON f.id=t.id_fonte "
                   + "LEFT JOIN destinatario d ON d.id=t.id_destinatario ORDER BY t.data DESC, t.id DESC";
        List<Object[]> linhas = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                linhas.add(new Object[]{rs.getInt("id"), rs.getString("usuario"), rs.getString("fonte"),
                    rs.getString("destinatario"), rs.getDate("data"), rs.getDouble("valor"),
                    rs.getString("tipo"), rs.getBoolean("fatura") ? "Sim" : "Nao",
                    rs.getDate("dia_fatura"), rs.getString("descricao")});
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar transacoes: " + ex.getMessage(), ex);
        }
        return linhas;
    }

    /** Transações de um usuário entre duas datas (inclusive), da mais antiga para a mais nova. */
    public List<ItemRelatorio> listarPorPeriodo(int idUsuario, LocalDate inicio, LocalDate fim) {
        // Fatura conta na data da fatura; as demais, na data do lançamento
        String efetiva = "(CASE WHEN t.fatura = 1 AND t.dia_fatura IS NOT NULL THEN t.dia_fatura ELSE t.data END)";
        String sql = "SELECT " + efetiva + " AS data_efetiva, t.data AS data_lancamento, t.fatura, "
                   + "t.tipo, f.nome AS fonte, d.nome AS destinatario, t.descricao, t.valor "
                   + "FROM transacao t "
                   + "LEFT JOIN fonte_renda f ON f.id=t.id_fonte "
                   + "LEFT JOIN destinatario d ON d.id=t.id_destinatario "
                   + "WHERE t.id_usuario=? AND " + efetiva + " BETWEEN ? AND ? "
                   + "ORDER BY data_efetiva, t.id";
        List<ItemRelatorio> lista = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idUsuario);
            st.setDate(2, Date.valueOf(inicio));
            st.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    ItemRelatorio i = new ItemRelatorio();
                    i.setData(rs.getDate("data_efetiva").toLocalDate());
                    i.setDataLancamento(rs.getDate("data_lancamento").toLocalDate());
                    i.setFatura(rs.getBoolean("fatura"));
                    i.setTipo(rs.getString("tipo"));
                    i.setFonte(rs.getString("fonte"));
                    i.setDestinatario(rs.getString("destinatario"));
                    i.setDescricao(rs.getString("descricao"));
                    i.setValor(rs.getBigDecimal("valor"));
                    lista.add(i);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao consultar o periodo: " + ex.getMessage(), ex);
        }
        return lista;
    }
}