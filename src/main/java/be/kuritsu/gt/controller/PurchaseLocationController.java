package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchaseLocationsApi;
import be.kuritsu.gt.model.PurchaseLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PurchaseLocationController implements PurchaseLocationsApi {

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<List<PurchaseLocation>> getPurchaseLocations() {
        return null;
    }
}
