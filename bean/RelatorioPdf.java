package bean;

import bean.ItemRelatorio;
import bean.Usuario;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gera o PDF de transações de um usuário em um período.
 * Saldo = soma dos créditos - soma dos débitos.
 *
 * Requer a biblioteca OpenPDF 1.3.x (ou iText 2.1.7): ambas usam com.lowagie.text.
 */
public class RelatorioPdf {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VERDE = new Color(0, 120, 0);
    private static final Color VERMELHO = new Color(180, 0, 0);

    // ------------------------------------------------------------ cálculos

    public static BigDecimal totalCreditos(List<ItemRelatorio> itens) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemRelatorio i : itens) {
            if (i.isCredito()) total = total.add(i.getValor().abs());
        }
        return total;
    }

    public static BigDecimal totalDebitos(List<ItemRelatorio> itens) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemRelatorio i : itens) {
            if (!i.isCredito()) total = total.add(i.getValor().abs());
        }
        return total;
    }

    /** Soma todos os créditos e subtrai todos os débitos. */
    public static BigDecimal saldo(List<ItemRelatorio> itens) {
        return totalCreditos(itens).subtract(totalDebitos(itens));
    }

    /** Descrição para exibição; em faturas informa a data em que o lançamento foi feito. */
    public static String descricao(ItemRelatorio i) {
        String d = i.getDescricao() == null ? "" : i.getDescricao();
        if (i.isFatura() && i.getDataLancamento() != null) {
            d = (d.isEmpty() ? "" : d + " ") + "(fatura - lançada em " + i.getDataLancamento().format(DATA) + ")";
        }
        return d;
    }

    /** Formata como "R$ 1.234,56" (negativo: "- R$ 1.234,56"). */
    public static String moeda(BigDecimal v) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));
        String s = "R$ " + df.format(v.abs());
        return v.signum() < 0 ? "- " + s : s;
    }

    // ------------------------------------------------------------ PDF

    public static void gerar(File destino, Usuario usuario, LocalDate inicio, LocalDate fim,
            List<ItemRelatorio> itens) throws IOException {

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font negrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font verde = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, VERDE);
        Font vermelho = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, VERMELHO);

        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(destino));
            doc.open();

            Paragraph p = new Paragraph("Relatório de Transações", titulo);
            p.setSpacingAfter(8);
            doc.add(p);
            doc.add(new Paragraph("Usuário: " + usuario.getNome()
                    + (usuario.getEmail() != null && !usuario.getEmail().isEmpty() ? " (" + usuario.getEmail() + ")" : ""),
                    normal));
            doc.add(new Paragraph("Período: " + inicio.format(DATA) + " a " + fim.format(DATA), normal));
            doc.add(new Paragraph("Transações de fatura são contabilizadas na data da fatura, e não na do lançamento.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)));
            Paragraph gerado = new Paragraph("Gerado em: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normal);
            gerado.setSpacingAfter(12);
            doc.add(gerado);

            if (itens.isEmpty()) {
                doc.add(new Paragraph("Nenhuma transação encontrada no período.", normal));
            } else {
                PdfPTable t = new PdfPTable(6);
                t.setWidthPercentage(100);
                t.setWidths(new float[]{12, 10, 17, 17, 27, 17});
                t.setHeaderRows(1);

                String[] cab = {"Data", "Tipo", "Fonte", "Destinatário", "Descrição", "Valor"};
                for (String c : cab) {
                    PdfPCell h = celula(c, negrito, c.equals("Valor") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                    h.setBackgroundColor(Color.LIGHT_GRAY);
                    t.addCell(h);
                }
                for (ItemRelatorio i : itens) {
                    t.addCell(celula(i.getData().format(DATA), normal, Element.ALIGN_LEFT));
                    t.addCell(celula(i.getTipo(), normal, Element.ALIGN_LEFT));
                    t.addCell(celula(vazio(i.getFonte()), normal, Element.ALIGN_LEFT));
                    t.addCell(celula(vazio(i.getDestinatario()), normal, Element.ALIGN_LEFT));
                    t.addCell(celula(descricao(i), normal, Element.ALIGN_LEFT));
                    String valor = (i.isCredito() ? "+ " : "- ") + moeda(i.getValor().abs());
                    t.addCell(celula(valor, i.isCredito() ? verde : vermelho, Element.ALIGN_RIGHT));
                }
                doc.add(t);
            }

            // Resumo: créditos somam, débitos subtraem
            BigDecimal saldo = saldo(itens);
            PdfPTable r = new PdfPTable(2);
            r.setWidthPercentage(48);
            r.setHorizontalAlignment(Element.ALIGN_RIGHT);
            r.setSpacingBefore(14);
            r.setWidths(new float[]{60, 40});
            r.addCell(celula("Total de créditos (+)", normal, Element.ALIGN_LEFT));
            r.addCell(celula(moeda(totalCreditos(itens)), verde, Element.ALIGN_RIGHT));
            r.addCell(celula("Total de débitos (-)", normal, Element.ALIGN_LEFT));
            r.addCell(celula(moeda(totalDebitos(itens)), vermelho, Element.ALIGN_RIGHT));
            PdfPCell l = celula("Saldo (créditos - débitos)", negrito, Element.ALIGN_LEFT);
            PdfPCell v = celula(moeda(saldo), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD,
                    saldo.signum() < 0 ? VERMELHO : VERDE), Element.ALIGN_RIGHT);
            l.setBackgroundColor(Color.LIGHT_GRAY);
            v.setBackgroundColor(Color.LIGHT_GRAY);
            r.addCell(l);
            r.addCell(v);
            doc.add(r);

        } catch (DocumentException ex) {
            throw new IOException(ex.getMessage(), ex);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    private static PdfPCell celula(String texto, Font fonte, int alinhamento) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte));
        c.setHorizontalAlignment(alinhamento);
        c.setPadding(4);
        return c;
    }

    private static String vazio(String s) {
        return s == null ? "" : s;
    }
}