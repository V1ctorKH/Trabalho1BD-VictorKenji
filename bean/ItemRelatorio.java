package bean;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma linha do relatório de transações (já com os nomes de fonte e destinatário). */
public class ItemRelatorio {
    /** Data efetiva: a data da fatura (se for fatura) ou a data do lançamento. */
    private LocalDate data;
    private LocalDate dataLancamento;
    private boolean fatura;
    private String tipo;
    private String fonte;
    private String destinatario;
    private String descricao;
    private BigDecimal valor;

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalDate getDataLancamento() { return dataLancamento; }
    public void setDataLancamento(LocalDate dataLancamento) { this.dataLancamento = dataLancamento; }
    public boolean isFatura() { return fatura; }
    public void setFatura(boolean fatura) { this.fatura = fatura; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    /** Crédito soma no saldo; qualquer outro tipo (Débito) subtrai. */
    public boolean isCredito() {
        return tipo != null && tipo.toLowerCase().startsWith("cr");
    }
}