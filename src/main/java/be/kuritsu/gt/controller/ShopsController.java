package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.ShopsApi;
import be.kuritsu.gt.model.Shop;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopsController implements ShopsApi {

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<List<Shop>> getShops() {
        // todo: implement
        return null;
    }
}
