package com.rentaherramientas.infrastructure.pdf;

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
import com.rentaherramientas.domain.model.Factura;
import com.rentaherramientas.domain.model.Reserva;
import com.rentaherramientas.domain.port.out.InvoicePdfPort;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Adaptador de salida: dibuja la factura en PDF con OpenPDF. */
@Component
public class OpenPdfInvoiceAdapter implements InvoicePdfPort {

    private static final Color AZUL = new Color(0x1F, 0x3A, 0x5F);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generar(Factura f) {
        Reserva r = f.getReserva();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, AZUL);
            Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font pequena = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

            doc.add(new Paragraph("Renta de Herramientas y Equipos", titulo));
            doc.add(new Paragraph("Factura de alquiler N.º " + f.getNumero(), negrita));
            doc.add(new Paragraph("Fecha de emisión: " + f.getFechaEmision().format(FECHA_HORA), normal));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Cliente: " + r.getClienteNombre(), normal));
            doc.add(new Paragraph("Proveedor: " + r.getProveedorNombre(), normal));
            doc.add(new Paragraph("Pago: " + f.getPago().getMetodo() + " · Ref. " + f.getPago().getReferencia(), normal));
            doc.add(new Paragraph(" "));

            PdfPTable detalle = new PdfPTable(new float[]{4, 2.2f, 1.2f, 2, 2});
            detalle.setWidthPercentage(100);
            for (String encabezado : new String[]{"Herramienta", "Periodo", "Días", "Valor día", "Subtotal"}) {
                PdfPCell c = new PdfPCell(new Phrase(encabezado, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
                c.setBackgroundColor(AZUL);
                c.setPadding(6);
                detalle.addCell(c);
            }
            detalle.addCell(celda(r.getHerramientaNombre(), normal, Element.ALIGN_LEFT));
            detalle.addCell(celda(r.getFechaInicio().format(FECHA) + " - " + r.getFechaFin().format(FECHA), normal, Element.ALIGN_LEFT));
            detalle.addCell(celda(String.valueOf(r.getDias()), normal, Element.ALIGN_CENTER));
            detalle.addCell(celda(dinero(r.getPrecioDia()), normal, Element.ALIGN_RIGHT));
            detalle.addCell(celda(dinero(f.getSubtotal()), normal, Element.ALIGN_RIGHT));
            doc.add(detalle);
            doc.add(new Paragraph(" "));

            PdfPTable totales = new PdfPTable(new float[]{6, 2});
            totales.setWidthPercentage(45);
            totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totales.addCell(celda("Subtotal", normal, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(f.getSubtotal()), normal, Element.ALIGN_RIGHT));
            totales.addCell(celda("IVA (19%)", normal, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(f.getIva()), normal, Element.ALIGN_RIGHT));
            totales.addCell(celda("TOTAL PAGADO", negrita, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(f.getTotal()), negrita, Element.ALIGN_RIGHT));
            doc.add(totales);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Comprobante generado automáticamente. Documento académico: no es una factura electrónica DIAN.", pequena));
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        } finally {
            doc.close();
        }
        return salida.toByteArray();
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(5);
        c.setBorderColor(Color.LIGHT_GRAY);
        return c;
    }

    private String dinero(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(valor);
    }
}
