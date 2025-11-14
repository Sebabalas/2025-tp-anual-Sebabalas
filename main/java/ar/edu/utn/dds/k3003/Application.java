package ar.edu.utn.dds.k3003;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(Application.class, args);
        System.out.println("Perfiles activos: " + Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
    }
}