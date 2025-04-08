package br.com.personadev.arduino_demo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.personadev.arduino_demo.domain.model.DeviceMessage;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RestController
public class DemoApplication {

  private final List<DeviceMessage> recentMessages = new CopyOnWriteArrayList<>();
  private final int MAX_MESSAGES = 100;

  @Bean
  public CommandLineRunner commandLineRunner(ArduinoService arduinoService) {
    return args -> {
      // List available ports for user selection
      ArduinoService.listAvailablePorts();

      // Initialize the Arduino connection
      arduinoService.initialize("cu.usbserial-210", 9600);

      // Set up a shutdown hook to close the port when the application exits
      Runtime.getRuntime().addShutdownHook(new Thread(arduinoService::close));

      System.out.println("Reading from Arduino. Press Ctrl+C to exit.");

      while (true) {
        DeviceMessage deviceMessage = DeviceMessage.fromJson(arduinoService.getNextMessage());
        System.out.println("Processing message from queue: " + deviceMessage);

        if (deviceMessage != null) {
          System.out.println("Parsed message: " + deviceMessage.toString());

          if (recentMessages.size() >= MAX_MESSAGES) {
            recentMessages.remove(0); // Remove the oldest message
          }
          recentMessages.add(deviceMessage);

        }
      }
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @GetMapping("/sensors")
  public List<DeviceMessage> getMessages() {
    System.out.println("Fetching recent messages");
    return recentMessages;
  }
}