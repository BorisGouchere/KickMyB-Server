package org.kickmyb.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kickmyb.server.account.MUser;
import org.kickmyb.server.account.MUserRepository;
import org.kickmyb.server.task.ServiceTask;
import org.kickmyb.transfer.AddTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO pour celui ci on aimerait pouvoir mocker l'utilisateur pour ne pas avoir à le créer

// https://reflectoring.io/spring-boot-mock/#:~:text=This%20is%20easily%20done%20by,our%20controller%20can%20use%20it.

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KickMyBServerApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@ActiveProfiles("test")
class ServiceTaskTests {

    @Autowired
    private MUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceTask serviceTask;

    @Test
    void testAddTask() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Tâche de test";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        serviceTask.addOne(atr, u);

        assertEquals(1, serviceTask.home(u.id).size());
    }

    @Test
    void testAddTaskEmpty()  {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Empty");
        } catch (Exception e) {
            assertEquals(ServiceTask.Empty.class, e.getClass());
        }
    }

    @Test
    void testAddTaskTooShort() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "o";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.TooShort");
        } catch (Exception e) {
            assertEquals(ServiceTask.TooShort.class, e.getClass());
        }
    }

    @Test
    void testAddTaskExisting() throws ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {
        MUser u = new MUser();
        u.username = "M. Test";
        u.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(u);

        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));

        try{
            serviceTask.addOne(atr, u);
            serviceTask.addOne(atr, u);
            fail("Aurait du lancer ServiceTask.Existing");
        } catch (Exception e) {
            assertEquals(ServiceTask.Existing.class, e.getClass());
        }
    }

    @Test
    void testDeleteTask() throws ServiceTask.NotFound, ServiceTask.NotAllowed, ServiceTask.Empty, ServiceTask.TooShort, ServiceTask.Existing {

        // Création d'un utilisateur
        MUser user = new MUser();
        user.username = "M. Test";
        user.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(user);
        // Ajout d'une tâche
        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "Bonne tâche";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(atr, user);
        // Vérification que la tâche a été ajoutée
        assertEquals(1, serviceTask.home(user.id).size());
        // récupération de l'utilisateur après la création de la tâche
        user = userRepository.findById(user.id).get();
        long taskID = serviceTask.home(user.id).get(0).id;
        // Suppression de la tâche
        serviceTask.deleteTask(taskID, user);
        assertEquals(0, serviceTask.home(user.id).size());
    }

    @Test
    void testDeleteTaskNotFound() throws ServiceTask.NotAllowed, ServiceTask.NotFound, ServiceTask.TooShort, ServiceTask.Existing, ServiceTask.Empty{
        // Création d'un utilisateur
        MUser user = new MUser();
        user.username = "M. Test";
        user.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(user);
       try {
           serviceTask.deleteTask(1, user);
              fail("Aurait dû lancer ServiceTask.NotFound");
       } catch (Exception e) {
           assertEquals(ServiceTask.NotFound.class, e.getClass());
       }
    }

    @Test
    void testDeleteTaskNotAllowed() throws ServiceTask.NotFound, ServiceTask.NotAllowed, ServiceTask.TooShort, ServiceTask.Existing, ServiceTask.Empty {
        // Création d'un utilisateur (Alice)
        MUser user = new MUser();
        user.username = "Alice";
        user.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(user);

        // Ajout d'une tâche à Alice
        AddTaskRequest atr = new AddTaskRequest();
        atr.name = "tâche de Alice";
        atr.deadline = Date.from(new Date().toInstant().plusSeconds(3600));
        serviceTask.addOne(atr, user);

        // Vérification que Alice a bien une tâche
        var aliceTasks = serviceTask.home(user.id);
        assertEquals(1, aliceTasks.size());
        long taskID = aliceTasks.get(0).id; // Récupère l'ID de la tâche d'Alice

        // Création d'un autre utilisateur (Bob)
        MUser user2 = new MUser();
        user2.username = "Bob";
        user2.password = passwordEncoder.encode("Passw0rd!");
        userRepository.saveAndFlush(user2);

        // Test : Bob essaie de supprimer la tâche d'Alice (doit lancer NotAllowed)
        try {
            serviceTask.deleteTask(taskID, user2); // user2 n'est pas le propriétaire
            fail("Aurait dû lancer ServiceTask.NotAllowed");
        } catch (Exception e) {
            assertEquals(ServiceTask.NotAllowed.class, e.getClass());
        }
    }
}
