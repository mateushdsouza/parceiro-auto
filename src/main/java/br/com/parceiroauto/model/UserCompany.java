package br.com.parceiroauto.model;


import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "UserCompanies")
public class UserCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_company", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "fk_id_user", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role;

    public UserCompany(long id, Company company, User user, String role) {
        this.id = id;
        this.company = company;
        this.user = user;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}