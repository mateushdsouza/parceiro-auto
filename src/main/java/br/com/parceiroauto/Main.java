package br.com.parceiroauto;
import br.com.parceiroauto.confg.FlyWayconfg;
import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.view.View;

import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {
        FlyWayconfg.migrate();
        EntityManager em = JPAUtil.getEntityManager();

        View.menuLogin();

    }
}