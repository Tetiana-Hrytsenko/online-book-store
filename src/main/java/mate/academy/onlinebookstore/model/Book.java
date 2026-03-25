package mate.academy.onlinebookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLDelete(sql = "UPDATE books SET is_deleted = true WHERE id=?")
@SQLRestriction(value = "is_deleted=false")
@Table(name = "books")
@Accessors(chain = true)
@Setter
@Getter
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    @Column(nullable = false, unique = true)
    private String isbn;
    @Column(nullable = false)
    private BigDecimal price;
    private String description;
    private String coverImage;
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean isDeleted = false;
    @ManyToMany
    @JoinTable(
            name = "books_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Category> categories = new HashSet<>();

    public Book(Long id) {
        this.id = id;
    }
}
