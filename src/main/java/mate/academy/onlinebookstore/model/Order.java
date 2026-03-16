package mate.academy.onlinebookstore.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET is_deleted = true WHERE id=?")
@SQLRestriction(value = "is_deleted=false")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private BigDecimal total;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime orderDate;
    @Column(nullable = false)
    private String shippingAddress;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<OrderItem> orderItems = new HashSet<>();
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean isDeleted = false;

    public enum Status {
        PENDING,
        COMPLETED,
        CANCELED,
        SHIPPED,
        DELIVERED
    }
}
