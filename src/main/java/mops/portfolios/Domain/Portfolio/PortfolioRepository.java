package mops.portfolios.Domain.Portfolio;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PortfolioRepository extends CrudRepository<Portfolio, Long> {
    
    List<Portfolio> findByUserId(String userId);

    List<Portfolio> findAllByGroupId(Long groupId);

}