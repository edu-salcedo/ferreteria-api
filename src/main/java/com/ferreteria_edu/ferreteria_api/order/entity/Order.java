package com.ferreteria_edu.ferreteria_api.order.entity;

import com.ferreteria_edu.ferreteria_api.enun.DocumentType;
import com.ferreteria_edu.ferreteria_api.enun.OrderType;
import com.ferreteria_edu.ferreteria_api.enun.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor // Requerido por JPA/Hibernate
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal surcharge = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.EFECTIVO;

    @Column(name = "is_invoice", nullable = false)
    private boolean invoice = false;

    @Column(nullable = false)
    private BigDecimal invoiceAmount = BigDecimal.ZERO;
    // Dentro de tu archivo Order.java
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType; // PRESUPUESTO o VENTA

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType; // FACTURA, TIQUE, REMITO, NINGUNO

    @Column(name = "pos_number")
    private Integer posNumber; // Siempre será 1 según tus comprobantes reales

    @Column(name = "invoice_number")
    private Long invoiceNumber; // Guardará la secuencia limpia (124, 377, etc.)

    @Column(name = "invoice_type")
    private String invoiceType; // Guardará "C" si corresponde a un documento fiscal

    // EL TRUCO: Coloca el @Builder aquí arriba de tu constructor completo
    @Builder
    public Order(Long id, LocalDateTime createdAt, List<OrderItem> items, BigDecimal totalAmount,
            BigDecimal subtotal, BigDecimal surcharge, BigDecimal discount,
            PaymentMethod paymentMethod, boolean invoice, BigDecimal invoiceAmount, OrderType orderType,
            DocumentType documentType, Integer posNumber,
            Long invoiceNumber, String invoiceType) {
        this.id = id;
        // Si usas el builder y no pasas el valor, usará el valor por defecto de la
        // variable
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.surcharge = surcharge != null ? surcharge : BigDecimal.ZERO;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.EFECTIVO;
        this.invoice = invoice;
        this.invoiceAmount = invoiceAmount != null ? invoiceAmount : BigDecimal.ZERO;
        this.orderType = orderType;
        this.documentType = documentType;
        this.posNumber = posNumber;
        this.invoiceNumber = invoiceNumber;
        this.invoiceType = invoiceType;
    }

    // Tus métodos de negocio se quedan igual...
    public boolean isBudget() {
        return OrderType.BUDGET.equals(this.orderType);
    }

    public boolean isInternalSale() {
        return OrderType.SALE.equals(this.orderType) && !DocumentType.INVOICE.equals(this.documentType);
    }

    public boolean isArcaInvoice() {
        return OrderType.SALE.equals(this.orderType) && DocumentType.INVOICE.equals(this.documentType);
    }

    // --- Métodos de negocio existentes ---
    public void calculateSubtotal() {
        this.subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.setOrder(this);
    }

}
