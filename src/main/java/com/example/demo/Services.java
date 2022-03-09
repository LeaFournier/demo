package com.example.demo;

import com.example.demo.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {

    private World world;

    //void saveWordlToXml(World world)
    //OutputStream output = new FileOutputStream(file);

    //World getWorld()

    private World readWorldFromXml() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            /*File f = new File(path+"/contacts.xml");*/
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) jaxbUnmarshaller.unmarshal(input);
        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }
    private void saveWorldToXml(World world) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            OutputStream output = new FileOutputStream("file.xml");
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }
    }

    World getWorld(){
        World getworld = readWorldFromXml();

        return getworld;
    }
}
