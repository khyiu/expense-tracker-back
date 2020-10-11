package be.kuritsu.gt.repository;

import be.kuritsu.gt.model.PurchaseEntity;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface PurchaseRepository extends CrudRepository<PurchaseEntity, String> {

    void deleteByTestData(boolean testData);
}