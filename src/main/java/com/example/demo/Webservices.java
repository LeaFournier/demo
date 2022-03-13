package com.example.demo;

import com.example.demo.generated.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin

public class Webservices {
    Services services;

    public Webservices() {
        services = new Services();
    }

    @GetMapping(value = "/world", produces = {"application/xml" , "application/json"})
    public ResponseEntity<World> getWorld(@RequestHeader(value = "X-User", required=false) String username) throws JAXBException {
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }

    @PutMapping(value="/product", consumes = {"application/xml" , "application/json"})
    public ProductType putProduct(@RequestHeader(value = "X-User", required=false) String username, @RequestBody ProductType produit) throws JAXBException {
        services.updateProduct(username, produit);
        return produit;
    }

    @PutMapping(value="/manager", consumes = {"application/xml" , "application/json"})
    public PallierType putManager(@RequestHeader(value = "X-User", required=false) String username, @RequestBody PallierType manager) throws JAXBException{
        services.updateManager(username, manager);
        return manager;
    }

    //A implémenter

    /*@PutMapping(value="/upgrade", consumes = {"application/xml" , "application/json"})
    public PallierType putUpgrade(@RequestHeader(value = "X-User", required=false) String username, @RequestBody PallierType upgrade) {
        services.updateUpgrade(username, upgrade);
        return upgrade;
    }

    @PutMapping(value="/angelUpgrade", consumes = {"application/xml" , "application/json"})
    public PallierType putAngelUpgrade(@RequestHeader(value = "X-User", required=false) String username, @RequestBody PallierType angel) {
        services.updateAngelUpgrade(username, angel);
        return angel;
    }*/

    @DeleteMapping(value="/world", produces = {"application/xml" , "application/json"})
    public void deleteWorld(@RequestHeader(value = "X-User", required=false) String username) {
        services.deleteWorld(username);
    }


}

