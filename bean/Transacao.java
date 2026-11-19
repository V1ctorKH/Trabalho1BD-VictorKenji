/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kenji
 */

package bean;

import java.time.LocalDate;

public class Transacao {
    private int id;
    private int idUsuario;          // FK -> usuario
    private Integer idFonte;        // FK -> fonte_renda (opcional)
    private Integer idDestinatario; // FK -> destinatario (opcional)
    private LocalDate data;
    private double valor;
    private String tipo;
    private boolean fatura;
    private LocalDate diaFatura;
    private String descricao;

    public int getId() {
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }
    public int getIdUsuario() { 
        return idUsuario; 
    }
    public void setIdUsuario(int idUsuario) { 
        this.idUsuario = idUsuario; 
    }
    public Integer getIdFonte() { 
        return idFonte;
    }
    public void setIdFonte(Integer idFonte) { 
        this.idFonte = idFonte; 
    }
    public Integer getIdDestinatario() {
        return idDestinatario; 
    }
    public void setIdDestinatario(Integer idDestinatario) { 
        this.idDestinatario = idDestinatario; 
    }
    public LocalDate getData() { 
        return data; 
    }
    public void setData(LocalDate data) { 
        this.data = data; 
    }
    public double getValor() {
        return valor; 
    }
    public void setValor(double valor) { 
        this.valor = valor; 
    }
    public String getTipo() {
        return tipo; 
    }
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
    public boolean isFatura() { 
        return fatura; 
    }
    public void setFatura(boolean fatura) { 
        this.fatura = fatura; 
    }
    public LocalDate getDiaFatura() { 
        return diaFatura;
    }
    public void setDiaFatura(LocalDate diaFatura) { 
        this.diaFatura = diaFatura; 
    }
    public String getDescricao() { 
        return descricao; 
    }
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }
}
