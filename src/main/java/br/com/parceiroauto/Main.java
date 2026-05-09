package br.com.parceiroauto;

import br.com.parceiroauto.confg.FlyWayconfg;
import br.com.parceiroauto.confg.JPAUtil;
import br.com.parceiroauto.controller.LoginController;
import br.com.parceiroauto.repository.UserRepository;
import br.com.parceiroauto.service.UserService;
import br.com.parceiroauto.view.swing.LoginFrame;
import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {

        FlyWayconfg.migrate();

        EntityManager em = JPAUtil.getEntityManager();

        UserRepository repository = new UserRepository(em);

        UserService service =
                new UserService(repository);

        LoginController controller =
                new LoginController(service);

        new LoginFrame(controller);
    }
}