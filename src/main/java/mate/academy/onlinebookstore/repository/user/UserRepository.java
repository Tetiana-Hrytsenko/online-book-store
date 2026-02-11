package mate.academy.onlinebookstore.repository.user;

import mate.academy.onlinebookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User save(User entity);

    boolean existsUserByEmail(String email);
}

