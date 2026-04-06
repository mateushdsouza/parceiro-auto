package br.com.parceiroauto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_company")
public class UserCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_id_company", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "fk_id_user", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserCompanyRole role;

    public UserCompany() {
    }

    public UserCompany(Company company, User user, UserCompanyRole role) {
        this.company = company;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public UserCompanyRole getRole() {
        return role;
    }

    public void setRole(UserCompanyRole role) {
        this.role = role;
    }
}