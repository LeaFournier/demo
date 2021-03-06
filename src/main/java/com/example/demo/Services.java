package com.example.demo;

import com.example.demo.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Services {

    String path = "src/main/resources";
    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");

    //void saveWordlToXml(World world)
    //OutputStream output = new FileOutputStream(file);

    //World getWorld()

    World readWorldFromXml(String pseudo) throws JAXBException {
        JAXBContext jaxbContext;
        World world = new World();
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File f = new File(path+ "/" + pseudo + "-world.xml");
            world = (World) jaxbUnmarshaller.unmarshal(f);
            return world;
        } catch (Exception ex) {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            world = (World) jaxbUnmarshaller.unmarshal(input);
            return world;
        }
    }

    void saveWorldToXml(World world, String pseudo) {
        JAXBContext jaxbContext;
        try {
            OutputStream output = new FileOutputStream(path+ "/" + pseudo + "-world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur de sauvegarde : "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        updateWorld(world);
        saveWorldToXml(world, username);
        return world;
    }

    // prend en param??tre le pseudo du joueur et le produit
// sur lequel une action a eu lieu (lancement manuel de production ou
// achat d???une certaine quantit?? de produit)
// renvoie false si l???action n???a pas pu ??tre trait??e
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit ??quivalent ?? celui pass??
        // en param??tre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }

        // calculer la variation de quantit??. Si elle est positive c'est
        // que le joueur a achet?? une certaine quantit?? de ce produit
        // sinon c???est qu???il s???agit d???un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantit??
            // achet??e et mettre ?? jour la quantit?? de product
            world.setMoney(world.getMoney()-coutProduit(product, qtchange));
            product.setCout(product.getCout()*Math.pow(product.getCroissance(), qtchange));
            product.setQuantite(newproduct.getQuantite());

        } else {
            // initialiser product.timeleft ?? product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
            world.setMoney(world.getMoney() + product.getRevenu() * product.getQuantite());
        }

        for (PallierType p : product.getPalliers().getPallier()) {
            if (!p.isUnlocked() && product.getQuantite()<p.getSeuil() && newproduct.getQuantite()>=p.getSeuil()) {
                addUnlock(p, product);
            }
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private double coutProduit(ProductType product, int qtchange) {
        double cout = product.getCout();
        return (cout * (1-Math.pow(product.getCroissance(), qtchange))) / (1-product.getCroissance());
    }


    private void addUnlock(PallierType p, ProductType product) {
        p.setUnlocked(true);
        if(p.getTyperatio()== TyperatioType.GAIN) {
            double revenu = product.getRevenu();
            revenu = revenu * p.getRatio();
            product.setRevenu(revenu);
        }
        if(p.getTyperatio()==TyperatioType.VITESSE) {
            double vitesse = product.getVitesse();
            vitesse = vitesse * p.getRatio();
            product.setVitesse((int) vitesse);
        }
    }

    // prend en param??tre le pseudo du joueur et le manager achet??.
// renvoie false si l???action n???a pas pu ??tre trait??e
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager ??quivalent ?? celui pass??
        // en param??tre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // d??bloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // d??bloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney()-manager.getSeuil());
        // sauvegarder les changements au monde
        saveWorldToXml(world,username);
        return true;
    }

    private PallierType findManagerByName(World world, String name) {
        for (PallierType manager : world.getManagers().getPallier()) {
            if (manager.getName()==name) {
                return manager;
            }
        }
        return null;
    }

    void updateWorld(World world) {
        long diffTemps = System.currentTimeMillis() - world.getLastupdate();
        int bonusAngel = world.getAngelbonus();
        for (ProductType produit : world.getProducts().getProduct()) {
            if (!produit.isManagerUnlocked()) {
                    produit.setTimeleft(produit.getTimeleft()-diffTemps);
            }
            else {
                long nombreProduitsCrees = diffTemps/produit.getVitesse();
                world.setScore(world.getScore() + (produit.getRevenu() * nombreProduitsCrees * (1 + world.getActiveangels() * bonusAngel / 100)));
                world.setMoney(world.getMoney() + (produit.getRevenu() * nombreProduitsCrees * (1 + world.getActiveangels() * bonusAngel / 100)));
                produit.setTimeleft(produit.getVitesse() - diffTemps%produit.getVitesse());
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }

    void deleteWorld(String username) {
        try {
            World world = readWorldFromXml(username);
            double anges = Math.round(150 * Math.sqrt((world.getScore()) / Math.pow(10,15))) - world.getTotalangels();
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(input);
            world.setActiveangels(world.getActiveangels() + anges);
            world.setTotalangels(world.getTotalangels() + anges);
            world.setScore(world.getScore());
            world.setMoney(0);
            saveWorldToXml(world, username);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
