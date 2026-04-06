package br.com.parceiroauto.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "TransactionCategory")

public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nameCategory;

    @Column(nullable = false, length = 10)
    private String typeCategory;

    @Column(nullable = false)
    private Boolean active;

    public TransactionCategory(Long id, String nameCategory, String typeCategory, Boolean active) {
        this.id = id;
        this.nameCategory = nameCategory;
        this.typeCategory = typeCategory;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public void setNameCategory(String nameCategory) {
        this.nameCategory = nameCategory;
    }

    public String getTypeCategory() {
        return typeCategory;
    }

    public void setTypeCategory(String typeCategory) {
        this.typeCategory = typeCategory;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
