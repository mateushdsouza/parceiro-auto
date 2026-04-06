package br.com.parceiroauto.model;


import java.util.List;
import jakarta.persistence.*;

@Entity(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50 )
    private String login;

    @Column(nullable = false, length = 20 )
    private String password;

    @OneToMany(mappedBy = "Users")
    private List<UserCompany> userCompanies;

    public UserSystem( long id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}