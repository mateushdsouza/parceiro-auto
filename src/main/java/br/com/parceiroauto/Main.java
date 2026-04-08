package br.com.parceiroauto;
import br.com.parceiroauto.confg.FlyWayconfg;
import br.com.parceiroauto.view.View;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;


public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("parceiroautoPU");
        EntityManager em = emf.createEntityManager();

        View.menuPrincipal();
        FlyWayconfg.migrate();

    }
}