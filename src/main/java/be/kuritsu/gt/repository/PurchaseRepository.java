package be.kuritsu.gt.repository;

import be.kuritsu.gt.model.PurchaseEntity;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.support.SimpleDynamoDBPagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@EnableScanCount
@EnableScan
public interface PurchaseRepository extends DynamoDBPagingAndSortingRepository<PurchaseEntity, String> {

    Page<PurchaseEntity> findByOwnr(String ownr, Pageable pageable);
}