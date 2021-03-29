package logging.repository;

import logging.entity.HttpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HttpLogRepository extends JpaRepository<HttpLog, Long> {

}
