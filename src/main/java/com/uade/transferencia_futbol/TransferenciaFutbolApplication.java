package com.uade.transferencia_futbol;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransferenciaFutbolApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TransferenciaFutbolApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Reemplaza con tu URI y password de Neo4j
        try (var driver = GraphDatabase.driver(
                "neo4j+s://5f9214a1.databases.neo4j.io",
                AuthTokens.basic("neo4j", "BaADau4pEODMdCOIcOJmA4UY-iYBOb-qJWzqdjA5Of8"))) {
            driver.verifyConnectivity();
            System.out.println("✅ Connection established to Neo4j!");
        } catch (Exception e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}